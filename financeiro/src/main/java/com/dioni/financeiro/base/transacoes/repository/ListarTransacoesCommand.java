package com.dioni.financeiro.base.transacoes.repository;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.dto.TransacaoDTO;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@AllArgsConstructor
public class ListarTransacoesCommand {

    private static final int JANELA_MESES = 12;

    private final TransacaoRepository transacaoRepository;

    public Page<TransacaoDTO> executar(Pageable pageable, Categoria categoria, TipoTransacao tipo) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        YearMonth atual = YearMonth.now();
        var inicio = atual.minusMonths(JANELA_MESES - 1).atDay(1);
        var fim = atual.atEndOfMonth();

        return transacaoRepository.findByPeriodo(inicio, fim, usuario, categoria, tipo, pageable)
                .map(TransacaoDTO::from);
    }
}