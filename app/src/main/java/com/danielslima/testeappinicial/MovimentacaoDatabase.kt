package com.danielslima.testeappinicial

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.time.LocalDateTime

class MovimentacaoDatabase(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE movimentacoes (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                tipo TEXT NOT NULL,
                descricao TEXT NOT NULL,
                valor_centavos INTEGER NOT NULL CHECK (valor_centavos > 0),
                data TEXT NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // A primeira versão do banco não possui migrações ainda.
    }

    fun inserir(
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long,
        data: LocalDateTime = LocalDateTime.now()
    ): Movimentacao {
        val values = ContentValues().apply {
            put(COLUNA_TIPO, tipo.name)
            put(COLUNA_DESCRICAO, descricao)
            put(COLUNA_VALOR_CENTAVOS, valorCentavos)
            put(COLUNA_DATA, data.toString())
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
            data = data
        )
    }

    fun atualizar(
        id: Long,
        tipo: TipoMovimentacao,
        descricao: String,
        valorCentavos: Long
    ): Boolean {
        val values = ContentValues().apply {
            put(COLUNA_TIPO, tipo.name)
            put(COLUNA_DESCRICAO, descricao)
            put(COLUNA_VALOR_CENTAVOS, valorCentavos)
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

    fun listarTodas(): List<Movimentacao> {
        val resultado = mutableListOf<Movimentacao>()

        readableDatabase.query(
            TABELA_MOVIMENTACOES,
            arrayOf(
                COLUNA_ID,
                COLUNA_TIPO,
                COLUNA_DESCRICAO,
                COLUNA_VALOR_CENTAVOS,
                COLUNA_DATA
            ),
            null,
            null,
            null,
            null,
            "$COLUNA_DATA DESC, $COLUNA_ID DESC"
        ).use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(COLUNA_ID)
            val tipoIndex = cursor.getColumnIndexOrThrow(COLUNA_TIPO)
            val descricaoIndex = cursor.getColumnIndexOrThrow(COLUNA_DESCRICAO)
            val valorIndex = cursor.getColumnIndexOrThrow(COLUNA_VALOR_CENTAVOS)
            val dataIndex = cursor.getColumnIndexOrThrow(COLUNA_DATA)

            while (cursor.moveToNext()) {
                val tipo = TipoMovimentacao.valueOf(cursor.getString(tipoIndex))
                val data = LocalDateTime.parse(cursor.getString(dataIndex))

                resultado.add(
                    Movimentacao(
                        id = cursor.getLong(idIndex),
                        tipo = tipo,
                        descricao = cursor.getString(descricaoIndex),
                        valorCentavos = cursor.getLong(valorIndex),
                        data = data
                    )
                )
            }
        }

        return resultado
    }

    companion object {
        private const val DATABASE_NAME = "entrou_saiu.db"
        private const val DATABASE_VERSION = 1

        private const val TABELA_MOVIMENTACOES = "movimentacoes"
        private const val COLUNA_ID = "id"
        private const val COLUNA_TIPO = "tipo"
        private const val COLUNA_DESCRICAO = "descricao"
        private const val COLUNA_VALOR_CENTAVOS = "valor_centavos"
        private const val COLUNA_DATA = "data"
    }
}
