# 8. Error Handling (Deep Dive) — tied to Robogebra

How the portal turns *any* failure — validation, business, DB, external service, auth — into a **consistent JSON error** the frontend can trust, without leaking internals.

---

## Q1. Walk me through your end-to-end error-handling strategy.

**Answer.** Three principles: (1) **throw typed exceptions** from business code (never return error codes), (2) **one place formats them** — a `@ControllerAdvice` — so every error has the same JSON shape, and (3) **log full detail server-side, return a safe message client-side**. Two delivery paths: normal request errors go through the advice; errors thrown **inside security filters** (before the advice can run) are written directly by an `ExceptionResponseWriter`.

**The response shape** every client gets (`ErrorMessageModel`):

```java
@Data @Builder
public class ErrorMessageModel {
    private Integer statusCode;   // HTTP status
    private Date    timestamp;
    private String  message;      // user-facing
    private String  description;  // request path
    private Integer errorCode;    // optional Robogebra code for programmatic handling
}
```

---

## Q2. Show the global exception handler. How is it organized?

**Answer.** A `@ControllerAdvice` with ~2 dozen `@ExceptionHandler` methods, ordered **specific → generic**, ending in a catch-all `Exception` that returns 500 with a generic message (so stack traces never reach the client).

**In Robogebra** (`common/exception/handler/ExceptionControllerAdvice.java`):

```java
@ControllerAdvice @Slf4j
public class ExceptionControllerAdvice {

  @ExceptionHandler(MethodArgumentNotValidException.class)   // @Valid body failures → 400
  public ResponseEntity<Object> handleValidation(MethodArgumentNotValidException ex, WebRequest req) {
      String msg = findErrorMessageForValidation(ex);
      return ResponseEntity.status(BAD_REQUEST).body(createErrorMessage(BAD_REQUEST, msg, req));
  }

  @ExceptionHandler(APIException.class)                       // our business errors → its own status
  public ResponseEntity<ErrorMessageModel> handleAPI(APIException ex, WebRequest req) {
      log.error(ex.getMessage(), ex);
      return ResponseEntity.status(ex.getStatus()).body(createErrorMessage(ex.getStatus(), ex.getMessage(), req));
  }

  @ExceptionHandler(Exception.class)                         // catch-all → 500, generic message
  public ResponseEntity<ErrorMessageModel> global(Exception ex, WebRequest req) {
      log.error("Unhandled exception: ", ex);               // full stack to server logs only
      return ResponseEntity.status(INTERNAL_SERVER_ERROR)
          .body(createErrorMessage(INTERNAL_SERVER_ERROR, "Something went wrong. Please try again later.", req));
  }
}
```

**Coverage** (know a handful, not all): validation (`MethodArgumentNotValidException`, `ConstraintViolationException`, `MethodArgumentTypeMismatchException`), resources (`ResourceNotFoundException`→404, `ResourceConflictException`→409), security (`SecurityPermissionException`→403), infra (`MongoWriteException`, `DatabaseException`, `MaxUploadSizeExceededException`), external (`PaymentException`, `ExternalApiException`, `CouponValidationException`), auth (`CognitoException`, `SsoTokenException`), and the generic `Exception`.

---

## Q3. What custom exceptions do you have, and how are they designed?

**Answer.** All extend `RuntimeException` (unchecked — so I don't pollute signatures). Three shapes by need:

1. **Status + message** — the workhorse: `APIException`, `ExternalApiException`, `DatabaseException`, `PaymentException` carry an `HttpStatus` + message.
2. **Message-only** — `ResourceNotFoundException("Exercise item", id)`, `ResourceConflictException`, `SecurityPermissionException`.
3. **Error-code-based** — `CognitoException`, `SsoTokenException`, `UsageLimitExceedException` carry a **`robogebraErrorCode`** (from an `ErrorCode` enum) so the client can branch programmatically (e.g., "token expired → silent refresh").

```java
@Getter
public class APIException extends RuntimeException {
    private final HttpStatus status;
    private final String errorMessage;
    public APIException(HttpStatus status, String msg) { super(msg); this.status = status; this.errorMessage = msg; }
}
```

**Why unchecked?** Business code stays clean (`throw new APIException(...)`), the advice is the single catch point, and I don't force every caller to declare/handle checked exceptions.

---

## Q4. How do you map an *error type* to an HTTP status cleanly?

**Answer.** With an **enum + switch expression** — no scattered `if/else`. Two patterns:

**Domain error → status** (`CouponValidationException`):

```java
HttpStatus status = switch (ex.getErrorType()) {
    case INVALID_COUPON                                   -> HttpStatus.BAD_REQUEST;
    case SERVICE_UNAVAILABLE, LAMBDA_ERROR, NETWORK_ERROR -> HttpStatus.SERVICE_UNAVAILABLE;
    case PARSING_ERROR                                    -> HttpStatus.INTERNAL_SERVER_ERROR;
};
```

**Error-code registry** (`ErrorCode` enum) — one place mapping a code to a status, reused by many exceptions:

```java
@Getter @AllArgsConstructor
public enum ErrorCode {
    COGNITO_TOKEN_EXPIRED(30000, HttpStatus.UNAUTHORIZED),
    SOLUTION_USAGE_LIMIT_EXCEEDED(50001, HttpStatus.FORBIDDEN),
    MULTI_USER_LOGIN_EXCEEDED(60001, HttpStatus.UNAUTHORIZED);
    private final Integer code; private final HttpStatus httpStatus;
}
```

The numeric `errorCode` is the real senior touch: the frontend can react to `30000` (token expired) differently from `50001` (upgrade prompt) without string-matching messages.

---

## Q5. How do you translate messy third-party errors into clean user messages?

**Answer.** External SDKs throw verbose, technical exceptions. I **catch at the boundary** and remap: AWS Cognito exceptions → friendly copy via a lookup enum; WebClient failures → a normalized `ExternalApiException` with the right status.

**Cognito → user message** (`CognitoExceptionType`):

```java
public enum CognitoExceptionType {
    USERNAME_EXISTS("UsernameExistsException", "Phone number already registered"),
    NOT_AUTHORIZED("NotAuthorizedException", "Incorrect password"),
    CODE_MISMATCH("CodeMismatchException", "Verification code incorrect. Please try again"),
    TOO_MANY_REQUESTS("TooManyRequestsException", "Too many requests. Please try again later");
    // getCustomMessage(raw) scans for the AWS name and returns the friendly text
}
```

**WebClient boundary mapping** (`SyncApiClientService`) — distinguish *downstream 4xx/5xx* from *network/timeout*:

```java
catch (WebClientResponseException e) {           // downstream returned an error
    throw new ExternalApiException(INTERNAL_SERVER_ERROR, e.getResponseBodyAsString());
} catch (WebClientRequestException e) {           // timeout / connection refused / DNS
    throw new ExternalApiException(GATEWAY_TIMEOUT, "Portal API timed out");
}
```

**MongoDB duplicate key → readable message** — regex-parse the driver error:

```java
Pattern.compile("dup key: \\{ (.+?): \"(.+?)\" }");  // → "The email 'a@b.com' already exists."
```

---

## Q6. Errors thrown in a filter can't be caught by `@ControllerAdvice`. How do you handle those?

**Answer.** Correct — the advice only wraps controller invocations; a `OncePerRequestFilter` runs *before* that. So auth filters catch their own exceptions and write the **same** `ErrorMessageModel` JSON directly via a shared `ExceptionResponseWriter`, keeping the contract identical.

**In Robogebra** (`JwtAuthenticationFilter` → `ExceptionResponseWriter`):

```java
// in the filter
catch (CognitoException e) { responseWriter.writeCognitoException(response, request, e); }
catch (SsoTokenException e) { responseWriter.writeSsoTokenException(response, request, e); }
```

```java
// ExceptionResponseWriter — builds the SAME ErrorMessageModel and writes JSON to the response
response.setStatus(ex.getStatus()); response.setContentType("application/json");
objectMapper.writeValue(response.getOutputStream(), ErrorMessageModel.builder()
    .errorCode(ex.getRobogebraErrorCode()).statusCode(ex.getStatus())
    .message(CognitoExceptionType.getCustomMessage(ex.getErrorMessage()))
    .description(request.getRequestURI()).timestamp(DateUtils.now()).build());
```

This is a great "I understand the framework internals" point — most candidates don't realize the advice can't see filter exceptions.

---

## Q7. Logging & security — what does the client see vs the server?

**Answer.** `@Slf4j` everywhere; `log.error("...", ex)` logs the **full stack trace to server logs** for debugging. The **client only gets a safe message** — the generic 500 handler returns *"Something went wrong. Please try again later."*, never the exception detail. That prevents leaking stack traces, class names, or query internals (an OWASP concern). Log levels: `error` for failures, `warn` for soft limits (e.g., session cap), `info` for successful sensitive ops (logout), `debug` for token/handler tracing.

---

## Q8. How does this connect to the frontend? (full loop)

**Answer.** Backend throws → advice/writer builds `{statusCode, message, errorCode}` → Angular's HTTP layer `catchError`s → a central `NotificationService` extracts `error.message` and toasts it. Specific `errorCode`s drive behavior — e.g. a token-expired code triggers the interceptor's silent refresh instead of a toast. So the **"email already exists"** message the user sees is literally the backend's `APIException` message flowing through this pipeline (frontend side in `01-frontend.md` Q14).

---

### Rapid-fire recap

| Concept | Robogebra implementation | File |
|---------|--------------------------|------|
| Central formatting | `@ControllerAdvice`, specific→generic | `ExceptionControllerAdvice.java` |
| Response shape | `ErrorMessageModel {status,msg,code,ts,path}` | `ErrorMessageModel.java` |
| Custom exceptions | unchecked, status/message/errorCode | `APIException.java`, `CognitoException.java` |
| Type→status | switch expr + `ErrorCode` enum | `ErrorCode.java`, `CouponValidationException.java` |
| 3rd-party remap | `CognitoExceptionType`, WebClient boundary | `CognitoExceptionType.java`, `SyncApiClientService.java` |
| Filter errors | `ExceptionResponseWriter` writes same JSON | `ExceptionResponseWriter.java` |
| Security | full stack to logs, generic msg to client | generic `Exception` handler |
