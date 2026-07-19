package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoNotaDebitoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoNotaDebitoRepository extends JpaRepository<TipoNotaDebitoEntity, Long> {
    @Query(value = "SELECT * FROM tipo_nota_debito ORDER BY codigo", nativeQuery = true)
    List<TipoNotaDebitoEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipo_nota_debito WHERE id = ?1", nativeQuery = true)
    TipoNotaDebitoEntity findByIdIncludingInactive(Long id);
}
