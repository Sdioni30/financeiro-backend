package com.dioni.financeiro.base.transacoes.web;

import com.dioni.financeiro.base.dto.TransacaoDTO;
import com.dioni.financeiro.base.dto.TransacaoRequest;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.exceptions.web.ErrorResponse;
import com.dioni.financeiro.base.transacoes.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@AllArgsConstructor
@RestController
@RequestMapping("/api/transacoes")
public class TransacaoController {

    private final CalcularSaldoCommand calcularSaldoCommand;
    private final ExportarRelatorioCommand exportarRelatorioCommand;
    private final ExportarRelatorioHistoricoCommand exportarRelatorioHistoricoCommand;
    private final DeletarTransacaoCommand deletarTransacaoCommand;
    private final ListarTransacoesCommand listarTransacoesCommand;
    private final CriarTransacaoCommand criarTransacaoCommand;

    @PostMapping
    public ResponseEntity<TransacaoDTO> criar(@RequestBody TransacaoRequest request) {
        return ResponseEntity.ok(criarTransacaoCommand.executar(request));
    }

    @GetMapping("/listar-ultimos-doze-meses")
    public ResponseEntity<?> listarUltimosDozeMeses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam Categoria categoria,
            @RequestParam(required = false) TipoTransacao tipo) {
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "data", "id"));
        return ResponseEntity.ok(listarTransacoesCommand.executar(pageable, categoria, tipo));
    }

    @GetMapping("/saldo/{categoria}")
    public ResponseEntity<?> obterSaldo(@PathVariable Categoria categoria,
                                         @RequestParam(required = false) Integer mes,
                                         @RequestParam(required = false) Integer ano) {
        int mesResolvido = mes != null ? mes : LocalDate.now().getMonthValue();
        int anoResolvido = ano != null ? ano : LocalDate.now().getYear();

        if (!HistoricoDozeMeses.verificarUltimosDozeMeses(mesResolvido, anoResolvido)) {
            return ResponseEntity.badRequest().body(new ErrorResponse(HistoricoDozeMeses.MENSAGEM_ERRO));
        }

        return ResponseEntity.ok(calcularSaldoCommand.executar(categoria, mesResolvido, anoResolvido));
    }

    @GetMapping("/download/relatorio/{categoria}")
    public ResponseEntity<byte[]> baixarRelatorio(@PathVariable Categoria categoria,
                                                  @RequestParam(required = false) TipoTransacao tipo) {
        return exportarRelatorioCommand.executar(categoria, tipo);
    }

    @GetMapping("/download/relatorio/historico-por-mes/{categoria}")
    public ResponseEntity<?> baixarRelatorioHistoricoPorMes(@PathVariable Categoria categoria,
                                                             @RequestParam int mes,
                                                             @RequestParam int ano,
                                                             @RequestParam(required = false) TipoTransacao tipo) {
        return exportarRelatorioHistoricoCommand.executar(categoria, tipo, mes, ano);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        return deletarTransacaoCommand.executar(id);
    }
}
