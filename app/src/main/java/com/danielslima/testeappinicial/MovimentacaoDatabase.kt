package com.danielslima.testeappinicial

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import java.time.YearMonth
import java.util.UUID
import org.json.JSONArray
import org.json.JSONObject

class MovimentacaoDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        criarTabelaMovimentacoes(db)
        criarTabelaRecorrencias(db)
        criarIndices(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_RECORRENCIA_ID INTEGER"
            )
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_COMPETENCIA TEXT"
            )
            criarTabelaRecorrencias(db)
            criarIndices(db)
        }

        if (oldVersion < 3) {
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_STATUS TEXT NOT NULL DEFAULT 'REALIZADO'"
            )
        }

        if (oldVersion < 4) {
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_CATEGORIA TEXT NOT NULL DEFAULT 'Outros'"
            )
            db.execSQL(
                "ALTER TABLE $TABELA_RECORRENCIAS ADD COLUMN $COLUNA_CATEGORIA TEXT NOT NULL DEFAULT 'Outros'"
            )
        }

        if (oldVersion < 5) {
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_PARCELAMENTO_ID TEXT"
            )
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_PARCELA_NUMERO INTEGER"
            )
            db.execSQL(
                "ALTER TABLE $TABELA_MOVIMENTACOES ADD COLUMN $COLUNA_PARCELAS_TOTAL INTEGER"
            )
        }
    }

    private fun criarTabelaMovimentacoes(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $TABELA_MOVIMENTACOES (
                $COLUNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUNA_TIPO TEXT NOT NULL,
                $COLUNA_DESCRICAO TEXT NOT NULL,
                $COLUNA_VALOR_CENTAVOS INTEGER NOT NULL CHECK ($COLUNA_VALOR_CENTAVOS > 0),
                $COLUNA_DATA TEXT NOT NULL,
                $COLUNA_RECORRENCIA_ID INTEGER,
                $COLUNA_COMPETENCIA TEXT,
                $COLUNA_STATUS TEXT NOT NULL DEFAULT 'REALIZADO',
                $COLUNA_CATEGORIA TEXT NOT NULL DEFAULT 'Outros',
                $COLUNA_PARCELAMENTO_ID TEXT,
                $COLUNA_PARCELA_NUMERO INTEGER,
                $COLUNA_PARCELAS_TOTAL INTEGER
            )
            """.trimIndent()
        )
    }

    private fun criarTabelaRecorrencias(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABELA_RECORRENCIAS (
                $COLUNA_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUNA_TIPO TEXT NOT NULL,
                $COLUNA_DESCRICAO TEXT NOT NULL,
                $COLUNA_VALOR_CENTAVOS INTEGER NOT NULL CHECK ($COLUNA_VALOR_CENTAVOS > 0),
                $COLUNA_DIA_MES INTEGER NOT NULL CHECK ($COLUNA_DIA_MES BETWEEN 1 AND 31),
                $COLUNA_INICIO_MES TEXT NOT NULL,
                $COLUNA_ATIVA INTEGER NOT NULL DEFAULT 1,
                $COLUNA_CATEGORIA TEXT NOT NULL DEFAULT 'Outros'
            )
            """.trimIndent()
        )
    }

    private fun criarIndices(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE UNIQUE INDEX IF NOT EXISTS idx_movimentacao_recorrencia_competencia
            ON $TABELA_MOVIMENTACOES ($COLUNA_RECORRENCIA_ID, $COLUNA_COMPETENCIA)
            """.trimIndent()
        )
    }

    fun inserir(
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long,
        categoria: String,
        data: LocalDateTime = LocalDateTime.now()
    ): Movimentacao {
        val values = ContentValues().apply {
            put(COLUNA_TIPO, tipo.name)
            put(COLUNA_DESCRICAO, descricao)
            put(COLUNA_VALOR_CENTAVOS, valorCentavos)
            put(COLUNA_DATA, data.toString())
            put(COLUNA_STATUS, StatusMovimentacao.REALIZADO.name)
            put(COLUNA_CATEGORIA, categoria)
        }

        val id = writableDatabase.insertOrThrow(
            TABELA_MOVIMENTACOES,
            null,
            values
        )

        return Movimentacao(
            id = id,
            tipo = tipo,
            descricao = descricao,
            valorCentavos = valorCentavos,
            data = data,
            status = StatusMovimentacao.REALIZADO,
            categoria = categoria
        )
    }

    fun atualizar(
        id: Long,
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long,
        status: StatusMovimentacao,
        categoria: String
    ): Boolean {
        val values = ContentValues().apply {
            put(COLUNA_TIPO, tipo.name)
            put(COLUNA_DESCRICAO, descricao)
            put(COLUNA_VALOR_CENTAVOS, valorCentavos)
            put(COLUNA_STATUS, status.name)
            put(COLUNA_CATEGORIA, categoria)
        }

        return writableDatabase.update(
            TABELA_MOVIMENTACOES,
            values,
            "$COLUNA_ID = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun excluir(id: Long): Boolean {
        return writableDatabase.delete(
            TABELA_MOVIMENTACOES,
            "$COLUNA_ID = ?",
            arrayOf(id.toString())
        ) > 0
    }

    fun inserirParcelamento(
        descricao: String,
        valorTotalCentavos: Long,
        categoria: String,
        quantidadeParcelas: Int,
        diaVencimento: Int,
        mesReferencia: YearMonth = YearMonth.now()
    ): List<Movimentacao> {
        require(quantidadeParcelas >= 2) {
            "Parcelamento deve ter pelo menos 2 parcelas."
        }
        require(diaVencimento in 1..31) {
            "Dia de vencimento inválido."
        }
        require(valorTotalCentavos > 0) {
            "Valor inválido."
        }

        val hoje = LocalDateTime.now()
        val primeiroMes = if (diaVencimento >= hoje.dayOfMonth) {
            mesReferencia
        } else {
            mesReferencia.plusMonths(1)
        }

        val valorBase = valorTotalCentavos / quantidadeParcelas
        val resto = valorTotalCentavos % quantidadeParcelas
        val parcelamentoId = UUID.randomUUID().toString()
        val resultado = mutableListOf<Movimentacao>()

        val db = writableDatabase
        db.beginTransaction()

        try {
            for (indice in 0 until quantidadeParcelas) {
                val numeroParcela = indice + 1
                val mes = primeiroMes.plusMonths(indice.toLong())
                val diaReal = minOf(diaVencimento, mes.lengthOfMonth())
                val data = mes.atDay(diaReal).atTime(12, 0)
                val valorParcela = valorBase + if (indice < resto) 1 else 0

                val values = ContentValues().apply {
                    put(COLUNA_TIPO, TipoMovimentacao.GASTO.name)
                    put(
                        COLUNA_DESCRICAO,
                        "$descricao ($numeroParcela/$quantidadeParcelas)"
                    )
                    put(COLUNA_VALOR_CENTAVOS, valorParcela)
                    put(COLUNA_DATA, data.toString())
                    put(COLUNA_STATUS, StatusMovimentacao.PENDENTE.name)
                    put(COLUNA_CATEGORIA, categoria)
                    put(COLUNA_PARCELAMENTO_ID, parcelamentoId)
                    put(COLUNA_PARCELA_NUMERO, numeroParcela)
                    put(COLUNA_PARCELAS_TOTAL, quantidadeParcelas)
                }

                val id = db.insertOrThrow(
                    TABELA_MOVIMENTACOES,
                    null,
                    values
                )

                resultado.add(
                    Movimentacao(
                        id = id,
                        tipo = TipoMovimentacao.GASTO,
                        descricao = "$descricao ($numeroParcela/$quantidadeParcelas)",
                        valorCentavos = valorParcela,
                        data = data,
                        status = StatusMovimentacao.PENDENTE,
                        categoria = categoria,
                        parcelamentoId = parcelamentoId,
                        parcelaNumero = numeroParcela,
                        parcelasTotal = quantidadeParcelas
                    )
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return resultado
    }

    fun inserirRecorrencia(
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long,
        diaMes: Int,
        categoria: String,
        inicioMes: YearMonth = YearMonth.now()
    ): Recorrencia {
        val values = ContentValues().apply {
            put(COLUNA_TIPO, tipo.name)
            put(COLUNA_DESCRICAO, descricao)
            put(COLUNA_VALOR_CENTAVOS, valorCentavos)
            put(COLUNA_DIA_MES, diaMes)
            put(COLUNA_INICIO_MES, inicioMes.toString())
            put(COLUNA_ATIVA, 1)
            put(COLUNA_CATEGORIA, categoria)
        }

        val id = writableDatabase.insertOrThrow(
            TABELA_RECORRENCIAS,
            null,
            values
        )

        return Recorrencia(
            id = id,
            tipo = tipo,
            descricao = descricao,
            valorCentavos = valorCentavos,
            diaMes = diaMes,
            inicioMes = inicioMes,
            ativa = true,
            categoria = categoria
        )
    }

    fun listarRecorrenciasAtivas(): List<Recorrencia> {
        val resultado = mutableListOf<Recorrencia>()

        readableDatabase.query(
            TABELA_RECORRENCIAS,
            arrayOf(
                COLUNA_ID,
                COLUNA_TIPO,
                COLUNA_DESCRICAO,
                COLUNA_VALOR_CENTAVOS,
                COLUNA_DIA_MES,
                COLUNA_INICIO_MES,
                COLUNA_ATIVA,
                COLUNA_CATEGORIA
            ),
            "$COLUNA_ATIVA = 1",
            null,
            null,
            null,
            "$COLUNA_DIA_MES ASC, $COLUNA_DESCRICAO COLLATE NOCASE ASC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = cursor.getColumnIndexOrThrow(COLUNA_TIPO)
            val descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valorIndex = cursor.getColumnIndexOrThrow(COLUNA_VALOR_CENTAVOS)
            val diaIndex = cursor.getColumnIndexOrThrow(COLUNA_DIA_MES)
            val inicioIndex = cursor.getColumnIndexOrThrow(COLUNA_INICIO_MES)
            val ativaIndex = cursor.getColumnIndexOrThrow(COLUNA_ATIVA)
            val categoriaIndex = cursor.getColumnIndexOrThrow(COLUNA_CATEGORIA)

            while (cursor.moveToNext()) {
                resultado.add(
                    Recorrencia(
                        id = cursor.getLong(idIndex),
                        tipo = TipoMovimentacao.valueOf(cursor.getString(tipoIndex)),
                        descricao = cursor.getString(descricaoIndex),
                        valorCentavos = cursor.getLong(valorIndex),
                        diaMes = cursor.getInt(diaIndex),
                        inicioMes = YearMonth.parse(cursor.getString(inicioIndex)),
                        ativa = cursor.getInt(ativaIndex) == 1,
                        categoria = cursor.getString(categoriaIndex)
                    )
                )
            }
        }

        return resultado
    }

    fun atualizarRecorrencia(
        id: Long,
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long,
        diaMes: Int,
        categoria: String
    ): Boolean {
        val db = writableDatabase
        db.beginTransaction()

        return try {
            val values = ContentValues().apply {
                put(COLUNA_TIPO, tipo.name)
                put(COLUNA_DESCRICAO, descricao)
                put(COLUNA_VALOR_CENTAVOS, valorCentavos)
                put(COLUNA_DIA_MES, diaMes)
                put(COLUNA_CATEGORIA, categoria)
            }

            val atualizou = db.update(
                TABELA_RECORRENCIAS,
                values,
                "$COLUNA_ID = ?",
                arrayOf(id.toString())
            ) > 0

            if (atualizou) {
                excluirProjecoesPendentes(db, id)
                db.setTransactionSuccessful()
            }

            atualizou
        } finally {
            db.endTransaction()
        }
    }

    fun desativarRecorrencia(id: Long): Boolean {
        val db = writableDatabase
        db.beginTransaction()

        return try {
            val values = ContentValues().apply {
                put(COLUNA_ATIVA, 0)
            }

            val desativou = db.update(
                TABELA_RECORRENCIAS,
                values,
                "$COLUNA_ID = ?",
                arrayOf(id.toString())
            ) > 0

            if (desativou) {
                excluirProjecoesPendentes(db, id)
                db.setTransactionSuccessful()
            }

            desativou
        } finally {
            db.endTransaction()
        }
    }

    private fun excluirProjecoesPendentes(
        db: SQLiteDatabase,
        recorrenciaId: Long
    ) {
        val inicioMesAtual = YearMonth.now()
            .atDay(1)
            .atStartOfDay()
            .toString()

        db.delete(
            TABELA_MOVIMENTACOES,
            "$COLUNA_RECORRENCIA_ID = ? AND $COLUNA_STATUS = ? AND $COLUNA_DATA >= ?",
            arrayOf(
                recorrenciaId.toString(),
                StatusMovimentacao.PENDENTE.name,
                inicioMesAtual
            )
        )
    }

    fun garantirRecorrenciasParaMes(mes: YearMonth) {
        val recorrencias = listarRecorrenciasAtivas()
            .filter { !mes.isBefore(it.inicioMes) }

        if (recorrencias.isEmpty()) return

        val db = writableDatabase
        db.beginTransaction()
        try {
            recorrencias.forEach { recorrencia ->
                val diaReal = minOf(recorrencia.diaMes, mes.lengthOfMonth())
                val data = mes.atDay(diaReal).atTime(12, 0)

                val values = ContentValues().apply {
                    put(COLUNA_TIPO, recorrencia.tipo.name)
                    put(COLUNA_DESCRICAO, recorrencia.descricao)
                    put(COLUNA_VALOR_CENTAVOS, recorrencia.valorCentavos)
                    put(COLUNA_DATA, data.toString())
                    put(COLUNA_RECORRENCIA_ID, recorrencia.id)
                    put(COLUNA_COMPETENCIA, mes.toString())
                    put(COLUNA_STATUS, StatusMovimentacao.PENDENTE.name)
                    put(COLUNA_CATEGORIA, recorrencia.categoria)
                }

                db.insertWithOnConflict(
                    TABELA_MOVIMENTACOES,
                    null,
                    values,
                    SQLiteDatabase.CONFLICT_IGNORE
                )
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun listarPorMes(mes: YearMonth): List<Movimentacao> {
        val inicio = mes.atDay(1).atStartOfDay()
        val fimExclusivo = mes.plusMonths(1).atDay(1).atStartOfDay()

        return consultar(
            selection = "$COLUNA_DATA >= ? AND $COLUNA_DATA < ?",
            selectionArgs = arrayOf(inicio.toString(), fimExclusivo.toString())
        )
    }

    fun criarBackupJson(carregarSaldo: Boolean): String {
        val root = JSONObject().apply {
            put("formato", BACKUP_FORMAT_VERSION)
            put("criadoEm", LocalDateTime.now().toString())
            put("carregarSaldo", carregarSaldo)
        }

        val recorrencias = JSONArray()
        readableDatabase.query(
            TABELA_RECORRENCIAS,
            arrayOf(
                COLUNA_ID,
                COLUNA_TIPO,
                COLUNA_DESCRICAO,
                COLUNA_VALOR_CENTAVOS,
                COLUNA_DIA_MES,
                COLUNA_INICIO_MES,
                COLUNA_ATIVA,
                COLUNA_CATEGORIA
            ),
            null,
            null,
            null,
            null,
            "$COLUNA_ID ASC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = cursor.getColumnIndexOrThrow(COLUNA_TIPO)
            val descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valorIndex = cursor.getColumnIndexOrThrow(COLUNA_VALOR_CENTAVOS)
            val diaIndex = cursor.getColumnIndexOrThrow(COLUNA_DIA_MES)
            val inicioIndex = cursor.getColumnIndexOrThrow(COLUNA_INICIO_MES)
            val ativaIndex = cursor.getColumnIndexOrThrow(COLUNA_ATIVA)
            val categoriaIndex = cursor.getColumnIndexOrThrow(COLUNA_CATEGORIA)

            while (cursor.moveToNext()) {
                recorrencias.put(
                    JSONObject().apply {
                        put("id", cursor.getLong(idIndex))
                        put("tipo", cursor.getString(tipoIndex))
                        put("descricao", cursor.getString(descricaoIndex))
                        put("valorCentavos", cursor.getLong(valorIndex))
                        put("diaMes", cursor.getInt(diaIndex))
                        put("inicioMes", cursor.getString(inicioIndex))
                        put("ativa", cursor.getInt(ativaIndex) == 1)
                        put("categoria", cursor.getString(categoriaIndex))
                    }
                )
            }
        }

        val movimentacoes = JSONArray()
        readableDatabase.query(
            TABELA_MOVIMENTACOES,
            arrayOf(
                COLUNA_ID,
                COLUNA_TIPO,
                COLUNA_DESCRICAO,
                COLUNA_VALOR_CENTAVOS,
                COLUNA_DATA,
                COLUNA_RECORRENCIA_ID,
                COLUNA_COMPETENCIA,
                COLUNA_STATUS,
                COLUNA_CATEGORIA,
                COLUNA_PARCELAMENTO_ID,
                COLUNA_PARCELA_NUMERO,
                COLUNA_PARCELAS_TOTAL
            ),
            null,
            null,
            null,
            null,
            "$COLUNA_ID ASC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = cursor.getColumnIndexOrThrow(COLUNA_TIPO)
            val descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valorIndex = cursor.getColumnIndexOrThrow(COLUNA_VALOR_CENTAVOS)
            val dataIndex = cursor.getColumnIndexOrThrow(COLUNA_DATA)
            val recorrenciaIndex = cursor.getColumnIndexOrThrow(COLUNA_RECORRENCIA_ID)
            val competenciaIndex = cursor.getColumnIndexOrThrow(COLUNA_COMPETENCIA)
            val statusIndex = cursor.getColumnIndexOrThrow(COLUNA_STATUS)
            val categoriaIndex = cursor.getColumnIndexOrThrow(COLUNA_CATEGORIA)
            val parcelamentoIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELAMENTO_ID)
            val parcelaNumeroIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELA_NUMERO)
            val parcelasTotalIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELAS_TOTAL)

            while (cursor.moveToNext()) {
                movimentacoes.put(
                    JSONObject().apply {
                        put("id", cursor.getLong(idIndex))
                        put("tipo", cursor.getString(tipoIndex))
                        put("descricao", cursor.getString(descricaoIndex))
                        put("valorCentavos", cursor.getLong(valorIndex))
                        put("data", cursor.getString(dataIndex))
                        put(
                            "recorrenciaId",
                            if (cursor.isNull(recorrenciaIndex)) {
                                JSONObject.NULL
                            } else {
                                cursor.getLong(recorrenciaIndex)
                            }
                        )
                        put(
                            "competencia",
                            if (cursor.isNull(competenciaIndex)) {
                                JSONObject.NULL
                            } else {
                                cursor.getString(competenciaIndex)
                            }
                        )
                        put("status", cursor.getString(statusIndex))
                        put("categoria", cursor.getString(categoriaIndex))
                        put(
                            "parcelamentoId",
                            if (cursor.isNull(parcelamentoIndex)) {
                                JSONObject.NULL
                            } else {
                                cursor.getString(parcelamentoIndex)
                            }
                        )
                        put(
                            "parcelaNumero",
                            if (cursor.isNull(parcelaNumeroIndex)) {
                                JSONObject.NULL
                            } else {
                                cursor.getInt(parcelaNumeroIndex)
                            }
                        )
                        put(
                            "parcelasTotal",
                            if (cursor.isNull(parcelasTotalIndex)) {
                                JSONObject.NULL
                            } else {
                                cursor.getInt(parcelasTotalIndex)
                            }
                        )
                    }
                )
            }
        }

        root.put("recorrencias", recorrencias)
        root.put("movimentacoes", movimentacoes)
        return root.toString(2)
    }

    fun restaurarBackupJson(conteudo: String): Boolean {
        val root = JSONObject(conteudo)
        val formato = root.getInt("formato")
        require(formato in 1..BACKUP_FORMAT_VERSION) {
            "Formato de backup não suportado."
        }

        val recorrencias = root.getJSONArray("recorrencias")
        val movimentacoes = root.getJSONArray("movimentacoes")
        val carregarSaldo = root.optBoolean("carregarSaldo", true)

        val db = writableDatabase
        db.beginTransaction()

        try {
            db.delete(TABELA_MOVIMENTACOES, null, null)
            db.delete(TABELA_RECORRENCIAS, null, null)

            for (index in 0 until recorrencias.length()) {
                val item = recorrencias.getJSONObject(index)
                val values = ContentValues().apply {
                    put(COLUNA_ID, item.getLong("id"))
                    put(COLUNA_TIPO, item.getString("tipo"))
                    put(COLUNA_DESCRICAO, item.getString("descricao"))
                    put(COLUNA_VALOR_CENTAVOS, item.getLong("valorCentavos"))
                    put(COLUNA_DIA_MES, item.getInt("diaMes"))
                    put(COLUNA_INICIO_MES, item.getString("inicioMes"))
                    put(COLUNA_ATIVA, if (item.getBoolean("ativa")) 1 else 0)
                    put(COLUNA_CATEGORIA, item.optString("categoria", "Outros"))
                }

                db.insertOrThrow(TABELA_RECORRENCIAS, null, values)
            }

            for (index in 0 until movimentacoes.length()) {
                val item = movimentacoes.getJSONObject(index)
                val values = ContentValues().apply {
                    put(COLUNA_ID, item.getLong("id"))
                    put(COLUNA_TIPO, item.getString("tipo"))
                    put(COLUNA_DESCRICAO, item.getString("descricao"))
                    put(COLUNA_VALOR_CENTAVOS, item.getLong("valorCentavos"))
                    put(COLUNA_DATA, item.getString("data"))

                    if (item.isNull("recorrenciaId")) {
                        putNull(COLUNA_RECORRENCIA_ID)
                    } else {
                        put(COLUNA_RECORRENCIA_ID, item.getLong("recorrenciaId"))
                    }

                    if (item.isNull("competencia")) {
                        putNull(COLUNA_COMPETENCIA)
                    } else {
                        put(COLUNA_COMPETENCIA, item.getString("competencia"))
                    }

                    put(
                        COLUNA_STATUS,
                        item.optString(
                            "status",
                            StatusMovimentacao.REALIZADO.name
                        )
                    )
                    put(COLUNA_CATEGORIA, item.optString("categoria", "Outros"))

                    if (item.has("parcelamentoId") && !item.isNull("parcelamentoId")) {
                        put(COLUNA_PARCELAMENTO_ID, item.getString("parcelamentoId"))
                    } else {
                        putNull(COLUNA_PARCELAMENTO_ID)
                    }

                    if (item.has("parcelaNumero") && !item.isNull("parcelaNumero")) {
                        put(COLUNA_PARCELA_NUMERO, item.getInt("parcelaNumero"))
                    } else {
                        putNull(COLUNA_PARCELA_NUMERO)
                    }

                    if (item.has("parcelasTotal") && !item.isNull("parcelasTotal")) {
                        put(COLUNA_PARCELAS_TOTAL, item.getInt("parcelasTotal"))
                    } else {
                        putNull(COLUNA_PARCELAS_TOTAL)
                    }
                }

                db.insertOrThrow(TABELA_MOVIMENTACOES, null, values)
            }

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }

        return carregarSaldo
    }

    fun saldoRealizadoAntesDoMes(mes: YearMonth): Long {
        val limite = mes.atDay(1).atStartOfDay().toString()

        val sql = """
            SELECT COALESCE(
                SUM(
                    CASE
                        WHEN $COLUNA_TIPO = 'GANHO' THEN $COLUNA_VALOR_CENTAVOS
                        ELSE -$COLUNA_VALOR_CENTAVOS
                    END
                ),
                0
            )
            FROM $TABELA_MOVIMENTACOES
            WHERE $COLUNA_DATA < ?
              AND $COLUNA_STATUS = 'REALIZADO'
        """.trimIndent()

        readableDatabase.rawQuery(sql, arrayOf(limite)).use { cursor ->
            return if (cursor.moveToFirst()) cursor.getLong(0) else 0L
        }
    }

    private fun consultar(
        selection: String?,
        selectionArgs: Array<String>?
    ): List<Movimentacao> {
        val resultado = mutableListOf<Movimentacao>()

        readableDatabase.query(
            TABELA_MOVIMENTACOES,
            arrayOf(
                COLUNA_ID,
                COLUNA_TIPO,
                COLUNA_DESCRICAO,
                COLUNA_VALOR_CENTAVOS,
                COLUNA_DATA,
                COLUNA_RECORRENCIA_ID,
                COLUNA_STATUS,
                COLUNA_CATEGORIA,
                COLUNA_PARCELAMENTO_ID,
                COLUNA_PARCELA_NUMERO,
                COLUNA_PARCELAS_TOTAL
            ),
            selection,
            selectionArgs,
            null,
            null,
            "$COLUNA_DATA DESC, $COLUNA_ID DESC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = cursor.getColumnIndexOrThrow(COLUNA_TIPO)
            val descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valorIndex = cursor.getColumnIndexOrThrow(COLUNA_VALOR_CENTAVOS)
            val dataIndex = cursor.getColumnIndexOrThrow(COLUNA_DATA)
            val recorrenciaIndex = cursor.getColumnIndexOrThrow(COLUNA_RECORRENCIA_ID)
            val statusIndex = cursor.getColumnIndexOrThrow(COLUNA_STATUS)
            val categoriaIndex = cursor.getColumnIndexOrThrow(COLUNA_CATEGORIA)
            val parcelamentoIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELAMENTO_ID)
            val parcelaNumeroIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELA_NUMERO)
            val parcelasTotalIndex =
                cursor.getColumnIndexOrThrow(COLUNA_PARCELAS_TOTAL)

            while (cursor.moveToNext()) {
                val recorrenciaId = if (cursor.isNull(recorrenciaIndex)) {
                    null
                } else {
                    cursor.getLong(recorrenciaIndex)
                }
                val parcelamentoId = if (cursor.isNull(parcelamentoIndex)) {
                    null
                } else {
                    cursor.getString(parcelamentoIndex)
                }
                val parcelaNumero = if (cursor.isNull(parcelaNumeroIndex)) {
                    null
                } else {
                    cursor.getInt(parcelaNumeroIndex)
                }
                val parcelasTotal = if (cursor.isNull(parcelasTotalIndex)) {
                    null
                } else {
                    cursor.getInt(parcelasTotalIndex)
                }

                resultado.add(
                    Movimentacao(
                        id = cursor.getLong(idIndex),
                        tipo = TipoMovimentacao.valueOf(cursor.getString(tipoIndex)),
                        descricao = cursor.getString(descricaoIndex),
                        valorCentavos = cursor.getLong(valorIndex),
                        data = LocalDateTime.parse(cursor.getString(dataIndex)),
                        recorrenciaId = recorrenciaId,
                        status = StatusMovimentacao.valueOf(cursor.getString(statusIndex)),
                        categoria = cursor.getString(categoriaIndex),
                        parcelamentoId = parcelamentoId,
                        parcelaNumero = parcelaNumero,
                        parcelasTotal = parcelasTotal
                    )
                )
            }
        }

        return resultado
    }

    companion object {
        private const val DATABASE_NAME = "entrou_saiu.db"
        private const val DATABASE_VERSION = 5
        private const val BACKUP_FORMAT_VERSION = 2

        private const val TABELA_MOVIMENTACOES = "movimentacoes"
        private const val TABELA_RECORRENCIAS = "recorrencias"

        private const val COLUNA_ID = "id"
        private const val COLUNA_TIPO = "tipo"
        private const val COLUNA_DESCRICAO = "descricao"
        private const val COLUNA_VALOR_CENTAVOS = "valor_centavos"
        private const val COLUNA_DATA = "data"
        private const val COLUNA_RECORRENCIA_ID = "recorrencia_id"
        private const val COLUNA_COMPETENCIA = "competencia"
        private const val COLUNA_STATUS = "status"
        private const val COLUNA_CATEGORIA = "categoria"
        private const val COLUNA_PARCELAMENTO_ID = "parcelamento_id"
        private const val COLUNA_PARCELA_NUMERO = "parcela_numero"
        private const val COLUNA_PARCELAS_TOTAL = "parcelas_total"
        private const val COLUNA_DIA_MES = "dia_mes"
        private const val COLUNA_INICIO_MES = "inicio_mes"
        private const val COLUNA_ATIVA = "ativa"
    }
}
