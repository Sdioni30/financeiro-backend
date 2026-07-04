package com.dioni.financeiro.base.transacoes.repository;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.exceptions.web.ErrorResponse;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ExportarRelatorioHistoricoCommand {

    private final TransacaoQuery transacaoQuery;
    private final RelatorioExcelBuilder relatorioExcelBuilder;

    public ResponseEntity<?> executar(Categoria categoria, TipoTransacao tipo, int mes, int ano) {
        if (!HistoricoDozeMeses.verificarUltimosDozeMeses(mes, ano)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HistoricoDozeMeses.MENSAGEM_ERRO));
        }

        Usuario usuario = (Usuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<Transacao> transacoes = transacaoQuery.filtrarPorMes(mes, ano, usuario.getId()).stream()
                .filter(t -> t.getCategoria().equals(categoria))
                .filter(t -> tipo == null || t.getTipo().equals(tipo))
                .toList();

        String sufixo = "_%02d_%d".formatted(mes, ano);
        return relatorioExcelBuilder.gerar(transacoes, categoria, tipo, sufixo);
    }
}
