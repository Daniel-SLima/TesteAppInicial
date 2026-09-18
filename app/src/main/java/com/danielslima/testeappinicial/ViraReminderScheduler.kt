package com.danielslima.testeappinicial

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId

object ViraReminderScheduler {
    const val CHANNEL_ID = "vira_vencimentos"
    const val ACTION_REMINDER = "com.danielslima.testeappinicial.VIRA_REMINDER"
    const val EXTRA_MOVEMENT_ID = "movement_id"
    const val EXTRA_KIND = "reminder_kind"
    const val KIND_BEFORE = 1
    const val KIND_DUE = 2
    const val KIND_OVERDUE = 3

    private const val PREFS = "fintest_preferences"
    private const val KEY_ENABLED = "notificacoes_vencimentos"
    private const val KEY_CODES = "notificacoes_alarm_codes"

    fun criarCanal(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Vencimentos e recebimentos",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Lembretes locais de contas a pagar e valores a receber."
            }
        )
    }

    fun temPermissao(context: Context): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
    }

    fun reagendar(context: Context) {
        cancelarTodos(context)
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_ENABLED, false) || !temPermissao(context)) return

        criarCanal(context)
        val database = MovimentacaoDatabase(context.applicationContext)
        val hoje = LocalDate.now()

        try {
            val mesAtual = YearMonth.from(hoje)
            repeat(3) { offset ->
                database.garantirRecorrenciasParaMes(mesAtual.plusMonths(offset.toLong()))
            }

            val pendentes = database.listarPendentesEntre(
                hoje.atStartOfDay(),
                mesAtual.plusMonths(3).atDay(1).atStartOfDay()
            )
            val codes = mutableSetOf<String>()
            pendentes.forEach { agendarMovimentacao(context, it, codes) }
            prefs.edit().putStringSet(KEY_CODES, codes).apply()
        } finally {
            database.close()
        }
    }

    fun cancelarTodos(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val alarmManager = context.getSystemService(AlarmManager::class.java)

        prefs.getStringSet(KEY_CODES, emptySet())?.toList().orEmpty().forEach { raw ->
            val code = raw.toIntOrNull() ?: return@forEach
            val pending = PendingIntent.getBroadcast(
                context,
                code,
                Intent(context, ViraReminderReceiver::class.java).apply {
                    action = ACTION_REMINDER
                },
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pending != null) {
                alarmManager.cancel(pending)
                pending.cancel()
            }
        }
        prefs.edit().remove(KEY_CODES).apply()
    }

    private fun agendarMovimentacao(
        context: Context,
        movimentacao: Movimentacao,
        codes: MutableSet<String>
    ) {
        val vencimento = movimentacao.data.toLocalDate()
        agendar(context, movimentacao.id, KIND_BEFORE, vencimento.minusDays(1), codes)
        agendar(context, movimentacao.id, KIND_DUE, vencimento, codes)
        agendar(context, movimentacao.id, KIND_OVERDUE, vencimento.plusDays(1), codes)
    }

    private fun agendar(
        context: Context,
        movementId: Long,
        kind: Int,
        date: LocalDate,
        codes: MutableSet<String>
    ) {
        val agora = System.currentTimeMillis()
        var triggerAt = date.atTime(LocalTime.of(9, 0))
            .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val hoje = LocalDate.now()

        if (triggerAt <= agora) {
            val podeAvisarAgora =
                (kind == KIND_BEFORE && date == hoje) ||
                    (kind == KIND_DUE && date == hoje)
            if (!podeAvisarAgora) return
            triggerAt = agora + 4_000L
        }

        val code = requestCode(movementId, kind)
        val pending = PendingIntent.getBroadcast(
            context,
            code,
            Intent(context, ViraReminderReceiver::class.java).apply {
                action = ACTION_REMINDER
                putExtra(EXTRA_MOVEMENT_ID, movementId)
                putExtra(EXTRA_KIND, kind)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        context.getSystemService(AlarmManager::class.java).setWindow(
            AlarmManager.RTC_WAKEUP,
            triggerAt,
            60 * 60 * 1000L,
            pending
        )
        codes.add(code.toString())
    }

    fun requestCode(movementId: Long, kind: Int): Int {
        return (((movementId % 100_000_000L) * 10L) + kind).toInt()
    }
}
