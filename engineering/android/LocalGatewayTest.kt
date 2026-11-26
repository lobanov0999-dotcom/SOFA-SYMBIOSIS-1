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
            gateway.authorize(GatewayRequest("core-1", GatewayOperation.APPLY_CORE_UPDATE, "sha256:core")))
    }

    @Test fun coreUpdateRequiresDigestAndFreshOwnerApproval() {
        val gate = CapabilityGate(setOf(GatewayCapability.MODIFY_CORE))
        val approvals = CoreChangeApprovalGate(nowMillis = { 10_000L })
        val gateway = LocalGateway(gate, approvals)
        assertEquals(GatewayResult.Invalid("changeDigest is required for core updates"),
            gateway.authorize(GatewayRequest("core-2", GatewayOperation.APPLY_CORE_UPDATE)))
        val request = GatewayRequest("core-2", GatewayOperation.APPLY_CORE_UPDATE, "sha256:proposal")
        assertEquals(GatewayResult.Denied("core-2", "Fresh owner approval required"), gateway.authorize(request))
        assertEquals(true, approvals.approve("core-2", "sha256:proposal"))
        assertEquals(GatewayResult.Allowed("core-2"), gateway.authorize(request))
        assertEquals(GatewayResult.Denied("core-2", "Request already processed"), gateway.authorize(request))
    }

    @Test fun successfulRequestIdCannotBeReplayedAcrossOperations() {
        val gateway = LocalGateway(CapabilityGate(setOf(
            GatewayCapability.READ_LOCAL_STATUS,
            GatewayCapability.WRITE_LOCAL_NOTE
        )))
        assertEquals(GatewayResult.Allowed("same-id"),
            gateway.authorize(GatewayRequest("same-id", GatewayOperation.READ_STATUS)))
        assertEquals(GatewayResult.Denied("same-id", "Request already processed"),
            gateway.authorize(GatewayRequest("same-id", GatewayOperation.SAVE_NOTE)))
    }

    @Test fun deniedRequestDoesNotConsumeItsId() {
        val gate = CapabilityGate()
        val gateway = LocalGateway(gate)
        val request = GatewayRequest("retry-after-consent", GatewayOperation.SAVE_NOTE)
        assertEquals(GatewayResult.Denied("retry-after-consent", "Capability not granted"), gateway.authorize(request))
        gate.grant(GatewayCapability.WRITE_LOCAL_NOTE)
        assertEquals(GatewayResult.Allowed("retry-after-consent"), gateway.authorize(request))
    }
}
