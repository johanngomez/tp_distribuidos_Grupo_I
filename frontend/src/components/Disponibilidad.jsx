import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { Search, Calendar, Tag, Car, DollarSign } from "lucide-react";
import { consultarDisponibilidad } from "../services/vehiculoGraphQLService";
import { crearReserva } from "../services/reservaService";
import { fetchAutenticado } from "../services/httpClient";
import CampoConIcono from "./CampoConIcono";
import { useMensajeExito } from "../utils/useMensajeExito";
import { ahoraParaInput } from "../utils/dateUtils";

const TIPOS = ["", "SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"];

// Convierte el valor de un input datetime-local (ej: "2026-09-21T13:30")
// al formato que espera LocalDateTime en el backend (sin milisegundos ni Z)
function aLocalDateTime(valorInput) {
    return `${valorInput}:00`;
}

function Disponibilidad() {
    const navigate = useNavigate();

    const [fechaInicio, setFechaInicio] = useState("");
    const [fechaFin, setFechaFin] = useState("");
    const [tipo, setTipo] = useState("");
    const [marca, setMarca] = useState("");
    const [modelo, setModelo] = useState("");
    const [precioMin, setPrecioMin] = useState("");
    const [precioMax, setPrecioMax] = useState("");

    const [resultados, setResultados] = useState([]);
    const [buscando, setBuscando] = useState(false);
    const [error, setError] = useState("");
    const [busquedaHecha, setBusquedaHecha] = useState(false);

    const [reservandoId, setReservandoId] = useState(null);
    const [mensajeExito, mostrarMensajeExito] = useMensajeExito();

    useEffect(() => {
        const token = localStorage.getItem("token");
        const esAdmin = localStorage.getItem("esAdmin") === "true";

        if (!token) {
            navigate("/login");
            return;
        }

        if (esAdmin) {
            navigate("/vehiculos");
            return;
        }
    }, []);

    async function handleBuscar(event) {
        event.preventDefault();

        setError("");
        setBuscando(true);
        setBusquedaHecha(true);

        try {
            const data = await consultarDisponibilidad({
                fechaInicio: aLocalDateTime(fechaInicio),
                fechaFin: aLocalDateTime(fechaFin),
                tipo: tipo || null,
                marca: marca || null,
                modelo: modelo || null,
                precioMin: precioMin ? Number(precioMin) : null,
                precioMax: precioMax ? Number(precioMax) : null,
            });
            setResultados(data);
        } catch (error) {
            setError(error.message);
        } finally {
            setBuscando(false);
        }
    }

    async function handleReservar(vehiculoId) {
        setError("");
        setReservandoId(vehiculoId);

        try {
            // saco el id del cliente logueado
            const resMe = await fetchAutenticado("/api/clientes/me", {
                method: "GET",
            });

            if (!resMe.ok) {
                throw new Error("No se pudo obtener el perfil del cliente");
            }

            const cliente = await resMe.json();

            await crearReserva({
                vehiculoId,
                clienteId: cliente.id,
                fechaInicio: aLocalDateTime(fechaInicio),
                fechaFin: aLocalDateTime(fechaFin),
            });

            mostrarMensajeExito("¡Reserva confirmada!");
            // saco el vehículo reservado de los resultados
            setResultados((prev) => prev.filter((v) => v.id !== vehiculoId));
        } catch (error) {
            setError(error.message);
        } finally {
            setReservandoId(null);
        }
    }

    return (
        <div>
            <h1>Buscar disponibilidad</h1>
            <p className="page-subtitle">
                Buscá vehículos disponibles para las fechas que necesites.
            </p>

            <div className="form-card">
                <form onSubmit={handleBuscar}>
                    <div className="form-grid">
                        <CampoConIcono
                            icon={Calendar}
                            label="Fecha y hora de inicio"
                            id="fechaInicio"
                            type="datetime-local"
                            value={fechaInicio}
                            onChange={(e) => setFechaInicio(e.target.value)}
                            min={ahoraParaInput()}
                            max="2100-12-31T23:59"
                            required
                        />

                        <CampoConIcono
                            icon={Calendar}
                            label="Fecha y hora de fin"
                            id="fechaFin"
                            type="datetime-local"
                            value={fechaFin}
                            onChange={(e) => setFechaFin(e.target.value)}
                            min={ahoraParaInput()}
                            max="2100-12-31T23:59"
                            required
                        />

                        <div className="form-group">
                            <label htmlFor="tipo">Tipo de vehículo</label>
                            <div className="input-icon">
                                <Car size={16} />
                                <select
                                    id="tipo"
                                    value={tipo}
                                    onChange={(e) => setTipo(e.target.value)}
                                >
                                    {TIPOS.map((t) => (
                                        <option key={t} value={t}>
                                            {t || "Cualquiera"}
                                        </option>
                                    ))}
                                </select>
                            </div>
                        </div>

                        <CampoConIcono
                            icon={Tag}
                            label="Marca"
                            id="marca"
                            value={marca}
                            onChange={(e) => setMarca(e.target.value)}
                        />

                        <CampoConIcono
                            icon={Car}
                            label="Modelo"
                            id="modelo"
                            value={modelo}
                            onChange={(e) => setModelo(e.target.value)}
                        />

                        <CampoConIcono
                            icon={DollarSign}
                            label="Precio mínimo"
                            id="precioMin"
                            type="number"
                            value={precioMin}
                            onChange={(e) => setPrecioMin(e.target.value)}
                        />

                        <div className="span-2">
                            <CampoConIcono
                                icon={DollarSign}
                                label="Precio máximo"
                                id="precioMax"
                                type="number"
                                value={precioMax}
                                onChange={(e) => setPrecioMax(e.target.value)}
                            />
                        </div>
                    </div>

                    {error && <p className="login-error">{error}</p>}

                    <div className="form-actions">
                        <button type="submit" disabled={buscando}>
                            <Search size={16} />{" "}
                            {buscando ? "Buscando..." : "Buscar"}
                        </button>
                    </div>
                </form>
            </div>

            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}

            {busquedaHecha && !buscando && (
                <table>
                    <thead>
                        <tr>
                            <th>Patente</th>
                            <th>Marca</th>
                            <th>Modelo</th>
                            <th>Año</th>
                            <th>Color</th>
                            <th>Tipo</th>
                            <th>Precio diario</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {resultados.map((vehiculo) => (
                            <tr key={vehiculo.id}>
                                <td>{vehiculo.patente}</td>
                                <td>{vehiculo.marca}</td>
                                <td>{vehiculo.modelo}</td>
                                <td>{vehiculo.anio}</td>
                                <td>{vehiculo.color}</td>
                                <td>{vehiculo.tipo}</td>
                                <td>{vehiculo.precioDiario}</td>
                                <td>
                                    <button
                                        onClick={() => handleReservar(vehiculo.id)}
                                        disabled={reservandoId === vehiculo.id}
                                    >
                                        {reservandoId === vehiculo.id
                                            ? "Reservando..."
                                            : "Reservar"}
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>
            )}

            {busquedaHecha && !buscando && resultados.length === 0 && (
                <p>No hay vehículos disponibles para esos filtros.</p>
            )}
        </div>
    );
}

export default Disponibilidad;