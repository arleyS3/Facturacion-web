package com.facturacion.api.core.catalogo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum PuertosPeru {
    PUB("PUB", "Bayóvar", "200801"),
    CLL("CLL", "Callao", "070101"),
    CON("CON", "Conchán", "150119"),
    CHY("CHY", "Chancay", "150605"),
    CHM("CHM", "Chimbote", "021801"),
    EEN("EEN", "Eten", "140113"),
    HCO("HCO", "Huacho", "150801"),
    HUY("HUY", "Huarmey", "021101"),
    ILQ("ILQ", "Ilo", "180301"),
    IQT("IQT", "Iquitos", "160101"),
    MRI("MRI", "Matarani", "040701"),
    PAI("PAI", "Paita", "200501"),
    PIO("PIO", "Pisco", "110505"),
    PCL("PCL", "Pucallpa", "250101"),
    PUN("PUN", "Puno", "210101"),
    SVY("SVY", "Salaverry", "130109"),
    SNX("SNX", "San Nicolas", "110304"),
    SUP("SUP", "Supe", "150204"),
    TYL("TYL", "Talara", "200701"),
    YMS("YMS", "Yurimaguas", "160201"),
    ZOR("ZOR", "Zorritos", "240103");

    private final String codigo;
    private final String descripcion;
    private final String codigoSunat;

}
