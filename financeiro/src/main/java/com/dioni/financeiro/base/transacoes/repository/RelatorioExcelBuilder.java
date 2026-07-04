package com.dioni.financeiro.base.transacoes.repository;

import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class RelatorioExcelBuilder {

    private static final MediaType XLSX = MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

    public ResponseEntity<byte[]> gerar(List<Transacao> transacoes, Categoria categoria, TipoTransacao tipo, String sufixoArquivo) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Relatorio");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Data");
            header.createCell(1).setCellValue("Descrição do item");
            header.createCell(2).setCellValue("Valor");

            int rowIdx = 1;
            for (Transacao t : transacoes) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(t.getData().toString());
                row.createCell(1).setCellValue(t.getDescricao());
                row.createCell(2).setCellValue(t.getValor());
            }

            double saldo = transacoes.stream()
                    .mapToDouble(t -> t.getTipo().name().equals("ENTRADA") ? t.getValor() : -t.getValor())
                    .sum();

            sheet.createRow(rowIdx);
            Row saldoRow = sheet.createRow(rowIdx + 1);
            saldoRow.createCell(1).setCellValue("Saldo");
            saldoRow.createCell(2).setCellValue(saldo);

            workbook.write(out);

            String nomeArquivo = escolherNomeArquivo(categoria, tipo, sufixoArquivo);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(XLSX);
            headers.setContentDispositionFormData("attachment", nomeArquivo);

            return ResponseEntity.ok().headers(headers).body(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Erro", e);
        }
    }

    private String escolherNomeArquivo(Categoria categoria, TipoTransacao tipo, String sufixoArquivo) {
        if (tipo == TipoTransacao.ENTRADA) return "entradas_" + categoria + sufixoArquivo + ".xlsx";
        if (tipo == TipoTransacao.SAIDA) return "saidas_" + categoria + sufixoArquivo + ".xlsx";
        return "relatorio_" + categoria + sufixoArquivo + ".xlsx";
    }
}
