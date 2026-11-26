package com.sofa.symbiosis.gateway

import com.sofa.symbiosis.JournalRepository

sealed class GatewayExecutionResult {
    data class Status(
        val coreState: String,
        val ema: String,
        val strategy: String
    ) : GatewayExecutionResult()

    data class NoteSaved(val characters: Int) : GatewayExecutionResult()
    data class JournalExport(val text: String) : GatewayExecutionResult()
    data class Denied(val requestId: String, val reason: String) : GatewayExecutionResult()
    data class Invalid(val reason: String) : GatewayExecutionResult()
}

/**
 * Executes only explicitly supported local journal operations.
 * Authorization remains in LocalGateway; this class never mutates CORE state,
 * performs network calls, or exposes journal contents through READ_STATUS.
 */
class LocalGatewayExecutor(
    private val gateway: LocalGateway,
    private val journal: JournalRepository
) {
    fun execute(request: GatewayRequest, note: String? = null): GatewayExecutionResult {
        if (request.operation == GatewayOperation.SAVE_NOTE) {
            val normalized = note?.trim()
                ?: return GatewayExecutionResult.Invalid("note is required")
            if (normalized.isEmpty()) return GatewayExecutionResult.Invalid("note is empty")
            if (normalized.length > MAX_NOTE_CHARACTERS) {
                return GatewayExecutionResult.Invalid("note exceeds $MAX_NOTE_CHARACTERS characters")
            }
        }

        return when (val authorization = gateway.authorize(request)) {
            is GatewayResult.Invalid -> GatewayExecutionResult.Invalid(authorization.reason)
            is GatewayResult.Denied -> GatewayExecutionResult.Denied(
                authorization.requestId, authorization.reason
            )
            is GatewayResult.Allowed -> executeAuthorized(request, note?.trim())
        }
    }

    private fun executeAuthorized(
        request: GatewayRequest,
        normalizedNote: String?
    ): GatewayExecutionResult = when (request.operation) {
        GatewayOperation.READ_STATUS -> GatewayExecutionResult.Status(
            coreState = journal.getValue("core_state", "0"),
            ema = journal.getValue("ema", "-1.0"),
            strategy = journal.getValue("strategy", "EVENTS")
        )
        GatewayOperation.SAVE_NOTE -> {
            if (normalizedNote == null) {
                GatewayExecutionResult.Invalid("note is required")
            } else {
                journal.saveNote(normalizedNote)
                GatewayExecutionResult.NoteSaved(normalizedNote.length)
            }
        }
        GatewayOperation.EXPORT_JOURNAL ->
            GatewayExecutionResult.JournalExport(journal.exportText())
        GatewayOperation.CLOUD_REQUEST ->
            GatewayExecutionResult.Denied(request.requestId, "Cloud execution is not implemented")
        GatewayOperation.APPLY_CORE_UPDATE ->
            GatewayExecutionResult.Denied(request.requestId, "Core updates are not executable through this gateway")
    }

    companion object {
        const val MAX_NOTE_CHARACTERS = 4_000
    }
}
