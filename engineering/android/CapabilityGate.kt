package com.sofa.symbiosis.gateway

/**
 * Fail-closed, in-memory capability gate for the local gateway.
 * Grants are intentionally ephemeral until an owner-confirmation UI and
 * durable approval policy are implemented. Nothing is granted by default.
 */
enum class GatewayCapability {
    READ_LOCAL_STATUS,
    WRITE_LOCAL_NOTE,
    EXPORT_LOCAL_JOURNAL,
    CONNECT_CLOUD,
    MODIFY_CORE
}

class CapabilityGate(initiallyGranted: Set<GatewayCapability> = emptySet()) {
    private val granted = initiallyGranted.toMutableSet()

    @Synchronized
    fun isAllowed(capability: GatewayCapability): Boolean = capability in granted

    @Synchronized
    fun grant(capability: GatewayCapability) {
        granted.add(capability)
    }

    @Synchronized
    fun revoke(capability: GatewayCapability) {
        granted.remove(capability)
    }

    @Synchronized
    fun revokeAll() {
        granted.clear()
    }
}
