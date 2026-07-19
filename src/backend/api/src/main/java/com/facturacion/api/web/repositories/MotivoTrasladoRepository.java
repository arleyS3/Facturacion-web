package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.MotivoTrasladoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MotivoTrasladoRepository extends JpaRepository<MotivoTrasladoEntity, Long> {
    @Query(value = "SELECT * FROM motivo_traslado ORDER BY codigo", nativeQuery = true)
    List<MotivoTrasladoEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM motivo_traslado WHERE id = ?1", nativeQuery = true)
    MotivoTrasladoEntity findByIdIncludingInactive(Long id);
}
