import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { CalendarDays } from "lucide-react";
import { listarReservas } from "../services/reservaGraphQLService";
import { formatearFecha } from "../utils/dateUtils";
import { cancelarReserva } from "../services/reservaService";

// Componente principal de la página de reservas, tanto para admin como para cliente
function Reservas() {
    const navigate = useNavigate();

    const [esAdmin, setEsAdmin] = useState(false);
    const [reservas, setReservas] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [clienteIdFiltro, setClienteIdFiltro] = useState("");
    const [vehiculoIdFiltro, setVehiculoIdFiltro] = useState("");
    const [tipoVehiculoFiltro, setTipoVehiculoFiltro] = useState("");
    const [fechaDesde, setFechaDesde] = useState("");
    const [fechaHasta, setFechaHasta] = useState("");

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
        
const filtro = {
        clienteId: esAdmin && clienteIdFiltro ? parseInt(clienteIdFiltro) : null,
        vehiculoId: vehiculoIdFiltro ? parseInt(vehiculoIdFiltro) : null,
        tipo: tipoVehiculoFiltro || null, 
        estado: estadoFiltro || null,            
        fechaInicioDesde: fechaDesde || null,    
        fechaInicioHasta: fechaHasta || null   
    };

    cargarReservas(filtro);

    }

    if (loading) {
        return <p>Cargando reservas...</p>;
    }

    const handleCancelarReserva = async (reservaId) => {
    const confirmar = window.confirm("¿Estás seguro de que querés cancelar esta reserva?");
    if (!confirmar) return;

    try {
        await cancelarReserva(reservaId);


        setReservas((prevReservas) =>
            prevReservas.map((reserva) =>
                reserva.id === reservaId ? { ...reserva, estado: "CANCELADA" } : reserva
            )
        );

        
        alert("Reserva cancelada con éxito");

    cargarReservas(); //recargar reservas

    } catch (error) {
        const mensaje = error.response?.data?.message || error.message || "Error al cancelar la reserva.";
        alert(mensaje);
    }
};

    return (
    <div className="filters-card">

            <h1>{esAdmin ? "Reservas" : "Mis reservas"}</h1>
            <p className="page-subtitle">
                {esAdmin
                    ? "Consultá las reservas de todos los clientes."
                    : "Consultá el estado de tus reservas."}
            </p>
                    
            {error && <p className="login-error">{error}</p>}

              <h3>Filtros de Búsqueda</h3>

<div className="filters-grid">
            {esAdmin && (
    <div className="form-group">
        <label htmlFor="clienteIdFiltro">Cliente</label>
        <select
            id="clienteIdFiltro"
            value={clienteIdFiltro}
            onChange={(e) => setClienteIdFiltro(e.target.value)}
        >
            <option value="">Todos los clientes</option>
            {Array.from(
                new Map(
                    reservas
                        ?.filter((r) => r.cliente)
                        .map((r) => [r.cliente.id, r.cliente])
                ).values()
            ).map((c) => (
                <option key={c.id} value={c.id}>
                    {c.nombre} {c.apellido}
                </option>
            ))}
        </select>
    </div>
)}


<div className="form-group">
    <label htmlFor="vehiculoIdFiltro">Vehículo</label>
    <select
        id="vehiculoIdFiltro"
        value={vehiculoIdFiltro}
        onChange={(e) => setVehiculoIdFiltro(e.target.value)}
    >
        <option value="">Todos los vehículos</option>
        {Array.from(
            new Map(
                reservas
                    ?.filter((r) => r.vehiculo)
                    .map((r) => [r.vehiculo.id, r.vehiculo])
            ).values()
        ).map((v) => (
            <option key={v.id} value={v.id}>
                {v.marca} {v.modelo}
            </option>
        ))}
    </select>
    </div>




            <div className="form-group">
                <label htmlFor="tipoVehiculoFiltro">Tipo de vehículo</label>
                <select
                    id="tipoVehiculoFiltro"
                    value={tipoVehiculoFiltro}
                    onChange={(e) => setTipoVehiculoFiltro(e.target.value)}
                >
                    <option value="">Todos los tipos</option>
                    <option value="SEDAN">Sedán</option>
                    <option value="SUV">SUV</option>
                    <option value="PICKUP">Pickup</option>
                    <option value="COUPE">Coupé</option>
                    <option value="HATCHBACK">Hatchback</option>
                </select>
            </div>


            {esAdmin && (
                <form onSubmit={handleFiltrar} className="form-group">
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
                </form>
            )}

<div className="filters-card">
            <div className="form-group">
                <label htmlFor="fechaDesde">Fecha desde</label>
                <input
                    type="date"
                    id="fechaDesde"
                    value={fechaDesde}
                    onChange={(e) => setFechaDesde(e.target.value)}
                />
            </div>
                        </div>

<div className="filters-card">
            <div className="form-group">
                <label htmlFor="fechaHasta">Fecha hasta</label>
                <input
                    type="date"
                    id="fechaHasta"
                    value={fechaHasta}
                    onChange={(e) => setFechaHasta(e.target.value)}
                />
            </div>

                                <div className="form-actions">
                        <button type="submit">Filtrar</button>
                    </div>
                    </div>
            </div>

                                                       



            

                        



            <table className="tabla-reservas">
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

                <td>{reserva.cliente ? `${reserva.cliente.nombre} ${reserva.cliente.apellido}` : 'Sin cliente'}</td>
  <td>
    {reserva.vehiculo
      ? `${reserva.vehiculo.marca || ''} ${reserva.vehiculo.modelo || ''}`.trim()
      : reserva.vehiculoNombre || reserva.modelo || "-"}
  </td>

  {/* 2. Patente */}
  <td>{reserva.vehiculo?.patente || reserva.patente || "-"}</td>

  {/* 3. Fecha inicio */}
<td>
  {reserva.fechaInicio ? (
    <>
      <div>{reserva.fechaInicio.split('T')[0]}</div>
      <small style={{ color: '#666', fontSize: '0.85em' }}>
        {reserva.fechaInicio.split('T')[1]?.substring(0, 5)} hs
      </small>
    </>
  ) : (
    '-'
  )}
</td>

  {/* 4. Fecha fin */}
  <td >
  {reserva.fechaFin ? (
    <>
      <div>{reserva.fechaFin.split('T')[0]}</div>
      <small style={{ color: '#666', fontSize: '0.85em' }}>
        {reserva.fechaFin.split('T')[1]?.substring(0, 5)} hs
      </small>
    </>
  ) : (
    '-'
  )}
</td>

  {/* 5. Precio diario */}
  <td>{reserva.precioDiario || reserva.vehiculo?.precioDiario || "-"}</td>

  {/* 6. Importe total */}
  <td>{reserva.montoTotal || reserva.importeTotal || "-"}</td>

  {/* 7. Estado */}
  <td>{reserva.estado || "-"}</td>
                {!esAdmin && (
                    <td>
                        {reserva.estado !== "CANCELADA" && reserva.estado !== "FINALIZADA" ? (
                            <button
                                onClick={() => handleCancelarReserva(reserva.id)}
                                style={{
                                    backgroundColor: '#e53e3e',
                                    color: '#ffffff',
                                    border: 'none',
                                    padding: '6px 12px',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '13px',
                                    fontWeight: 'bold'
                                }}
                            >
                                Cancelar
                            </button>
                        ) : null} 
                    </td>
                )}
            </tr>
        ))}
                    
                    {reservas.map((reserva) => (
                        <tr key={reserva.id}>
                            {esAdmin && (
                                <td>
                                    {reserva.cliente.nombre}{" "}
                                    {reserva.cliente.apellido}
                                </td>
                            )}
                            <td><strong> 
                                {reserva.vehiculo.marca} {reserva.vehiculo.modelo}
                            </strong></td>
                            <td>{reserva.vehiculo.patente}</td>
                            <td>{formatearFecha(reserva.fechaInicio)}</td>
                            <td>{formatearFecha(reserva.fechaFin)}</td>
                            <td>{reserva.precioDiario}</td>
                            <td>{reserva.importeTotal}</td>
                            <td>{reserva.estado}</td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {reservas.length === 0 && <p>No hay reservas para mostrar.</p>}
        </div>
    );
}

export default Reservas;