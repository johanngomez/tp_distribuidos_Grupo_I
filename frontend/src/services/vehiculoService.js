const API_URL = "http://localhost:8080";

function getAuthHeaders() {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
}

async function manejarError(response, mensajePorDefecto) {
    try {
        const data = await response.json();
        throw new Error(data.message || data.error || mensajePorDefecto);
    } catch (error) {
        if (error instanceof SyntaxError) {
            throw new Error(mensajePorDefecto);
        }
        throw error;
    }
}

export async function listarVehiculos() {
    const response = await fetch(`${API_URL}/vehiculos`, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudieron obtener los vehículos");
    }

    return await response.json();
}

export async function buscarVehiculo(id) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo obtener el vehículo");
    }

    return await response.json();
}

export async function crearVehiculo(datos) {
    const response = await fetch(`${API_URL}/vehiculos`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo crear el vehículo");
    }

    return await response.json();
}

export async function modificarVehiculo(id, datos) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo modificar el vehículo");
    }

    return await response.json();
}

export async function eliminarVehiculo(id) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo eliminar el vehículo");
    }

    return await response.json();
}