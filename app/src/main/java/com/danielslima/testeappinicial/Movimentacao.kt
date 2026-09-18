package com.danielslima.testeappinicial

import java.time.LocalDateTime

enum class TipoMovimentacao {
    GASTO,
    GANHO
}

data class Movimentacao(
    val id: Long,
    val tipo: TipoMovimentacao,
    val descricao: String,
    val valorCentavos: Long,
    val data: LocalDateTime = LocalDateTime.now()
)
