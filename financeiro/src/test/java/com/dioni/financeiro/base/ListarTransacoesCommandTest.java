package com.dioni.financeiro.base;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.dto.TransacaoDTO;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import com.dioni.financeiro.base.transacoes.repository.ListarTransacoesCommand;
import com.dioni.financeiro.base.transacoes.repository.TransacaoRepository;
import com.dioni.financeiro.support.TestSupport;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListarTransacoesCommandTest extends TestSupport {

    @Mock
    private TransacaoRepository transacaoRepository;

    private ListarTransacoesCommand listarTransacoesCommand;

    private Usuario usuario;

    @Override
    public void init() {
        listarTransacoesCommand = new ListarTransacoesCommand(transacaoRepository);

        usuario = new Usuario();
        usuario.setId(1L);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(usuario);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void should_list_transacoes_within_last_twelve_months_window() {
        Pageable pageable = PageRequest.of(0, 10);

        YearMonth atual = YearMonth.now();
        LocalDate inicioEsperado = atual.minusMonths(11).atDay(1);
        LocalDate fimEsperado = atual.atEndOfMonth();

        Transacao t = new Transacao();
        t.setCategoria(Categoria.PESSOAL);
        t.setTipo(TipoTransacao.SAIDA);
        t.setValor(1500.0);
        t.setData(LocalDate.of(atual.getYear(), atual.getMonthValue(), 10));
        t.setDescricao("Cartão de crédito");

        Page<Transacao> pagina = new PageImpl<>(List.of(t));
        when(transacaoRepository.findByPeriodo(inicioEsperado, fimEsperado, usuario, Categoria.PESSOAL, TipoTransacao.SAIDA, pageable))
                .thenReturn(pagina);

        Page<TransacaoDTO> resultado = listarTransacoesCommand.executar(pageable, Categoria.PESSOAL, TipoTransacao.SAIDA);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getDescricao()).isEqualTo("Cartão de crédito");

        InOrder inOrder = this.inOrder(transacaoRepository);
        inOrder.verify(transacaoRepository).findByPeriodo(inicioEsperado, fimEsperado, usuario, Categoria.PESSOAL, TipoTransacao.SAIDA, pageable);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void should_return_empty_page_when_no_transacoes_found_without_tipo_filter() {
        Pageable pageable = PageRequest.of(0, 10);

        when(transacaoRepository.findByPeriodo(any(), any(), any(), any(), any(), any()))
                .thenReturn(Page.empty());

        Page<TransacaoDTO> resultado = listarTransacoesCommand.executar(pageable, Categoria.PROFISSIONAL, null);

        assertThat(resultado.getContent()).isEmpty();
    }
}
