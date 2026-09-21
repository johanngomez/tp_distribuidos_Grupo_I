import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays } from "lucide-react";
import { listarReservas } from "../services/reservaGraphQLService";
import { cancelarReserva } from "../services/reservaService";
import { formatearFecha } from "../utils/dateUtils";
import { useMensajeExito } from "../utils/useMensajeExito";

// Componente principal de la página de reservas, tanto para admin como para cliente
function Reservas() {
    const navigate = useNavigate();

    const [esAdmin, setEsAdmin] = useState(false);
    const [reservas, setReservas] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [cancelandoId, setCancelandoId] = useState(null);

    // filtros, solo se muestran/usan si es admin
    const [estadoFiltro, setEstadoFiltro] = useState("");
    // Carga las reservas al montar el componente, y verifica si hay token
    useEffect(() => {
        const token = localStorage.getItem("token");
        // Si no hay token, redirige al login
        if (!token) {
            navigate("/login");
            return;
        }
        // Si hay token, verifica si es admin y carga las reservas
        setEsAdmin(localStorage.getItem("esAdmin") === "true");
        cargarReservas();
    }, []);
    // Función para cargar reservas desde el backend, con filtro opcional
    async function cargarReservas(filtro = {}) {
        setError("");
        setLoading(true);

        try {
            const data = await listarReservas(filtro);
            setReservas(data);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }
    // Función para manejar el submit del formulario de filtrado
    function handleFiltrar(event) {
        event.preventDefault();
        cargarReservas({ // Solo aplica el filtro de estado si es admin
            estado: estadoFiltro || null, // Si el filtro está vacío, pasa null para no filtrar por estado
        });
    }

    // Cancela una reserva propia, con confirmación previa
    async function handleCancelar(id) {
        const confirmar = window.confirm(
            "¿Estás seguro que querés cancelar esta reserva?"
        );

        if (!confirmar) {
            return;
        }

        setError("");
        setCancelandoId(id);

        try {
            await cancelarReserva(id);
            await cargarReservas();
        } catch (error) {
            setError(error.message);
        } finally {
            setCancelandoId(null);
        }
    }

    if (loading) {
        return <p>Cargando reservas...</p>;
    }

    return (
        <div>
            <h1>{esAdmin ? "Reservas" : "Mis reservas"}</h1>
            <p className="page-subtitle">
                {esAdmin
                    ? "Consultá las reservas de todos los clientes."
                    : "Consultá el estado de tus reservas."}
            </p>

            {error && <p className="login-error">{error}</p>}

            {esAdmin && (
                <form onSubmit={handleFiltrar} className="form-card">
                    <div className="form-group">
                        <label htmlFor="estadoFiltro">Filtrar por estado</label>
                        <select
                            id="estadoFiltro"
                            value={estadoFiltro}
                            onChange={(e) => setEstadoFiltro(e.target.value)}
                        >
                            <option value="">Todos</option>
                            <option value="CONFIRMADA">Confirmada</option>
                            <option value="CANCELADA">Cancelada</option>
                            <option value="FINALIZADA">Finalizada</option>
                        </select>
                    </div>

                    <div className="form-actions">
                        <button type="submit">Filtrar</button>
                    </div>
                </form>
            )}

            <table>
                <thead>
                    <tr>
                        {esAdmin && <th>Cliente</th>}
                        <th>Vehículo</th>
                        <th>Patente</th>
                        <th>Fecha inicio</th>
                        <th>Fecha fin</th>
                        <th>Precio diario</th>
                        <th>Importe total</th>
                        <th>Estado</th>
                        {!esAdmin && <th>Acciones</th>}
                    </tr>
                </thead>
                <tbody>
                    {reservas.map((reserva) => (
                        <tr key={reserva.id}>
                            {esAdmin && (
                                <td>
                                    {reserva.cliente.nombre}{" "}
                                    {reserva.cliente.apellido}
                                </td>
                            )}
                            <td>
                                <strong>
                                    {reserva.vehiculo.marca} {reserva.vehiculo.modelo}
                                </strong>
                            </td>
                            <td>{reserva.vehiculo.patente}</td>
                            <td>{formatearFecha(reserva.fechaInicio)}</td>
                            <td>{formatearFecha(reserva.fechaFin)}</td>
                            <td>${reserva.precioDiario}</td>
                            <td>${reserva.importeTotal}</td>
                            <td>{reserva.estado}</td>
                            {!esAdmin && (
                                <td>
                                    {reserva.estado === "CONFIRMADA" && (
                                        <button
                                            onClick={() => handleCancelar(reserva.id)}
                                            disabled={cancelandoId === reserva.id}
                                        >
                                            {cancelandoId === reserva.id
                                                ? "Cancelando..."
                                                : "Cancelar"}
                                        </button>
                                    )}
                                </td>
                            )}
                        </tr>
                    ))}
                </tbody>
            </table>

            {reservas.length === 0 && <p>No hay reservas para mostrar.</p>}
        </div>
    );
}

export default Reservas;