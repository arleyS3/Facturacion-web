package com.facturacion.api.application.comprobante.ubl.strategy;

import com.facturacion.api.application.comprobante.modelo.ComprobanteCanonico;
import com.facturacion.api.application.comprobante.ubl.builder.guiaRemision.GuiaRemisionUblBuilder;
import com.facturacion.api.application.comprobante.ubl.mapper.guiaRemision.GuiaRemisionUblMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuiaRemisionUblStrategyTest {

    @Mock
    private GuiaRemisionUblMapper mapper;

    @Mock
    private GuiaRemisionUblBuilder builder;

    @Mock
    private ComprobanteCanonico canonico;

    private GuiaRemisionUblStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GuiaRemisionUblStrategy(mapper, builder);
    }

    @Test
    void generarXml_rejectsBlankReceptorRazonSocial() {
        when(canonico.receptorRazonSocial()).thenReturn(" ");

        assertThrows(IllegalArgumentException.class, () -> strategy.generarXml(canonico));

        verifyNoInteractions(mapper, builder);
    }
}
