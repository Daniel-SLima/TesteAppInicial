package com.danielslima.testeappinicial

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale

class ViraReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (
            intent.action != ViraReminderScheduler.ACTION_REMINDER ||
            !ViraReminderScheduler.temPermissao(context)
        ) return

        val movementId = intent.getLongExtra(
            ViraReminderScheduler.EXTRA_MOVEMENT_ID,
            -1L
        )
        val kind = intent.getIntExtra(ViraReminderScheduler.EXTRA_KIND, 0)
        if (movementId <= 0L) return

        val database = MovimentacaoDatabase(context.applicationContext)
        val movimentacao = try {
            database.buscarPorId(movementId)
        } finally {
            database.close()
        } ?: return

        if (movimentacao.status != StatusMovimentacao.PENDENTE) return

        val hoje = LocalDate.now()
        val vencimento = movimentacao.data.toLocalDate()
        val valido = when (kind) {
            ViraReminderScheduler.KIND_BEFORE -> vencimento == hoje.plusDays(1)
            ViraReminderScheduler.KIND_DUE -> vencimento == hoje
            ViraReminderScheduler.KIND_OVERDUE -> vencimento.isBefore(hoje)
            else -> false
        }
        if (!valido) return

        val token = "vira_notified_" + movementId + "_" + kind + "_" + vencimento
        val prefs = context.getSharedPreferences("fintest_preferences", Context.MODE_PRIVATE)
        if (prefs.getBoolean(token, false)) return

        ViraReminderScheduler.criarCanal(context)

        val moeda = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        val valor = moeda.format(BigDecimal.valueOf(movimentacao.valorCentavos, 2))
        val gasto = movimentacao.tipo == TipoMovimentacao.GASTO

        val titulo = when (kind) {
            ViraReminderScheduler.KIND_BEFORE ->
                if (gasto) "Vence amanhã" else "Você recebe amanhã"
            ViraReminderScheduler.KIND_DUE ->
                if (gasto) "Vence hoje" else "Recebimento previsto hoje"
            else ->
                if (gasto) "Pagamento atrasado" else "Recebimento pendente"
        }

        val openApp = PendingIntent.getActivity(
            context,
            ViraReminderScheduler.requestCode(movementId, kind) + 1_000_000_000,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("abrir_movimentacoes", true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, ViraReminderScheduler.CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }

        val notification = builder
            .setSmallIcon(R.drawable.ic_vira_notification)
            .setContentTitle(titulo)
            .setContentText(movimentacao.descricao + " • " + valor)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .setCategory(Notification.CATEGORY_REMINDER)
            .build()

        context.getSystemService(NotificationManager::class.java).notify(
            ViraReminderScheduler.requestCode(movementId, kind),
            notification
        )
        prefs.edit().putBoolean(token, true).apply()
    }
}
