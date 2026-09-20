import { graphqlFetch } from "./graphqlClient";

const QUERY_DISPONIBILIDAD = `
    query ConsultarDisponibilidad(
        $fechaInicio: String!
        $fechaFin: String!
        $tipo: TipoVehiculo
        $marca: String
        $modelo: String
        $precioMin: Float
        $precioMax: Float
    ) {
        consultarDisponibilidad(
            fechaInicio: $fechaInicio
            fechaFin: $fechaFin
            tipo: $tipo
            marca: $marca
            modelo: $modelo
            precioMin: $precioMin
            precioMax: $precioMax
        ) {
            id
            patente
            marca
            modelo
            anio
            color
            tipo
            precioDiario
        }
    }
`;

export async function consultarDisponibilidad(filtros) {
    const data = await graphqlFetch(QUERY_DISPONIBILIDAD, filtros);
    return data.consultarDisponibilidad;
}