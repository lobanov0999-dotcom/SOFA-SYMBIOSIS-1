package com.sofa.symbiosis

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.widget.*
import android.content.Intent
import com.sofa.symbiosis.gateway.CapabilityGate
import com.sofa.symbiosis.gateway.GatewayCapability
import com.sofa.symbiosis.gateway.GatewayExecutionResult
import com.sofa.symbiosis.gateway.GatewayOperation
import com.sofa.symbiosis.gateway.GatewayRequest
import com.sofa.symbiosis.gateway.LocalGateway
import com.sofa.symbiosis.gateway.LocalGatewayExecutor
import java.util.UUID

class MainActivity : Activity() {
    private lateinit var store: JournalRepository
    private lateinit var capabilityGate: CapabilityGate
    private lateinit var gatewayExecutor: LocalGatewayExecutor
    private var coreState = 0
    private var ema = -1.0
    private var strategy = "EVENTS"
    private lateinit var status: TextView
    private lateinit var journal: TextView
    private lateinit var density: SeekBar
    private lateinit var densityLabel: TextView

    private val bg = Color.rgb(16, 21, 34)
    private val panel = Color.rgb(27, 35, 53)
    private val ink = Color.rgb(235, 240, 250)
    private val muted = Color.rgb(164, 177, 201)
    private val accent = Color.rgb(126, 157, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store = LocalStoreJournalRepository(LocalStore(this))
        capabilityGate = CapabilityGate()
        gatewayExecutor = LocalGatewayExecutor(LocalGateway(capabilityGate), store)
        coreState = store.getValue("core_state", "0").toIntOrNull() ?: 0
        ema = store.getValue("ema", "-1.0").toDoubleOrNull() ?: -1.0
        strategy = store.getValue("strategy", "EVENTS")
        render()
    }

    private fun render() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 26, 22, 18); setBackgroundColor(bg) }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        content.addView(text("SOFA", 30, ink, true))
        content.addView(text("SYMBIOSIS 1  ·  LOCAL CORE", 13, accent, true))
        content.addView(text("Автономный прототип • данные остаются на устройстве", 12, muted, false).apply { setPadding(0, 5, 0, 18) })

        status = text("CORE-10: $coreState    Фаза: —    Стратегия: $strategy", 16, ink, true)
        content.addView(card(status))
        content.addView(text("Тестовая плотность активных процессов", 14, ink, true).apply { setPadding(0, 18, 0, 4) })
        densityLabel = text("Активно: 20%", 13, muted, false)
        content.addView(densityLabel)
        density = SeekBar(this).apply { max = 100; progress = 20; progressTintList = android.content.res.ColorStateList.valueOf(accent); thumbTintList = android.content.res.ColorStateList.valueOf(accent) }
        content.addView(density)
        density.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(s: SeekBar?, p: Int, user: Boolean) { densityLabel.text = "Активно: $p%" }
            override fun onStartTrackingTouch(s: SeekBar?) {}
            override fun onStopTrackingTouch(s: SeekBar?) {}
        })
        val run = button("Выполнить цикл CORE") {
            val sample = density.progress / 100.0
            val nextEma = CoreEngine.ema(ema, sample)
            val d = CoreEngine.decide(coreState, sample, ema)
            if (d.strategy != "HOLD") strategy = d.strategy
            coreState = d.coreState; ema = nextEma
            store.saveCycle(coreState, d.phase, sample, ema, strategy, d.explanation)
            store.putValue("core_state", coreState.toString())
            store.putValue("ema", ema.toString())
            store.putValue("strategy", strategy)
            status.text = "CORE-10: $coreState    Фаза: ${d.phase}    Стратегия: $strategy\nEMA: ${(ema * 100).toInt()}%\n${d.explanation}"
            refreshJournal()
        }
        content.addView(run)

        content.addView(text("Память", 19, ink, true).apply { setPadding(0, 22, 0, 8) })
        val noteInput = EditText(this).apply {
            hint = "Записать наблюдение или результат…"; setHintTextColor(muted); setTextColor(ink)
            setBackgroundColor(panel); setPadding(14, 12, 14, 12); minLines = 2; gravity = Gravity.TOP
        }
        content.addView(noteInput)
        content.addView(button("Сохранить в локальную память") {
            val value = noteInput.text.toString().trim()
            if (value.isNotEmpty()) {
                confirmSaveNote(value) {
                    noteInput.setText("")
                    refreshJournal()
                }
            } else {
                Toast.makeText(this, "Сначала введи заметку", Toast.LENGTH_SHORT).show()
            }
        })
        content.addView(text("Последние записи", 15, ink, true).apply { setPadding(0, 18, 0, 8) })
        journal = text("Пока нет записей.", 12, muted, false)
        content.addView(card(journal))
        content.addView(button("Экспортировать журнал") { confirmExportJournal() })
        content.addView(text("Версия 0.2 • Локальный шлюз • Сетевой доступ не включён", 11, muted, false).apply { setPadding(0, 20, 0, 8) })
        setContentView(root)
        refreshJournal()
    }

    private fun confirmSaveNote(value: String, onSaved: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle("Разрешить сохранение?")
            .setMessage("Следующая заметка будет записана в локальную память SOFA:\n\n$value")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Разрешить") { _, _ ->
                val request = GatewayRequest(UUID.randomUUID().toString(), GatewayOperation.SAVE_NOTE)
                executeWithTemporaryGrant(GatewayCapability.WRITE_LOCAL_NOTE, request, value) { result ->
                    when (result) {
                        is GatewayExecutionResult.NoteSaved -> {
                            Toast.makeText(this, "Записано в локальную память", Toast.LENGTH_SHORT).show()
                            onSaved()
                        }
                        else -> showGatewayResult(result)
                    }
                }
            }
            .show()
    }

    private fun confirmExportJournal() {
        AlertDialog.Builder(this)
            .setTitle("Подтвердить экспорт?")
            .setMessage("Журнал будет передан в системное меню «Поделиться». Выбранное тобой приложение сможет получить его содержимое.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Продолжить") { _, _ ->
                val request = GatewayRequest(UUID.randomUUID().toString(), GatewayOperation.EXPORT_JOURNAL)
                executeWithTemporaryGrant(GatewayCapability.EXPORT_LOCAL_JOURNAL, request) { result ->
                    if (result is GatewayExecutionResult.JournalExport) {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "SOFA SYMBIOSIS — local journal")
                            putExtra(Intent.EXTRA_TEXT, result.text)
                        }
                        startActivity(Intent.createChooser(intent, "Поделиться / сохранить журнал"))
                    } else {
                        showGatewayResult(result)
                    }
                }
            }
            .show()
    }

    private fun executeWithTemporaryGrant(
        capability: GatewayCapability,
        request: GatewayRequest,
        note: String? = null,
        onResult: (GatewayExecutionResult) -> Unit
    ) {
        capabilityGate.grant(capability)
        val result = try {
            gatewayExecutor.execute(request, note)
        } catch (_: Exception) {
            GatewayExecutionResult.Denied(request.requestId, "Local operation failed")
        } finally {
            capabilityGate.revoke(capability)
        }
        onResult(result)
    }

    private fun showGatewayResult(result: GatewayExecutionResult) {
        val message = when (result) {
            is GatewayExecutionResult.Denied -> "Операция отклонена: ${result.reason}"
            is GatewayExecutionResult.Invalid -> "Некорректный запрос: ${result.reason}"
            else -> "Операция не выполнена"
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun refreshJournal() { journal.text = store.recent().ifEmpty { listOf("Пока нет записей.") }.joinToString("\n\n──────────\n\n") }

    private fun text(value: String, size: Int, color: Int, bold: Boolean) = TextView(this).apply {
        text = value; textSize = size.toFloat(); setTextColor(color)
        if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
    }
    private fun card(view: TextView): LinearLayout = LinearLayout(this).apply {
        setBackgroundColor(panel); setPadding(15, 14, 15, 14); addView(view)
        val lp = LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = 10; layoutParams = lp
    }
    private fun button(label: String, action: () -> Unit) = Button(this).apply {
        text = label; setTextColor(ink); setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(49, 65, 101)))
        setOnClickListener { action() }
        val lp = LinearLayout.LayoutParams(-1, -2); lp.topMargin = 8; layoutParams = lp
    }
}
