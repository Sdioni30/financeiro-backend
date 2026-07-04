package com.dioni.financeiro.base;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import com.dioni.financeiro.base.transacoes.repository.ExportarRelatorioCommand;
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
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExportarRelatorioCommandTest extends TestSupport {

    @Mock
    private TransacaoQuery transacaoQuery;

    private ExportarRelatorioCommand exportarRelatorioCommand;

    @Override
    public void init() {
        exportarRelatorioCommand = new ExportarRelatorioCommand(transacaoQuery, new RelatorioExcelBuilder());

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    private Transacao criarTransacao(Categoria categoria, TipoTransacao tipo, Double valor) {
        Transacao t = new Transacao();
        t.setCategoria(categoria);
        t.setTipo(tipo);
        t.setValor(valor);
        t.setData(LocalDate.now());
        t.setDescricao("Teste");
        return t;
    }

    @Test
    void should_generate_excel_for_current_month() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of(
                criarTransacao(Categoria.PESSOAL, TipoTransacao.ENTRADA, 200.0),
                criarTransacao(Categoria.PESSOAL, TipoTransacao.SAIDA, 50.0),
                criarTransacao(Categoria.PROFISSIONAL, TipoTransacao.ENTRADA, 500.0)
        ));

        ResponseEntity<byte[]> resultado = exportarRelatorioCommand.executar(Categoria.PESSOAL, null);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotEmpty();

        InOrder inOrder = this.inOrder(transacaoQuery);
        inOrder.verify(transacaoQuery).filtrarPorMes(mes, ano, 1L);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_return_empty_excel_when_no_transactions_for_category() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of());

        ResponseEntity<byte[]> resultado = exportarRelatorioCommand.executar(Categoria.PROFISSIONAL, null);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotEmpty();

        InOrder inOrder = this.inOrder(transacaoQuery);
        inOrder.verify(transacaoQuery).filtrarPorMes(mes, ano, 1L);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_filter_only_entradas_when_tipo_is_entrada() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of(
                criarTransacao(Categoria.PESSOAL, TipoTransacao.ENTRADA, 200.0),
                criarTransacao(Categoria.PESSOAL, TipoTransacao.SAIDA, 50.0)
        ));

        ResponseEntity<byte[]> resultado = exportarRelatorioCommand.executar(Categoria.PESSOAL, TipoTransacao.ENTRADA);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotEmpty();
    }

    @Test
    void should_filter_only_saidas_when_tipo_is_saida() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of(
                criarTransacao(Categoria.PESSOAL, TipoTransacao.ENTRADA, 200.0),
                criarTransacao(Categoria.PESSOAL, TipoTransacao.SAIDA, 50.0)
        ));

        ResponseEntity<byte[]> resultado = exportarRelatorioCommand.executar(Categoria.PESSOAL, TipoTransacao.SAIDA);

        assertThat(resultado.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resultado.getBody()).isNotEmpty();
    }
}
