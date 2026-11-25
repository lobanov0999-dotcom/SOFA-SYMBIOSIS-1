# SOFA Gateway implementation status

## Implemented in this branch
- Added a fail-closed capability gate as a standalone Kotlin source overlay.
- All capabilities are denied by default.
- Grants are narrow, revocable, and in-memory only.
- Added JVM tests for default denial, scoped grants, revocation, and revoke-all.
- The overlay is copied into the extracted Android project by the branch build workflow.

## Deliberate limitations
- This is not yet wired to UI actions or SQLite operations.
- Grants do not survive process death; this is safer than silently persisting consent.
- There is no network transport, endpoint, model runtime, or background service.
- MODIFY_CORE is a reserved capability and remains denied unless explicitly granted; no core update operation is implemented.
- No secret or API key is stored in the app.

## Next integration steps
1. Add an owner-facing approval screen/notification and bind each action to the capability gate.
2. Wrap LocalStore behind a repository interface without changing the database schema.
3. Add database migration tests before any schema change.
4. Add Android Keystore operations only for concrete device-held secrets.
5. Define and approve a cloud protocol and data-sharing policy before adding INTERNET permission.
