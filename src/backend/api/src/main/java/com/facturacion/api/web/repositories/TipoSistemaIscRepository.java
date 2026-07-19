package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoSistemaIscEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoSistemaIscRepository extends JpaRepository<TipoSistemaIscEntity, Long> {
    @Query(value = "SELECT * FROM tipo_sistema_isc ORDER BY codigo", nativeQuery = true)
    List<TipoSistemaIscEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipo_sistema_isc WHERE id = ?1", nativeQuery = true)
    TipoSistemaIscEntity findByIdIncludingInactive(Long id);
}
