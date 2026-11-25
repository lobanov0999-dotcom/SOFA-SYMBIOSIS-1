package com.sofa.symbiosis.gateway

import org.junit.Assert.assertEquals
import org.junit.Test

class LocalGatewayTest {
    @Test fun requestIsDeniedUntilCapabilityIsGranted() {
        val gate = CapabilityGate()
        val gateway = LocalGateway(gate)
        val request = GatewayRequest("r-1", GatewayOperation.SAVE_NOTE)
        assertEquals(GatewayResult.Denied("r-1", "Capability not granted"), gateway.authorize(request))
        gate.grant(GatewayCapability.WRITE_LOCAL_NOTE)
        assertEquals(GatewayResult.Allowed("r-1"), gateway.authorize(request))
    }

    @Test fun blankRequestIdIsRejected() {
        val gateway = LocalGateway(CapabilityGate(setOf(GatewayCapability.READ_LOCAL_STATUS)))
        assertEquals(GatewayResult.Invalid("requestId is required"),
            gateway.authorize(GatewayRequest(" ", GatewayOperation.READ_STATUS)))
    }

    @Test fun coreUpdateRequiresItsOwnCapability() {
        val gate = CapabilityGate(setOf(GatewayCapability.WRITE_LOCAL_NOTE))
        val gateway = LocalGateway(gate)
        assertEquals(GatewayResult.Denied("core-1", "Capability not granted"),
            gateway.authorize(GatewayRequest("core-1", GatewayOperation.APPLY_CORE_UPDATE)))
    }
}
