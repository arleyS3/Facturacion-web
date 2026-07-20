package com.facturacion.api.web.controller;

import com.facturacion.api.security.JwtService;
import com.facturacion.api.security.TokenBlacklistService;
import com.facturacion.api.web.models.TipoNotaDebitoEntity;
import com.facturacion.api.web.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminCatalogController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private TokenBlacklistService tokenBlacklistService;

    @MockitoBean
    private TipoOperacionRepository tipoOperacionRepository;

    @MockitoBean
    private TipoNotaCreditoRepository tipoNotaCreditoRepository;

    @MockitoBean
    private TipoNotaDebitoRepository tipoNotaDebitoRepository;

    @MockitoBean
    private TipoSistemaIscRepository tipoSistemaIscRepository;

    @MockitoBean
    private TipoAfectacionIgvRepository tipoAfectacionIgvRepository;

    @MockitoBean
    private TipoDocumentoIdentidadRepository tipoDocumentoIdentidadRepository;

    @MockitoBean
    private MotivoTrasladoRepository motivoTrasladoRepository;

    @Test
    void reactivar_returnsOk_whenCatalogItemIsReactivated() throws Exception {
        var entity = new TipoNotaDebitoEntity();
        entity.setId(1L);
        entity.setCodigo("01");
        entity.setDescripcion("Interés por mora");
        entity.setActivo(false);

        when(tipoNotaDebitoRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(tipoNotaDebitoRepository.save(any(TipoNotaDebitoEntity.class))).thenAnswer(i -> i.getArgument(0));

        mockMvc.perform(patch("/api/v1/admin/catalogos/tipos-nota-debito/1/reactivar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigo").value("01"))
                .andExpect(jsonPath("$.activo").value(true));
    }
}
