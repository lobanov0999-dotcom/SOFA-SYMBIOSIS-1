package com.sofa.symbiosis.gateway

import com.sofa.symbiosis.JournalRepository
import org.junit.Assert.*
import org.junit.Test

class LocalGatewayExecutorTest {
    private class FakeJournal : JournalRepository {
        val values = mutableMapOf<String, String>()
        val notes = mutableListOf<String>()
        var exported = "private journal"
        override fun putValue(key: String, value: String) { values[key] = value }
        override fun getValue(key: String, fallback: String) = values[key] ?: fallback
        override fun saveCycle(state: Int, phase: Int, density: Double, ema: Double, strategy: String, note: String) {}
        override fun saveNote(note: String) { notes.add(note) }
        override fun recent(limit: Int) = emptyList<String>()
        override fun exportText() = exported
    }

    @Test fun statusRequiresCapabilityAndReturnsOnlyStatusFields() {
        val repo = FakeJournal().apply { values["core_state"]="4"; values["ema"]="0.5"; values["strategy"]="EVENTS" }
        val executor = LocalGatewayExecutor(LocalGateway(CapabilityGate()), repo)
        assertTrue(executor.execute(GatewayRequest("s1", GatewayOperation.READ_STATUS)) is GatewayExecutionResult.Denied)
        val allowed = LocalGatewayExecutor(
            LocalGateway(CapabilityGate(setOf(GatewayCapability.READ_LOCAL_STATUS))), repo
        ).execute(GatewayRequest("s2", GatewayOperation.READ_STATUS))
        assertEquals(GatewayExecutionResult.Status("4", "0.5", "EVENTS"), allowed)
    }

    @Test fun noteIsValidatedBeforeWrite() {
        val repo = FakeJournal()
        val executor = LocalGatewayExecutor(
            LocalGateway(CapabilityGate(setOf(GatewayCapability.WRITE_LOCAL_NOTE))), repo
        )
        assertTrue(executor.execute(GatewayRequest("n1", GatewayOperation.SAVE_NOTE)) is GatewayExecutionResult.Invalid)
        assertTrue(executor.execute(GatewayRequest("n2", GatewayOperation.SAVE_NOTE), "  ") is GatewayExecutionResult.Invalid)
        assertTrue(executor.execute(GatewayRequest("n3", GatewayOperation.SAVE_NOTE), "x".repeat(4001)) is GatewayExecutionResult.Invalid)
        assertTrue(repo.notes.isEmpty())
    }

    @Test fun noteRequiresGrantAndSavesTrimmedValue() {
        val repo = FakeJournal()
        val executor = LocalGatewayExecutor(
            LocalGateway(CapabilityGate(setOf(GatewayCapability.WRITE_LOCAL_NOTE))), repo
        )
        assertEquals(GatewayExecutionResult.NoteSaved(4),
            executor.execute(GatewayRequest("n4", GatewayOperation.SAVE_NOTE), "  test  "))
        assertEquals(listOf("test"), repo.notes)
    }

    @Test fun exportRequiresGrantAndReturnsJournalOnlyAfterAuthorization() {
        val repo = FakeJournal()
        val denied = LocalGatewayExecutor(LocalGateway(CapabilityGate()), repo)
            .execute(GatewayRequest("e1", GatewayOperation.EXPORT_JOURNAL))
        assertTrue(denied is GatewayExecutionResult.Denied)
        val allowed = LocalGatewayExecutor(
            LocalGateway(CapabilityGate(setOf(GatewayCapability.EXPORT_LOCAL_JOURNAL))), repo
        ).execute(GatewayRequest("e2", GatewayOperation.EXPORT_JOURNAL))
        assertEquals(GatewayExecutionResult.JournalExport("private journal"), allowed)
    }

    @Test fun cloudAndCoreOperationsNeverExecute() {
        val all = GatewayCapability.values().toSet()
        val executor = LocalGatewayExecutor(LocalGateway(CapabilityGate(all)), FakeJournal())
        assertTrue(executor.execute(GatewayRequest("c1", GatewayOperation.CLOUD_REQUEST)) is GatewayExecutionResult.Denied)
        assertTrue(executor.execute(GatewayRequest("k1", GatewayOperation.APPLY_CORE_UPDATE, "sha256:x")) is GatewayExecutionResult.Denied)
    }
}
