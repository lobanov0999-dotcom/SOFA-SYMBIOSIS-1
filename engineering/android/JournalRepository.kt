package com.sofa.symbiosis

/**
 * Stable application-facing contract for the existing on-device journal.
 * Implementations must preserve the current LocalStore database and records.
 */
interface JournalRepository {
    fun putValue(key: String, value: String)
    fun getValue(key: String, fallback: String): String
    fun saveCycle(state: Int, phase: Int, density: Double, ema: Double, strategy: String, note: String)
    fun saveNote(note: String)
    fun recent(limit: Int = 12): List<String>
    fun exportText(): String
}

/** Adapter over the existing SQLite LocalStore; no schema or data changes. */
class LocalStoreJournalRepository(
    private val store: LocalStore
) : JournalRepository {
    override fun putValue(key: String, value: String) = store.putValue(key, value)
    override fun getValue(key: String, fallback: String): String = store.getValue(key, fallback)
    override fun saveCycle(state: Int, phase: Int, density: Double, ema: Double, strategy: String, note: String) =
        store.saveCycle(state, phase, density, ema, strategy, note)
    override fun saveNote(note: String) = store.saveNote(note)
    override fun recent(limit: Int): List<String> = store.recent(limit)
    override fun exportText(): String = store.exportText()
}
