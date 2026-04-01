export interface RucResponse {
  nombre: string;
  tipoDocumento: string;
  numeroDocumento: string;
  estado: string;
  condicion: string;
  direccion: string;
  ubigeo: string;
  viaTipo: string;
  viaNombre: string;
  zonaCodigo: string;
  zonaTipo: string;
  numero: string;
  interior: string;
  lote: string;
  dpto: string;
  manzana: string;
  kilometro: string;
  distrito: string;
  provincia: string;
  departamento: string;
}

export type MaybeRucResponse = RucResponse | null;
