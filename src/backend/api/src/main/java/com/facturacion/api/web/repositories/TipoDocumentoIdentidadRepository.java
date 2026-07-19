package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoDocumentoIdentidadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TipoDocumentoIdentidadRepository extends JpaRepository<TipoDocumentoIdentidadEntity, Long> {
    @Query(value = "SELECT * FROM tipos_documento_identidad ORDER BY codigo", nativeQuery = true)
    List<TipoDocumentoIdentidadEntity> findAllIncludingInactive();

    @Query(value = "SELECT * FROM tipos_documento_identidad WHERE id = ?1", nativeQuery = true)
    TipoDocumentoIdentidadEntity findByIdIncludingInactive(Long id);
}
