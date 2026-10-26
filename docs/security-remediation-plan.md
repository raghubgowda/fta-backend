# Security Remediation Plan — Feature Toggle Application

**Scope:** `fta-backend` (Spring Boot 2.2.6, Java 8, MongoDB, jjwt 0.9.1) and `fta-frontend` (Angular 9)
**Method:** Static code review of pinned commits — `fta-backend@0d8aad1`, `fta-frontend@36aa3fc`. Build and runtime behavior were not verified.
**Goal:** Restore a defensible security baseline in four waves: contain active risks (P0), gate deployment (P1), modernize (P2), and optional hardening.

---

## 1. Risk overview

| ID | Finding | Severity | Effort | Priority |
|----|---------|----------|--------|----------|
| A1 | No server-side authorization — every authenticated user is effectively admin | Critical | M | P0 |
| A2 | `PUT /api/v1/users` persists submitted user objects verbatim (incl. unhashed passwords) | Critical | M | P0 |
| A3 | Password hashes returned in API responses; plaintext passwords written to debug logs | Critical | S | P0 |
| A4 | JWT signing key committed to source control | Critical | S | P0 |
| B1 | Supplied Compose file shows MongoDB without authentication and publishes port 27017 to the host | High* | S | P1 |
| B2 | No TLS configuration found in the repo (deployment posture not verified) | High | S–M | P1 |
| B3 | No application-level rate limiting on login and sign-up | High | S | P1 |
| B4 | Public evaluation API keyed only by customer ID | Medium* | M | P1 (decision required) |
| C1 | Legacy dependency stack (Spring Boot 2.2.6, jjwt 0.9.1, Mongo 3.2 image, Angular 9) — support status not verified in this review | Medium–High | L | P2 |
| C2 | No externalized configuration; URLs and secrets in code | Medium | S | P2 |
| C3 | Frontend attaches bearer token to every outgoing HTTP request | Low | S | P2 |

\* Severity depends on actual deployment exposure and on business requirements, which this static review could not confirm. B1 rises to P0 if the database is deployed and reachable without authentication. B4 rises to P0 only if the flag values are confidential — public reachability alone does not determine this.

---

## 2. P0 — Contain now

Complete before any further feature work. Each fix ships with its regression tests (see §6).

### A1. Server-side authorization

**Evidence**
- [`JwtAuthenticationFilter`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/config/security/JwtAuthenticationFilter.java) hardcodes `ROLE_ADMIN` in the `UsernamePasswordAuthenticationToken` for every validated token.
- [`UserService.loadUserByUsername`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/services/UserService.java) also grants `ROLE_ADMIN` unconditionally.
- [`UsersController.sign-up`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/restcontrollers/UsersController.java) binds the client-supplied `User` entity and persists the client-chosen `role` field.
- The only role check in the system is the frontend [`RoleGuard`](https://github.com/raghubgowda/fta-frontend/blob/36aa3fc/src/app/helpers/role.guard.ts), which is trivially bypassed by calling the API directly.

**Impact:** Any registered user can create, modify, and delete all features, users, and customers. Because the `role` field was client-controlled at sign-up, stored roles cannot be trusted — existing accounts may already hold self-assigned privileges.

**Remediation**
1. Define a server-side permission matrix for all endpoints. Suggested starting point (adjust to product needs):

   | Operation | Viewer (default) | ProductOwner | Admin |
   |---|---|---|---|
   | Read features (management API) | ✓ | ✓ | ✓ |
   | Create/update/delete features | — | ✓ | ✓ |
   | Read customers | — | ✓ | ✓ |
   | Create/update/delete customers | — | — | ✓ |
   | Read users | — | — | ✓ |
   | Create/update/delete users | — | — | ✓ |

2. Derive Spring authorities from the user's stored role at authentication time (both in the filter and in `loadUserByUsername`), never a constant.
3. Enforce the matrix with method-level security, e.g. `@PreAuthorize("hasRole('ADMIN')")` on user management endpoints, `@PreAuthorize("hasAnyRole('ADMIN','PRODUCT_OWNER')")` on feature writes.
4. Sign-up: ignore any client-supplied `role` and assign a server-selected least-privileged default (e.g. `VIEWER`). Never bind the entity directly — use an allowlisted request DTO.
5. Distinguish "edit my own profile" from "administer another user" if profile self-service is required; object-level checks must prevent cross-user modification.
6. **Audit existing accounts:** list all users and their roles; reset or re-verify any account whose role was set through the sign-up path. Until then, treat all stored roles as untrusted.

**Acceptance tests**
- A `VIEWER`-role token calling `POST /api/v1/features` returns `403`.
- Sign-up with `"role": "ADMIN"` in the payload results in the stored role being `VIEWER`.
- A non-admin token calling `DELETE /api/v1/users/{id}` returns `403`.

**Effort:** M (0.5–2 days) · **Dependencies:** none · **Owner:** backend developer

### A2. Unsafe user update path

**Evidence:** [`UsersController.updateUser`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/restcontrollers/UsersController.java) saves the submitted `User` object directly via `userService.updateUser`. There is no hashing, no field restriction, and (per A1) no authorization check.

**Impact:** Any authenticated user can overwrite any other user's password (persisted unhashed), role, and email — including their own login. This is account takeover of every user in the system.

**Remediation**
1. Replace entity binding with an allowlisted `UpdateUserRequest` DTO (e.g. `name` only).
2. Move password changes to a dedicated `POST /api/v1/users/{id}/password` endpoint that always hashes with `BCryptPasswordEncoder`. Self-service changes must verify the current password; an admin-initiated reset, if required, needs its own authorization flow — do not overload the self-service path.
3. Never accept `role` changes through the generic update path; role changes are an admin-only operation on a dedicated endpoint.
4. Add the same DTO treatment to sign-up (overlaps with A1, step 4).

**Acceptance tests**
- `PUT /api/v1/users` with a `password` field does not change the stored hash.
- Password change endpoint rejects a wrong current password.
- Update request containing `role` leaves the stored role unchanged.

**Effort:** M · **Dependencies:** A1 (permission matrix) · **Owner:** backend developer

### A3. Credential exposure in responses and logs

**Evidence**
- The [`User` model](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/model/User.java) has no serialization exclusion on `password`; `GET /api/v1/users` and `GET /api/v1/users/{id}` return the BCrypt hash.
- [`UsersController.addUser`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/restcontrollers/UsersController.java) logs the incoming user object (`logger.debug("Adding user: {}", user)`) before hashing. Lombok's `@Data`-generated `toString()` on `User` includes the `password` field, so the plaintext password is written to debug logs.

**Impact:** Offline cracking of password hashes; plaintext passwords in any retained debug-level logs.

**Remediation**
1. Return dedicated response DTOs (`UserResponse` with `id`, `name`, `email`, `role`) from all user endpoints. Prefer DTOs over a blanket `@JsonIgnore` on the entity, because the same entity is reused for login/sign-up request binding where the field must remain deserializable.
2. Remove entity-level logging of user objects; log identifiers only (`logger.debug("Adding user with email: {}", user.getEmail())`). Add a rule to the lint/review checklist: never log request payloads containing credentials.
3. If logs have been retained at debug level, scrub or redact them — but preserve any audit evidence needed for investigating misuse before deletion.

**Acceptance tests**
- `GET /api/v1/users` response body contains no `password` key (automated JSON-schema assertion).
- Sign-up at `DEBUG` log level produces no log line containing the submitted password.

**Effort:** S (<0.5 day) · **Dependencies:** none · **Owner:** backend developer

### A4. JWT signing key in source control

**Evidence:** [`Constants.java`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/config/Constants.java) contains the HS256 signing key as a string literal.

**Impact:** Anyone with repo access (including historical clones) can forge valid tokens for any user.

**Remediation**
1. Generate a new cryptographically random secret of at least 256 bits (32 bytes) for HS256 and deploy it **together with the P0 authorization fixes in a single release** — rotation invalidates all tokens signed with the old key, which is the right moment for the new authorization rules to take effect.
2. Load it from an environment variable or a secrets manager; fail startup if it is absent (no default fallback in production).
3. Use a consistent encoding (e.g. Base64) for both signing and parsing.
4. Removing the constant from the current source does not remediate anything by itself — the key is in Git history. Rotation is the fix; also consider BFG/git-filter-repo if the repo will be shared further.
5. While here, externalize `ACCESS_TOKEN_VALIDITY_SECONDS` too.

**Acceptance tests**
- Application fails to start when the secret env var is unset.
- Tokens issued before rotation are rejected with `401` after deployment.

**Effort:** S · **Dependencies:** none · **Owner:** backend developer

### P0 deployment note

Deploy A1–A4 as one release: the key rotation logs everyone out, which is the right moment for the new authorization rules to take effect. Prepare the new key in advance so the release is a single coordinated deployment.

---

## 3. P1 — Deployment gates

Treat these as release gates: the application should not be exposed beyond localhost without them.

### B1. Database authentication and network isolation

**Evidence:** The supplied [`docker-compose.yml`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/docker-compose.yml) runs `mongo:3.2.4` with no authentication environment variables and publishes `27017:27017` to the host. No `application.properties`/`application.yml` exists in the repo, so database connectivity configuration could not be reviewed — verify both during implementation.

**Remediation**
1. Enable MongoDB access control and provision a **dedicated least-privileged application account** (read/write on the application database only) — do not use the root account for application access. Verify provisioning against the selected image and existing data.
2. Remove the `ports` mapping for MongoDB; keep it on the compose network only. If host access is needed for development, bind to `127.0.0.1:27017:27017`.
3. Keep the database on a private network in any real deployment.

**Acceptance tests:** Connecting to MongoDB without credentials fails; the application connects with the configured app user.

**Effort:** S · **Owner:** backend developer / DevOps

### B2. TLS/HTTPS

**Evidence:** No TLS configuration was found in the repo, and the frontend calls `http://localhost:8080`. TLS termination at an ingress layer was not established by this review — verify the actual deployment posture.

**Remediation:** Terminate TLS at the load balancer/reverse proxy for the API and the SPA; redirect HTTP to HTTPS; set `Strict-Transport-Security`. For local development, keep plain HTTP but isolate it from anything reachable.

**Acceptance tests:** No production endpoint reachable over plain HTTP; HSTS header present.

**Effort:** S–M (depends on hosting setup) · **Owner:** DevOps

### B3. Rate limiting on authentication endpoints

**Evidence:** `/token/generate-token` and `/api/v1/users/sign-up` are unauthenticated, and no application-level throttling exists in the code. Any ingress-level controls (WAF, reverse-proxy limits) are outside this repo and were not verified.

**Impact:** Credential stuffing and brute-force attacks against login; bulk account creation via sign-up.

**Remediation:** Check existing ingress controls first, then add application-level rate limiting (e.g. Bucket4j per-IP + per-account lockout after N failed attempts) and consider CAPTCHA on sign-up. Server-side, not frontend-only.

**Acceptance tests:** N rapid failed logins from one IP receive `429` or equivalent; the account is temporarily locked.

**Effort:** S · **Owner:** backend developer

### B4. Evaluation API access — decision required

**Evidence:** [`FeaturesAPI`](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/src/main/java/com/raghu/fta/restcontrollers/api/FeaturesAPI.java) serves `POST /public/api/v1/features` without authentication. It requires both a customer ID and the requested feature names — it does not enumerate every flag — but access is keyed only by the customer ID, so anyone holding an ID can read that customer's toggle values.

**Remediation (choose per business requirement)**
- **If flag values are confidential:** authenticate consumer applications (API key or OAuth client credentials) **and** bind each authenticated consumer to the set of customers it may query. An API key without this customer binding still leaves cross-customer reads possible.
- **If deliberately public:** document the decision explicitly (e.g. in the README and an ADR) so it is a conscious risk acceptance rather than a default. Flag values for customer-scoped toggles rarely qualify as public.

**Acceptance tests:** (if secured) Unauthenticated requests receive `401`; a consumer cannot query a customer outside its allowed set.

**Effort:** M · **Owner:** backend developer + product owner

### B5. Security regression tests and CI gate

Add automated tests alongside the P0/P1 fixes (not later): the acceptance tests in §2–§3 above belong in the suite that runs on every PR. Add a CI job that fails the build on any `password` field in user response schemas. Test the missing-secret startup failure using a dedicated test configuration — do not put production secrets into CI.

**Effort:** S–M · **Owner:** backend developer

---

## 4. P2 — Modernization

### C1. Upgrade the dependency stack

**Evidence:** [pom.xml](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/pom.xml) shows Spring Boot 2.2.6 and jjwt 0.9.1; [docker-compose.yml](https://github.com/raghubgowda/fta-backend/blob/0d8aad1/docker-compose.yml) uses the `mongo:3.2.4` image; [package.json](https://github.com/raghubgowda/fta-frontend/blob/36aa3fc/package.json) shows Angular 9 and Protractor.

**Impact:** These are legacy versions. Whether they still receive security patches was **not verified in this review** — the support and vulnerability status of each component must be confirmed against vendor policies and current advisories before treating this as a known gap. Unpatched dependencies turn future CVEs into permanent exposure.

**Remediation**
1. **Assess first:** run a current support-status and vulnerability assessment (e.g. OWASP DependencyCheck, Snyk, or the vendors' support pages) on all dependencies. Make this assessment a production-release gate: no production deployment while a known-unsupported or known-vulnerable component is in the runtime path.
2. Backend upgrade: follow the vendor's documented migration path from Spring Boot 2.2 to a currently supported line; the security config rewrite (`WebSecurityConfigurerAdapter` → `SecurityFilterChain`) should be done together with A1. Upgrade jjwt per its migration guide.
3. Database: upgrade the MongoDB image to a currently supported version per the vendor's upgrade path.
4. Frontend: upgrade Angular major-by-major using `ng update`; replace Protractor with a maintained e2e framework (e.g. Cypress or Playwright).
5. Select concrete version targets at implementation time from vendor support policies — do not treat any version number in this plan as a verified current target.

**Effort:** L (multi-day/multi-week) · **Owner:** backend + frontend developers

### C2. Externalize configuration

**Evidence:** No application configuration files were found in the backend repo; the JWT key and token validity are compile-time constants. Frontend service classes hardcode `http://localhost:8080` instead of using the (unused) `environments` mechanism. Other configuration may exist outside the repo and was not reviewed.

**Remediation:** Create `application.yml` with env-var placeholders for the MongoDB URI/credentials and JWT settings; move frontend API URLs into `environment.ts`/`environment.prod.ts`; keep secrets out of the repo.

**Effort:** S · **Owner:** backend + frontend developers

### C3. Scope bearer-token attachment

**Evidence:** [`JwtInterceptor`](https://github.com/raghubgowda/fta-frontend/blob/36aa3fc/src/app/helpers/jwt.interceptor.ts) attaches the `Authorization` header to every outgoing HTTP request.

**Impact:** The JWT leaks to any third-party origin the app calls. Low severity for the current app (no third-party calls), but a good hygiene fix.

**Remediation:** Only attach the header when the request targets the configured API origin. If a future redesign moves the token from `localStorage` to cookies, add CSRF protection in the same change — cookie storage without CSRF defense is not automatically safer.

**Effort:** S · **Owner:** frontend developer

---

## 5. Optional — requirement-driven hardening

Not automatic requirements; adopt only if the product needs them:
- **Refresh tokens / revocation:** current 5-minute access-token validity limits replay value; add refresh + revocation if sessions must be terminated server-side.
- **HMAC-signed or mTLS-secured evaluation API:** stronger alternatives to API keys for B4 if consumer systems can support them.
- **Audit logging:** who changed which toggle, when — valuable for a feature-flag platform's accountability.

---

## 6. Acceptance test summary (release checklist)

- [ ] `VIEWER` token → feature write returns `403`
- [ ] Full permission matrix enforced for every endpoint in the matrix table (including customer endpoints)
- [ ] Sign-up cannot set `role`; stored default is least-privileged
- [ ] `PUT /api/v1/users` cannot change password or role
- [ ] Cross-user modification attempts are denied
- [ ] No `password` key in any user API response — including the sign-up response
- [ ] Password-change endpoint requires the current password for self-service
- [ ] No log line contains a plaintext password (debug-level sign-up test)
- [ ] App refuses to start without the JWT secret env var
- [ ] Pre-rotation tokens rejected after key rotation
- [ ] Existing user accounts audited; roles re-verified
- [ ] MongoDB unreachable without credentials; port not published
- [ ] Production endpoints HTTPS-only with HSTS
- [ ] Login rate limiting active
- [ ] Evaluation API access decision documented (secured or accepted)
- [ ] Security regression tests run in CI on every PR

---

## 7. Suggested sequence

| Wave | Items | Outcome |
|------|-------|---------|
| 1 — now | A1 + A2 + A3 + A4 (single coordinated release, with tests) | Active risks contained; old tokens invalidated |
| 2 — before exposure | B1, B2, B3, **B4 decision**, B5, C1 dependency/vulnerability assessment | Production-ready posture: safe to deploy beyond localhost only once the B4 decision is made and the dependency assessment passes |
| 3 — next iteration | C2, C3 | Configuration hygiene |
| 4 — planned project | C1 upgrades | Supported, patchable stack (assessment-driven) |

**Review limitations:** This plan is based on a static review of the pinned commits above. Runtime behavior, deployment configuration, and the actual support status of dependencies at implementation time should be verified during remediation.
