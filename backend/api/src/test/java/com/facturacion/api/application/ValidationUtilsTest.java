package com.facturacion.api.application;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ValidationUtilsTest {

    @Test
    void serieSegunTipo_factura() {
        assertTrue(ValidationUtils.serieSegunTipo("F001", "01"));
        assertFalse(ValidationUtils.serieSegunTipo("B001", "01"));
    }

    @Test
    void serieSegunTipo_boleta() {
        assertTrue(ValidationUtils.serieSegunTipo("B001", "03"));
        assertFalse(ValidationUtils.serieSegunTipo("F001", "03"));
    }

    @Test
    void correlativo8() {
        assertTrue(ValidationUtils.correlativo8("00000001"));
        assertFalse(ValidationUtils.correlativo8("1"));
        assertFalse(ValidationUtils.correlativo8("000000001"));
    }

    @Test
    void fechaYHora() {
        assertTrue(ValidationUtils.fechaIso("2026-03-24"));
        assertFalse(ValidationUtils.fechaIso("24/03/2026"));

        assertTrue(ValidationUtils.hora("23:59:59"));
        assertFalse(ValidationUtils.hora("24:00:00"));
    }

    @Test
    void rucYdni() {
        assertTrue(ValidationUtils.ruc("20123456789"));
        assertFalse(ValidationUtils.ruc("1012345678"));

        assertTrue(ValidationUtils.dni("12345678"));
        assertFalse(ValidationUtils.dni("123"));
    }
}
