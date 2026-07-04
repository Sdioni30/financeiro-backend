package com.dioni.financeiro.base.transacoes.repository;

import com.dioni.financeiro.base.auth.model.Usuario;
import com.dioni.financeiro.base.enums.Categoria;
import com.dioni.financeiro.base.enums.TipoTransacao;
import com.dioni.financeiro.base.transacoes.model.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

import static com.dioni.financeiro.base.transacoes.repository.TransacaoQueries.FILTRAR_TRANSACAO_POR_PERIODO_PAGINADO;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    @Query(value = FILTRAR_TRANSACAO_POR_PERIODO_PAGINADO)
    Page<Transacao> findByPeriodo(@Param("inicio") LocalDate inicio,
                                   @Param("fim") LocalDate fim,
                                   @Param("usuario") Usuario usuario,
                                   @Param("categoria") Categoria categoria,
                                   @Param("tipo") TipoTransacao tipo,
                                   Pageable pageable);

    Optional<Transacao> findByIdAndUsuario(Long id, Usuario usuario);
}
