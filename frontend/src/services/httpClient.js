const API_URL = import.meta.env.VITE_API_URL || "http://127.0.0.1:8000";

// Headers con el token guardado, reutilizado por todos los services
function getAuthHeaders() {
    const token = localStorage.getItem("token");

    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
}

// Wrapper de fetch: agrega el token automáticamente, y si el
// backend responde 401 (token vencido o inválido), limpia la
// sesión y redirige al login en vez de dejar que cada pantalla
// falle con un error confuso.
async function fetchAutenticado(path, options = {}) {
    const response = await fetch(`${API_URL}${path}`, {
        ...options,
        headers: getAuthHeaders(),
    });

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = "/login";
        // cortamos la ejecución acá
        throw new Error("Sesión expirada");
    }

    return response;
}

// Lee el mensaje de error real del backend, sea que venga como
// JSON (vehículos) o como texto plano (clientes)
async function manejarError(response, mensajePorDefecto) {
    const contentType = response.headers.get("content-type") || "";

    try {
        if (contentType.includes("application/json")) {
            const data = await response.json();
            const detalle = Array.isArray(data.detail)
                ? data.detail.map((item) => {
                    const ubicacion = Array.isArray(item.loc)
                        ? item.loc.filter((parte) => parte !== "body").join(".")
                        : "";
                    return ubicacion ? `${ubicacion}: ${item.msg}` : item.msg;
                }).join("; ")
                : data.detail;
            throw new Error(detalle || data.message || data.error || mensajePorDefecto);
        } else {
            const texto = await response.text();
            throw new Error(texto || mensajePorDefecto);
        }
    } catch (error) {
        if (error instanceof SyntaxError) {
            throw new Error(mensajePorDefecto);
        }
        throw error;
    }
}

export { fetchAutenticado, manejarError };
