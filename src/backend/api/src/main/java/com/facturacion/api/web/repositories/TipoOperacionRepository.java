package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.TipoOperacionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TipoOperacionRepository extends JpaRepository<TipoOperacionEntity, Long> {
}
