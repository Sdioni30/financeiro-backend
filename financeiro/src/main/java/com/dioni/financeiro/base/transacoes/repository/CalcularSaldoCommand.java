package com.dioni.financeiro.base.transacoes.repository;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CalcularSaldoCommand {

    private final TransacaoRepository transacaoRepository;
    private final TransacaoQuery transacaoQuery;

    public Double executar(Categoria categoria, int mes, int ano) {
        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return transacaoQuery.filtrarPorMes(mes, ano, usuario.getId()).stream()
                .filter(t -> t.getCategoria().equals(categoria))
                .mapToDouble(t -> t.getTipo().equals(TipoTransacao.ENTRADA) ? t.getValor() : -t.getValor())
                .sum();
    }
}
