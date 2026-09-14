import { NavLink, useNavigate, Outlet } from "react-router-dom";

function Layout() {
    const navigate = useNavigate();
    const esAdmin = localStorage.getItem("esAdmin") === "true";

    function handleLogout() {
        localStorage.clear();
        navigate("/login");
    }

    return (
        <div>
            <header className="app-header">
                
                <nav className="app-nav">
                    {esAdmin ? (
                            //navlinks para saber en que pagina estoy, si es la pagina activa le pongo la clase active
                        <>  
                            <NavLink to="/vehiculos" className={({ isActive }) => isActive ? "active" : ""}>
                                Vehículos
                            </NavLink>
                            <NavLink to="/clientes" className={({ isActive }) => isActive ? "active" : ""}>
                                Clientes
                            </NavLink>
                            <NavLink to="/reservas" className={({ isActive }) => isActive ? "active" : ""}>
                                Reservas
                            </NavLink>
                        </>
                    ) : (
                        <>
                            <NavLink to="/disponibilidad" className={({ isActive }) => isActive ? "active" : ""}>
                                Buscar vehículos
                            </NavLink>
                            <NavLink to="/reservas" className={({ isActive }) => isActive ? "active" : ""}>
                                Mis reservas
                            </NavLink>
                            <NavLink to="/historial" className={({ isActive }) => isActive ? "active" : ""}>
                                Historial
                            </NavLink>
                        </>
                    )}
                    <button onClick={handleLogout}>Salir</button>
                </nav>
            </header>

            <main className="app-content">
                <Outlet />
            </main>
        </div>
    );
}

export default Layout;