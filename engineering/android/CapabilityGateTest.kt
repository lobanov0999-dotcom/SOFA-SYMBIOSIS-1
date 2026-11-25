package com.sofa.symbiosis.gateway

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CapabilityGateTest {
    @Test fun deniesEverythingByDefault() {
        val gate = CapabilityGate()
        GatewayCapability.values().forEach { assertFalse(gate.isAllowed(it)) }
    }

    @Test fun grantIsNarrowAndRevocable() {
        val gate = CapabilityGate()
        gate.grant(GatewayCapability.WRITE_LOCAL_NOTE)
        assertTrue(gate.isAllowed(GatewayCapability.WRITE_LOCAL_NOTE))
        assertFalse(gate.isAllowed(GatewayCapability.CONNECT_CLOUD))
        gate.revoke(GatewayCapability.WRITE_LOCAL_NOTE)
        assertFalse(gate.isAllowed(GatewayCapability.WRITE_LOCAL_NOTE))
    }

    @Test fun revokeAllClearsEveryGrant() {
        val gate = CapabilityGate(setOf(
            GatewayCapability.READ_LOCAL_STATUS,
            GatewayCapability.EXPORT_LOCAL_JOURNAL
        ))
        gate.revokeAll()
        GatewayCapability.values().forEach { assertFalse(gate.isAllowed(it)) }
    }
}
