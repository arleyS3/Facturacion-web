package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.ConfiguracionCertificadoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
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
}