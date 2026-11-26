package com.sofa.symbiosis.gateway

/**
 * Short-lived, single-use approval for one exact core-change request.
 *
 * The Android owner-confirmation UI must call approve() only after the owner
 * explicitly accepts the displayed proposal. This class is an in-memory
 * policy primitive, not a notification or UI implementation.
 */
class CoreChangeApprovalGate(
    private val nowMillis: () -> Long = System::currentTimeMillis,
    private val maxTtlMillis: Long = 5 * 60 * 1000L
) {
    private data class Approval(val digest: String, val expiresAt: Long)
    private val approvals = mutableMapOf<String, Approval>()

    @Synchronized
    fun approve(requestId: String, changeDigest: String, ttlMillis: Long = 60_000L): Boolean {
        if (requestId.isBlank() || changeDigest.isBlank()) return false
        if (ttlMillis <= 0 || ttlMillis > maxTtlMillis) return false
        val now = nowMillis()
        purgeExpired(now)
        if (requestId !in approvals && approvals.size >= MAX_PENDING_APPROVALS) return false
        approvals[requestId] = Approval(changeDigest, now + ttlMillis)
        return true
    }

    /** Consumes the approval; it cannot authorize a second request. */
    @Synchronized
    fun consume(requestId: String, changeDigest: String): Boolean {
        if (requestId.isBlank() || changeDigest.isBlank()) return false
        val approval = approvals.remove(requestId) ?: return false
        return approval.digest == changeDigest && nowMillis() < approval.expiresAt
    }

    private fun purgeExpired(now: Long) {
        val expiredIds = approvals.filterValues { now >= it.expiresAt }.keys
        expiredIds.forEach(approvals::remove)
    }

    @Synchronized
    fun revoke(requestId: String) {
        approvals.remove(requestId)
    }

    @Synchronized
    fun revokeAll() {
        approvals.clear()
    }

    companion object {
        const val MAX_PENDING_APPROVALS = 256
    }
}
