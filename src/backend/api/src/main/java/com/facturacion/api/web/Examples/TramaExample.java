package com.facturacion.api.web.Examples;

public class TramaExample {
		public static final String FACTURA_EXONERADA_SIMPLE = """
{
	"tipoDocumento": "01",
	"secciones": {
		"campos": {
			"A": {
				"CODI_EMPR": "1",
				"Serie": "F001",
				"Correlativo": "00000001",
				"FchEmis": "2025-01-01",
				"HoraEmision": "10:00:00",
				"TipoRucEmis": "6",
				"RUTEmis": "20123456789",
				"RznSocEmis": "EMPRESA",
				"CodigoLocalAnexo": "0000",
				"TipoRutReceptor": "6",
				"RUTRecep": "20123456789",
				"RznSocRecep": "CLIENTE"
			}
		},
		"listas": {
			"B": [
				{
					"NroLinDet": "1",
					"QtyItem": "1",
					"UnmdItem": "NIU",
					"NmbItem": "SERVICIO",
					"MontoItem": "10.00",
					"CodigoTipoIgv": "9998",
					"TasaIgv": "0",
					"ImpuestoIgv": "0.00"
				}
			]
		}
	}
}
""";

		public static final String BOLETA_ALIAS = """
{
	"tipoDocumento": "Boleta",
	"secciones": {
		"campos": {
			"A": {
				"CODI_EMPR": "1",
				"Serie": "B001",
				"Correlativo": "00000099",
				"FchEmis": "2025-01-01",
				"HoraEmision": "10:00:00",
				"TipoRucEmis": "6",
				"RUTEmis": "20123456789",
				"RznSocEmis": "EMPRESA",
				"CodigoLocalAnexo": "0000",
				"TipoRutReceptor": "1",
				"RUTRecep": "12345678",
				"RznSocRecep": "CLIENTE"
			}
		},
		"listas": {
			"B": [
				{
					"NroLinDet": "1",
					"QtyItem": "2",
					"UnmdItem": "NIU",
					"NmbItem": "PRODUCTO",
					"MontoItem": "20.00",
					"CodigoTipoIgv": "9995",
					"TasaIgv": "0",
					"ImpuestoIgv": "0.00"
				}
			]
		}
	}
}
""";

		public static final String NOTA_CREDITO_BASE = """
{
	"tipoDocumento": "07",
	"secciones": {
		"campos": {
			"A": {
				"CODI_EMPR": "1",
				"Serie": "FC01",
				"Correlativo": "00000001",
				"FchEmis": "2025-01-01",
				"HoraEmision": "10:00:00",
				"TipoRucEmis": "6",
				"RUTEmis": "20123456789",
				"RznSocEmis": "EMPRESA",
				"CodigoLocalAnexo": "0000",
				"TipoRutReceptor": "6",
				"RUTRecep": "20123456789",
				"RznSocRecep": "CLIENTE"
			}
		},
		"listas": {
			"B": [
				{
					"NroLinDet": "1",
					"QtyItem": "1",
					"UnmdItem": "NIU",
					"NmbItem": "AJUSTE",
					"MontoItem": "10.00",
					"CodigoTipoIgv": "9998",
					"TasaIgv": "0",
					"ImpuestoIgv": "0.00"
				}
			]
		}
	}
}
""";

		public static final String NOTA_DEBITO_BASE = """
{
	"tipoDocumento": "08",
	"secciones": {
		"campos": {
			"A": {
				"CODI_EMPR": "1",
				"Serie": "FD01",
				"Correlativo": "00000001",
				"FchEmis": "2025-01-01",
				"HoraEmision": "10:00:00",
				"TipoRucEmis": "6",
				"RUTEmis": "20123456789",
				"RznSocEmis": "EMPRESA",
				"CodigoLocalAnexo": "0000",
				"TipoRutReceptor": "6",
				"RUTRecep": "20123456789",
				"RznSocRecep": "CLIENTE"
			}
		},
		"listas": {
			"B": [
				{
					"NroLinDet": "1",
					"QtyItem": "1",
					"UnmdItem": "NIU",
					"NmbItem": "RECARGO",
					"MontoItem": "10.00",
					"CodigoTipoIgv": "9998",
					"TasaIgv": "0",
					"ImpuestoIgv": "0.00"
				}
			]
		}
	}
}
""";

		public static final String GUIA_REMISION_MINIMA = """
{
	"tipoDocumento": "09",
	"secciones": {
		"campos": {
			"A": {
				"CODI_EMPR": "1",
				"Serie": "T001",
				"Correlativo": "00000001",
				"FchEmis": "2025-01-01",
				"HoraEmision": "10:00:00",
				"TipoRucEmis": "6",
				"RUTEmis": "20123456789",
				"RznSocEmis": "EMPRESA",
				"CodigoLocalAnexo": "0000",
				"TipoRutReceptor": "6",
				"RUTRecep": "20123456789",
				"RznSocRecep": "CLIENTE"
			},
			"G": {
				"MotTras": "01",
				"ModTras": "02"
			},
			"G1": {
				"FecIniTras": "2025-01-01"
			}
		},
		"listas": {
		}
	}
}
""";
}
