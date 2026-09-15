import { fetchAutenticado, manejarError } from "./httpClient";

export async function listarClientes() {
    const response = await fetchAutenticado("/api/clientes", {
        method: "GET",
    });

    if (!response.ok) {
        await manejarError(response, "No se pudieron obtener los clientes");
    }

    return await response.json();
}

export async function buscarCliente(id) {
    const response = await fetchAutenticado(`/api/clientes/${id}`, {
        method: "GET",
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo obtener el cliente");
    }

    return await response.json();
}

export async function crearCliente(datos) {
    const response = await fetchAutenticado("/api/clientes", {
        method: "POST",
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo crear el cliente");
    }

    return await response.json();
}

export async function modificarCliente(id, datos) {
    const response = await fetchAutenticado(`/api/clientes/${id}`, {
        method: "PUT",
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo modificar el cliente");
    }

    return await response.json();
}

export async function bajaLogicaCliente(id) {
    const response = await fetchAutenticado(`/api/clientes/${id}`, {
        method: "DELETE",
    });

    if (!response.ok) {
        await manejarError(response, "No se pudo dar de baja el cliente");
    }

    return await response.text();
}