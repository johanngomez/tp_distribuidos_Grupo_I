// Campo de formulario con un ícono a la izquierda del input.
// Reutilizado en cualquier pantalla con formularios (clientes,
// vehículos, etc.) para no repetir la misma estructura de div+icono.
function CampoConIcono({ icon: Icon, label, id, ...inputProps }) {
    return (
        <div className="form-group">
            <label htmlFor={id}>{label}</label>
            <div className="input-icon">
                <Icon size={16} />
                <input id={id} {...inputProps} />
            </div>
        </div>
    );
}

export default CampoConIcono;