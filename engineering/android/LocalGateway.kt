package com.sofa.symbiosis.gateway

enum class GatewayOperation(val capability: GatewayCapability) {
    READ_STATUS(GatewayCapability.READ_LOCAL_STATUS),
    SAVE_NOTE(GatewayCapability.WRITE_LOCAL_NOTE),
    EXPORT_JOURNAL(GatewayCapability.EXPORT_LOCAL_JOURNAL),
    CLOUD_REQUEST(GatewayCapability.CONNECT_CLOUD),
    APPLY_CORE_UPDATE(GatewayCapability.MODIFY_CORE)
}

data class GatewayRequest(
    val requestId: String,
    val operation: GatewayOperation,
    /** Stable digest of the exact proposed change; required for core updates. */
    val changeDigest: String? = null
)

sealed class GatewayResult {
    data class Allowed(val requestId: String) : GatewayResult()
    data class Denied(val requestId: String, val reason: String) : GatewayResult()
    data class Invalid(val reason: String) : GatewayResult()
}

/**
 * Policy-only request router. It deliberately does not execute operations.
 * Core updates require both the MODIFY_CORE capability and a fresh,
 * single-use approval bound to the exact change digest.
 */
class LocalGateway(
    private val gate: CapabilityGate,
    private val coreApprovals: CoreChangeApprovalGate = CoreChangeApprovalGate()
) {
    fun authorize(request: GatewayRequest): GatewayResult {
        if (request.requestId.isBlank()) return GatewayResult.Invalid("requestId is required")
        if (request.operation == GatewayOperation.APPLY_CORE_UPDATE &&
            request.changeDigest.isNullOrBlank()) {
            return GatewayResult.Invalid("changeDigest is required for core updates")
        }
        if (!gate.isAllowed(request.operation.capability)) {
            return GatewayResult.Denied(request.requestId, "Capability not granted")
        }
        if (request.operation == GatewayOperation.APPLY_CORE_UPDATE &&
            !coreApprovals.consume(request.requestId, request.changeDigest!!)) {
            return GatewayResult.Denied(request.requestId, "Fresh owner approval required")
        }
        return GatewayResult.Allowed(request.requestId)
    }
}
