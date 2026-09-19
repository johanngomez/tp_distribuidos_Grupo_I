import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { History } from "lucide-react";
import { fetchAutenticado } from "../services/httpClient";
import { obtenerHistorialAlquileres } from "../services/reservaGraphQLService";
import { formatearFecha } from "../utils/dateUtils";

export default function Historial() {
    const navigate = useNavigate();

    const [historial, setHistorial] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const token = localStorage.getItem("token");
        const esAdmin = localStorage.getItem("esAdmin") === "true";

        if (!token) {
            navigate("/login");
            return;
        }

        // el historial es una vista solo del clientes.
        if (esAdmin) {
            navigate("/reservas");
            return;
        }

        cargarDatos();
    }, []);

    async function cargarDatos() {
        setError("");
        setLoading(true);

        try {
            // 1. Obtener el id del cliente logueado
            const resMe = await fetchAutenticado("/api/clientes/me", {
                method: "GET",
            });

            if (!resMe.ok) {
                throw new Error("No se pudo obtener el perfil del cliente");
            }

            const cliente = await resMe.json();

            // 2. Pedir su historial con ese id
            const data = await obtenerHistorialAlquileres(cliente.id);
            setHistorial(data);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }

    if (loading) {
        return <p>Cargando historial...</p>;
    }

    return (
        <div>
            <h1>Historial de alquileres</h1>
            <p className="page-subtitle">
                Alquileres finalizados y reservas canceladas.
            </p>

            {error && <p className="login-error">{error}</p>}

            <table>
                <thead>
                    <tr>
                        <th>Vehículo</th>
                        <th>Patente</th>
                        <th>Fecha inicio</th>
                        <th>Fecha fin</th>
                        <th>Cantidad de días</th>
                        <th>Importe total</th>
                        <th>Estado</th>
                    </tr>
                </thead>
                <tbody>
                    {historial.map((item, index) => (
                        <tr key={index}>
                            <td><strong>{item.vehiculo}</strong></td>
                            <td>{item.patente}</td>
                            <td>{formatearFecha(item.fechaInicio)}</td>
                            <td>{formatearFecha(item.fechaFin)}</td>
                            <td>{item.cantidadDias}</td>
                            <td>${item.importeTotal}</td>
                            <td>{item.estado}</td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {historial.length === 0 && (
                <p>No hay alquileres finalizados ni reservas canceladas todavía.</p>
            )}
        </div>
    );
}