package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoNotaCreditoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoNotaCreditoRepository extends JpaRepository<TipoNotaCreditoEntity, Long> {
    @Query(value = "SELECT * FROM tipo_nota_credito ORDER BY codigo", nativeQuery = true)
    List<TipoNotaCreditoEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipo_nota_credito WHERE id = ?1", nativeQuery = true)
    TipoNotaCreditoEntity findByIdIncludingInactive(Long id);
}
