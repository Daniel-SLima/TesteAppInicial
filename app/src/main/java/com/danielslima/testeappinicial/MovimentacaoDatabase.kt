package com.danielslima.testeappinicial

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime
import java.time.YearMonth

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
                $COLUNA_CATEGORIA TEXT NOT NULL DEFAULT 'Outros'
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
                COLUNA_CATEGORIA
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

            while (cursor.moveToNext()) {
                val recorrenciaId = if (cursor.isNull(recorrenciaIndex)) {
                    null
                } else {
                    cursor.getLong(recorrenciaIndex)
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
                        categoria = cursor.getString(categoriaIndex)
                    )
                )
            }
        }

        return resultado
    }

    companion object {
        private const val DATABASE_NAME = "entrou_saiu.db"
        private const val DATABASE_VERSION = 4

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
        private const val COLUNA_DIA_MES = "dia_mes"
        private const val COLUNA_INICIO_MES = "inicio_mes"
        private const val COLUNA_ATIVA = "ativa"
    }
}
