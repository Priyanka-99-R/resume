# 11. Testing — Interview Q&A tied to Robogebra

Senior interviews always probe testing. Your project has **~32 backend test classes** (JUnit 5 + Spring Boot Test + **Testcontainers**) and **~147 frontend `.spec.ts`** files (Jasmine/Karma). That's a strong, honest story.

---

## Q1. How do you test your Spring Boot backend?

**Answer.** Two levels:
- **Unit tests** — a service in isolation with **Mockito** mocks for its collaborators; fast, no Spring context. Assert business logic and edge cases.
- **Integration tests** — `@SpringBootTest` boots the app and hits real controllers through `MockMvc`, with a **real MongoDB in a Testcontainer** so I'm testing against actual Mongo behavior (indexes, queries), not an in-memory fake.

**In Robogebra.** Integration tests live under `src/test/java/.../integeration/` — e.g. `AuthControllerTest`, `ChapterControllerTest`, `ExerciseSolutionWorkoutControllerTest`, `UserControllerTest`. A `BaseBusinessTest` + `TestConfig` provide shared setup. `pom.xml` pulls `spring-boot-starter-test` (JUnit 5, Mockito, AssertJ) and `testcontainers:junit-jupiter`.

**Unit test shape (Mockito):**

```java
@ExtendWith(MockitoExtension.class)
class SolutionVisibilityServiceTest {
    @Mock SolutionVisibilityPersistenceService persistence;
    @Mock StudentEnrollmentService enrollment;
    @InjectMocks SolutionVisibilityService service;

    @Test void studentWithNoClass_getsAccess() {
        when(enrollment.klassIds(any())).thenReturn(List.of());
        assertTrue(service.isSolutionEnabledForStudent(student, "item-1"));  // default open
        verify(persistence, never()).findByKlassIdsAndTargetIds(any(), any());
    }
}
```

**Integration test shape (Testcontainers + MockMvc):**

```java
@SpringBootTest @AutoConfigureMockMvc @Testcontainers
class ChapterControllerTest {
    @Container static MongoDBContainer mongo = new MongoDBContainer("mongo:6");
    @DynamicPropertySource static void props(DynamicPropertyRegistry r) {
        r.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }
    @Autowired MockMvc mvc;
    @Test void getChapters_returns200() throws Exception {
        mvc.perform(get("/api/chapters/...").header("Authorization","Bearer " + token))
           .andExpect(status().isOk());
    }
}
```

---

## Q2. Unit vs integration — when each?

**Answer.** **Unit** for logic-heavy code with branches (the visibility precedence resolver, validators, mappers) — fast, deterministic, run on every save. **Integration** for wiring I can't fake safely — actual Mongo queries/indexes, the security filter chain, serialization, and end-to-end controller behavior. I aim for many fast unit tests + a focused set of integration tests on critical paths (auth, solutions, payments).

**Why Testcontainers over an embedded/mock Mongo?** It runs the *real* MongoDB in Docker, so unique indexes, `@DBRef`, and aggregation pipelines behave exactly as in prod. Embedded fakes drift from real behavior and give false confidence.

---

## Q3. How do you mock, and what do you assert?

**Answer.** Mockito `@Mock` + `when(...).thenReturn(...)` to stub collaborators, `verify(...)` to assert interactions (e.g. "we did NOT hit the DB when the student has no class"). I assert **behavior + outputs + edge cases** (null/empty, unauthorized, boundary), not just the happy path. For exceptions I use `assertThrows(APIException.class, ...)` and check the status/message.

---

## Q4. How do you test the Angular frontend?

**Answer.** **Jasmine** specs run by **Karma**, using Angular's `TestBed`. Components are tested with mocked services (spy objects); services with `HttpTestingController` to assert the right requests and simulate error responses; guards/resolvers with mocked router + service. RxJS is tested with marble-style or `fakeAsync`/`tick`.

**In Robogebra.** ~147 `.spec.ts` files (co-located `*.component.spec.ts`). Example patterns I'd describe:
- A guard test: mock `AuthService.isLoggedInObservable()` → assert `canActivate` redirects to `/login` and returns false when logged out.
- A service test: `HttpTestingController` — call `authService.createUser(...)`, expect a `POST`, flush a **409** and assert the error path fires the notification.

```typescript
it('redirects to login when not authenticated', (done) => {
  authService.isLoggedInObservable.and.returnValue(of(false));
  guard.canActivate(route, state).subscribe(allowed => {
    expect(allowed).toBeFalse();
    expect(router.navigate).toHaveBeenCalledWith(['/login']);
    done();
  });
});
```

---

## Q5. What's your overall test strategy / philosophy?

**Answer.** **Test pyramid** — lots of fast unit tests, fewer integration tests, minimal E2E. Test **behavior, not implementation**, so refactors don't break tests. Cover the **risk**: auth, authorization, money, and the tricky logic (visibility precedence). Tests run in **CI on every PR** (the repo has a `.github` pipeline + Dockerfile), so nothing merges red. I don't chase 100% coverage — I chase coverage of the code that would hurt if it broke.

**Honest note.** Coverage isn't uniform across 60+ domains; critical paths (auth, exercise solutions, chapters, users) have integration tests, and I'd expand coverage on newer domains. Saying that is more credible than claiming everything is tested.

---

### Rapid-fire recap

| Topic | Robogebra reality |
|-------|-------------------|
| Backend unit | JUnit 5 + Mockito (`@Mock`/`@InjectMocks`) |
| Backend integration | `@SpringBootTest` + `MockMvc` + **Testcontainers Mongo** |
| Test count | ~32 backend classes, ~147 frontend specs |
| Frontend | Jasmine + Karma + `TestBed` + `HttpTestingController` |
| Philosophy | pyramid, behavior-focused, risk-first, CI on every PR |
