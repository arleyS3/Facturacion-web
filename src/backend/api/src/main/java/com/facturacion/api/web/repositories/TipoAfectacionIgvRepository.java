package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoAfectacionIgvEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoAfectacionIgvRepository extends JpaRepository<TipoAfectacionIgvEntity, Long> {
    @Query(value = "SELECT * FROM tipo_afectacion_igv ORDER BY codigo", nativeQuery = true)
    List<TipoAfectacionIgvEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipo_afectacion_igv WHERE id = ?1", nativeQuery = true)
    TipoAfectacionIgvEntity findByIdIncludingInactive(Long id);
}
