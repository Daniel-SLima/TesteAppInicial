package com.danielslima.testeappinicial

import android.app.Activity
import android.app.AlertDialog
import android.app.KeyguardManager
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
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
    private lateinit var installmentCheckBox: CheckBox
    private lateinit var installmentOptions: LinearLayout
    private lateinit var installmentCountInput: EditText
    private lateinit var installmentDayInput: EditText
    private lateinit var recurringCheckBox: CheckBox
    private lateinit var recurringOptions: LinearLayout
    private lateinit var recurringDayInput: EditText
    private lateinit var carryBalanceCheckBox: CheckBox
    private lateinit var securityCheckBox: CheckBox
    private lateinit var monthText: TextView
    private lateinit var monthStateText: TextView
    private lateinit var initialBalanceText: TextView
    private lateinit var balanceText: TextView
    private lateinit var forecastBalanceText: TextView
    private lateinit var incomeTotalText: TextView
    private lateinit var expenseTotalText: TextView
    private lateinit var pendingIncomeText: TextView
    private lateinit var pendingExpenseText: TextView
    private lateinit var categorySummaryText: TextView
    private lateinit var categoryChartContainer: LinearLayout
    private lateinit var movementSearchInput: EditText
    private lateinit var filterTypeSpinner: Spinner
    private lateinit var filterStatusSpinner: Spinner
    private lateinit var filterCategorySpinner: Spinner
    private lateinit var emptyStateText: TextView
    private lateinit var movementsContainer: LinearLayout
    private lateinit var historySection: LinearLayout

    private var tipoSelecionado = TipoMovimentacao.GASTO
    private var mesSelecionado = YearMonth.now()
    private var mesAtualReferencia = YearMonth.now()
    private var mesExportacaoPendente: YearMonth? = null
    private var autenticadoNestaSessao = false
    private var autenticacaoEmAndamento = false
    private var alterandoProtecao = false

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
        installmentCheckBox = findViewById(R.id.installmentCheckBox)
        installmentOptions = findViewById(R.id.installmentOptions)
        installmentCountInput = findViewById(R.id.installmentCountInput)
        installmentDayInput = findViewById(R.id.installmentDayInput)
        recurringCheckBox = findViewById(R.id.recurringCheckBox)
        recurringOptions = findViewById(R.id.recurringOptions)
        recurringDayInput = findViewById(R.id.recurringDayInput)
        carryBalanceCheckBox = findViewById(R.id.carryBalanceCheckBox)
        securityCheckBox = findViewById(R.id.securityCheckBox)
        monthText = findViewById(R.id.monthText)
        monthStateText = findViewById(R.id.monthStateText)
        initialBalanceText = findViewById(R.id.initialBalanceText)
        balanceText = findViewById(R.id.balanceText)
        forecastBalanceText = findViewById(R.id.forecastBalanceText)
        incomeTotalText = findViewById(R.id.incomeTotalText)
        expenseTotalText = findViewById(R.id.expenseTotalText)
        pendingIncomeText = findViewById(R.id.pendingIncomeText)
        pendingExpenseText = findViewById(R.id.pendingExpenseText)
        categorySummaryText = findViewById(R.id.categorySummaryText)
        categoryChartContainer = findViewById(R.id.categoryChartContainer)
        movementSearchInput = findViewById(R.id.movementSearchInput)
        filterTypeSpinner = findViewById(R.id.filterTypeSpinner)
        filterStatusSpinner = findViewById(R.id.filterStatusSpinner)
        filterCategorySpinner = findViewById(R.id.filterCategorySpinner)
        emptyStateText = findViewById(R.id.emptyStateText)
        movementsContainer = findViewById(R.id.movementsContainer)
        historySection = findViewById(R.id.historySection)

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CATEGORIAS
        )
        categorySpinner.setSelection(CATEGORIAS.indexOf("Outros"))

        configurarFiltros()
        recurringDayInput.setText(LocalDate.now().dayOfMonth.toString())
        installmentCountInput.setText("2")
        installmentDayInput.setText(LocalDate.now().dayOfMonth.toString())

        installmentCheckBox.setOnCheckedChangeListener { _, checked ->
            installmentOptions.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) {
                recurringCheckBox.isChecked = false
            }
        }

        recurringCheckBox.setOnCheckedChangeListener { _, checked ->
            recurringOptions.visibility = if (checked) View.VISIBLE else View.GONE
            if (checked) {
                installmentCheckBox.isChecked = false
            }
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

        securityCheckBox.isChecked = preferencias.getBoolean(
            CHAVE_PROTEGER_APP,
            false
        )
        securityCheckBox.setOnCheckedChangeListener { _, checked ->
            if (alterandoProtecao) {
                return@setOnCheckedChangeListener
            }

            if (checked) {
                val keyguard = getSystemService(KeyguardManager::class.java)

                if (!keyguard.isDeviceSecure) {
                    alterandoProtecao = true
                    securityCheckBox.isChecked = false
                    alterandoProtecao = false

                    Toast.makeText(
                        this,
                        "Configure um PIN, padrão ou senha no Android primeiro.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnCheckedChangeListener
                }

                preferencias.edit()
                    .putBoolean(CHAVE_PROTEGER_APP, true)
                    .apply()

                autenticadoNestaSessao = false
                solicitarAutenticacao()
            } else {
                preferencias.edit()
                    .putBoolean(CHAVE_PROTEGER_APP, false)
                    .apply()

                autenticadoNestaSessao = true
            }
        }

        findViewById<Button>(R.id.manageRecurringButton).setOnClickListener {
            abrirGerenciadorFixos()
        }

        findViewById<Button>(R.id.manageBudgetsButton).setOnClickListener {
            abrirGerenciadorOrcamentos()
        }

        findViewById<Button>(R.id.exportCsvButton).setOnClickListener {
            iniciarExportacaoCsv()
        }

        findViewById<Button>(R.id.backupButton).setOnClickListener {
            iniciarExportacaoBackup()
        }

        findViewById<Button>(R.id.restoreBackupButton).setOnClickListener {
            iniciarImportacaoBackup()
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

        if (
            ::securityCheckBox.isInitialized &&
            securityCheckBox.isChecked &&
            !autenticadoNestaSessao &&
            !autenticacaoEmAndamento
        ) {
            solicitarAutenticacao()
        }
    }

    override fun onStop() {
        if (
            ::securityCheckBox.isInitialized &&
            securityCheckBox.isChecked &&
            !autenticacaoEmAndamento
        ) {
            autenticadoNestaSessao = false
        }

        super.onStop()
    }

    override fun onDestroy() {
        database.close()
        super.onDestroy()
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_AUTH_APP) {
            autenticacaoEmAndamento = false

            if (resultCode == RESULT_OK) {
                autenticadoNestaSessao = true
            } else {
                finish()
            }
            return
        }

        if (resultCode != RESULT_OK) return

        val uri = data?.data ?: return

        when (requestCode) {
            REQUEST_EXPORT_CSV -> exportarCsv(uri)
            REQUEST_EXPORT_BACKUP -> exportarBackup(uri)
            REQUEST_IMPORT_BACKUP -> prepararRestauracaoBackup(uri)
        }
    }

    private fun carregarMovimentacoes() {
        try {
            database.garantirRecorrenciasParaMes(mesSelecionado)

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

    private fun configurarFiltros() {
        filterTypeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Todos", "Gastos", "Ganhos")
        )
        filterStatusSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Todos status", "Realizados", "Pendentes")
        )
        filterCategorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Todas categorias") + CATEGORIAS
        )

        val listener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (::movementsContainer.isInitialized) {
                    renderizarMovimentacoes()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        filterTypeSpinner.onItemSelectedListener = listener
        filterStatusSpinner.onItemSelectedListener = listener
        filterCategorySpinner.onItemSelectedListener = listener

        movementSearchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                renderizarMovimentacoes()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })
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

        installmentCheckBox.visibility =
            if (gastoSelecionado) View.VISIBLE else View.GONE

        if (!gastoSelecionado) {
            installmentCheckBox.isChecked = false
            installmentOptions.visibility = View.GONE
        }
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

        if (
            tipoSelecionado == TipoMovimentacao.GASTO &&
            installmentCheckBox.isChecked
        ) {
            registrarParcelamento(descricao, valorCentavos, categoria)
        } else if (recurringCheckBox.isChecked) {
            registrarRecorrencia(descricao, valorCentavos, categoria)
        } else {
            registrarMovimentacaoUnica(descricao, valorCentavos, categoria)
        }
    }

    private fun registrarParcelamento(
        descricao: String,
        valorTotalCentavos: Long,
        categoria: String
    ) {
        val quantidade = installmentCountInput.text.toString().toIntOrNull()
        val dia = installmentDayInput.text.toString().toIntOrNull()

        if (quantidade == null || quantidade !in 2..60) {
            installmentCountInput.error = "Use entre 2 e 60 parcelas"
            installmentCountInput.requestFocus()
            return
        }

        if (dia == null || dia !in 1..31) {
            installmentDayInput.error = "Use um dia entre 1 e 31"
            installmentDayInput.requestFocus()
            return
        }

        val parcelas = try {
            database.inserirParcelamento(
                descricao = descricao,
                valorTotalCentavos = valorTotalCentavos,
                categoria = categoria,
                quantidadeParcelas = quantidade,
                diaVencimento = dia
            )
        } catch (erro: Exception) {
            Toast.makeText(
                this,
                "Não foi possível criar o parcelamento.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val primeiraParcela = parcelas.firstOrNull()
        if (primeiraParcela != null) {
            mesSelecionado = YearMonth.from(primeiraParcela.data)
        }

        limparFormulario()
        recarregarInterface()

        Toast.makeText(
            this,
            "Parcelamento criado em $quantidade parcelas",
            Toast.LENGTH_SHORT
        ).show()
        rolarParaHistorico()
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
        installmentCheckBox.isChecked = false
        installmentCountInput.setText("2")
        installmentDayInput.setText(LocalDate.now().dayOfMonth.toString())
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
        val view = layoutInflater.inflate(R.layout.dialog_editar_recorrencia, null)
        val typeSpinner = view.findViewById<Spinner>(R.id.recurringTypeSpinner)
        val categorySpinner = view.findViewById<Spinner>(R.id.recurringCategorySpinner)
        val descriptionEdit = view.findViewById<EditText>(R.id.recurringDescriptionInput)
        val valueEdit = view.findViewById<EditText>(R.id.recurringValueInput)
        val dayEdit = view.findViewById<EditText>(R.id.recurringDayEditInput)

        typeSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Gasto", "Ganho")
        )
        typeSpinner.setSelection(
            if (recorrencia.tipo == TipoMovimentacao.GASTO) 0 else 1
        )

        categorySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            CATEGORIAS
        )
        val categoriaIndex = CATEGORIAS.indexOf(recorrencia.categoria)
        categorySpinner.setSelection(
            if (categoriaIndex >= 0) categoriaIndex else CATEGORIAS.indexOf("Outros")
        )

        descriptionEdit.setText(recorrencia.descricao)
        valueEdit.setText(formatarValorParaEdicao(recorrencia.valorCentavos))
        dayEdit.setText(recorrencia.diaMes.toString())

        val dialog = AlertDialog.Builder(this)
            .setTitle("Editar fixo mensal")
            .setView(view)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)
            .setNeutralButton("Desativar", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val descricao = descriptionEdit.text.toString().trim()
                val valor = parseValorCentavos(valueEdit.text.toString())
                val dia = dayEdit.text.toString().toIntOrNull()
                val tipo = if (typeSpinner.selectedItemPosition == 0) {
                    TipoMovimentacao.GASTO
                } else {
                    TipoMovimentacao.GANHO
                }
                val categoria = categorySpinner.selectedItem?.toString() ?: "Outros"

                if (descricao.isBlank()) {
                    descriptionEdit.error = "Digite o nome do fixo"
                    descriptionEdit.requestFocus()
                    return@setOnClickListener
                }

                if (valor == null || valor <= 0) {
                    valueEdit.error = "Digite um valor maior que zero"
                    valueEdit.requestFocus()
                    return@setOnClickListener
                }

                if (dia == null || dia !in 1..31) {
                    dayEdit.error = "Use um dia entre 1 e 31"
                    dayEdit.requestFocus()
                    return@setOnClickListener
                }

                val atualizou = try {
                    database.atualizarRecorrencia(
                        id = recorrencia.id,
                        tipo = tipo,
                        descricao = descricao,
                        valorCentavos = valor,
                        diaMes = dia,
                        categoria = categoria
                    )
                } catch (erro: SQLiteException) {
                    false
                }

                if (atualizou) {
                    recarregarInterface()
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Fixo atualizado para as próximas projeções",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this,
                        "Não foi possível atualizar o fixo.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Desativar lançamento fixo?")
                    .setMessage(
                        "O histórico realizado será mantido e as projeções pendentes futuras serão removidas."
                    )
                    .setPositiveButton("Desativar") { _, _ ->
                        desativarRecorrencia(recorrencia.id)
                        dialog.dismiss()
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }

        dialog.show()
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
                "Fixo desativado. Histórico mantido e projeções futuras removidas.",
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
        atualizarResumoCategorias()
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

    private fun atualizarResumoCategorias() {
        val gastosPorCategoria = movimentacoes
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .groupBy { it.categoria }
            .mapValues { (_, itens) -> itens.sumOf { it.valorCentavos } }

        val orcamentos = try {
            database.listarOrcamentos()
        } catch (erro: SQLiteException) {
            emptyMap()
        }

        val categoriasExibidas = (
            gastosPorCategoria.keys + orcamentos.keys
        )
            .distinct()
            .sortedByDescending { gastosPorCategoria[it] ?: 0L }

        categoryChartContainer.removeAllViews()

        if (categoriasExibidas.isEmpty()) {
            categorySummaryText.text = "Sem gastos previstos neste mês."
            return
        }

        val totalGastos = gastosPorCategoria.values.sum()
        categorySummaryText.text =
            "Total de gastos do mês: ${formatarMoeda(totalGastos)}"

        val maiorValorSemOrcamento = gastosPorCategoria.values
            .maxOrNull()
            ?.coerceAtLeast(1L)
            ?: 1L

        categoriasExibidas.forEach { categoria ->
            val total = gastosPorCategoria[categoria] ?: 0L
            val limite = orcamentos[categoria]

            val bloco = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(0, 0, 0, dp(14))
            }

            val cabecalho = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val nome = TextView(this).apply {
                text = categoria
                textSize = 14f
                setTextColor(getColor(R.color.text_primary))
            }
            cabecalho.addView(
                nome,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val valor = TextView(this).apply {
                text = if (limite != null) {
                    "${formatarMoeda(total)} / ${formatarMoeda(limite)}"
                } else {
                    formatarMoeda(total)
                }
                textSize = 14f
                setTextColor(
                    getColor(
                        if (limite != null && total > limite) {
                            R.color.expense
                        } else {
                            R.color.text_primary
                        }
                    )
                )
            }
            cabecalho.addView(valor)

            val barra = ProgressBar(
                this,
                null,
                android.R.attr.progressBarStyleHorizontal
            ).apply {
                max = 1000
                progress = if (limite != null) {
                    ((total * 1000L) / limite.coerceAtLeast(1L))
                        .toInt()
                        .coerceIn(0, 1000)
                } else {
                    ((total * 1000L) / maiorValorSemOrcamento)
                        .toInt()
                        .coerceIn(0, 1000)
                }

                progressTintList = ColorStateList.valueOf(
                    getColor(
                        if (limite != null && total > limite) {
                            R.color.expense
                        } else {
                            R.color.accent
                        }
                    )
                )
                progressBackgroundTintList =
                    ColorStateList.valueOf(getColor(R.color.unselected))
            }

            bloco.addView(cabecalho)
            bloco.addView(
                barra,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(8)
                ).apply {
                    topMargin = dp(5)
                }
            )

            if (limite != null) {
                val diferenca = limite - total
                val detalhe = TextView(this).apply {
                    text = if (diferenca >= 0) {
                        "Restam ${formatarMoeda(diferenca)} neste mês"
                    } else {
                        "Orçamento excedido em ${formatarMoeda(-diferenca)}"
                    }
                    textSize = 12f
                    setTextColor(
                        getColor(
                            if (diferenca >= 0) {
                                R.color.text_secondary
                            } else {
                                R.color.expense
                            }
                        )
                    )
                }

                bloco.addView(
                    detalhe,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        topMargin = dp(4)
                    }
                )
            }

            categoryChartContainer.addView(bloco)
        }
    }

    private fun abrirGerenciadorOrcamentos() {
        val orcamentos = try {
            database.listarOrcamentos()
        } catch (erro: SQLiteException) {
            Toast.makeText(
                this,
                "Não foi possível carregar os orçamentos.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val itens = CATEGORIAS.map { categoria ->
            val limite = orcamentos[categoria]
            if (limite == null) {
                "$categoria • Sem limite"
            } else {
                "$categoria • ${formatarMoeda(limite)}/mês"
            }
        }.toTypedArray()

        AlertDialog.Builder(this)
            .setTitle("Orçamentos mensais")
            .setMessage("Escolha uma categoria para definir ou alterar o limite.")
            .setItems(itens) { _, position ->
                val categoria = CATEGORIAS[position]
                abrirEditorOrcamento(
                    categoria,
                    orcamentos[categoria]
                )
            }
            .setNegativeButton("Fechar", null)
            .show()
    }

    private fun abrirEditorOrcamento(
        categoria: String,
        limiteAtual: Long?
    ) {
        val input = EditText(this).apply {
            hint = "R$ 0,00"
            inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or
                    android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            setPadding(dp(14), 0, dp(14), 0)
            setBackgroundResource(R.drawable.bg_input)

            if (limiteAtual != null) {
                setText(formatarValorParaEdicao(limiteAtual))
            }
        }

        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(24), dp(8), dp(24), 0)

            addView(
                input,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dp(52)
                )
            )
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Orçamento: $categoria")
            .setMessage("Defina o limite mensal desta categoria.")
            .setView(container)
            .setPositiveButton("Salvar", null)
            .setNegativeButton("Cancelar", null)

        if (limiteAtual != null) {
            builder.setNeutralButton("Remover", null)
        }

        val dialog = builder.create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val limite = parseValorCentavos(input.text.toString())

                if (limite == null || limite <= 0) {
                    input.error = "Digite um valor maior que zero"
                    input.requestFocus()
                    return@setOnClickListener
                }

                try {
                    database.salvarOrcamento(categoria, limite)
                    atualizarResumoCategorias()
                    dialog.dismiss()
                    Toast.makeText(
                        this,
                        "Orçamento de $categoria atualizado",
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (erro: Exception) {
                    Toast.makeText(
                        this,
                        "Não foi possível salvar o orçamento.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            if (limiteAtual != null) {
                dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
                    try {
                        database.removerOrcamento(categoria)
                        atualizarResumoCategorias()
                        dialog.dismiss()
                        Toast.makeText(
                            this,
                            "Orçamento de $categoria removido",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (erro: SQLiteException) {
                        Toast.makeText(
                            this,
                            "Não foi possível remover o orçamento.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }

        dialog.show()
    }

    @Suppress("DEPRECATION")
    private fun solicitarAutenticacao() {
        if (autenticacaoEmAndamento) return

        val keyguard = getSystemService(KeyguardManager::class.java)

        if (!keyguard.isDeviceSecure) {
            preferencias.edit()
                .putBoolean(CHAVE_PROTEGER_APP, false)
                .apply()

            alterandoProtecao = true
            securityCheckBox.isChecked = false
            alterandoProtecao = false
            autenticadoNestaSessao = true

            Toast.makeText(
                this,
                "Proteção desativada: o aparelho não possui bloqueio seguro.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val intent = keyguard.createConfirmDeviceCredentialIntent(
            "Desbloquear FinTest",
            "Confirme o bloqueio do aparelho para acessar suas finanças."
        )

        if (intent == null) {
            Toast.makeText(
                this,
                "Não foi possível abrir a autenticação do Android.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        autenticacaoEmAndamento = true
        startActivityForResult(intent, REQUEST_AUTH_APP)
    }

    private fun iniciarExportacaoBackup() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(
                Intent.EXTRA_TITLE,
                "FinTest-backup-${LocalDate.now()}.json"
            )
        }

        startActivityForResult(intent, REQUEST_EXPORT_BACKUP)
    }

    private fun exportarBackup(uri: Uri) {
        try {
            val conteudo = database.criarBackupJson(carryBalanceCheckBox.isChecked)
            val output = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Não foi possível abrir o arquivo.")

            output.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(conteudo)
            }

            Toast.makeText(
                this,
                "Backup do FinTest criado com sucesso",
                Toast.LENGTH_LONG
            ).show()
        } catch (erro: Exception) {
            Toast.makeText(
                this,
                "Não foi possível criar o backup.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun iniciarImportacaoBackup() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }

        startActivityForResult(intent, REQUEST_IMPORT_BACKUP)
    }

    private fun prepararRestauracaoBackup(uri: Uri) {
        val conteudo = try {
            contentResolver.openInputStream(uri)
                ?.bufferedReader(Charsets.UTF_8)
                ?.use { it.readText() }
                ?: throw IllegalStateException("Não foi possível abrir o backup.")
        } catch (erro: Exception) {
            Toast.makeText(
                this,
                "Não foi possível ler o arquivo de backup.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Restaurar backup?")
            .setMessage(
                "Os dados atuais do FinTest serão substituídos pelos dados do backup. " +
                    "Se o arquivo for inválido, nada será alterado."
            )
            .setPositiveButton("Restaurar") { _, _ ->
                restaurarBackup(conteudo)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun restaurarBackup(conteudo: String) {
        try {
            val carregarSaldo = database.restaurarBackupJson(conteudo)

            preferencias.edit()
                .putBoolean(CHAVE_CARREGAR_SALDO, carregarSaldo)
                .apply()

            carryBalanceCheckBox.isChecked = carregarSaldo
            mesSelecionado = YearMonth.now()
            recarregarInterface()

            Toast.makeText(
                this,
                "Backup restaurado com sucesso",
                Toast.LENGTH_LONG
            ).show()
        } catch (erro: Exception) {
            Toast.makeText(
                this,
                "Backup inválido ou incompatível. Nenhum dado foi alterado.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun iniciarExportacaoCsv() {
        mesExportacaoPendente = mesSelecionado

        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(
                Intent.EXTRA_TITLE,
                "FinTest-${mesSelecionado}.csv"
            )
        }

        startActivityForResult(intent, REQUEST_EXPORT_CSV)
    }

    private fun exportarCsv(uri: Uri) {
        val mes = mesExportacaoPendente ?: mesSelecionado

        try {
            val output = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Não foi possível abrir o arquivo.")

            output.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("\uFEFF")
                writer.write(
                    "Data;Tipo;Status;Categoria;Descrição;Valor;Fixo;Parcela\n"
                )

                movimentacoes
                    .sortedBy { it.data }
                    .forEach { movimentacao ->
                        val tipo = if (
                            movimentacao.tipo == TipoMovimentacao.GASTO
                        ) {
                            "Gasto"
                        } else {
                            "Ganho"
                        }

                        val status = when {
                            movimentacao.tipo == TipoMovimentacao.GASTO &&
                                movimentacao.status == StatusMovimentacao.REALIZADO ->
                                "Pago"
                            movimentacao.tipo == TipoMovimentacao.GASTO ->
                                "Pendente"
                            movimentacao.status == StatusMovimentacao.REALIZADO ->
                                "Recebido"
                            else ->
                                "A receber"
                        }

                        val dataTexto = movimentacao.data.format(
                            DateTimeFormatter.ofPattern(
                                "dd/MM/yyyy HH:mm",
                                localeBrasil
                            )
                        )
                        val valorTexto = BigDecimal.valueOf(
                            movimentacao.valorCentavos,
                            2
                        )
                            .toPlainString()
                            .replace('.', ',')

                        val linha = listOf(
                            dataTexto,
                            tipo,
                            status,
                            movimentacao.categoria,
                            movimentacao.descricao,
                            valorTexto,
                            if (movimentacao.recorrenciaId != null) "Sim" else "Não",
                            if (
                                movimentacao.parcelaNumero != null &&
                                movimentacao.parcelasTotal != null
                            ) {
                                "${movimentacao.parcelaNumero}/${movimentacao.parcelasTotal}"
                            } else {
                                ""
                            }
                        ).joinToString(";") { csvCampo(it) }

                        writer.write(linha)
                        writer.write("\n")
                    }
            }

            Toast.makeText(
                this,
                "CSV de ${formatarMes(mes)} exportado",
                Toast.LENGTH_LONG
            ).show()
        } catch (erro: Exception) {
            Toast.makeText(
                this,
                "Não foi possível exportar o CSV.",
                Toast.LENGTH_LONG
            ).show()
        } finally {
            mesExportacaoPendente = null
        }
    }

    private fun csvCampo(valor: String): String {
        val escapado = valor.replace("\"", "\"\"")
        return if (
            escapado.contains(';') ||
            escapado.contains('"') ||
            escapado.contains('\n')
        ) {
            "\"$escapado\""
        } else {
            escapado
        }
    }

    private fun dp(valor: Int): Int {
        return (valor * resources.displayMetrics.density).toInt()
    }

    private fun movimentacoesFiltradas(): List<Movimentacao> {
        val busca = movementSearchInput.text
            ?.toString()
            ?.trim()
            ?.lowercase(localeBrasil)
            .orEmpty()

        val tipoFiltro = filterTypeSpinner.selectedItemPosition
        val statusFiltro = filterStatusSpinner.selectedItemPosition
        val categoriaFiltro =
            filterCategorySpinner.selectedItem?.toString() ?: "Todas categorias"

        return movimentacoes.filter { movimentacao ->
            val correspondeBusca = busca.isBlank() ||
                movimentacao.descricao.lowercase(localeBrasil).contains(busca) ||
                movimentacao.categoria.lowercase(localeBrasil).contains(busca)

            val correspondeTipo = when (tipoFiltro) {
                1 -> movimentacao.tipo == TipoMovimentacao.GASTO
                2 -> movimentacao.tipo == TipoMovimentacao.GANHO
                else -> true
            }

            val correspondeStatus = when (statusFiltro) {
                1 -> movimentacao.status == StatusMovimentacao.REALIZADO
                2 -> movimentacao.status == StatusMovimentacao.PENDENTE
                else -> true
            }

            val correspondeCategoria =
                categoriaFiltro == "Todas categorias" ||
                    movimentacao.categoria == categoriaFiltro

            correspondeBusca &&
                correspondeTipo &&
                correspondeStatus &&
                correspondeCategoria
        }
    }

    private fun renderizarMovimentacoes() {
        movementsContainer.removeAllViews()

        val filtradas = movimentacoesFiltradas()
        emptyStateText.visibility = if (filtradas.isEmpty()) View.VISIBLE else View.GONE
        emptyStateText.text = if (movimentacoes.isEmpty()) {
            getString(R.string.empty_state_month)
        } else {
            getString(R.string.empty_state_filtered)
        }

        val inflater = LayoutInflater.from(this)
        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM • HH:mm", localeBrasil)

        filtradas.forEach { movimentacao ->
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
            val parcelaTexto = if (
                movimentacao.parcelaNumero != null &&
                movimentacao.parcelasTotal != null
            ) {
                " • Parcela ${movimentacao.parcelaNumero}/${movimentacao.parcelasTotal}"
            } else {
                ""
            }
            val statusTexto = when {
                movimentacao.tipo == TipoMovimentacao.GASTO &&
                    movimentacao.status == StatusMovimentacao.REALIZADO -> "Pago"
                movimentacao.tipo == TipoMovimentacao.GASTO -> "Pendente"
                movimentacao.status == StatusMovimentacao.REALIZADO -> "Recebido"
                else -> "A receber"
            }

            row.findViewById<TextView>(R.id.movementMeta).text =
                "${movimentacao.categoria} • ${tipoTexto}${fixoTexto}${parcelaTexto} • $statusTexto • ${movimentacao.data.format(dateFormatter)}"

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
        private const val CHAVE_PROTEGER_APP = "proteger_app_bloqueio_android"
        private const val REQUEST_EXPORT_CSV = 1201
        private const val REQUEST_EXPORT_BACKUP = 1202
        private const val REQUEST_IMPORT_BACKUP = 1203
        private const val REQUEST_AUTH_APP = 1204

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
