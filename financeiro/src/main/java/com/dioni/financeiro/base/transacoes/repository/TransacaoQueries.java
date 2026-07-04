package com.dioni.financeiro.base.transacoes.repository;

public class TransacaoQueries {

    public static final String FILTRAR_TRANSACAO_POR_MES = """
            SELECT * FROM transacao
            WHERE MONTH(data) = ?
            AND YEAR(data) = ?
            AND usuario_id = ?
            """;

    public static final String FILTRAR_TRANSACAO_POR_PERIODO_PAGINADO =
            "SELECT t FROM Transacao t WHERE t.data BETWEEN :inicio AND :fim AND t.usuario = :usuario "
                    + "AND t.categoria = :categoria AND (:tipo IS NULL OR t.tipo = :tipo)";
}
