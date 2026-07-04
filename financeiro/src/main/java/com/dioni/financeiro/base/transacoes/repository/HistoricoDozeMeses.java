package com.dioni.financeiro.base.transacoes.repository;

import java.time.DateTimeException;
import java.time.YearMonth;

public final class HistoricoDozeMeses {

    public static final String MENSAGEM_ERRO = "Só é possível consultar os últimos 12 meses (incluindo o mês atual).";

    private HistoricoDozeMeses() {
    }

    public static boolean verificarUltimosDozeMeses(int mes, int ano) {
        try {
            YearMonth informado = YearMonth.of(ano, mes);
            YearMonth atual = YearMonth.now();
            return !informado.isBefore(atual.minusMonths(11)) && !informado.isAfter(atual);
        } catch (DateTimeException e) {
            return false;
        }
    }
}
