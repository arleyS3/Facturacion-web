package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum TipoDocumentoIdentidad {
    DOC_TRIB_NO_DOM_SIN_RUC("0", "DOC.TRIB.NO.DOM.SIN.RUC"),
    DNI("1", "Documento Nacional de Identidad"),
    CARNET_EXTRANJERIA("4", "Carnet de extranjería"),
    RUC("6", "Registro Unico de Contributentes"),
    PASAPORTE("7", "Pasaporte"),
    CEDULA_DIPLOMATICA("A", "Cédula Diplomática de identidad"),
    DOC_IDENT_PAIS_RESIDENCIA("B", "DOC.IDENT.PAIS.RESIDENCIA-NO.D"),
    TIN("C", "Tax Identification Number - TIN – Doc Trib PP.NN"),
    IN("D", "Identification Number - IN – Doc Trib PP. JJ"),
    TAM("E", "TAM- Tarjeta Andina de Migración"),
    PTP("F", "Permiso Temporal de Permanencia - PTP"),
    SALVOCONDUCTO("G", "Salvoconducto");

    private final String codigo;
    private final String descripcion;

}
