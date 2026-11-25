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

## Core-change approval hardening (new, policy primitive only)
- Added a short-lived, single-use approval gate bound to one request ID and one exact change digest.
- A core-update request now requires both the MODIFY_CORE capability and a matching unexpired approval.
- Approval records are in-memory only; they expire after 60 seconds by default and are capped at five minutes.
- Replays, mismatched digests, missing digests, expired approvals, and revoked approvals are rejected.
- This is not yet connected to an Android notification/dialog. No core update is executed by LocalGateway; it remains an authorization-only router.
- Added JVM tests for expiry, scope binding, one-time use, revocation, and gateway enforcement.

## Local journal repository integration
- Added a `JournalRepository` interface and `LocalStoreJournalRepository` adapter.
- MainActivity now depends on the repository contract while the adapter delegates to the existing `LocalStore`.
- Existing SQLite database name, schema version, tables, SQL, key names, and record formats are unchanged.
- The CI workflow overlays the adapter and updated activity into the extracted Android source before compilation.
- This is an abstraction boundary only: no database migration, encryption claim, backup-policy change, or background access was introduced.
- Physical-device upgrade/data-retention testing is still required before treating persistence integration as production-ready.
