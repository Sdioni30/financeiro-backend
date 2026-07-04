package com.dioni.financeiro.base;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import com.dioni.financeiro.base.transacoes.repository.CalcularSaldoCommand;
import com.dioni.financeiro.base.transacoes.repository.TransacaoQuery;
import com.dioni.financeiro.base.transacoes.repository.TransacaoRepository;
import com.dioni.financeiro.support.TestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CalcularSaldoCommandTest extends TestSupport {

    @Mock
    private TransacaoRepository transacaoRepository;

    @Mock
    private TransacaoQuery transacaoQuery;

    private CalcularSaldoCommand calcularSaldoCommand;

    @Override
    public void init() {
        calcularSaldoCommand = new CalcularSaldoCommand(transacaoRepository, transacaoQuery);

        Usuario usuario = new Usuario();
        usuario.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void should_calculate_correct_balance_for_current_month() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        Transacao t1 = new Transacao();
        t1.setCategoria(Categoria.PROFISSIONAL);
        t1.setTipo(TipoTransacao.ENTRADA);
        t1.setValor(150.0);

        Transacao t2 = new Transacao();
        t2.setCategoria(Categoria.PROFISSIONAL);
        t2.setTipo(TipoTransacao.SAIDA);
        t2.setValor(50.0);

        Transacao t3 = new Transacao();
        t3.setCategoria(Categoria.PESSOAL);
        t3.setTipo(TipoTransacao.ENTRADA);
        t3.setValor(500.0);

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of(t1, t2, t3));

        Double saldoResult = calcularSaldoCommand.executar(Categoria.PROFISSIONAL, mes, ano);

        assertThat(saldoResult).isEqualTo(100.0);

        InOrder inOrder = this.inOrder(transacaoQuery);
        inOrder.verify(transacaoQuery).filtrarPorMes(mes, ano, 1L);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_return_zero_when_no_transactions_found_for_category() {
        int mes = LocalDate.now().getMonthValue();
        int ano = LocalDate.now().getYear();

        when(transacaoQuery.filtrarPorMes(mes, ano, 1L)).thenReturn(List.of());

        Double saldoResult = calcularSaldoCommand.executar(Categoria.PESSOAL, mes, ano);

        assertThat(saldoResult).isEqualTo(0.0);

        InOrder inOrder = this.inOrder(transacaoQuery);
        inOrder.verify(transacaoQuery).filtrarPorMes(mes, ano, 1L);
        inOrder.verifyNoMoreInteractions();
    }
}
