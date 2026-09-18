package com.danielslima.testeappinicial

import android.app.Activity
import android.app.AlertDialog
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.database.sqlite.SQLiteException
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : Activity() {

    private val localeBrasil = Locale.forLanguageTag("pt-BR")
    private val moeda = NumberFormat.getCurrencyInstance(localeBrasil)
    private val movimentacoes = mutableListOf<Movimentacao>()

    private lateinit var database: MovimentacaoDatabase
    private lateinit var preferencias: SharedPreferences
    private lateinit var mainScroll: ScrollView
    private lateinit var expenseButton: Button
    private lateinit var incomeButton: Button
    private lateinit var descriptionInput: EditText
    private lateinit var valueInput: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var recurringOptions: LinearLayout
    private lateinit var recurringDayInput: EditText
    private lateinit var carryBalanceCheckBox: CheckBox
    private lateinit var monthText: TextView
    private lateinit var monthStateText: TextView
    private lateinit var initialBalanceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var forecastBalanceText: TextView
    private lateinit var incomeTotalText: TextView
    private lateinit var expenseTotalText: TextView
    private lateinit var pendingIncomeText: TextView
    private lateinit var pendingExpenseText: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var movementsContainer: LinearLayout
    private lateinit var historySection: LinearLayout

    private var tipoSelecionado = TipoMovimentacao.GASTO
    private var mesSelecionado = YearMonth.now()
    private var mesAtualReferencia = YearMonth.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = MovimentacaoDatabase(applicationContext)
        preferencias = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE)

        mainScroll = findViewById(R.id.mainScroll)
        expenseButton = findViewById(R.id.expenseButton)
        incomeButton = findViewById(R.id.incomeButton)
        descriptionInput = findViewById(R.id.descriptionInput)
        valueInput = findViewById(R.id.valueInput)
        categorySpinner = findViewById(R.id.categorySpinner)
        recurringCheckBox = findViewById(R.id.recurringCheckBox)
        recurringOptions = findViewById(R.id.recurringOptions)
        recurringDayInput = findViewById(R.id.recurringDayInput)
        carryBalanceCheckBox = findViewById(R.id.carryBalanceCheckBox)
        monthText = findViewById(R.id.monthText)
        monthStateText = findViewById(R.id.monthStateText)
        initialBalanceText = findViewById(R.id.initialBalanceText)
        balanceText = findViewById(R.id.balanceText)
        forecastBalanceText = findViewById(R.id.forecastBalanceText)
        incomeTotalText = findViewById(R.id.incomeTotalText)
        expenseTotalText = findViewById(R.id.expenseTotalText)
        pendingIncomeText = findViewById(R.id.pendingIncomeText)
        pendingExpenseText = findViewById(R.id.pendingExpenseText)
        emptyStateText = findViewById(R.id.emptyStateText)
        movementsContainer = findViewById(R.id.movementsContainer)
        historySection = findViewById(R.id.historySection)

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CATEGORIAS
        )
        categorySpinner.setSelection(CATEGORIAS.indexOf("Outros"))

        recurringDayInput.setText(LocalDate.now().dayOfMonth.toString())

        recurringCheckBox.setOnCheckedChangeListener { _, checked ->
            recurringOptions.visibility = if (checked) View.VISIBLE else View.GONE
        }

        carryBalanceCheckBox.isChecked = preferencias.getBoolean(
            CHAVE_CARREGAR_SALDO,
            true
        )
        carryBalanceCheckBox.setOnCheckedChangeListener { _, checked ->
            preferencias.edit()
                .putBoolean(CHAVE_CARREGAR_SALDO, checked)
                .apply()
            atualizarResumo()
        }

        findViewById<Button>(R.id.manageRecurringButton).setOnClickListener {
            abrirGerenciadorFixos()
        }

        findViewById<Button>(R.id.previousMonthButton).setOnClickListener {
            mesSelecionado = mesSelecionado.minusMonths(1)
            recarregarInterface()
        }

        findViewById<Button>(R.id.nextMonthButton).setOnClickListener {
            mesSelecionado = mesSelecionado.plusMonths(1)
            recarregarInterface()
        }

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
        recarregarInterface()
    }

    override fun onResume() {
        super.onResume()

        val mesAgora = YearMonth.now()
        if (mesAgora != mesAtualReferencia) {
            val acompanhavaMesAtual = mesSelecionado == mesAtualReferencia
            mesAtualReferencia = mesAgora

            if (acompanhavaMesAtual && ::database.isInitialized) {
                mesSelecionado = mesAgora
                recarregarInterface()
            }
        }
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    private fun carregarMovimentacoes() {
        try {
            if (!mesSelecionado.isAfter(YearMonth.now())) {
                database.garantirRecorrenciasParaMes(mesSelecionado)
            }

            movimentacoes.clear()
            movimentacoes.addAll(database.listarPorMes(mesSelecionado))
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
        val categoria = categorySpinner.selectedItem?.toString() ?: "Outros"

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

        if (recurringCheckBox.isChecked) {
            registrarRecorrencia(descricao, valorCentavos, categoria)
        } else {
            registrarMovimentacaoUnica(descricao, valorCentavos, categoria)
        }
    }

    private fun registrarMovimentacaoUnica(
        descricao: String,
        valorCentavos: Long,
        categoria: String
    ) {
        val novaMovimentacao = try {
            database.inserir(
                tipo = tipoSelecionado,
                descricao = descricao,
                valorCentavos = valorCentavos,
                categoria = categoria
            )
        } catch (erro: SQLiteException) {
            Toast.makeText(
                this,
                "Não foi possível salvar a movimentação.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        mesSelecionado = YearMonth.from(novaMovimentacao.data)
        limparFormulario()
        recarregarInterface()

        Toast.makeText(this, "Movimentação salva no aparelho", Toast.LENGTH_SHORT).show()
        rolarParaHistorico()
    }

    private fun registrarRecorrencia(
        descricao: String,
        valorCentavos: Long,
        categoria: String
    ) {
        val diaMes = recurringDayInput.text.toString().toIntOrNull()

        if (diaMes == null || diaMes !in 1..31) {
            recurringDayInput.error = "Use um dia entre 1 e 31"
            recurringDayInput.requestFocus()
            return
        }

        try {
            database.inserirRecorrencia(
                tipo = tipoSelecionado,
                descricao = descricao,
                valorCentavos = valorCentavos,
                diaMes = diaMes,
                categoria = categoria,
                inicioMes = YearMonth.now()
            )
            database.garantirRecorrenciasParaMes(YearMonth.now())
        } catch (erro: SQLiteException) {
            Toast.makeText(
                this,
                "Não foi possível criar o lançamento fixo.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        mesSelecionado = YearMonth.now()
        limparFormulario()
        recarregarInterface()

        Toast.makeText(this, "Fixo mensal criado como pendente", Toast.LENGTH_SHORT).show()
        rolarParaHistorico()
    }

    private fun limparFormulario() {
        descriptionInput.text.clear()
        valueInput.text.clear()
        categorySpinner.setSelection(CATEGORIAS.indexOf("Outros"))
        recurringCheckBox.isChecked = false
        recurringDayInput.setText(LocalDate.now().dayOfMonth.toString())
        descriptionInput.requestFocus()
    }

    private fun rolarParaHistorico() {
        historySection.post {
            mainScroll.smoothScrollTo(0, historySection.top)
        }
    }

    private fun abrirGerenciadorFixos() {
        val recorrencias = try {
            database.listarRecorrenciasAtivas()
        } catch (erro: SQLiteException) {
            Toast.makeText(this, "Não foi possível carregar os fixos.", Toast.LENGTH_LONG).show()
            return
        }

        if (recorrencias.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Fixos mensais")
                .setMessage("Você ainda não cadastrou nenhum gasto ou ganho fixo.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val itens = recorrencias.map { recorrencia ->
            val tipo = if (recorrencia.tipo == TipoMovimentacao.GASTO) "Gasto" else "Ganho"
            "${recorrencia.descricao} • ${recorrencia.categoria} • $tipo • ${formatarMoeda(recorrencia.valorCentavos)} • dia ${recorrencia.diaMes}"
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Fixos mensais")
            .setItems(itens) { _, position ->
                abrirDetalheRecorrencia(recorrencias[position])
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun abrirDetalheRecorrencia(recorrencia: Recorrencia) {
        val tipo = if (recorrencia.tipo == TipoMovimentacao.GASTO) "Gasto" else "Ganho"

        AlertDialog.Builder(this)
            .setTitle(recorrencia.descricao)
            .setMessage(
                "$tipo mensal\n${formatarMoeda(recorrencia.valorCentavos)}\nDia ${recorrencia.diaMes} de cada mês"
            )
            .setPositiveButton("Desativar") { _, _ ->
                desativarRecorrencia(recorrencia.id)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun desativarRecorrencia(recorrenciaId: Long) {
        val desativou = try {
            database.desativarRecorrencia(recorrenciaId)
        } catch (erro: SQLiteException) {
            false
        }

        if (desativou) {
            recarregarInterface()
            Toast.makeText(
                this,
                "Fixo desativado. O histórico foi mantido.",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                this,
                "Não foi possível desativar o fixo.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun abrirEditor(movimentacao: Movimentacao) {
        val view = layoutInflater.inflate(R.layout.dialog_editar_movimentacao, null)
        val typeSpinner = view.findViewById<Spinner>(R.id.editTypeSpinner)
        val statusSpinner = view.findViewById<Spinner>(R.id.editStatusSpinner)
        val categoryEditSpinner = view.findViewById<Spinner>(R.id.editCategorySpinner)
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

        configurarStatusSpinner(statusSpinner, movimentacao.tipo, movimentacao.status)

        categoryEditSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CATEGORIAS
        )
        val categoriaAtualIndex = CATEGORIAS.indexOf(movimentacao.categoria)
        categoryEditSpinner.setSelection(if (categoriaAtualIndex >= 0) categoriaAtualIndex else CATEGORIAS.indexOf("Outros"))

        typeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val statusAtual = if (statusSpinner.selectedItemPosition == 1) {
                    StatusMovimentacao.PENDENTE
                } else {
                    StatusMovimentacao.REALIZADO
                }
                val tipoAtual = if (position == 0) {
                    TipoMovimentacao.GASTO
                } else {
                    TipoMovimentacao.GANHO
                }
                configurarStatusSpinner(statusSpinner, tipoAtual, statusAtual)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        descriptionEdit.setText(movimentacao.descricao)
        valueEdit.setText(formatarValorParaEdicao(movimentacao.valorCentavos))

        val neutralText = if (movimentacao.recorrenciaId == null) {
            "Excluir"
        } else {
            "Desativar fixo"
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar movimentação")
            .setView(view)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .setNeutralButton(neutralText, null)
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
                val novoStatus = if (statusSpinner.selectedItemPosition == 0) {
                    StatusMovimentacao.REALIZADO
                } else {
                    StatusMovimentacao.PENDENTE
                }
                val novaCategoria = categoryEditSpinner.selectedItem?.toString() ?: "Outros"

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
                        valorCentavos = novoValor,
                        status = novoStatus,
                        categoria = novaCategoria
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
                if (movimentacao.recorrenciaId == null) {
                    confirmarExclusao(movimentacao, dialog)
                } else {
                    confirmarDesativacaoFixo(movimentacao.recorrenciaId, dialog)
                }
            }
        }

        dialog.show()
    }

    private fun configurarStatusSpinner(
        spinner: Spinner,
        tipo: TipoMovimentacao,
        status: StatusMovimentacao
    ) {
        val opcoes = if (tipo == TipoMovimentacao.GASTO) {
            listOf("Pago", "Pendente")
        } else {
            listOf("Recebido", "A receber")
        }

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            opcoes
        )
        spinner.setSelection(if (status == StatusMovimentacao.REALIZADO) 0 else 1)
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

    private fun confirmarDesativacaoFixo(
        recorrenciaId: Long,
        editorDialog: AlertDialog
    ) {
        AlertDialog.Builder(this)
            .setTitle("Desativar lançamento fixo?")
            .setMessage("Os meses já registrados serão mantidos. Novos meses não serão criados.")
            .setPositiveButton("Desativar") { _, _ ->
                val desativou = try {
                    database.desativarRecorrencia(recorrenciaId)
                } catch (erro: SQLiteException) {
                    false
                }

                if (desativou) {
                    recarregarInterface()
                    editorDialog.dismiss()
                    Toast.makeText(this, "Fixo desativado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        this,
                        "Não foi possível desativar o fixo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun recarregarInterface() {
        monthText.text = formatarMes(mesSelecionado)
        monthStateText.text = estadoDoMes(mesSelecionado)
        carregarMovimentacoes()
        atualizarResumo()
        renderizarMovimentacoes()
    }

    private fun atualizarResumo() {
        val carregarSaldo = if (::carryBalanceCheckBox.isInitialized) {
            carryBalanceCheckBox.isChecked
        } else {
            true
        }

        val saldoInicial = if (carregarSaldo) {
            try {
                database.saldoRealizadoAntesDoMes(mesSelecionado)
            } catch (erro: SQLiteException) {
                0L
            }
        } else {
            0L
        }

        val realizados = movimentacoes.filter { it.status == StatusMovimentacao.REALIZADO }
        val pendentes = movimentacoes.filter { it.status == StatusMovimentacao.PENDENTE }

        val ganhosRealizados = realizados
            .filter { it.tipo == TipoMovimentacao.GANHO }
            .sumOf { it.valorCentavos }

        val gastosRealizados = realizados
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .sumOf { it.valorCentavos }

        val ganhosPendentes = pendentes
            .filter { it.tipo == TipoMovimentacao.GANHO }
            .sumOf { it.valorCentavos }

        val gastosPendentes = pendentes
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .sumOf { it.valorCentavos }

        val saldoRealizado = saldoInicial + ganhosRealizados - gastosRealizados
        val saldoPrevisto = saldoRealizado + ganhosPendentes - gastosPendentes

        initialBalanceText.text = formatarMoeda(saldoInicial)
        balanceText.text = formatarMoeda(saldoRealizado)
        forecastBalanceText.text = formatarMoeda(saldoPrevisto)
        incomeTotalText.text = formatarMoeda(ganhosRealizados)
        expenseTotalText.text = formatarMoeda(gastosRealizados)
        pendingIncomeText.text = formatarMoeda(ganhosPendentes)
        pendingExpenseText.text = formatarMoeda(gastosPendentes)

        balanceText.setTextColor(
            getColor(
                when {
                    saldoRealizado > 0 -> R.color.income
                    saldoRealizado < 0 -> R.color.expense
                    else -> R.color.text_primary
                }
            )
        )

        forecastBalanceText.setTextColor(
            getColor(
                when {
                    saldoPrevisto > 0 -> R.color.income
                    saldoPrevisto < 0 -> R.color.expense
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
            val fixoTexto = if (movimentacao.recorrenciaId != null) " • Fixo" else ""
            val statusTexto = when {
                movimentacao.tipo == TipoMovimentacao.GASTO &&
                    movimentacao.status == StatusMovimentacao.REALIZADO -> "Pago"
                movimentacao.tipo == TipoMovimentacao.GASTO -> "Pendente"
                movimentacao.status == StatusMovimentacao.REALIZADO -> "Recebido"
                else -> "A receber"
            }

            row.findViewById<TextView>(R.id.movementMeta).text =
                "${movimentacao.categoria} • ${tipoTexto}${fixoTexto} • $statusTexto • ${movimentacao.data.format(dateFormatter)}"

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

            row.alpha = if (movimentacao.status == StatusMovimentacao.PENDENTE) 0.72f else 1f

            row.setOnClickListener {
                abrirEditor(movimentacao)
            }

            movementsContainer.addView(row)
        }
    }

    private fun estadoDoMes(mes: YearMonth): String {
        val atual = YearMonth.now()

        return when {
            mes.isBefore(atual) -> "Mês encerrado automaticamente"
            mes.isAfter(atual) -> "Mês futuro"
            else -> "Mês atual"
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

    private fun formatarMes(mes: YearMonth): String {
        val formato = DateTimeFormatter.ofPattern("MMMM 'de' yyyy", localeBrasil)
        return mes.atDay(1)
            .format(formato)
            .replaceFirstChar { it.uppercase(localeBrasil) }
    }

    companion object {
        private const val PREFERENCIAS = "fintest_preferences"
        private const val CHAVE_CARREGAR_SALDO = "carregar_saldo_entre_meses"
        private val CATEGORIAS = listOf(
            "Alimentação",
            "Transporte",
            "Casa",
            "Lazer",
            "Saúde",
            "Compras",
            "Salário",
            "Outros"
        )
    }
}
