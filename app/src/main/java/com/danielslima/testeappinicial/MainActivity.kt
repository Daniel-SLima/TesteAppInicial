package com.danielslima.testeappinicial

import android.app.Activity
import android.app.AlertDialog
import android.content.res.ColorStateList
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {

    private val localeBrasil = Locale.forLanguageTag("pt-BR")
    private val moeda = NumberFormat.getCurrencyInstance(localeBrasil)
    private val movimentacoes = mutableListOf<Movimentacao>()

    private lateinit var database: MovimentacaoDatabase
    private lateinit var mainScroll: ScrollView
    private lateinit var expenseButton: Button
    private lateinit var incomeButton: Button
    private lateinit var descriptionInput: EditText
    private lateinit var valueInput: EditText
    private lateinit var balanceText: TextView
    private lateinit var incomeTotalText: TextView
    private lateinit var expenseTotalText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var movementsContainer: LinearLayout
    private lateinit var historySection: LinearLayout

    private var tipoSelecionado = TipoMovimentacao.GASTO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = MovimentacaoDatabase(applicationContext)

        mainScroll = findViewById(R.id.mainScroll)
        expenseButton = findViewById(R.id.expenseButton)
        incomeButton = findViewById(R.id.incomeButton)
        descriptionInput = findViewById(R.id.descriptionInput)
        valueInput = findViewById(R.id.valueInput)
        balanceText = findViewById(R.id.balanceText)
        incomeTotalText = findViewById(R.id.incomeTotalText)
        expenseTotalText = findViewById(R.id.expenseTotalText)
        emptyStateText = findViewById(R.id.emptyStateText)
        movementsContainer = findViewById(R.id.movementsContainer)
        historySection = findViewById(R.id.historySection)

        findViewById<TextView>(R.id.monthText).text = formatarMesAtual()

        expenseButton.setOnClickListener {
            selecionarTipo(TipoMovimentacao.GASTO)
        }

        incomeButton.setOnClickListener {
            selecionarTipo(TipoMovimentacao.GANHO)
        }

        findViewById<Button>(R.id.registerButton).setOnClickListener {
            registrarMovimentacao()
        }

        selecionarTipo(TipoMovimentacao.GASTO)
        carregarMovimentacoes()
        atualizarResumo()
        renderizarMovimentacoes()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    private fun carregarMovimentacoes() {
        try {
            movimentacoes.clear()
            movimentacoes.addAll(database.listarTodas())
        } catch (erro: SQLiteException) {
            Toast.makeText(
                this,
                "Não foi possível carregar as movimentações salvas.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun selecionarTipo(tipo: TipoMovimentacao) {
        tipoSelecionado = tipo

        val gastoSelecionado = tipo == TipoMovimentacao.GASTO

        expenseButton.text = if (gastoSelecionado) "✓ GASTOU" else "GASTOU"
        incomeButton.text = if (!gastoSelecionado) "✓ GANHOU" else "GANHOU"

        expenseButton.backgroundTintList = ColorStateList.valueOf(
            getColor(if (gastoSelecionado) R.color.expense else R.color.unselected)
        )
        incomeButton.backgroundTintList = ColorStateList.valueOf(
            getColor(if (!gastoSelecionado) R.color.income else R.color.unselected)
        )

        expenseButton.setTextColor(
            getColor(if (gastoSelecionado) R.color.white else R.color.text_primary)
        )
        incomeButton.setTextColor(
            getColor(if (!gastoSelecionado) R.color.white else R.color.text_primary)
        )
    }

    private fun registrarMovimentacao() {
        val descricao = descriptionInput.text.toString().trim()
        val valorCentavos = parseValorCentavos(valueInput.text.toString())

        if (descricao.isBlank()) {
            descriptionInput.error = "Digite o nome da movimentação"
            descriptionInput.requestFocus()
            return
        }

        if (valorCentavos == null || valorCentavos <= 0) {
            valueInput.error = "Digite um valor maior que zero"
            valueInput.requestFocus()
            return
        }

        val novaMovimentacao = try {
            database.inserir(
                tipo = tipoSelecionado,
                descricao = descricao,
                valorCentavos = valorCentavos
            )
        } catch (erro: SQLiteException) {
            Toast.makeText(
                this,
                "Não foi possível salvar a movimentação.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        movimentacoes.add(0, novaMovimentacao)

        descriptionInput.text.clear()
        valueInput.text.clear()
        descriptionInput.requestFocus()

        atualizarResumo()
        renderizarMovimentacoes()

        Toast.makeText(this, "Movimentação salva no aparelho", Toast.LENGTH_SHORT).show()

        historySection.post {
            mainScroll.smoothScrollTo(0, historySection.top)
        }
    }

    private fun abrirEditor(movimentacao: Movimentacao) {
        val view = layoutInflater.inflate(R.layout.dialog_editar_movimentacao, null)
        val typeSpinner = view.findViewById<Spinner>(R.id.editTypeSpinner)
        val descriptionEdit = view.findViewById<EditText>(R.id.editDescriptionInput)
        val valueEdit = view.findViewById<EditText>(R.id.editValueInput)

        val tipos = listOf("Gasto", "Ganho")
        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            tipos
        )
        typeSpinner.setSelection(
            if (movimentacao.tipo == TipoMovimentacao.GASTO) 0 else 1
        )
        descriptionEdit.setText(movimentacao.descricao)
        valueEdit.setText(formatarValorParaEdicao(movimentacao.valorCentavos))

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar movimentação")
            .setView(view)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Excluir", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val novaDescricao = descriptionEdit.text.toString().trim()
                val novoValor = parseValorCentavos(valueEdit.text.toString())
                val novoTipo = if (typeSpinner.selectedItemPosition == 0) {
                    TipoMovimentacao.GASTO
                } else {
                    TipoMovimentacao.GANHO
                }

                if (novaDescricao.isBlank()) {
                    descriptionEdit.error = "Digite o nome da movimentação"
                    descriptionEdit.requestFocus()
                    return@setOnClickListener
                }

                if (novoValor == null || novoValor <= 0) {
                    valueEdit.error = "Digite um valor maior que zero"
                    valueEdit.requestFocus()
                    return@setOnClickListener
                }

                val atualizou = try {
                    database.atualizar(
                        id = movimentacao.id,
                        tipo = novoTipo,
                        descricao = novaDescricao,
                        valorCentavos = novoValor
                    )
                } catch (erro: SQLiteException) {
                    false
                }

                if (!atualizou) {
                    Toast.makeText(
                        this,
                        "Não foi possível atualizar a movimentação.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }

                recarregarInterface()
                dialog.dismiss()
                Toast.makeText(this, "Movimentação atualizada", Toast.LENGTH_SHORT).show()
            }

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                confirmarExclusao(movimentacao, dialog)
            }
        }

        dialog.show()
    }

    private fun confirmarExclusao(
        movimentacao: Movimentacao,
        editorDialog: AlertDialog
    ) {
        AlertDialog.Builder(this)
            .setTitle("Excluir movimentação?")
            .setMessage("Essa ação não pode ser desfeita.")
            .setPositiveButton("Excluir") { _, _ ->
                val excluiu = try {
                    database.excluir(movimentacao.id)
                } catch (erro: SQLiteException) {
                    false
                }

                if (excluiu) {
                    recarregarInterface()
                    editorDialog.dismiss()
                    Toast.makeText(this, "Movimentação excluída", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Não foi possível excluir a movimentação.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun recarregarInterface() {
        carregarMovimentacoes()
        atualizarResumo()
        renderizarMovimentacoes()
    }

    private fun atualizarResumo() {
        val totalGanhos = movimentacoes
            .filter { it.tipo == TipoMovimentacao.GANHO }
            .sumOf { it.valorCentavos }

        val totalGastos = movimentacoes
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .sumOf { it.valorCentavos }

        val saldo = totalGanhos - totalGastos

        balanceText.text = formatarMoeda(saldo)
        incomeTotalText.text = formatarMoeda(totalGanhos)
        expenseTotalText.text = formatarMoeda(totalGastos)

        balanceText.setTextColor(
            getColor(
                when {
                    saldo > 0 -> R.color.income
                    saldo < 0 -> R.color.expense
                    else -> R.color.text_primary
                }
            )
        )
    }

    private fun renderizarMovimentacoes() {
        movementsContainer.removeAllViews()

        emptyStateText.visibility = if (movimentacoes.isEmpty()) View.VISIBLE else View.GONE

        val inflater = LayoutInflater.from(this)
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM • HH:mm", localeBrasil)

        movimentacoes.forEach { movimentacao ->
            val row = inflater.inflate(
                R.layout.item_movimentacao,
                movementsContainer,
                false
            )

            row.findViewById<TextView>(R.id.movementDescription).text =
                movimentacao.descricao

            val tipoTexto = if (movimentacao.tipo == TipoMovimentacao.GASTO) {
                "Gasto"
            } else {
                "Ganho"
            }

            row.findViewById<TextView>(R.id.movementMeta).text =
                "${tipoTexto} • ${movimentacao.data.format(dateFormatter)}"

            val amountText = row.findViewById<TextView>(R.id.movementAmount)
            val sinal = if (movimentacao.tipo == TipoMovimentacao.GASTO) "-" else "+"
            amountText.text = "${sinal} ${formatarMoeda(movimentacao.valorCentavos)}"
            amountText.setTextColor(
                getColor(
                    if (movimentacao.tipo == TipoMovimentacao.GASTO) {
                        R.color.expense
                    } else {
                        R.color.income
                    }
                )
            )

            row.setOnClickListener {
                abrirEditor(movimentacao)
            }

            movementsContainer.addView(row)
        }
    }

    private fun parseValorCentavos(valorDigitado: String): Long? {
        val limpo = valorDigitado
            .replace("R$", "", ignoreCase = true)
            .replace(" ", "")
            .filter { it.isDigit() || it == ',' || it == '.' }

        if (limpo.isBlank()) return null

        val ultimoSeparador = maxOf(limpo.lastIndexOf(','), limpo.lastIndexOf('.'))
        val casasDecimais = if (ultimoSeparador >= 0) {
            limpo.length - ultimoSeparador - 1
        } else {
            0
        }

        val apenasDigitos = limpo.filter { it.isDigit() }
        val numero = apenasDigitos.toLongOrNull() ?: return null

        return when {
            ultimoSeparador >= 0 && casasDecimais == 1 -> numero * 10
            ultimoSeparador >= 0 && casasDecimais == 2 -> numero
            else -> numero * 100
        }
    }

    private fun formatarMoeda(valorCentavos: Long): String {
        return moeda.format(BigDecimal.valueOf(valorCentavos, 2))
    }

    private fun formatarValorParaEdicao(valorCentavos: Long): String {
        return BigDecimal.valueOf(valorCentavos, 2)
            .toPlainString()
            .replace('.', ',')
    }

    private fun formatarMesAtual(): String {
        val formato = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", localeBrasil)
        return LocalDate.now()
            .format(formato)
            .replaceFirstChar { it.uppercase(localeBrasil) }
    }
}
