const API_URL = "http://localhost:8080";

// Ejecuta una query/mutation GraphQL, agregando el token
// automáticamente. Igual que fetchAutenticado (httpClient.js),
// si el backend responde 401 limpia la sesión y redirige al login.
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

    const json = await response.json();

    // GraphQL responde 200 casi siempre, incluso con errores;
    // los errores vienen en el campo "errors" del body
    if (json.errors && json.errors.length > 0) {
        throw new Error(json.errors[0].message || "Error en la consulta");
    }

    return json.data;
}