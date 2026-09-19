// Formatea una fecha ISO a un string legible en español
export function formatearFecha(fechaIso) {
    if (!fechaIso) return "-";
    const fecha = new Date(fechaIso);
    return fecha.toLocaleString("es-AR", {
        day: "2-digit",
        month: "2-digit",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    });
}