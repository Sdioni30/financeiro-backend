package com.dioni.financeiro.base;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import com.dioni.financeiro.base.transacoes.repository.ExportarRelatorioHistoricoCommand;
import com.dioni.financeiro.base.transacoes.repository.RelatorioExcelBuilder;
import com.dioni.financeiro.base.transacoes.repository.TransacaoQuery;
import com.dioni.financeiro.support.TestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ExportarRelatorioHistoricoCommandTest extends TestSupport {

    @Mock
    private TransacaoQuery transacaoQuery;

    private ExportarRelatorioHistoricoCommand exportarRelatorioHistoricoCommand;

    @Override
    public void init() {
        exportarRelatorioHistoricoCommand = new ExportarRelatorioHistoricoCommand(transacaoQuery, new RelatorioExcelBuilder());

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void should_generate_excel_for_a_past_month_within_window() {
        YearMonth passado = YearMonth.now().minusMonths(3);
        int mes = passado.getMonthValue();
        int ano = passado.getYear();

        Transacao t = new Transacao();
        t.setCategoria(Categoria.PESSOAL);
        t.setTipo(TipoTransacao.ENTRADA);
        t.setValor(300.0);
        t.setData(LocalDate.of(ano, mes, 5));
        t.setDescricao("Teste histórico");

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of(t));

        ResponseEntity<?> resultado = exportarRelatorioHistoricoCommand.executar(Categoria.PESSOAL, null, mes, ano);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isInstanceOf(byte[].class);
        assertThat((byte[]) resultado.getBody()).isNotEmpty();

        InOrder inOrder = this.inOrder(transacaoQuery);
        inOrder.verify(transacaoQuery).filtrarPorMes(mes, ano, 1L);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_reject_month_outside_twelve_month_window() {
        YearMonth foraDaJanela = YearMonth.now().minusMonths(12);

        ResponseEntity<?> resultado = exportarRelatorioHistoricoCommand.executar(
                Categoria.PESSOAL, null, foraDaJanela.getMonthValue(), foraDaJanela.getYear());

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verifyNoInteractions(transacaoQuery);
    }
}
