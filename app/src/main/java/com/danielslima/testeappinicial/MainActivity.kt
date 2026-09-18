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
import android.os.SystemClock
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
import java.nio.charset.Charset
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
    private lateinit var filterTypeChip: TextView
    private lateinit var filterStatusChip: TextView
    private lateinit var filterCategoryChip: TextView
    private lateinit var clearFiltersButton: TextView
    private lateinit var movementsResultSummary: TextView
    private lateinit var emptyStateText: TextView
    private lateinit var movementsContainer: LinearLayout
    private lateinit var historySection: LinearLayout
    private lateinit var homeScreen: ScrollView
    private lateinit var planningScreen: ScrollView
    private lateinit var settingsScreen: ScrollView
    private lateinit var navHome: LinearLayout
    private lateinit var navMovements: LinearLayout
    private lateinit var navPlanning: LinearLayout
    private lateinit var navSettings: LinearLayout
    private lateinit var homeUpcomingContainer: LinearLayout
    private lateinit var homeBudgetProgressBar: ProgressBar
    private lateinit var homeBudgetProgressText: TextView
    private lateinit var movementsMonthText: TextView
    private lateinit var planningMonthText: TextView
    private lateinit var planningMonthNetText: TextView
    private lateinit var planningIncomeExpectedText: TextView
    private lateinit var planningExpenseExpectedText: TextView
    private lateinit var planningMonthsContainer: LinearLayout
    private lateinit var planningCommitmentsContainer: LinearLayout
    private lateinit var planningCommitmentsEmpty: TextView
    private lateinit var planningRecurringCountText: TextView
    private lateinit var planningRecurringValueText: TextView
    private lateinit var planningInstallmentCountText: TextView
    private lateinit var planningInstallmentValueText: TextView

    private var tipoSelecionado = TipoMovimentacao.GASTO
    private var mesSelecionado = YearMonth.now()
    private var mesAtualReferencia = YearMonth.now()
    private var mesExportacaoPendente: YearMonth? = null
    private var autenticadoNestaSessao = false
    private var autenticacaoEmAndamento = false
    private var alterandoProtecao = false
    private var momentoSegundoPlanoMs: Long? = null
    private var filtroTipo = 0
    private var filtroStatus = 0
    private var filtroCategoria = "Todas categorias"
    private var categoriaNovoLancamento = "Outros"
    private var dialogNovoLancamento: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = MovimentacaoDatabase(applicationContext)
        preferencias = getSharedPreferences(PREFERENCIAS, MODE_PRIVATE)

        mainScroll = findViewById(R.id.mainScroll)
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
        filterTypeChip = findViewById(R.id.filterTypeChip)
        filterStatusChip = findViewById(R.id.filterStatusChip)
        filterCategoryChip = findViewById(R.id.filterCategoryChip)
        clearFiltersButton = findViewById(R.id.clearFiltersButton)
        movementsResultSummary = findViewById(R.id.movementsResultSummary)
        emptyStateText = findViewById(R.id.emptyStateText)
        movementsContainer = findViewById(R.id.movementsContainer)
        historySection = findViewById(R.id.historySection)
        homeScreen = findViewById(R.id.homeScreen)
        planningScreen = findViewById(R.id.planningScreen)
        settingsScreen = findViewById(R.id.settingsScreen)
        navHome = findViewById(R.id.navHome)
        navMovements = findViewById(R.id.navMovements)
        navPlanning = findViewById(R.id.navPlanning)
        navSettings = findViewById(R.id.navSettings)
        homeUpcomingContainer = findViewById(R.id.homeUpcomingContainer)
        homeBudgetProgressBar = findViewById(R.id.homeBudgetProgressBar)
        homeBudgetProgressText = findViewById(R.id.homeBudgetProgressText)
        movementsMonthText = findViewById(R.id.movementsMonthText)
        planningMonthText = findViewById(R.id.planningMonthText)
        planningMonthNetText = findViewById(R.id.planningMonthNetText)
        planningIncomeExpectedText = findViewById(R.id.planningIncomeExpectedText)
        planningExpenseExpectedText = findViewById(R.id.planningExpenseExpectedText)
        planningMonthsContainer = findViewById(R.id.planningMonthsContainer)
        planningCommitmentsContainer = findViewById(R.id.planningCommitmentsContainer)
        planningCommitmentsEmpty = findViewById(R.id.planningCommitmentsEmpty)
        planningRecurringCountText = findViewById(R.id.planningRecurringCountText)
        planningRecurringValueText = findViewById(R.id.planningRecurringValueText)
        planningInstallmentCountText = findViewById(R.id.planningInstallmentCountText)
        planningInstallmentValueText = findViewById(R.id.planningInstallmentValueText)

        configurarFiltros()

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
                momentoSegundoPlanoMs = null
                solicitarAutenticacao()
            } else {
                preferencias.edit()
                    .putBoolean(CHAVE_PROTEGER_APP, false)
                    .apply()

                autenticadoNestaSessao = true
                momentoSegundoPlanoMs = null
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

        findViewById<TextView>(R.id.planningPreviousMonthButton).setOnClickListener {
            mesSelecionado = mesSelecionado.minusMonths(1)
            recarregarInterface()
        }

        findViewById<TextView>(R.id.planningNextMonthButton).setOnClickListener {
            mesSelecionado = mesSelecionado.plusMonths(1)
            recarregarInterface()
        }

        findViewById<TextView>(R.id.newMovementButton).setOnClickListener {
            abrirNovoLancamento(TipoMovimentacao.GASTO)
        }

        navHome.setOnClickListener {
            selecionarSecao(ViraSection.HOME)
        }
        navMovements.setOnClickListener {
            selecionarSecao(ViraSection.MOVEMENTS)
        }
        navPlanning.setOnClickListener {
            selecionarSecao(ViraSection.PLANNING)
        }
        navSettings.setOnClickListener {
            selecionarSecao(ViraSection.SETTINGS)
        }

        findViewById<Button>(R.id.homeExpenseQuickButton).setOnClickListener {
            selecionarSecao(ViraSection.MOVEMENTS)
            abrirNovoLancamento(TipoMovimentacao.GASTO)
        }

        findViewById<Button>(R.id.homeIncomeQuickButton).setOnClickListener {
            selecionarSecao(ViraSection.MOVEMENTS)
            abrirNovoLancamento(TipoMovimentacao.GANHO)
        }

        findViewById<TextView>(R.id.homeSeeAllMovements).setOnClickListener {
            selecionarSecao(ViraSection.MOVEMENTS)
            mainScroll.post {
                mainScroll.smoothScrollTo(0, historySection.top)
            }
        }

        findViewById<LinearLayout>(R.id.homeBudgetSection).setOnClickListener {
            selecionarSecao(ViraSection.PLANNING)
        }

        selecionarSecao(ViraSection.initial())
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
            autenticadoNestaSessao &&
            momentoSegundoPlanoMs != null
        ) {
            val tempoForaMs =
                SystemClock.elapsedRealtime() - momentoSegundoPlanoMs!!

            if (tempoForaMs >= TEMPO_REAUTENTICACAO_MS) {
                autenticadoNestaSessao = false
            }

            momentoSegundoPlanoMs = null
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
            autenticadoNestaSessao &&
            !autenticacaoEmAndamento
        ) {
            momentoSegundoPlanoMs = SystemClock.elapsedRealtime()
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
                momentoSegundoPlanoMs = null
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

    private fun selecionarSecao(secao: ViraSection) {
        homeScreen.visibility =
            if (secao == ViraSection.HOME) View.VISIBLE else View.GONE
        mainScroll.visibility =
            if (secao == ViraSection.MOVEMENTS) View.VISIBLE else View.GONE
        planningScreen.visibility =
            if (secao == ViraSection.PLANNING) View.VISIBLE else View.GONE
        settingsScreen.visibility =
            if (secao == ViraSection.SETTINGS) View.VISIBLE else View.GONE

        navHome.isSelected = secao == ViraSection.HOME
        navMovements.isSelected = secao == ViraSection.MOVEMENTS
        navPlanning.isSelected = secao == ViraSection.PLANNING
        navSettings.isSelected = secao == ViraSection.SETTINGS
    }

    private fun configurarFiltros() {
        filterTypeChip.setOnClickListener {
            abrirSeletorVira(
                titulo = "Tipo de movimentação",
                opcoes = listOf("Todos", "Gastos", "Ganhos"),
                indiceAtual = filtroTipo
            ) { index, _ ->
                filtroTipo = index
                atualizarChipsFiltro()
                renderizarMovimentacoes()
            }
        }

        filterStatusChip.setOnClickListener {
            abrirSeletorVira(
                titulo = "Status",
                opcoes = listOf("Todos status", "Realizados", "Pendentes"),
                indiceAtual = filtroStatus
            ) { index, _ ->
                filtroStatus = index
                atualizarChipsFiltro()
                renderizarMovimentacoes()
            }
        }

        filterCategoryChip.setOnClickListener {
            val opcoes = listOf("Todas categorias") + CATEGORIAS
            abrirSeletorVira(
                titulo = "Categoria",
                opcoes = opcoes,
                indiceAtual = opcoes.indexOf(filtroCategoria).coerceAtLeast(0)
            ) { _, valor ->
                filtroCategoria = valor
                atualizarChipsFiltro()
                renderizarMovimentacoes()
            }
        }

        clearFiltersButton.setOnClickListener {
            filtroTipo = 0
            filtroStatus = 0
            filtroCategoria = "Todas categorias"
            movementSearchInput.text.clear()
            atualizarChipsFiltro()
            renderizarMovimentacoes()
        }

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
                atualizarChipsFiltro()
                renderizarMovimentacoes()
            }

            override fun afterTextChanged(s: Editable?) = Unit
        })

        atualizarChipsFiltro()
    }

    private fun atualizarChipsFiltro() {
        filterTypeChip.text = when (filtroTipo) {
            1 -> "Gastos"
            2 -> "Ganhos"
            else -> "Todos"
        }
        filterStatusChip.text = when (filtroStatus) {
            1 -> "Realizados"
            2 -> "Pendentes"
            else -> "Status"
        }
        filterCategoryChip.text =
            if (filtroCategoria == "Todas categorias") {
                "Categorias"
            } else {
                filtroCategoria
            }

        val tipoAtivo = filtroTipo != 0
        val statusAtivo = filtroStatus != 0
        val categoriaAtiva = filtroCategoria != "Todas categorias"

        filterTypeChip.setBackgroundResource(
            if (tipoAtivo) R.drawable.vira_filter_chip_active
            else R.drawable.vira_filter_chip
        )
        filterStatusChip.setBackgroundResource(
            if (statusAtivo) R.drawable.vira_filter_chip_active
            else R.drawable.vira_filter_chip
        )
        filterCategoryChip.setBackgroundResource(
            if (categoriaAtiva) R.drawable.vira_filter_chip_active
            else R.drawable.vira_filter_chip
        )

        clearFiltersButton.visibility =
            if (
                tipoAtivo ||
                statusAtivo ||
                categoriaAtiva ||
                movementSearchInput.text?.isNotBlank() == true
            ) {
                View.VISIBLE
            } else {
                View.GONE
            }
    }

    private fun abrirSeletorVira(
        titulo: String,
        opcoes: List<String>,
        indiceAtual: Int,
        onSelected: (Int, String) -> Unit
    ) {
        val view = layoutInflater.inflate(R.layout.dialog_vira_choice, null)
        view.findViewById<TextView>(R.id.choiceDialogTitle).text = titulo
        val optionsContainer =
            view.findViewById<LinearLayout>(R.id.choiceOptionsContainer)
        val closeButton =
            view.findViewById<TextView>(R.id.choiceDialogCloseButton)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        opcoes.forEachIndexed { index, opcao ->
            val row = TextView(this).apply {
                text = if (index == indiceAtual) "✓  $opcao" else opcao
                textSize = 14f
                gravity = android.view.Gravity.CENTER_VERTICAL
                minimumHeight = dp(52)
                setPadding(dp(14), 0, dp(14), 0)
                setTextColor(
                    getColor(
                        if (index == indiceAtual) {
                            R.color.brand_secondary
                        } else {
                            R.color.text_primary
                        }
                    )
                )
                setBackgroundResource(R.drawable.vira_dialog_row)
                isClickable = true
                isFocusable = true
                setOnClickListener {
                    dialog.dismiss()
                    onSelected(index, opcao)
                }
            }

            optionsContainer.addView(row)

            if (index < opcoes.lastIndex) {
                optionsContainer.addView(
                    View(this).apply {
                        setBackgroundColor(getColor(R.color.border))
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    ).apply {
                        marginStart = dp(12)
                        marginEnd = dp(12)
                    }
                )
            }
        }

        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.show()
        aplicarEstiloDialogVira(dialog)
    }

    private fun abrirNovoLancamento(tipoInicial: TipoMovimentacao) {
        val view = layoutInflater.inflate(
            R.layout.dialog_vira_new_movement,
            null
        )

        expenseButton = view.findViewById(R.id.expenseButton)
        incomeButton = view.findViewById(R.id.incomeButton)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        valueInput = view.findViewById(R.id.valueInput)
        installmentCheckBox = view.findViewById(R.id.installmentCheckBox)
        installmentOptions = view.findViewById(R.id.installmentOptions)
        installmentCountInput = view.findViewById(R.id.installmentCountInput)
        installmentDayInput = view.findViewById(R.id.installmentDayInput)
        recurringCheckBox = view.findViewById(R.id.recurringCheckBox)
        recurringOptions = view.findViewById(R.id.recurringOptions)
        recurringDayInput = view.findViewById(R.id.recurringDayInput)

        val categoryChoice =
            view.findViewById<TextView>(R.id.newMovementCategoryChoice)
        val cancelButton =
            view.findViewById<TextView>(R.id.newMovementCancelButton)
        val saveButton =
            view.findViewById<TextView>(R.id.newMovementSaveButton)

        categoriaNovoLancamento = "Outros"
        categoryChoice.text = categoriaNovoLancamento
        installmentCountInput.setText("2")
        installmentDayInput.setText(LocalDate.now().dayOfMonth.toString())
        recurringDayInput.setText(LocalDate.now().dayOfMonth.toString())

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()
        dialogNovoLancamento = dialog

        expenseButton.setOnClickListener {
            selecionarTipo(TipoMovimentacao.GASTO)
        }
        incomeButton.setOnClickListener {
            selecionarTipo(TipoMovimentacao.GANHO)
        }

        installmentCheckBox.setOnCheckedChangeListener { _, checked ->
            installmentOptions.visibility =
                if (checked) View.VISIBLE else View.GONE
            if (checked) recurringCheckBox.isChecked = false
        }

        recurringCheckBox.setOnCheckedChangeListener { _, checked ->
            recurringOptions.visibility =
                if (checked) View.VISIBLE else View.GONE
            if (checked) installmentCheckBox.isChecked = false
        }

        categoryChoice.setOnClickListener {
            abrirSeletorVira(
                titulo = "Categoria",
                opcoes = CATEGORIAS,
                indiceAtual = CATEGORIAS.indexOf(categoriaNovoLancamento)
                    .coerceAtLeast(0)
            ) { _, categoria ->
                categoriaNovoLancamento = categoria
                categoryChoice.text = categoria
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
            dialogNovoLancamento = null
        }

        saveButton.setOnClickListener {
            registrarMovimentacao()
        }

        selecionarTipo(tipoInicial)
        dialog.setOnDismissListener {
            if (dialogNovoLancamento === dialog) {
                dialogNovoLancamento = null
            }
        }

        dialog.show()
        aplicarEstiloDialogVira(dialog)
        descriptionInput.requestFocus()
    }

    private fun selecionarTipo(tipo: TipoMovimentacao) {
        tipoSelecionado = tipo

        val gastoSelecionado = tipo == TipoMovimentacao.GASTO

        expenseButton.text = if (gastoSelecionado) "✓ GASTOU" else "GASTOU"
        incomeButton.text = if (!gastoSelecionado) "✓ GANHOU" else "GANHOU"

        expenseButton.setBackgroundResource(
            if (gastoSelecionado) {
                R.drawable.vira_button_expense
            } else {
                R.drawable.vira_button_secondary
            }
        )
        incomeButton.setBackgroundResource(
            if (!gastoSelecionado) {
                R.drawable.vira_button_income
            } else {
                R.drawable.vira_button_secondary
            }
        )

        expenseButton.setTextColor(
            getColor(
                if (gastoSelecionado) {
                    R.color.white
                } else {
                    R.color.text_primary
                }
            )
        )
        incomeButton.setTextColor(
            getColor(
                if (!gastoSelecionado) {
                    R.color.white
                } else {
                    R.color.text_primary
                }
            )
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
        val categoria = categoriaNovoLancamento

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
        dialogNovoLancamento?.dismiss()
        dialogNovoLancamento = null

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
        dialogNovoLancamento?.dismiss()
        dialogNovoLancamento = null

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
        dialogNovoLancamento?.dismiss()
        dialogNovoLancamento = null

        Toast.makeText(this, "Fixo mensal criado como pendente", Toast.LENGTH_SHORT).show()
        rolarParaHistorico()
    }

    private fun limparFormulario() {
        descriptionInput.text.clear()
        valueInput.text.clear()
        categoriaNovoLancamento = "Outros"
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
            Toast.makeText(
                this,
                "Não foi possível carregar os fixos.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val view = layoutInflater.inflate(
            R.layout.dialog_vira_recurring_manager,
            null
        )
        val recurringContainer =
            view.findViewById<LinearLayout>(R.id.recurringManagerContainer)
        val emptyText =
            view.findViewById<TextView>(R.id.recurringManagerEmptyText)
        val closeButton =
            view.findViewById<TextView>(R.id.recurringManagerCloseButton)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        if (recorrencias.isEmpty()) {
            emptyText.visibility = View.VISIBLE
        } else {
            emptyText.visibility = View.GONE

            recorrencias.forEachIndexed { index, recorrencia ->
                val row = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    minimumHeight = dp(70)
                    setPadding(dp(12), dp(8), dp(10), dp(8))
                    setBackgroundResource(R.drawable.vira_dialog_row)
                    isClickable = true
                    isFocusable = true
                }

                val textos = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                }

                textos.addView(
                    TextView(this).apply {
                        text = recorrencia.descricao
                        textSize = 14f
                        setTextColor(getColor(R.color.text_primary))
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                val tipo = if (
                    recorrencia.tipo == TipoMovimentacao.GASTO
                ) {
                    "Gasto"
                } else {
                    "Ganho"
                }

                textos.addView(
                    TextView(this).apply {
                        text =
                            "${recorrencia.categoria} • $tipo • dia ${recorrencia.diaMes}"
                        textSize = 12f
                        setTextColor(getColor(R.color.text_secondary))
                    }
                )

                row.addView(
                    textos,
                    LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    )
                )

                row.addView(
                    TextView(this).apply {
                        text = formatarMoeda(recorrencia.valorCentavos)
                        textSize = 13f
                        setTextColor(
                            getColor(
                                if (
                                    recorrencia.tipo ==
                                    TipoMovimentacao.GASTO
                                ) {
                                    R.color.expense
                                } else {
                                    R.color.income
                                }
                            )
                        )
                        setTypeface(
                            typeface,
                            android.graphics.Typeface.BOLD
                        )
                    }
                )

                row.setOnClickListener {
                    dialog.dismiss()
                    abrirDetalheRecorrencia(recorrencia)
                }

                recurringContainer.addView(row)

                if (index < recorrencias.lastIndex) {
                    recurringContainer.addView(
                        View(this).apply {
                            setBackgroundColor(getColor(R.color.border))
                        },
                        LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            dp(1)
                        ).apply {
                            marginStart = dp(12)
                            marginEnd = dp(12)
                        }
                    )
                }
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        aplicarEstiloDialogVira(dialog)
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
        val view = layoutInflater.inflate(
            R.layout.dialog_vira_edit_movement,
            null
        )
        val typeChoice =
            view.findViewById<TextView>(R.id.editTypeChoice)
        val statusChoice =
            view.findViewById<TextView>(R.id.editStatusChoice)
        val categoryChoice =
            view.findViewById<TextView>(R.id.editCategoryChoice)
        val descriptionEdit =
            view.findViewById<EditText>(R.id.editDescriptionInput)
        val valueEdit =
            view.findViewById<EditText>(R.id.editValueInput)
        val deleteButton =
            view.findViewById<TextView>(R.id.editDeleteButton)
        val cancelButton =
            view.findViewById<TextView>(R.id.editCancelButton)
        val saveButton =
            view.findViewById<TextView>(R.id.editSaveButton)

        var tipoEditado = movimentacao.tipo
        var statusEditado = movimentacao.status
        var categoriaEditada = movimentacao.categoria

        fun atualizarLabels() {
            typeChoice.text =
                if (tipoEditado == TipoMovimentacao.GASTO) {
                    "Gasto"
                } else {
                    "Ganho"
                }

            statusChoice.text = when {
                tipoEditado == TipoMovimentacao.GASTO &&
                    statusEditado == StatusMovimentacao.REALIZADO ->
                    "Pago"
                tipoEditado == TipoMovimentacao.GASTO ->
                    "Pendente"
                statusEditado == StatusMovimentacao.REALIZADO ->
                    "Recebido"
                else ->
                    "A receber"
            }

            categoryChoice.text = categoriaEditada
        }

        descriptionEdit.setText(movimentacao.descricao)
        valueEdit.setText(
            formatarValorParaEdicao(movimentacao.valorCentavos)
        )
        deleteButton.text =
            if (movimentacao.recorrenciaId == null) {
                "Excluir lançamento"
            } else {
                "Desativar fixo"
            }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        typeChoice.setOnClickListener {
            val tipos = listOf("Gasto", "Ganho")
            abrirSeletorVira(
                titulo = "Tipo de lançamento",
                opcoes = tipos,
                indiceAtual =
                    if (tipoEditado == TipoMovimentacao.GASTO) 0 else 1
            ) { index, _ ->
                tipoEditado =
                    if (index == 0) {
                        TipoMovimentacao.GASTO
                    } else {
                        TipoMovimentacao.GANHO
                    }
                atualizarLabels()
            }
        }

        statusChoice.setOnClickListener {
            val opcoes =
                if (tipoEditado == TipoMovimentacao.GASTO) {
                    listOf("Pago", "Pendente")
                } else {
                    listOf("Recebido", "A receber")
                }

            abrirSeletorVira(
                titulo = "Status",
                opcoes = opcoes,
                indiceAtual =
                    if (statusEditado == StatusMovimentacao.REALIZADO) {
                        0
                    } else {
                        1
                    }
            ) { index, _ ->
                statusEditado =
                    if (index == 0) {
                        StatusMovimentacao.REALIZADO
                    } else {
                        StatusMovimentacao.PENDENTE
                    }
                atualizarLabels()
            }
        }

        categoryChoice.setOnClickListener {
            abrirSeletorVira(
                titulo = "Categoria",
                opcoes = CATEGORIAS,
                indiceAtual = CATEGORIAS
                    .indexOf(categoriaEditada)
                    .coerceAtLeast(0)
            ) { _, categoria ->
                categoriaEditada = categoria
                atualizarLabels()
            }
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val novaDescricao =
                descriptionEdit.text.toString().trim()
            val novoValor =
                parseValorCentavos(valueEdit.text.toString())

            if (novaDescricao.isBlank()) {
                descriptionEdit.error =
                    "Digite o nome da movimentação"
                descriptionEdit.requestFocus()
                return@setOnClickListener
            }

            if (novoValor == null || novoValor <= 0) {
                valueEdit.error =
                    "Digite um valor maior que zero"
                valueEdit.requestFocus()
                return@setOnClickListener
            }

            val atualizou = try {
                database.atualizar(
                    id = movimentacao.id,
                    tipo = tipoEditado,
                    descricao = novaDescricao,
                    valorCentavos = novoValor,
                    status = statusEditado,
                    categoria = categoriaEditada
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
            Toast.makeText(
                this,
                "Movimentação atualizada",
                Toast.LENGTH_SHORT
            ).show()
        }

        deleteButton.setOnClickListener {
            if (movimentacao.recorrenciaId == null) {
                confirmarExclusao(movimentacao, dialog)
            } else {
                confirmarDesativacaoFixo(
                    movimentacao.recorrenciaId,
                    dialog
                )
            }
        }

        atualizarLabels()
        dialog.show()
        aplicarEstiloDialogVira(dialog)
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
        val mesFormatado = formatarMes(mesSelecionado)
        monthText.text = mesFormatado
        movementsMonthText.text = mesFormatado
        planningMonthText.text = mesFormatado
        monthStateText.text = estadoDoMes(mesSelecionado)

        carregarMovimentacoes()
        atualizarResumo()
        atualizarResumoCategorias()
        renderizarMovimentacoes()
        renderizarProximosVencimentos()
        atualizarResumoOrcamentoHome()
        atualizarPlanejamento()
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

    private fun renderizarProximosVencimentos() {
        homeUpcomingContainer.removeAllViews()

        val proximos = movimentacoes
            .filter {
                it.tipo == TipoMovimentacao.GASTO &&
                    it.status == StatusMovimentacao.PENDENTE
            }
            .sortedBy { it.data }
            .take(3)

        if (proximos.isEmpty()) {
            homeUpcomingContainer.addView(
                TextView(this).apply {
                    text = "Nenhum pagamento pendente neste mês."
                    textSize = 13f
                    setTextColor(getColor(R.color.text_secondary))
                    setPadding(0, dp(14), 0, dp(14))
                }
            )
            return
        }

        val dateFormatter = DateTimeFormatter.ofPattern("dd/MM", localeBrasil)

        proximos.forEachIndexed { index, movimentacao ->
            val linha = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, dp(12), 0, dp(12))
            }

            val textos = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            textos.addView(
                TextView(this).apply {
                    text = movimentacao.descricao
                    textSize = 13f
                    setTextColor(getColor(R.color.text_primary))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
            )

            val contexto = when {
                movimentacao.parcelaNumero != null &&
                    movimentacao.parcelasTotal != null ->
                    "Parcela ${movimentacao.parcelaNumero}/${movimentacao.parcelasTotal}"
                movimentacao.recorrenciaId != null -> "Fixo mensal"
                else -> movimentacao.categoria
            }

            textos.addView(
                TextView(this).apply {
                    text = "$contexto • ${movimentacao.data.format(dateFormatter)}"
                    textSize = 11f
                    setTextColor(getColor(R.color.text_secondary))
                }
            )

            linha.addView(
                textos,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            linha.addView(
                TextView(this).apply {
                    text = formatarMoeda(movimentacao.valorCentavos)
                    textSize = 13f
                    setTextColor(getColor(R.color.expense))
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                }
            )

            homeUpcomingContainer.addView(linha)

            if (index < proximos.lastIndex) {
                homeUpcomingContainer.addView(
                    View(this).apply {
                        setBackgroundColor(getColor(R.color.border))
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    )
                )
            }
        }
    }

    private fun atualizarResumoOrcamentoHome() {
        val orcamentos = try {
            database.listarOrcamentos()
        } catch (erro: SQLiteException) {
            emptyMap()
        }

        if (orcamentos.isEmpty()) {
            homeBudgetProgressBar.progress = 0
            homeBudgetProgressText.text = "Sem limite definido"
            return
        }

        val limiteTotal = orcamentos.values.sum().coerceAtLeast(1L)
        val gastoPrevisto = movimentacoes
            .filter {
                it.tipo == TipoMovimentacao.GASTO &&
                    orcamentos.containsKey(it.categoria)
            }
            .sumOf { it.valorCentavos }

        val progresso = ((gastoPrevisto * 1000L) / limiteTotal)
            .toInt()
            .coerceIn(0, 1000)

        val percentual = ((gastoPrevisto * 100L) / limiteTotal)
            .toInt()
            .coerceAtLeast(0)

        homeBudgetProgressBar.progress = progresso
        homeBudgetProgressBar.progressTintList = ColorStateList.valueOf(
            getColor(
                if (gastoPrevisto > limiteTotal) {
                    R.color.expense
                } else {
                    R.color.brand_secondary
                }
            )
        )
        homeBudgetProgressText.text = "$percentual% utilizado"
    }

    private fun atualizarPlanejamento() {
        val entradas = movimentacoes
            .filter { it.tipo == TipoMovimentacao.GANHO }
            .sumOf { it.valorCentavos }
        val saidas = movimentacoes
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .sumOf { it.valorCentavos }
        val resultado = entradas - saidas

        planningMonthNetText.text = formatarMoeda(resultado)
        planningMonthNetText.setTextColor(
            getColor(
                when {
                    resultado > 0 -> R.color.income
                    resultado < 0 -> R.color.expense
                    else -> R.color.white
                }
            )
        )
        planningIncomeExpectedText.text = formatarMoeda(entradas)
        planningExpenseExpectedText.text = formatarMoeda(saidas)

        renderizarMesesPlanejamento()
        renderizarCompromissosPlanejamento()
        atualizarResumoPlanejamentoAtivo()
    }

    private fun renderizarMesesPlanejamento() {
        planningMonthsContainer.removeAllViews()

        repeat(3) { offset ->
            val mes = mesSelecionado.plusMonths(offset.toLong())

            val itens = try {
                database.garantirRecorrenciasParaMes(mes)
                database.listarPorMes(mes)
            } catch (erro: SQLiteException) {
                emptyList()
            }

            val entradas = itens
                .filter { it.tipo == TipoMovimentacao.GANHO }
                .sumOf { it.valorCentavos }
            val saidas = itens
                .filter { it.tipo == TipoMovimentacao.GASTO }
                .sumOf { it.valorCentavos }
            val resultado = entradas - saidas
            val selecionado = offset == 0

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                minimumWidth = dp(148)
                setPadding(dp(14), dp(13), dp(14), dp(13))
                setBackgroundResource(
                    if (selecionado) {
                        R.drawable.vira_month_card_active
                    } else {
                        R.drawable.vira_month_card
                    }
                )
                isClickable = true
                isFocusable = true
            }

            val mesLabel = TextView(this).apply {
                val formato = DateTimeFormatter.ofPattern(
                    "MMM",
                    localeBrasil
                )
                text = mes.atDay(1)
                    .format(formato)
                    .replaceFirstChar { it.uppercase(localeBrasil) }
                textSize = 12f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(
                    getColor(
                        if (selecionado) {
                            R.color.white
                        } else {
                            R.color.text_secondary
                        }
                    )
                )
            }

            val resultadoText = TextView(this).apply {
                text = formatarMoeda(resultado)
                textSize = 17f
                setTypeface(
                    typeface,
                    android.graphics.Typeface.BOLD
                )
                setTextColor(
                    getColor(
                        when {
                            selecionado -> R.color.white
                            resultado >= 0 -> R.color.income
                            else -> R.color.expense
                        }
                    )
                )
            }

            val detalhe = TextView(this).apply {
                text = formatarMoeda(saidas) + " em saídas"
                textSize = 10f
                setTextColor(
                    getColor(
                        if (selecionado) {
                            R.color.brand_soft
                        } else {
                            R.color.text_secondary
                        }
                    )
                )
            }

            card.addView(mesLabel)
            card.addView(
                resultadoText,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(7)
                }
            )
            card.addView(
                detalhe,
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = dp(3)
                }
            )

            card.setOnClickListener {
                if (mes != mesSelecionado) {
                    mesSelecionado = mes
                    recarregarInterface()
                }
            }

            planningMonthsContainer.addView(
                card,
                LinearLayout.LayoutParams(
                    dp(148),
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            )

            if (offset < 2) {
                planningMonthsContainer.addView(
                    Space(this),
                    LinearLayout.LayoutParams(
                        dp(10),
                        dp(1)
                    )
                )
            }
        }
    }

    private fun renderizarCompromissosPlanejamento() {
        planningCommitmentsContainer.removeAllViews()

        val pendentes = movimentacoes
            .filter { it.status == StatusMovimentacao.PENDENTE }
            .sortedBy { it.data }
            .take(6)

        planningCommitmentsEmpty.visibility =
            if (pendentes.isEmpty()) View.VISIBLE else View.GONE

        val formatter = DateTimeFormatter.ofPattern(
            "dd/MM",
            localeBrasil
        )

        pendentes.forEachIndexed { index, movimentacao ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                minimumHeight = dp(62)
                setPadding(0, dp(10), 0, dp(10))
                isClickable = true
                isFocusable = true
            }

            val indicador = View(this).apply {
                setBackgroundResource(
                    if (movimentacao.tipo == TipoMovimentacao.GASTO) {
                        R.drawable.vira_dot_expense
                    } else {
                        R.drawable.vira_dot_income
                    }
                )
            }
            row.addView(
                indicador,
                LinearLayout.LayoutParams(dp(10), dp(10)).apply {
                    marginEnd = dp(12)
                }
            )

            val textos = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            textos.addView(
                TextView(this).apply {
                    text = movimentacao.descricao
                    textSize = 13f
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setTextColor(getColor(R.color.text_primary))
                }
            )

            val contexto = when {
                movimentacao.parcelaNumero != null &&
                    movimentacao.parcelasTotal != null ->
                    "Parcela " +
                        movimentacao.parcelaNumero +
                        "/" +
                        movimentacao.parcelasTotal
                movimentacao.recorrenciaId != null ->
                    "Fixo mensal"
                else ->
                    movimentacao.categoria
            }

            textos.addView(
                TextView(this).apply {
                    text = contexto + " • " +
                        movimentacao.data.format(formatter)
                    textSize = 11f
                    setTextColor(getColor(R.color.text_secondary))
                }
            )

            row.addView(
                textos,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                TextView(this).apply {
                    val sinal =
                        if (movimentacao.tipo == TipoMovimentacao.GASTO) {
                            "-"
                        } else {
                            "+"
                        }
                    text = sinal + " " +
                        formatarMoeda(movimentacao.valorCentavos)
                    textSize = 13f
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setTextColor(
                        getColor(
                            if (
                                movimentacao.tipo ==
                                TipoMovimentacao.GASTO
                            ) {
                                R.color.expense
                            } else {
                                R.color.income
                            }
                        )
                    )
                }
            )

            row.setOnClickListener {
                abrirEditor(movimentacao)
            }

            planningCommitmentsContainer.addView(row)

            if (index < pendentes.lastIndex) {
                planningCommitmentsContainer.addView(
                    View(this).apply {
                        setBackgroundColor(getColor(R.color.border))
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    ).apply {
                        marginStart = dp(22)
                    }
                )
            }
        }
    }

    private fun atualizarResumoPlanejamentoAtivo() {
        val recorrencias = try {
            database.listarRecorrenciasAtivas()
        } catch (erro: SQLiteException) {
            emptyList()
        }

        val fixosGastos = recorrencias
            .filter { it.tipo == TipoMovimentacao.GASTO }
            .sumOf { it.valorCentavos }
        val fixosGanhos = recorrencias
            .filter { it.tipo == TipoMovimentacao.GANHO }
            .sumOf { it.valorCentavos }

        planningRecurringCountText.text =
            when (recorrencias.size) {
                0 -> "Nenhum fixo ativo"
                1 -> "1 fixo ativo"
                else -> recorrencias.size.toString() + " fixos ativos"
            }

        planningRecurringValueText.text =
            if (recorrencias.isEmpty()) {
                "Crie gastos ou ganhos que se repetem todo mês."
            } else {
                formatarMoeda(fixosGastos) + " saídas • " +
                    formatarMoeda(fixosGanhos) + " entradas"
            }

        val parcelas = try {
            database.listarParcelasPendentesAPartirDe(mesSelecionado)
        } catch (erro: SQLiteException) {
            emptyList()
        }

        val compras = parcelas
            .mapNotNull { it.parcelamentoId }
            .distinct()
            .size
        val totalParcelas = parcelas.size
        val valorRestante = parcelas.sumOf { it.valorCentavos }

        planningInstallmentCountText.text =
            when (compras) {
                0 -> "Nenhum parcelamento ativo"
                1 -> "1 compra parcelada"
                else -> compras.toString() + " compras parceladas"
            }

        planningInstallmentValueText.text =
            if (parcelas.isEmpty()) {
                "Quando parcelar um gasto, o restante aparece aqui."
            } else {
                totalParcelas.toString() + " parcelas • " +
                    formatarMoeda(valorRestante) +
                    " ainda previsto"
            }
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

        val todasCategoriasExibidas = (
            gastosPorCategoria.keys + orcamentos.keys
        )
            .distinct()
            .sortedByDescending { gastosPorCategoria[it] ?: 0L }

        val categoriasExibidas = todasCategoriasExibidas.take(4)

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

        val restantes =
            todasCategoriasExibidas.size - categoriasExibidas.size

        if (restantes > 0) {
            categoryChartContainer.addView(
                TextView(this).apply {
                    text = "+ " + restantes +
                        " categorias no gerenciador"
                    textSize = 12f
                    setTextColor(getColor(R.color.brand_secondary))
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                    setPadding(0, dp(4), 0, 0)
                }
            )
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

        val view = layoutInflater.inflate(
            R.layout.dialog_vira_budget_manager,
            null
        )
        val categoriesContainer =
            view.findViewById<LinearLayout>(R.id.budgetCategoriesContainer)
        val closeButton =
            view.findViewById<TextView>(R.id.budgetManagerCloseButton)

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        CATEGORIAS.forEachIndexed { index, categoria ->
            val limite = orcamentos[categoria]

            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                minimumHeight = dp(62)
                setPadding(dp(12), dp(8), dp(10), dp(8))
                setBackgroundResource(R.drawable.vira_dialog_row)
                isClickable = true
                isFocusable = true
            }

            val textos = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
            }

            textos.addView(
                TextView(this).apply {
                    text = categoria
                    textSize = 14f
                    setTextColor(getColor(R.color.text_primary))
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            textos.addView(
                TextView(this).apply {
                    text = if (limite == null) {
                        "Sem limite definido"
                    } else {
                        "${formatarMoeda(limite)} por mês"
                    }
                    textSize = 12f
                    setTextColor(getColor(R.color.text_secondary))
                }
            )

            row.addView(
                textos,
                LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            row.addView(
                TextView(this).apply {
                    text = if (limite == null) "Definir  ›" else "Editar  ›"
                    textSize = 12f
                    setTextColor(getColor(R.color.brand_secondary))
                    setTypeface(
                        typeface,
                        android.graphics.Typeface.BOLD
                    )
                }
            )

            row.setOnClickListener {
                dialog.dismiss()
                abrirEditorOrcamento(categoria, limite)
            }

            categoriesContainer.addView(row)

            if (index < CATEGORIAS.lastIndex) {
                categoriesContainer.addView(
                    View(this).apply {
                        setBackgroundColor(getColor(R.color.border))
                    },
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(1)
                    ).apply {
                        marginStart = dp(12)
                        marginEnd = dp(12)
                    }
                )
            }
        }

        closeButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
        aplicarEstiloDialogVira(dialog)
    }

    private fun abrirEditorOrcamento(
        categoria: String,
        limiteAtual: Long?
    ) {
        val view = layoutInflater.inflate(
            R.layout.dialog_vira_budget_editor,
            null
        )
        val categoryText =
            view.findViewById<TextView>(R.id.budgetEditorCategoryText)
        val input =
            view.findViewById<EditText>(R.id.budgetValueInput)
        val removeButton =
            view.findViewById<TextView>(R.id.budgetRemoveButton)
        val cancelButton =
            view.findViewById<TextView>(R.id.budgetCancelButton)
        val saveButton =
            view.findViewById<TextView>(R.id.budgetSaveButton)

        categoryText.text = categoria

        if (limiteAtual != null) {
            input.setText(formatarValorParaEdicao(limiteAtual))
            removeButton.visibility = View.VISIBLE
        } else {
            removeButton.visibility = View.GONE
        }

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        saveButton.setOnClickListener {
            val limite = parseValorCentavos(input.text.toString())

            if (limite == null || limite <= 0) {
                input.error = "Digite um valor maior que zero"
                input.requestFocus()
                return@setOnClickListener
            }

            try {
                database.salvarOrcamento(categoria, limite)
                recarregarInterface()
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Orçamento de $categoria atualizado",
                    Toast.LENGTH_SHORT
                ).show()
                abrirGerenciadorOrcamentos()
            } catch (erro: Exception) {
                Toast.makeText(
                    this,
                    "Não foi possível salvar o orçamento.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        removeButton.setOnClickListener {
            try {
                database.removerOrcamento(categoria)
                recarregarInterface()
                dialog.dismiss()
                Toast.makeText(
                    this,
                    "Limite de $categoria removido",
                    Toast.LENGTH_SHORT
                ).show()
                abrirGerenciadorOrcamentos()
            } catch (erro: SQLiteException) {
                Toast.makeText(
                    this,
                    "Não foi possível remover o orçamento.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        dialog.show()
        aplicarEstiloDialogVira(dialog)
        input.requestFocus()
    }

    private fun aplicarEstiloDialogVira(dialog: AlertDialog) {
        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(
                android.graphics.Color.TRANSPARENT
            )
        )
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.92f).toInt(),
            android.view.WindowManager.LayoutParams.WRAP_CONTENT
        )
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
            "Desbloquear Vira",
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
                "Vira-backup-${LocalDate.now()}.json"
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
                "Backup do Vira criado com sucesso",
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
                "Os dados atuais do Vira serão substituídos pelos dados do backup. " +
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
                "Vira-${mesSelecionado}.csv"
            )
        }

        startActivityForResult(intent, REQUEST_EXPORT_CSV)
    }

    private fun exportarCsv(uri: Uri) {
        val mes = mesExportacaoPendente ?: mesSelecionado

        try {
            val output = contentResolver.openOutputStream(uri)
                ?: throw IllegalStateException("Não foi possível abrir o arquivo.")

            output.bufferedWriter(CSV_CHARSET).use { writer ->
                writer.write(
                    "Data;Tipo;Status;Categoria;Descrição;Valor;Fixo;Parcela\r\n"
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
                        writer.write("\r\n")
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
        val protegido = if (
            valor.startsWith("=") ||
            valor.startsWith("+") ||
            valor.startsWith("-") ||
            valor.startsWith("@")
        ) {
            "'$valor"
        } else {
            valor
        }

        val escapado = protegido.replace("\"", "\"\"")

        return if (
            escapado.contains(';') ||
            escapado.contains('"') ||
            escapado.contains('\n') ||
            escapado.contains('\r')
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

        val tipoFiltro = filtroTipo
        val statusFiltro = filtroStatus
        val categoriaFiltro = filtroCategoria

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
        movementsResultSummary.text = when {
            movimentacoes.isEmpty() -> "Nenhum lançamento neste mês"
            filtradas.size == movimentacoes.size ->
                "${filtradas.size} lançamentos neste mês"
            else -> "${filtradas.size} de ${movimentacoes.size} lançamentos"
        }
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
                "${movimentacao.categoria} • ${tipoTexto}${fixoTexto}${parcelaTexto} • ${movimentacao.data.format(dateFormatter)}"

            row.findViewById<TextView>(R.id.movementStatus).apply {
                text = statusTexto
                setTextColor(
                    getColor(
                        if (movimentacao.status == StatusMovimentacao.PENDENTE) {
                            R.color.brand_gold
                        } else if (
                            movimentacao.tipo == TipoMovimentacao.GASTO
                        ) {
                            R.color.expense
                        } else {
                            R.color.income
                        }
                    )
                )
            }

            row.findViewById<View>(R.id.movementAccent).setBackgroundColor(
                getColor(
                    if (movimentacao.tipo == TipoMovimentacao.GASTO) {
                        R.color.expense
                    } else {
                        R.color.income
                    }
                )
            )

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
        private const val TEMPO_REAUTENTICACAO_MS = 5 * 60 * 1000L
        private val CSV_CHARSET: Charset = Charset.forName("windows-1252")

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
