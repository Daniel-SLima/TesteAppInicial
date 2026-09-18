package com.danielslima.testeappinicial

import java.time.YearMonth

data class Recorrencia(
    val id: Long,
    val tipo: TipoMovimentacao,
    val descricao: String,
    val valorCentavos: Long,
    val diaMes: Int,
    val inicioMes: YearMonth,
    val ativa: Boolean
)
