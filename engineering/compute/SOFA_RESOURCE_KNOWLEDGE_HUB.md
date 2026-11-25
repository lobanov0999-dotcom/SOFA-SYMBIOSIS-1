# SOFA Resource & Knowledge Hub — architecture proposal

**Status:** design-only; no runtime integration
**Date:** 2026-11-25
**Target branch:** `work/sofa-foundation-2026-11-25`

## Purpose

Define a provider-neutral way for SOFA to discover, evaluate, catalogue, and later use openly available knowledge sources, software, models, datasets, and voluntarily contributed compute nodes.

The hub must increase capability without creating a paid dependency, bypassing access controls, or weakening owner control.

## Non-negotiable policy

- **Zero-cost execution by default.** No paid provider, paid API, paid quota upgrade, billing account, wallet, or transaction is activated by the hub.
- **Owner-controlled participation.** A compute node is usable only when its owner has explicitly offered it for the stated purpose and scope.
- **Public does not mean unrestricted.** Respect licenses, terms of service, rate limits, robots policies where applicable, attribution, privacy, and redistribution restrictions.
- **No credential harvesting or access bypass.** Use documented public endpoints and credentials intentionally supplied by the owner for permitted scopes only.
- **Local-first.** Prefer local data and local execution. Network access is optional and must be separately designed and approved.
- **Verify before trust.** Record source, retrieval time, license/terms, integrity digest, and verification status. Treat remote content and code as untrusted input.
- **No silent core changes.** This hub does not modify CORE-10, `CoreEngine.kt`, or any core algorithm. `MODIFY_CORE` remains default-deny.
- **No hidden network permission.** The current Android snapshot has no INTERNET permission. This proposal does not add one.
- **Fail closed.** Unknown license, unclear authorization, missing provenance, expired free quota, or uncertain cost means do not use the resource.

## Resource classes

### 1. Knowledge sources
Examples: public documentation, open educational materials, public datasets, research repositories, standards, and public web pages.

Catalogue fields:
- stable source identifier and canonical URL;
- publisher/owner and retrieval timestamp;
- content type, language, and topic tags;
- license or terms URL and allowed-use summary;
- attribution requirements and redistribution status;
- content digest (SHA-256) and parser/version metadata;
- trust/review status and last verification timestamp.

Ingestion should store metadata first. Full-content caching is allowed only when the applicable terms permit it. Preserve source attribution and distinguish source facts from SOFA-generated interpretation.

### 2. Open software and models
Catalogue fields:
- project/model name, version, canonical repository or model card;
- exact license and any model-specific use restrictions;
- supported platforms, runtime, hardware requirements, and size;
- cryptographic digest/signature where published;
- dependency and vulnerability review status;
- provenance and last update.

Do not execute downloaded code or model conversion scripts in the Android app. Review and test in an isolated development environment first.

### 3. Compute resources
Potential classes:
- owner-operated devices explicitly enrolled as SOFA nodes;
- community or research compute explicitly offered for public use;
- genuinely free public tiers with documented quotas and no automatic paid overage;
- local CPU/GPU/NPU resources on the user's own devices.

A resource is not considered free merely because it offers a trial, promotional credit, or a free signup. Exclude resources that require a payment method with automatic billing or can silently incur charges unless a future explicit owner-approved policy says otherwise.

Catalogue fields:
- provider/node identity and authorization basis;
- CPU/GPU model, accelerator memory, system RAM, storage;
- region and measured latency;
- available quota, expiry, and restrictions;
- data-handling and retention policy;
- supported task types and runtime;
- health, availability, and last successful probe.

Never infer permission from a reachable IP address, open port, public dashboard, or exposed API.

## Task scheduling model

Represent each job with:
- task ID and owner;
- task class: local-only, public-data, or sensitive;
- CPU/GPU/VRAM/RAM/storage requirements;
- expected duration and input/output size;
- network requirement and latency ceiling;
- maximum cost fixed to zero under this policy;
- allowed data classification and retention;
- integrity/verification method;
- cancellation and timeout policy.

Scheduling order:
1. Run locally when feasible.
2. Otherwise consider only explicitly authorized, policy-compatible, zero-cost resources.
3. Reject a candidate if license, quota, cost, privacy, security, or reliability is unknown.
4. Send the minimum required data; sensitive/private data stays local unless a separate explicit data-sharing approval exists.
5. Verify returned output and record provenance.
6. Fall back to local execution or mark the task unavailable; never silently switch to a paid resource.

Independent jobs can be distributed more easily than one tightly coupled model/training job. The scheduler must not claim arbitrary neural-network workloads can be split across unrelated machines.

## Security and integrity controls

- Separate discovery metadata from executable payloads.
- Verify TLS endpoints and published hashes/signatures; a hash alone proves integrity against a known digest, not publisher identity.
- Sandbox parsers and any approved third-party runtime.
- Apply time, memory, file-size, and network limits.
- Do not send secrets, authentication tokens, personal records, or private journal entries to community nodes.
- Require explicit consent for each new node/provider and data scope.
- Maintain an append-only audit record of source, decision, consent scope, resource used, quota, and result digest.
- Provide immediate node/provider revocation and task cancellation.

## Suggested module boundaries

These are conceptual interfaces, not implemented classes:

- `ResourceCatalog`: stores normalized source and compute metadata.
- `KnowledgeSourceAdapter`: reads one documented public source.
- `SoftwareModelRegistry`: records open-source and model metadata.
- `ComputeProviderAdapter`: reports offers and executes only authorized jobs.
- `ResourcePolicy`: enforces zero-cost, license, privacy, and consent constraints.
- `TaskRequirements`: describes resource and data needs.
- `Scheduler`: selects only policy-compliant resources.
- `ResultVerifier`: checks returned output and provenance.
- `AuditLedger`: records decisions and outcomes.

Adapters must be replaceable; SOFA must not depend on one provider's proprietary SDK or identity system.

## Delivery sequence

1. **Catalogue schema and policy tests** — no network permission, no provider credentials, no execution.
2. **Static source registry** — curated public documentation, licenses, open software, and model metadata.
3. **Local resource inventory** — measure only capabilities the device explicitly exposes; do not mislabel synthetic test inputs as system telemetry.
4. **Read-only discovery adapters** — only for documented public endpoints, after a separate network/data approval.
5. **Volunteer node protocol** — opt-in enrollment, identity, narrow capabilities, revocation, quotas, and audit.
6. **Sandboxed task execution** — only after threat model, tests, and explicit owner approval.
7. **Phone integration** — separate review for Android INTERNET permission, data disclosures, and user controls.

## Acceptance criteria before any runtime integration

- Unit tests prove that unknown, paid, expired, unauthorized, or policy-incompatible resources are rejected.
- No code path can fall back to paid execution.
- A resource cannot receive data outside its declared scope.
- Every external result has source and integrity metadata.
- Provider/node revocation prevents new dispatches.
- No core algorithm or database schema is modified by this work.
- CI verifies the policy tests and source package.
- Physical-device testing is completed before claiming Android integration works.

## Current reality

This document is an architecture proposal only. The existing Android prototype does not yet contain this hub, a general task queue, a network adapter, a local model runtime, or a compute dispatcher. No external compute has been connected, no data has been uploaded, and no money has been spent.
