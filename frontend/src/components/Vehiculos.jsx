import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useMensajeExito } from "../utils/useMensajeExito";

//importamos iconos de lucide-react
import {
    IdCard,
    Tag,
    Car,
    Calendar,
    Palette,
    LayoutGrid,
    DollarSign,
    Plus,
} from "lucide-react";
import {
    listarVehiculos,
    crearVehiculo,
    modificarVehiculo,
    eliminarVehiculo,
} from "../services/vehiculoService";
import CampoConIcono from "./CampoConIcono";

const TIPOS = ["SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"];
const ESTADOS = ["DISPONIBLE", "RESERVADO", "EN_ALQUILER"];

function Vehiculos() {
    const navigate = useNavigate();

    const [vehiculos, setVehiculos] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [mostrarListado, setMostrarListado] = useState(false);
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
    const [mensajeExito, mostrarMensajeExito] = useMensajeExito();
    
    //ingresar a /vehiculos solo si es admin
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
    //barra de busqueda para filtrar por patente
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
    //limpiar formulario una vez que se agrega un vehiculo 
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
    //funcion para editar un vehiculo, se cargan los datos del vehiculo en el formulario
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
    //dar de baja un vehiculo, se confirma la accion y se llama a la funcion eliminarVehiculo
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
    //funcion para agregar o modificar un vehiculo, se valida el formulario y se llama a la funcion crearVehiculo o modificarVehiculo
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
            mostrarMensajeExito(editandoId ? "Vehículo modificado con éxito" : "Vehículo agregado con éxito");
            limpiarFormulario();
            await cargarVehiculos();

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
    //filtrar los vehiculos por patente, si la busqueda esta vacia se muestran todos los vehiculos
    const vehiculosFiltrados = vehiculos.filter((v) =>
        v.patente.toLowerCase().includes(busqueda.toLowerCase())
    );

    return (
        <div>
            <h1>Gestión de vehículos</h1>
            <p className="page-subtitle">
                Administrá vehículos y agregá nuevos.
            </p>

            {error && <p className="login-error">{error}</p>}

            <button
                type="button"
                className="toggle-listado"
                onClick={() => setMostrarListado(!mostrarListado)}
            >
                <Car size={16} />
                Vehículos registrados
                <span className="chevron">{mostrarListado ? "▲" : "▼"}</span>
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

                    {vehiculosFiltrados.length === 0 && (
                        <p>No se encontraron vehículos.</p>
                    )}
                </>
            )}

            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            <div className="form-card">
                <div className="card-header">
                    <span className="icon-circle">
                        <Plus size={16} />
                    </span>
                    <h2>{editandoId ? "Editar vehículo" : "Nuevo vehículo"}</h2>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <CampoConIcono
                            icon={IdCard}
                            label="Patente"
                            id="patente"
                            value={patente}
                            onChange={(e) => setPatente(e.target.value)}
                            disabled={!!editandoId}
                            required
                        />

                        <CampoConIcono
                            icon={Tag}
                            label="Marca"
                            id="marca"
                            value={marca}
                            onChange={(e) => setMarca(e.target.value)}
                            required
                        />

                        <CampoConIcono
                            icon={Car}
                            label="Modelo"
                            id="modelo"
                            value={modelo}
                            onChange={(e) => setModelo(e.target.value)}
                            required
                        />

                        <CampoConIcono
                            icon={Calendar}
                            label="Año"
                            id="anio"
                            type="number"
                            value={anio}
                            onChange={(e) => setAnio(e.target.value)}
                            required
                        />

                        <CampoConIcono
                            icon={Palette}
                            label="Color"
                            id="color"
                            value={color}
                            onChange={(e) => setColor(e.target.value)}
                        />

                        <div className="form-group">
                            <label htmlFor="tipo">Tipo</label>
                            <div className="input-icon">
                                <LayoutGrid size={16} />
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
                        </div>

                        <div className="span-2">
                            <CampoConIcono
                                icon={DollarSign}
                                label="Precio diario"
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
                            <Plus size={16} />{" "}
                            {guardando
                                ? "Guardando..."
                                : editandoId
                                ? "Guardar cambios"
                                : "Agregar vehículo"}
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