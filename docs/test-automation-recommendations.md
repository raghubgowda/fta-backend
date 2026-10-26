# Test Automation Recommendations — Feature Toggle Application

**Scope:** `fta-backend` (Spring Boot 2.2.6, Java 8, MongoDB, jjwt) and `fta-frontend` (Angular 9)
**Context:** Companion to `docs/security-remediation-plan.md`. Current state: almost no meaningful behavioral tests exist — the backend's only test class ([`FeatureToggleApplicationTests`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/test/java/com/raghu/fta/FeatureToggleApplicationTests.java)) lacks `@SpringBootTest` and the JUnit 5 Spring extension (its `contextLoads()` proves nothing), and the frontend has no component/service specs; the [Protractor e2e spec](https://github.com/raghubgowda/fta-frontend/blob/36aa3fc/e2e/src/app.e2e-spec.ts) is the untouched CLI default. Coverage was not measured in this review.

---

## 1. Strategy: risk-first, contract-driven

Do not chase a coverage number. Write tests as the **desired secure contract** — red against today's code, green after the remediation — not tests that preserve current (insecure) behavior.

### Test levels

| Level | Backend | Frontend |
|---|---|---|
| Unit | Role mapping, DTO mapping, password handling, toggle evaluation | Auth service, guards, interceptors, form validation |
| MVC/security slice (`@WebMvcTest`) | Endpoint permissions, validation, response status and serialization | — |
| Full filter-chain integration | Real JWT parsing, user lookup, authority assignment through the actual security filter | — |
| Database integration | Actual MongoDB query behavior | — |
| Browser E2E | — | A few complete user journeys across both apps |

**Critical subtlety:** do not validate the authorization fix with `@WithMockUser` alone. It seeds an already-authenticated security context and bypasses exactly the code that is broken — the hardcoded `ROLE_ADMIN` authority in [`JwtAuthenticationFilter`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/config/security/JwtAuthenticationFilter.java). Use it for permission-matrix tests, but also send locally signed test JWTs through the real filter chain, with users of different stored roles loaded from the database.

---

## 2. Priority 1 — security regression suite (ships with the P0 fixes)

Delivered together with the P0 remediation items as their acceptance criteria:

- Anonymous requests to management endpoints rejected; authenticated-but-forbidden operations return `403`
- Viewer / ProductOwner / Admin permissions tested across features, customers, and users — the full permission matrix
- Sign-up cannot select a role; generic updates cannot change password or role; cross-user changes denied per ownership rules
- No `password` key in sign-up **or** user-read responses; no credential-bearing log lines at debug level
- Password changes persist a hash verified with `passwordEncoder.matches()`
- Valid, expired, tampered, malformed, and old-key (pre-rotation) JWTs all handled correctly
- Application refuses to start without the signing key configured

## 3. Priority 2 — feature correctness

- The [`FeaturesRepo.getFeatureByNames`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/dao/FeaturesRepo.java) query embeds `new Date()` for expiry — the most logic-heavy code in the app. Test it against a real database, not mocks, and make it deterministic by passing a clock-derived cutoff into the query.
- Before locking tests around the display-name/technical-name lookup (with its `toLowerCase()` handling), pin down the intended contract — the tests should document that decision.
- Cover: customer assignments, enabled/disabled flags, expiry behavior, and unknown/unassigned feature names.

## 4. Tooling — incremental, not a rewrite

- **Backend:** keep JUnit 5 + Mockito; add `spring-security-test`; use a real, isolated MongoDB container for query integration tests. Verify container-tooling compatibility with the legacy Mongo 3.2-era image rather than assuming it — or align the test database version with the deployment under test.
- **Frontend:** start unit-testing with the existing Jasmine/Karma setup (`HttpClientTestingModule` for the auth service, guards, and interceptor) before touching any framework. Frontend route-denial tests are **not** a substitute for the API authorization tests.
- **Protractor → Cypress/Playwright:** a separate migration step, chosen after compatibility checks; it must not block the backend security suite. First E2E journey: login → view features → guarded edit route.

## 5. CI — two repos, one contract

- Parallel backend and frontend unit jobs, a database integration job, and a full-stack E2E job that checks out **pinned compatible revisions of both repositories**, with isolated seed data, synthetic credentials, and cleanup.
- No production secrets in CI; test the missing-secret startup failure with a dedicated test configuration (remediation plan item B5).
- **Repo-specific trap:** the `dockerfile-maven-plugin` in [`pom.xml`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/pom.xml) binds Docker image building to the `package` phase — separate test execution from image building, or `mvn verify` will unexpectedly require a Docker daemon.

## 6. Quality gates

- Measure a baseline first, then set changed-code line **and branch** coverage targets — specific percentages are team policy, not proof of security.
- The mandatory security scenarios block merging regardless of coverage percentage.
- Track flakes, execution time, and prohibit unexplained skips.
- Mutation testing (PIT) is a good later step, once the important suite is stable.

## 7. Sequencing

1. Stand up the security regression suite alongside the P0 fixes — it is the acceptance criteria of the remediation plan.
2. Add the database-backed feature-correctness tests.
3. Frontend unit tests for auth plumbing, then the first E2E journey.
4. Coverage gates into CI, then framework migrations (Protractor replacement, PIT) last.

---

**Review limitations:** Based on static review at `fta-backend@0d8aad1` / `fta-frontend@36aa3fc`. Runtime behavior and tooling compatibility were not verified; container/test-framework pairings should be confirmed during implementation.
