package com.facturacion.api.application.comprobante.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link GenerarXmlResult}.
 * <p>
 * Covers construction, null-safety, immutability, and structural equality.
 * </p>
 */
class GenerarXmlResultTest {

    @Test
    void constructor_setsXmlAndValidationErrors() {
        var result = new GenerarXmlResult("<invoice>content</invoice>", List.of("err1", "err2"));

        assertEquals("<invoice>content</invoice>", result.xml());
        assertEquals(2, result.validationErrors().size());
        assertTrue(result.validationErrors().contains("err1"));
        assertTrue(result.validationErrors().contains("err2"));
    }

    @Test
    void constructor_nullValidationErrors_defaultsToEmptyList() {
        var result = new GenerarXmlResult("<xml/>", null);

        assertNotNull(result.validationErrors());
        assertTrue(result.validationErrors().isEmpty());
    }

    @Test
    void validationErrors_isImmutable() {
        var mutableList = new ArrayList<>(List.of("initial"));
        var result = new GenerarXmlResult("<xml/>", mutableList);

        mutableList.add("should-not-appear");

        assertEquals(1, result.validationErrors().size(),
                "Modifying the original list must not affect the record");
        assertEquals("initial", result.validationErrors().get(0));
    }

    @Test
    void xml_canBeNull() {
        var result = new GenerarXmlResult(null, List.of());

        assertNull(result.xml());
        assertTrue(result.validationErrors().isEmpty());
    }

    @Test
    void equalsAndHashCode_basedOnBothFields() {
        var a = new GenerarXmlResult("<a/>", List.of("e1"));
        var b = new GenerarXmlResult("<a/>", List.of("e1"));
        var c = new GenerarXmlResult("<c/>", List.of("e1"));

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a, c);
    }

    @Test
    void toString_containsBothFields() {
        var result = new GenerarXmlResult("<x/>", List.of("warning"));

        String str = result.toString();
        assertTrue(str.contains("<x/>"), "toString should contain xml value");
        assertTrue(str.contains("warning"), "toString should contain validationErrors");
    }
}
