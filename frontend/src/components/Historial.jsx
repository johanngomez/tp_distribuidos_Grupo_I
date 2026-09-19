import { useEffect, useState } from 'react';
import { obtenerHistorialAlquileres } from "../services/reservaGraphQLService";
import { formatearFecha } from "../utils/dateUtils";

export default function Historial() {
  const [historial, setHistorial] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    async function cargarDatos() {
      try {
        // 1. Obtener datos del cliente autenticado
        const resMe = await fetch('http://localhost:8080/api/clientes/me', {
          headers: {
            Authorization: `Bearer ${localStorage.getItem('token')}`,
          },
        });

        if (!resMe.ok) throw new Error('Error al obtener perfil');
        const cliente = await resMe.json();

        // 2. Llamar al servicio GraphQL con el ID obtenido
        const data = await obtenerHistorialAlquileres(cliente.id);
        setHistorial(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    cargarDatos();
  }, []);

  if (loading) return <p className="page-subtitle">Cargando historial...</p>;
  if (error) return <div className="login-error">{error}</div>;

  return (
    <div className="form-card">
      <div className="card-header">
        <div className="icon-circle">📋</div>
        <h2>Mi Historial de Alquileres</h2>
      </div>

      <p className="page-subtitle">Revisá tus reservas registradas</p>

      {historial.length === 0 ? (
        <p style={{ textAlign: 'center', color: 'var(--color-ink-soft)' }}>
          No tenés reservas registradas.
        </p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>Vehículo</th>
              <th>Patente</th>
              <th>Inicio</th>
              <th>Fin</th>
              <th>Días</th>
              <th>Total</th>
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
      )}
    </div>
  );
}