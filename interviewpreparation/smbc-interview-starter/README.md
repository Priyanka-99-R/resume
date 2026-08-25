# SMBC interview starter

Satisfies the emailed backend prerequisites: one class with a method that returns
true, and a test class that validates it.

```bash
mvn test          # 12 tests, runs offline
```

- `src/main/java/com/smbc/interview/Sample.java` — `isReady()` returns `true`.
- `src/test/java/com/smbc/interview/SampleTest.java` — JUnit 5 + AssertJ.

Java 17, Maven, JUnit 5.11.3, AssertJ 3.26.3. All dependencies are cached
locally, so this builds with no network.

Use `Sample` as the scratch class for anything live-coded during the interview.
