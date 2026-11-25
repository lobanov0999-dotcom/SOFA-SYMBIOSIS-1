# SOFA SYMBIOSIS — engineering audit (2026-11-25)

## Repository boundaries
- Nine-state research is preserved separately in `archive/nine-state-cycle-2026-11-25`.
- Application work is isolated in `work/sofa-foundation-2026-11-25`.
- The default `main` branch and Android core have not been modified by this audit.

## Verified app snapshot
Reviewed the source artifact from successful Android Actions run `45568183532` (main commit `19e5e2234b85d4fb5ee3f378b0252789ba97f8ed`).
The app is an offline Android prototype with:
- `MainActivity.kt`: native UI, synthetic density slider, CORE cycle trigger, note entry and journal export.
- `CoreEngine.kt`: pure CORE-10 state/phase/EMA/scheduler decision logic.
- `LocalStore.kt`: SQLite journal and key/value persistence.
- `AndroidManifest.xml`: launcher activity; no INTERNET permission.
- No background service, model runtime, cloud connector, or real task/event queue.

## Findings to address before adding assistant capabilities
1. **Backup policy:** manifest currently has `android:allowBackup="true"`. This may permit Android backup flows, so the current “local-only” privacy statement needs a deliberate backup policy. Do not silently disable backup until the user-facing recovery/export implications are clear.
2. **No automated app unit tests:** the source snapshot has no `app/src/test` suite. Add tests for CORE-10 transitions, phase markers, EMA, thresholds and hysteresis before changing the algorithm.
3. **Database migrations:** `LocalStore.onUpgrade` is empty. Any schema change needs explicit migration tests; never discard the user's journal.
4. **Synthetic scheduler input:** the density slider is test-only, not a measurement of Android process/system load. Keep this label visible and do not claim adaptive performance yet.
5. **Journal export:** export is plain text via Android share sheet. Do not describe it as encrypted. An encrypted backup/restore flow needs a separate design.
6. **No actual AI assistant yet:** the app is a local scheduler and journal prototype, not a language model or an autonomous assistant. A local model/runtime and a permission-controlled tool layer remain future work.

## User approval boundary
The user requires the phone to be notified and explicit approval obtained before changes to the core or its algorithms. This audit and the next proposed test/backup work do not authorize changes to `CoreEngine.kt` or other core algorithms.

## Safe next sequence
1. Add tests around current behavior without changing core logic.
2. Decide and document the backup/privacy policy.
3. Add database migration tests before changing the schema.
4. Add a transparent, user-controlled notification/approval mechanism before any future core update.
5. Only then plan a local model/runtime and carefully scoped Android capabilities.
