import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { FileText, User, Mail, Phone, Calendar, Lock, Plus, Users } from "lucide-react";
import {
    listarClientes,
    crearCliente,
    modificarCliente,
    bajaLogicaCliente,
} from "../services/clienteService";
import CampoConIcono from "./CampoConIcono";
import { useMensajeExito } from "../utils/useMensajeExito";

function Clientes() {
    const navigate = useNavigate();

    const [clientes, setClientes] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);
    const [mostrarListado, setMostrarListado] = useState(false);
    const [busqueda, setBusqueda] = useState("");

    const [editandoId, setEditandoId] = useState(null);

    const [documento, setDocumento] = useState("");
    const [nombre, setNombre] = useState("");
    const [apellido, setApellido] = useState("");
    const [email, setEmail] = useState("");
    const [telefono, setTelefono] = useState("");
    const [fechaNacimiento, setFechaNacimiento] = useState("");
    const [password, setPassword] = useState("");
    const [guardando, setGuardando] = useState(false);
    const [formError, setFormError] = useState("");
    const [mensajeExito, mostrarMensajeExito] = useMensajeExito();

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

        cargarClientes();
    }, []);

    async function cargarClientes() {
        setError("");
        setLoading(true);

        try {
            const data = await listarClientes();
            setClientes(data);
        } catch (error) {
            setError(error.message);
        } finally {
            setLoading(false);
        }
    }

    function limpiarFormulario() {
        setEditandoId(null);
        setDocumento("");
        setNombre("");
        setApellido("");
        setEmail("");
        setTelefono("");
        setFechaNacimiento("");
        setPassword("");
    }

    function handleEditar(cliente) {
        setEditandoId(cliente.id);
        setDocumento(cliente.documento);
        setNombre(cliente.nombre);
        setApellido(cliente.apellido);
        setEmail(cliente.email);
        setTelefono(cliente.telefono || "");
        setFechaNacimiento(cliente.fechaNacimiento || "");
        setPassword("");
    }

    async function handleBaja(id) {
        const confirmar = window.confirm(
            "¿Seguro que querés dar de baja este cliente?"
        );

        if (!confirmar) {
            return;
        }

        try {
            await bajaLogicaCliente(id);
            await cargarClientes();
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
                await modificarCliente(editandoId, {
                    nombre,
                    apellido,
                    telefono,
                    fechaNacimiento: fechaNacimiento || null,
                });
            } else {
                await crearCliente({
                    documento,
                    nombre,
                    apellido,
                    email,
                    telefono,
                    fechaNacimiento: fechaNacimiento || null,
                    password,
                });
            }
            
            mostrarMensajeExito(editandoId ? "Cliente modificado con éxito" : "Cliente agregado con éxito");
            limpiarFormulario();
            await cargarClientes();

        } catch (error) {
            setFormError(error.message);
        } finally {
            setGuardando(false);
        }
    }

    if (loading) {
        return <p>Cargando clientes...</p>;
    }

    const clientesFiltrados = clientes.filter((c) =>
        c.documento.toLowerCase().includes(busqueda.toLowerCase())
    );

    return (
        <div>
            <h1>Gestión de clientes</h1>
            <p className="page-subtitle">
                Administrá los clientes registrados y agregá nuevos.
            </p>

            {error && <p className="login-error">{error}</p>}

            <button
                type="button"
                className="toggle-listado"
                onClick={() => setMostrarListado(!mostrarListado)}
            >
                <Users size={16} />
                Clientes registrados
                <span className="chevron">{mostrarListado ? "▲" : "▼"}</span>
            </button>

            {mostrarListado && (
                <>
                    <div className="form-group">
                        <label htmlFor="busqueda">Buscar por documento</label>
                        <input
                            id="busqueda"
                            type="text"
                            value={busqueda}
                            onChange={(e) => setBusqueda(e.target.value)}
                            placeholder="Ej: 30123456"
                        />
                    </div>

                    <table>
                        <thead>
                            <tr>
                                <th>Documento</th>
                                <th>Nombre</th>
                                <th>Apellido</th>
                                <th>Email</th>
                                <th>Teléfono</th>
                                <th>Activo</th>
                                <th>Acciones</th>
                            </tr>
                        </thead>
                        <tbody>
                            {clientesFiltrados.map((cliente) => (
                                <tr key={cliente.id}>
                                    <td>{cliente.documento}</td>
                                    <td>{cliente.nombre}</td>
                                    <td>{cliente.apellido}</td>
                                    <td>{cliente.email}</td>
                                    <td>{cliente.telefono}</td>
                                    <td>{cliente.activo ? "Sí" : "No"}</td>
                                    <td>
                                        <button onClick={() => handleEditar(cliente)}>
                                            Editar
                                        </button>
                                        <button
                                            onClick={() => handleBaja(cliente.id)}
                                            disabled={!cliente.activo}
                                        >
                                            Dar de baja
                                        </button>
                                    </td>
                                </tr>
                            ))}
                        </tbody>
                    </table>

                    {clientesFiltrados.length === 0 && (
                        <p>No se encontraron clientes.</p>
                    )}
                </>
            )}
            {mensajeExito && <p className="mensaje-exito">{mensajeExito}</p>}
            <div className="form-card">
                <div className="card-header">
                    <span className="icon-circle">
                        <Plus size={16} />
                    </span>
                    <h2>{editandoId ? "Editar cliente" : "Nuevo cliente"}</h2>
                </div>

                <form onSubmit={handleSubmit}>
                    <div className="form-grid">
                        <CampoConIcono
                        icon={FileText}
                        label="Documento"
                        id="documento"
                        value={documento}
                        onChange={(e) => {
                            const valor = e.target.value.replace(/\D/g, "");
                            if (valor.length <= 8) {
                                setDocumento(valor);
                            }
                        }}
                        disabled={!!editandoId}
                        required
                    />

                        <CampoConIcono
                            icon={User}
                            label="Nombre"
                            id="nombre"
                            value={nombre}
                            onChange={(e) => setNombre(e.target.value)}
                            required
                        />

                        <CampoConIcono
                            icon={User}
                            label="Apellido"
                            id="apellido"
                            value={apellido}
                            onChange={(e) => setApellido(e.target.value)}
                            required
                        />

                        <CampoConIcono
                            icon={Mail}
                            label="Email"
                            id="email"
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            disabled={!!editandoId}
                            required
                        />

                        <CampoConIcono
                            icon={Phone}
                            label="Teléfono"
                            id="telefono"
                            value={telefono}
                            onChange={(e) => {
                                const valor = e.target.value.replace(/\D/g, "");
                                if (valor.length <= 10) {
                                    setTelefono(valor);
                                }
                            }}
                        />

                        <CampoConIcono
                            icon={Calendar}
                            label="Fecha de nacimiento"
                            id="fechaNacimiento"
                            type="date"
                            value={fechaNacimiento}
                            onChange={(e) => setFechaNacimiento(e.target.value)}
                            min="1900-01-01"
                            max="2024-12-31"
                        />

                        {!editandoId && (
                            <div className="span-2">
                                <CampoConIcono
                                    icon={Lock}
                                    label="Contraseña"
                                    id="password"
                                    type="password"
                                    value={password}
                                    onChange={(e) => setPassword(e.target.value)}
                                    required
                                />
                            </div>
                        )}
                    </div>

                    {formError && <p className="login-error">{formError}</p>}

                    <div className="form-actions">
                        <button type="submit" disabled={guardando}>
                            <Plus size={16} />{" "}
                            {guardando
                                ? "Guardando..."
                                : editandoId
                                ? "Guardar cambios"
                                : "Agregar cliente"}
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

export default Clientes;