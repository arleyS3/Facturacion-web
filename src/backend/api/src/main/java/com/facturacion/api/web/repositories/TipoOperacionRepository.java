package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoOperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoOperacionRepository extends JpaRepository<TipoOperacionEntity, Long> {
    @Query(value = "SELECT * FROM tipo_operacion ORDER BY codigo", nativeQuery = true)
    List<TipoOperacionEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipo_operacion WHERE id = ?1", nativeQuery = true)
    TipoOperacionEntity findByIdIncludingInactive(Long id);
}
