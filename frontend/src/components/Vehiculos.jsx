import { useState, useEffect } from "react";
import { listarVehiculos, crearVehiculo } from "../services/vehiculoService";

const TIPOS = ["SEDAN", "SUV", "PICKUP", "COUPE", "HATCHBACK"];

function Vehiculos() {
    const [vehiculos, setVehiculos] = useState([]);
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(true);

    const [patente, setPatente] = useState("");
    const [marca, setMarca] = useState("");
    const [modelo, setModelo] = useState("");
    const [anio, setAnio] = useState("");
    const [color, setColor] = useState("");
    const [tipo, setTipo] = useState(TIPOS[0]);
    const [precioDiario, setPrecioDiario] = useState("");
    const [guardando, setGuardando] = useState(false);
    const [formError, setFormError] = useState("");

    useEffect(() => {
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

    async function handleSubmit(event) {
        event.preventDefault();

        setFormError("");
        setGuardando(true);

        try {
            await crearVehiculo({
                patente,
                marca,
                modelo,
                anio: Number(anio),
                color,
                tipo,
                precioDiario: Number(precioDiario),
            });

            setPatente("");
            setMarca("");
            setModelo("");
            setAnio("");
            setColor("");
            setTipo(TIPOS[0]);
            setPrecioDiario("");

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

    return (
        <div>
            <h1>Gestión de vehículos</h1>

            <h2>Nuevo vehículo</h2>

            <form onSubmit={handleSubmit}>
                <div className="form-group">
                    <label htmlFor="patente">Patente</label>
                    <input
                        id="patente"
                        value={patente}
                        onChange={(e) => setPatente(e.target.value)}
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

                <div className="form-group">
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

                {formError && <p className="login-error">{formError}</p>}

                <button type="submit" disabled={guardando}>
                    {guardando ? "Guardando..." : "Crear vehículo"}
                </button>
            </form>

            <h2>Listado</h2>

            {error && <p className="login-error">{error}</p>}

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
                    </tr>
                </thead>
                <tbody>
                    {vehiculos.map((vehiculo) => (
                        <tr key={vehiculo.id}>
                            <td>{vehiculo.patente}</td>
                            <td>{vehiculo.marca}</td>
                            <td>{vehiculo.modelo}</td>
                            <td>{vehiculo.anio}</td>
                            <td>{vehiculo.tipo}</td>
                            <td>{vehiculo.precioDiario}</td>
                            <td>{vehiculo.estado}</td>
                            <td>{vehiculo.activo ? "Sí" : "No"}</td>
                        </tr>
                    ))}
                </tbody>
            </table>

            {vehiculos.length === 0 && <p>No hay vehículos cargados todavía.</p>}
        </div>
    );
}

export default Vehiculos;