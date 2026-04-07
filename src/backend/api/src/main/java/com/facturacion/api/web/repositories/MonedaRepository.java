package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.MonedaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonedaRepository extends JpaRepository<MonedaEntity, Long> {
}
