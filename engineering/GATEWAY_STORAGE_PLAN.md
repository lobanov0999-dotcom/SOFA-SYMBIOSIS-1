# SOFA SYMBIOSIS — gateway and local storage implementation plan

Status: design baseline only. This document does not claim that a gateway, model runtime, or phone bridge has been implemented.

## Goal

Create a dependable path:

Phone UI -> local Android gateway -> permission checks -> local storage / optional model adapter

Offline-first behavior is the default. Network access is an optional capability, never a prerequisite for reading the local journal or using local app functions.

## Non-negotiable boundaries

- Keep CORE-10 and its algorithms unchanged unless the owner is notified on the phone and explicitly approves the exact proposed change.
- No silent background upload, telemetry, remote command execution, or hidden network behavior.
- The gateway must expose only explicitly allow-listed operations. No arbitrary shell, filesystem-wide access, or unrestricted intents.
- Never embed provider API secrets in the APK. APK obfuscation or endpoint "masking" is not a security boundary.
- Use TLS for network transport. Keep device-held secrets in Android Keystore; do not log secrets.
- Make network state, destination, data category, and consent visible to the owner.
- Preserve existing journal data. Database schema changes require forward migrations and migration tests.
- Backup/export policy must be explicit. Do not silently change the current Android backup setting.

## Proposed modules (separate from CORE)

1. `gateway/GatewayRequest`: typed request envelope with request ID, operation, payload version, and timestamp.
2. `gateway/Capability`: small allow-list such as READ_LOCAL_STATUS, WRITE_LOCAL_NOTE, EXPORT_USER_DATA, and CONNECT_CLOUD. Every operation is denied by default.
3. `gateway/ApprovalStore`: records owner decisions for narrowly scoped capabilities. Core/algorithm updates always require a fresh, explicit approval.
4. `storage/JournalRepository`: interface over the existing local journal; no destructive migration.
5. `storage/SecretStore`: Android Keystore-backed key operations; callers never receive raw master keys.
6. `model/AssistantAdapter`: interface only. Local model and optional cloud model are separate implementations, selected explicitly.
7. `transport/CloudTransport`: disabled unless CONNECT_CLOUD is granted; sends only the minimum user-approved payload.

## Data handling

- Journal and settings remain on-device by default.
- Separate ordinary preferences from secrets.
- Export is a user-initiated action and must disclose format and destination.
- Do not claim encryption at rest until the implementation and tests prove it.
- Do not upload journal contents by default.
- Add retention and deletion controls before introducing remote sync.

## Request flow

1. UI submits a typed operation to the local gateway.
2. Gateway validates schema and checks the capability allow-list.
3. If permission is absent, return DENIED; do not try a fallback.
4. If user approval is required, show a phone notification/dialog describing the exact action and data.
5. Execute only after approval; record a non-secret audit event.
6. Return a typed result to the UI.

## Implementation order

1. Recover and version the actual Android source tree and workflow build inputs. The currently inspected source is an Actions artifact, not yet a clearly maintained source directory on this branch.
2. Add durable tests for existing CORE-10 behavior without changing the core.
3. Introduce gateway request/response interfaces and deny-by-default capability checks, outside CORE.
4. Add repository abstraction around existing SQLite storage without changing schema.
5. Add approval UI/notification and tests.
6. Add Keystore-backed secret operations and tests.
7. Add optional cloud transport only after endpoint, protocol, data scope, and consent UX are defined.
8. Add a local model runtime only after device memory/storage/performance constraints are measured.

## Acceptance criteria

- Airplane mode: journal, notes, and local CORE prototype remain usable.
- No network call occurs unless the owner enabled the relevant capability.
- A denied capability cannot be invoked through an alternate code path.
- Core update requests cannot apply without a fresh owner approval.
- Existing journal survives app upgrade and migration tests.
- CI reports the number of tests executed and fails if expected test sources are missing.
- No secrets appear in logs, exported diagnostics, or APK resources.

## Open integration facts

The current audit has not established a real gateway endpoint, API contract, or local model runtime. Do not invent these. Cloud transport remains unconfigured until the owner supplies or approves a concrete provider/endpoint and data policy.
