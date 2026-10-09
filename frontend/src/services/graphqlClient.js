const API_URL = import.meta.env.VITE_API_URL || "http://127.0.0.1:8000";

// Ejecuta una query/mutation GraphQL hacia el API Gateway
export async function graphqlFetch(query, variables = {}) {
    const token = localStorage.getItem("token");
    
    const response = await fetch(`${API_URL}/graphql`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ query, variables }),
    });

    if (response.status === 401) {
        localStorage.clear();
        window.location.href = "/login";
        throw new Error("Sesión expirada");
    }

    if (response.status === 503) {
        throw new Error("El servicio gRPC no se encuentra disponible momentáneamente.");
    }
    
    const json = await response.json();

    // GraphQL responde 200 casi siempre, incluso con errores;
    // los errores vienen en el campo "errors" del body
    if (json.errors && json.errors.length > 0) {
        throw new Error(json.errors[0].message || "Error en la consulta");
    }

    return json.data;
}