package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository para configuración de certificados de firma digital.
 */
@Repository
public interface ConfiguracionCertificadoRepository 
        extends JpaRepository<ConfiguracionCertificadoEntity, Long> {

    /**
     * Busca configuración activa por RUC de emisor.
     *
     * @param rucEmisor RUC del emisor
     * @return Optional con la configuración si existe y está activa
     */
    Optional<ConfiguracionCertificadoEntity> findByRucEmisorAndActivoTrue(String rucEmisor);

    /**
     * Busca cualquier configuración por RUC de emisor (independientemente del estado activo).
     *
     * @param rucEmisor RUC del emisor
     * @return Optional con la configuración si existe
     */
    Optional<ConfiguracionCertificadoEntity> findByRucEmisor(String rucEmisor);

    /**
     * Obtiene todos los certificados ordenados por fecha de creación descendente.
     *
     * @return Lista de certificados
     */
    List<ConfiguracionCertificadoEntity> findAllByOrderByCreadoAtDesc();
}