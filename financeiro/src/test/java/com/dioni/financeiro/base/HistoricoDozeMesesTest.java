package com.dioni.financeiro.base;

import com.dioni.financeiro.base.transacoes.repository.HistoricoDozeMeses;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

class HistoricoDozeMesesTest {

    @Test
    void should_accept_current_month() {
        YearMonth atual = YearMonth.now();
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(atual.getMonthValue(), atual.getYear())).isTrue();
    }

    @Test
    void should_accept_eleven_months_ago() {
        YearMonth limite = YearMonth.now().minusMonths(11);
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(limite.getMonthValue(), limite.getYear())).isTrue();
    }

    @Test
    void should_reject_twelve_months_ago() {
        YearMonth foraDaJanela = YearMonth.now().minusMonths(12);
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(foraDaJanela.getMonthValue(), foraDaJanela.getYear())).isFalse();
    }

    @Test
    void should_reject_future_month() {
        YearMonth futuro = YearMonth.now().plusMonths(1);
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(futuro.getMonthValue(), futuro.getYear())).isFalse();
    }

    @Test
    void should_reject_invalid_month_without_throwing() {
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(13, YearMonth.now().getYear())).isFalse();
        assertThat(HistoricoDozeMeses.verificarUltimosDozeMeses(0, YearMonth.now().getYear())).isFalse();
    }
}
