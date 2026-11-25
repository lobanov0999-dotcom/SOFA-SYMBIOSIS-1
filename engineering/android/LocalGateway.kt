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
    val operation: GatewayOperation
)

sealed class GatewayResult {
    data class Allowed(val requestId: String) : GatewayResult()
    data class Denied(val requestId: String, val reason: String) : GatewayResult()
    data class Invalid(val reason: String) : GatewayResult()
}

/**
 * Policy-only request router. It deliberately does not execute operations;
 * Android UI/repository adapters must be wired separately after approval UX exists.
 */
class LocalGateway(private val gate: CapabilityGate) {
    fun authorize(request: GatewayRequest): GatewayResult {
        if (request.requestId.isBlank()) return GatewayResult.Invalid("requestId is required")
        if (!gate.isAllowed(request.operation.capability)) {
            return GatewayResult.Denied(request.requestId, "Capability not granted")
        }
        return GatewayResult.Allowed(request.requestId)
    }
}
