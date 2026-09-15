import { fetchAutenticado, manejarError } from "./httpClient";
// Funciones para interactuar con la API de vehículos
export async function listarVehiculos() {
    const response = await fetchAutenticado("/vehiculos", { method: "GET" });

    if (!response.ok) {
        await manejarError(response, "No se pudieron obtener los vehículos");
    }

    return await response.json();
}
// Función para buscar un vehículo por su ID
export async function buscarVehiculo(id) {
    const response = await fetchAutenticado(`/vehiculos/${id}`, {
        method: "GET",
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo obtener el vehículo");
    }

    return await response.json();
}
// Función para crear un nuevo vehículo
export async function crearVehiculo(datos) {
    const response = await fetchAutenticado("/vehiculos", {
        method: "POST",
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo crear el vehículo");
    }

    return await response.json();
}
// Función para modificar un vehículo existente
export async function modificarVehiculo(id, datos) {
    const response = await fetchAutenticado(`/vehiculos/${id}`, {
        method: "PUT",
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo modificar el vehículo");
    }

    return await response.json();
}
// Función para eliminar un vehículo por su ID
export async function eliminarVehiculo(id) {
    const response = await fetchAutenticado(`/vehiculos/${id}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo eliminar el vehículo");
    }

    return await response.json();
}