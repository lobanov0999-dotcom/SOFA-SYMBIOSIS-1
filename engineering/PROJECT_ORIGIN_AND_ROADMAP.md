# SOFA SYMBIOSIS 1 — origin, continuity, and roadmap

**Document status:** reconstructed project record  
**Reconstruction date:** 2026-11-25  
**Working branch:** `work/sofa-foundation-2026-11-25`

## 1. Why this document exists

This document connects the original SOFA SYMBIOSIS idea to the Android/Kotlin prototype and the current engineering work. It is a continuity record: implementation details may change, but the original purpose and the user's approval boundary must remain visible.

This is a reconstruction from the conversation history currently available and the repository audit. It is not a verbatim transcript of the first conversation. Where the exact original wording is unavailable, the record says so rather than inventing it.

## 2. Earliest recovered project record

The earliest currently recovered project-specific record is dated **2026-11-22, 11:50 UTC**.

That record identifies SOFA SYMBIOSIS 1 as:
- an Android/Kotlin project;
- intended for one Xiaomi phone as its initial device;
- local-first, with internet as optional rather than mandatory;
- intended to preserve state and experience locally.

The associated early project description names these initial building blocks:
- **CORE-10** — the initial state/decision-cycle mechanism;
- **SQLite memory/journal** — local persistence;
- **persisted state** — continuity across app launches;
- **EVENTS / FULL_SCAN strategy switching** — an early strategy-selection concept;
- **journal export** — a way to retain or move a record of experience;
- **offline autonomy** — the design direction for operation without a network connection.

The original source conversation is not fully available in this reconstruction. These points are the earliest recoverable summary, not a claim that every detail above appeared in exactly this form in the first message.

## 3. Original purpose, preserved as project intent

The project is not merely an APK or a chat interface. Its purpose is to explore a user-controlled, local-first environment in which an AI assistant can maintain useful continuity, inspect information, reason over accumulated records, and use device capabilities through explicit permissions.

The intended direction is:
1. **Continuity:** retain a local journal and relevant state between sessions.
2. **Local-first operation:** keep core records and basic operation on the device; make network access optional and deliberate.
3. **A controlled device bridge:** expose narrow, understandable capabilities for reading, saving, exporting, and later other explicitly approved actions.
4. **Analysis and learning from records:** use stored experience to compare outcomes, identify patterns, test hypotheses, and improve methods.
5. **Auditable improvement:** allow the system to propose changes, explain them, and test them; never silently rewrite its own core.
6. **Optional cloud connection:** use remote models or services only through a separately designed and approved data-sharing boundary.

Long-term ambition includes exploring increasingly capable planning, self-evaluation, and problem-solving. This is a research direction, not a claim that the current prototype is conscious, generally intelligent, or capable of recursive self-improvement.

## 4. Non-negotiable owner approval boundary

The user explicitly requires advance notice and explicit approval on the phone before changes to the core or its algorithms.

Therefore:
- core and algorithm changes must not be made unilaterally;
- a proposal must identify what will change and why;
- approval must be explicit, narrow, and tied to the proposed change;
- denial or revocation must remain possible;
- no hidden or remote approval is acceptable;
- the initial approval scope is one phone.

This rule is part of the project's architecture and governance, not an optional UI preference.

## 5. Evolution from idea to current prototype

### Stage A — initial local-first concept (2026-11-22; recovered summary)
The project was framed as an Android/Kotlin implementation for one phone, with local persistence, a CORE-10 cycle, strategy switching, journal export, and offline-first operation.

### Stage B — source audit and reality check (2026-11-25)
The inspected source snapshot showed a small offline Android prototype:
- `MainActivity.kt`: native UI, synthetic density slider, CORE cycle trigger, note entry, and journal export;
- `CoreEngine.kt`: CORE-10 state/phase/EMA/scheduler decision logic;
- `LocalStore.kt`: SQLite journal and key/value persistence;
- launcher manifest with no INTERNET permission.

The audit found no local language-model runtime, no cloud connector, no background service, and no real task/event queue. The density slider is synthetic test input, not a measurement of actual Android system load. The journal export is plain text, not encrypted.

### Stage C — safety and test foundations
The working branch added a fail-closed capability gate and a short-lived, single-use approval primitive for a future core-change request. These are authorization primitives only: they do not yet execute operations and are not yet connected to a phone notification or approval screen. Grants and approvals are in-memory.

### Stage D — persistence boundary
A `JournalRepository` interface and `LocalStoreJournalRepository` adapter were added around the existing SQLite store. The intent is to establish a stable boundary without changing the database schema. This does not add encryption, migration, background access, or a new memory model.

## 6. Current architecture map

| Layer | Current state | Intended next capability |
|---|---|---|
| Android shell | Existing native app prototype | Clear owner-facing controls and status |
| CORE-10 | Existing experimental scheduler/state logic | Preserve behavior; add tests before any approved algorithm change |
| Local persistence | Existing SQLite journal and key/value store | Repository abstraction, migration tests, deliberate backup policy |
| Capability gate | Default-deny JVM primitive | Visible, narrow, revocable phone-side permissions |
| Local gateway | Authorization-only prototype | Execute only explicitly authorized, bounded operations |
| Core-change approval | Single-use, digest-bound in-memory primitive | Phone notification/dialog with approve, deny, and revoke |
| AI/model runtime | Not implemented | Evaluate a local model/runtime separately |
| Cloud bridge | Not implemented; app manifest currently has no INTERNET permission in audited snapshot | Design data minimization, explicit consent, and network boundary first |
| Learning/research loop | Not implemented as an autonomous learning system | Journal analysis, hypothesis tracking, evaluation, and human-reviewed proposals |

## 7. What must not be overstated

As of this reconstruction:
- the app is not yet an AI assistant or an autonomous agent;
- the gateway authorizes requests but does not yet perform the corresponding operations;
- approval primitives are not yet connected to Android UI or notifications;
- permissions are not persisted;
- no encryption guarantee exists;
- no local LLM runtime or cloud connector is present;
- physical-device installation, upgrade, and data-retention behavior still require testing;
- a successful CI build is not proof of correct operation on the phone.

These limits do not invalidate the direction. They define the gap between the original ambition and the current prototype.

## 8. Roadmap, ordered by dependency and safety

1. **Make the current build reproducible:** inspect the latest CI run and ensure the downloadable source matches what CI actually compiles.
2. **Test without changing CORE-10:** add and run durable tests for existing behavior; do not alter the algorithm as part of this step.
3. **Protect the journal:** define backup/recovery behavior and add migration/data-retention tests before changing the SQLite schema.
4. **Complete the phone-side approval experience:** show proposed action/change, exact scope, approve/deny, and revoke. Keep core modification default-deny.
5. **Connect safe gateway operations:** begin with bounded local status, note saving, and journal export; require visible permission checks.
6. **Evaluate a local model/runtime:** keep it separate from CORE-10 initially and measure device constraints, offline behavior, and resource use.
7. **Design optional cloud access:** only after defining exactly what data can leave the phone and obtaining explicit consent.
8. **Build an evidence-based learning loop:** store hypotheses, observations, outcomes, and evaluations; require owner approval for core/algorithm modifications.

## 9. Project principle

**Preserve the original purpose; verify every capability; ask before changing the core.**

The APK is a vehicle. The enduring project is the local-first, permission-controlled system and the continuity of its records, decisions, and development history.

## 10. Source and confidence notes

This reconstruction is based on:
- recovered project-history summary for 2026-11-22 11:50 UTC;
- repository engineering audit dated 2026-11-25;
- gateway implementation status and current working-branch design notes.

The full verbatim first conversation has not been recovered here. If it becomes available, this document should be amended with exact quotations and a clearer separation between the user's original wording and later engineering interpretation. Do not replace the original wording with a polished paraphrase.
