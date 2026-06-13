TESTING
=======

How to run tests for the backend module (`BE/demo`)

Quick commands
```
mvn test
mvn -Dtest=PlanServiceRepositoryTest test
```

Notes
- Integration tests use H2 configured in `src/test/resources/application-test.properties`.
- The benchmark `GeneticBenchmarkTest` is disabled by default; enable locally by removing `@Disabled`.
- For deterministic failures, tests seed the optimizer via `GeneticPlanOptimizer.setRandom(new Random(seed))`.

If tests fail locally, re-run the failing test with `-Dtest=...` and attach the stacktrace to issues.
