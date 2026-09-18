import { graphqlFetch } from "./graphqlClient";
// Query GraphQL para listar reservas, con filtro opcional
const QUERY_RESERVAS = `
    query ReservasQuery($filtro: FiltroReservaInput) {
        reservas(filtro: $filtro) {
            id
            fechaInicio
            fechaFin
            precioDiario
            importeTotal
            estado
            cliente {
                id
                nombre
                apellido
                documento
            }
            vehiculo {
                id
                patente
                marca
                modelo
            }
        }
    }
`;

// El backend ya distingue admin/cliente por el token: si sos
// cliente, ignora cualquier filtro y devuelve solo tus reservas;
// si sos admin, aplica el filtro (cuando lo soporte) sobre todas.
export async function listarReservas(filtro = {}) {
    const data = await graphqlFetch(QUERY_RESERVAS, { filtro });
    return data.reservas;
}