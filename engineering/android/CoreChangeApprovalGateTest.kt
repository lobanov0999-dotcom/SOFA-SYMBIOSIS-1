package com.sofa.symbiosis.gateway

import org.junit.Assert.*
import org.junit.Test

class CoreChangeApprovalGateTest {
    @Test fun approvalIsBoundToExactRequestAndDigestAndCanBeUsedOnce() {
        var now = 1_000L
        val approvals = CoreChangeApprovalGate(nowMillis = { now })
        assertTrue(approvals.approve("req-1", "sha256:abc"))
        assertFalse(approvals.consume("req-1", "sha256:wrong"))
        assertFalse(approvals.consume("req-1", "sha256:abc"))
        assertTrue(approvals.approve("req-1", "sha256:abc"))
        assertTrue(approvals.consume("req-1", "sha256:abc"))
        assertFalse(approvals.consume("req-1", "sha256:abc"))
    }

    @Test fun expiredApprovalIsRejected() {
        var now = 1_000L
        val approvals = CoreChangeApprovalGate(nowMillis = { now })
        assertTrue(approvals.approve("req-2", "sha256:def", 500L))
        now += 500L
        assertFalse(approvals.consume("req-2", "sha256:def"))
    }

    @Test fun invalidAndOverlongApprovalsAreRejected() {
        val approvals = CoreChangeApprovalGate(nowMillis = { 1_000L })
        assertFalse(approvals.approve("", "sha256:x"))
        assertFalse(approvals.approve("req", ""))
        assertFalse(approvals.approve("req", "sha256:x", 0L))
        assertFalse(approvals.approve("req", "sha256:x", 300_001L))
    }

    @Test fun revokeRemovesPendingApproval() {
        val approvals = CoreChangeApprovalGate(nowMillis = { 1_000L })
        assertTrue(approvals.approve("req-3", "sha256:ghi"))
        approvals.revoke("req-3")
        assertFalse(approvals.consume("req-3", "sha256:ghi"))
    }
}
