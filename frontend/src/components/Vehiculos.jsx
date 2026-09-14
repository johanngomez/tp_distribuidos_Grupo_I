import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import {
    listarVehiculos,
    crearVehiculo,
    modificarVehiculo,
    eliminarVehiculo,
} from "../services/vehiculoService";

const TIPOS = ["SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"];
const ESTADOS = ["DISPONIBLE", "RESERVADO", "EN_ALQUILER"];

function Vehiculos() {
    const navigate = useNavigate();
    const [vehiculos, setVehiculos] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    const [busqueda, setBusqueda] = useState("");

    const [editandoId, setEditandoId] = useState(null);

    const [patente, setPatente] = useState("");
    const [marca, setMarca] = useState("");
    const [modelo, setModelo] = useState("");
    const [anio, setAnio] = useState("");
    const [color, setColor] = useState("");
    const [tipo, setTipo] = useState(TIPOS[0]);
    const [precioDiario, setPrecioDiario] = useState("");
    const [estado, setEstado] = useState(ESTADOS[0]);
    const [guardando, setGuardando] = useState(false);
    const [formError, setFormError] = useState("");

    const [mostrarListado, setMostrarListado] = useState(false);
    //si nunca se logueo e intenta ingresar a /vehiculos lo manda al loguin, si es admin lo deja, si no es admin lo manda a /disponibilidad
    useEffect(() => {
    const token = localStorage.getItem("token");
    const esAdmin = localStorage.getItem("esAdmin") === "true";

    if (!token) {
        navigate("/login");
        return;
    }

    if (!esAdmin) {
        navigate("/disponibilidad");
        return;
    }

    cargarVehiculos();
    }, []);

    async function cargarVehiculos() {
        setError("");
        setLoading(true);

        try {
            const data = await listarVehiculos();
            setVehiculos(data);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }

    function limpiarFormulario() {
        setEditandoId(null);
        setPatente("");
        setMarca("");
        setModelo("");
        setAnio("");
        setColor("");
        setTipo(TIPOS[0]);
        setPrecioDiario("");
        setEstado(ESTADOS[0]);
    }

    function handleEditar(vehiculo) {
        setEditandoId(vehiculo.id);
        setPatente(vehiculo.patente);
        setMarca(vehiculo.marca);
        setModelo(vehiculo.modelo);
        setAnio(vehiculo.anio);
        setColor(vehiculo.color || "");
        setTipo(vehiculo.tipo);
        setPrecioDiario(vehiculo.precioDiario);
        setEstado(vehiculo.estado);
    }

    async function handleBaja(id) {
        const confirmar = window.confirm(
            "¿Seguro que querés dar de baja este vehículo?"
        );

        if (!confirmar) {
            return;
        }

        try {
            await eliminarVehiculo(id);
            await cargarVehiculos();
        } catch (error) {
            setError(error.message);
        }
    }

    async function handleSubmit(event) {
        event.preventDefault();

        setFormError("");
        setGuardando(true);

        try {
            if (editandoId) {
                await modificarVehiculo(editandoId, {
                    marca,
                    modelo,
                    anio: Number(anio),
                    color,
                    tipo,
                    precioDiario: Number(precioDiario),
                    estado,
                });
            } else {
                await crearVehiculo({
                    patente,
                    marca,
                    modelo,
                    anio: Number(anio),
                    color,
                    tipo,
                    precioDiario: Number(precioDiario),
                });
            }

            limpiarFormulario();
            await cargarVehiculos();
        } catch (error) {
            setFormError(error.message);
        } finally {
            setGuardando(false);
        }
    }

    if (loading) {
        return <p>Cargando vehículos...</p>;
    }
    const vehiculosFiltrados = vehiculos.filter((v) =>
        v.patente.toLowerCase().includes(busqueda.toLowerCase())
    );

    return (
    <div>
        <h1>Gestión de vehículos</h1>

        {error && <p className="login-error">{error}</p>}

        <button
            type="button"
            className="toggle-listado"
            onClick={() => setMostrarListado(!mostrarListado)}
        >
            Vehículos registrados {mostrarListado ? "▲" : "▼"}
        </button>

        {mostrarListado && (
            <>
                <div className="form-group">
                    <label htmlFor="busqueda">Buscar por patente</label>
                    <input
                        id="busqueda"
                        type="text"
                        value={busqueda}
                        onChange={(e) => setBusqueda(e.target.value)}
                        placeholder="Ej: AA111BB"
                    />
                </div>

                <table>
                    <thead>
                        <tr>
                            <th>Patente</th>
                            <th>Marca</th>
                            <th>Modelo</th>
                            <th>Año</th>
                            <th>Tipo</th>
                            <th>Precio diario</th>
                            <th>Estado</th>
                            <th>Activo</th>
                            <th>Acciones</th>
                        </tr>
                    </thead>
                    <tbody>
                        {vehiculosFiltrados.map((vehiculo) => (
                            <tr key={vehiculo.id}>
                                <td>{vehiculo.patente}</td>
                                <td>{vehiculo.marca}</td>
                                <td>{vehiculo.modelo}</td>
                                <td>{vehiculo.anio}</td>
                                <td>{vehiculo.tipo}</td>
                                <td>{vehiculo.precioDiario}</td>
                                <td>{vehiculo.estado}</td>
                                <td>{vehiculo.activo ? "Sí" : "No"}</td>
                                <td>
                                    <button onClick={() => handleEditar(vehiculo)}>
                                        Editar
                                    </button>
                                    <button
                                        onClick={() => handleBaja(vehiculo.id)}
                                        disabled={!vehiculo.activo}
                                    >
                                        Dar de baja
                                    </button>
                                </td>
                            </tr>
                        ))}
                    </tbody>
                </table>

                {vehiculosFiltrados.length === 0 && <p>No se encontraron vehículos.</p>}
            </>
        )}

        <h2>{editandoId ? "Editar vehículo" : "Nuevo vehículo"}</h2>
        <div className="form-card">
            <form onSubmit={handleSubmit}>
                <div className="form-grid">
                    <div className="form-group">
                        <label htmlFor="patente">Patente</label>
                        <input
                            id="patente"
                            value={patente}
                            onChange={(e) => setPatente(e.target.value)}
                            disabled={!!editandoId}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="marca">Marca</label>
                        <input
                            id="marca"
                            value={marca}
                            onChange={(e) => setMarca(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="modelo">Modelo</label>
                        <input
                            id="modelo"
                            value={modelo}
                            onChange={(e) => setModelo(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="anio">Año</label>
                        <input
                            id="anio"
                            type="number"
                            value={anio}
                            onChange={(e) => setAnio(e.target.value)}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="color">Color</label>
                        <input
                            id="color"
                            value={color}
                            onChange={(e) => setColor(e.target.value)}
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="tipo">Tipo</label>
                        <select
                            id="tipo"
                            value={tipo}
                            onChange={(e) => setTipo(e.target.value)}
                        >
                            {TIPOS.map((t) => (
                                <option key={t} value={t}>
                                    {t}
                                </option>
                            ))}
                        </select>
                    </div>

                    <div className="form-group span-2">
                        <label htmlFor="precioDiario">Precio diario</label>
                        <input
                            id="precioDiario"
                            type="number"
                            step="0.01"
                            value={precioDiario}
                            onChange={(e) => setPrecioDiario(e.target.value)}
                            required
                        />
                    </div>
                </div>

                {editandoId && (
                    <div className="form-group">
                        <label htmlFor="estado">Estado</label>
                        <select
                            id="estado"
                            value={estado}
                            onChange={(e) => setEstado(e.target.value)}
                        >
                            {ESTADOS.map((e) => (
                                <option key={e} value={e}>
                                    {e}
                                </option>
                            ))}
                        </select>
                    </div>
                )}

                {formError && <p className="login-error">{formError}</p>}
                <div className="form-actions">
                    <button type="submit" disabled={guardando}>
                        {guardando
                            ? "Guardando..."
                            : editandoId
                            ? "Guardar cambios"
                            : "Agregar"}
                    </button>

                    {editandoId && (
                        <button type="button" onClick={limpiarFormulario}>
                            Cancelar
                        </button>
                    )}
                </div>
            </form>
        </div>
    </div>
);
}

export default Vehiculos;