import { Routes, Route, Navigate } from 'react-router-dom'
import Login from './components/Login'
import Vehiculos from './components/Vehiculos'
import Disponibilidad from './components/Disponibilidad'
import Clientes from './components/Clientes'
import Reservas from './components/Reservas'
import Historial from './components/Historial'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/login" replace />} />
      <Route path="/login" element={<Login />} />
      <Route path="/vehiculos" element={<Vehiculos />} />
      <Route path="/disponibilidad" element={<Disponibilidad />} />
      <Route path="/clientes" element={<Clientes />} />
      <Route path="/reservas" element={<Reservas />} />
      <Route path="/historial" element={<Historial />} />
    </Routes>
  )
}

export default App;