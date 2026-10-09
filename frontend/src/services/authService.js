const JAVA_BACKEND_URL = import.meta.env.VITE_JAVA_API_URL || "http://localhost:8080";

// esto genera un token de acceso y lo devuelve al frontend
export async function login(email, password) {
    const response = await fetch(`${JAVA_BACKEND_URL}/auth/login`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify({
            email,
            password,
        }),
    });

    if (!response.ok) {
        throw new Error("Credenciales inválidas");
    }

    return await response.json();
}