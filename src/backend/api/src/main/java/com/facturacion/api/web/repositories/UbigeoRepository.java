package com.facturacion.api.web.repositories;

import com.facturacion.api.web.models.UbigeoEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UbigeoRepository extends JpaRepository<UbigeoEntity, String> {


        @Query("SELECT DISTINCT u.departamento FROM UbigeoEntity u ORDER BY u.departamento")
        List<String> findDistinctDepartamentos();


        @Query("SELECT DISTINCT u.provincia FROM UbigeoEntity u WHERE u.departamento = :departamento ORDER BY u.provincia")
        List<String> findProvinciasByDepartamento(@Param("departamento") String departamento);


        @Query("SELECT u FROM UbigeoEntity u WHERE u.departamento = :departamento AND u.provincia = :provincia ORDER BY u.distrito")
        List<UbigeoEntity> findUbigeoByDepartamentoAndProvincia(
                        @Param("departamento") String departamento,
                        @Param("provincia") String provincia);


        @Query("SELECT u FROM UbigeoEntity u WHERE UPPER(TRIM(u.departamento)) = UPPER(TRIM(:departamento)) AND UPPER(TRIM(u.provincia)) = UPPER(TRIM(:provincia)) AND UPPER(TRIM(u.distrito)) = UPPER(TRIM(:distrito))")
        UbigeoEntity findUbigeoByUbicacion(
                        @Param("departamento") String departamento,
                        @Param("provincia") String provincia,
                        @Param("distrito") String distrito);
}
