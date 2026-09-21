import { fetchAutenticado, manejarError } from "./httpClient";

export async function crearReserva(datos) {
    try {
    const response = await fetchAutenticado("/api/reservas", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(datos),
    });

    if (!response.ok) {
        const errorDelBack = await response.text();

        throw new Error(errorDelBack);
    }

    return await response.text();
    } catch (error) {
        console.log("Error atrapado en crearReserva:", error);
        throw error;
    }
}

export const cancelarReserva = async (reservaId) => {

    const response = await fetchAutenticado(`/api/reservas/${reservaId}/cancelar`, {
         method: "PUT",
    });

return response.data;
};
