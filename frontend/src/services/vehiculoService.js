const API_URL = "http://localhost:8080";

//arma los "headers" (información extra que viaja junto con cada pedido HTTP) como el token del usuario logueado
function getAuthHeaders() {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
}

// Trae todos los vehículos
export async function listarVehiculos() {
    const response = await fetch(`${API_URL}/vehiculos`, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error("No se pudieron obtener los vehículos");
    }

    return await response.json();
}

// Trae un vehículo por id
export async function buscarVehiculo(id) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "GET",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error("No se pudo obtener el vehículo");
    }

    return await response.json();
}

// Crea un nuevo vehículo
export async function crearVehiculo(datos) {
    const response = await fetch(`${API_URL}/vehiculos`, {
        method: "POST",
        headers: getAuthHeaders(),
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        throw new Error("No se pudo crear el vehículo");
    }

    return await response.json();
}

// Modifica un vehículo existente
export async function modificarVehiculo(id, datos) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "PUT",
        headers: getAuthHeaders(),
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        throw new Error("No se pudo modificar el vehículo");
    }

    return await response.json();
}

// Baja lógica de un vehículo
export async function eliminarVehiculo(id) {
    const response = await fetch(`${API_URL}/vehiculos/${id}`, {
        method: "DELETE",
        headers: getAuthHeaders(),
    });

    if (!response.ok) {
        throw new Error("No se pudo eliminar el vehículo");
    }

    return await response.json();
}