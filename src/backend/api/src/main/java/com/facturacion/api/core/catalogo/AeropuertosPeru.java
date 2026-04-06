package com.facturacion.api.core.catalogo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AeropuertosPeru {
    AQP("AQP", "Rodríguez Ballón", "040104"),
    ANS("ANS", "Andahuaylas", "030201"),
    ATA("ATA", "Comandante FAP Germán Arias Graciani", "020604"),
    AYP("AYP", "Coronel FAP Alfredo Mendívil Duarte", "050113"),
    CJA("CJA", "Mayor Gral. FAP Armando Revoredo Iglesias", "060108"),
    CHM("CHM", "Tnte. FAP Jaime De Montruil M.", "021809"),
    CUZ("CUZ", "Alejandro Velazco Astete", "080108"),
    CHH("CHH", "Chachapoyas", "010101"),
    CIX("CIX", "Capitán FAP José Quiñones G.", "140101"),
    HUU("HUU", "Alférez FAP David Figueroa Fernandini", "100101"),
    ILO("ILO", "Ilo", "180301"),
    IQT("IQT", "Coronel FAP Francisco Secada Vignetta", "160101"),
    JAE("JAE", "Jaén - Shumba", "060802"),
    JJI("JJI", "Juanjuí", "220601"),
    JUL("JUL", "Manco Cápac", "211101"),
    JAU("JAU", "Francisco Carlé", "120430"),
    LIM("LIM", "Internacional Jorge Chávez", "070101"),
    MBP("MBP", "Moyobamba", "220101"),
    PIO("PIO", "Capitán FAP Renán Elías Olivera", "110506"),
    PIU("PIU", "Capitán FAP Carlos Concha Iberico", "200104"),
    PCL("PCL", "Capitán FAP David Abensur Rengifo", "250105"),
    PEM("PEM", "Padre Aldamiz", "170101"),
    RIJ("RIJ", "Juan Simons Vela - Rioja", "220801"),
    TCQ("TCQ", "Coronel FAP Carlos Ciriani Santa Rosa", "230101"),
    TYL("TYL", "Capitán FAP Montes Arias", "200701"),
    TPP("TPP", "Cadete FAP Guillermo del Castillo Paredes", "220901"),
    TIG("TIG", "Tingo María", "100601"),
    TRU("TRU", "Capitán FAP Carlos Martínez Pinillos", "130104"),
    TBP("TBP", "Capitán FAP Pedro Canga Rodríguez", "240101"),
    ATG("ATG", "Atalaya - Tnte. Gral. Gerardo Pérez Pinedo", "250201"),
    YMS("YMS", "Moisés Benzaquen Rengifo", "160201");

    private final String codigo;
    private final String descripcion;
    private final String codigoSunat;
}
