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

// Devuelve la fecha/hora actual en el formato que necesita un
// input datetime-local (ej: "2026-09-20T14:30"), para usarla
// como valor mínimo y evitar que se elijan fechas pasadas
export function ahoraParaInput() {
    const ahora = new Date();
    const offset = ahora.getTimezoneOffset() * 60000;
    const local = new Date(ahora - offset);
    return local.toISOString().slice(0, 16);
}