-- Reparacion de instalaciones que importaron PENDIENTE con el enum antiguo.
-- Conserva los vehiculos y su indicador activo; no recarga los datos demo.
USE rentar_vehiculos;

ALTER TABLE vehiculo
  MODIFY COLUMN estado
    ENUM('DISPONIBLE','EN_ALQUILER','PENDIENTE','RESERVADO','VEHICLE_STATUS_UNSPECIFIED')
    DEFAULT NULL;

-- El dump identifica este vehiculo como PENDIENTE e inactivo.
-- Solo restaura el estado si todavia contiene el valor vacio de la importacion.
UPDATE vehiculo
SET estado = 'PENDIENTE'
WHERE id = 50 AND patente = 'ROBADO01' AND estado = '';

SELECT id, patente, estado, activo FROM vehiculo WHERE id = 50;
SELECT id, patente, estado FROM vehiculo WHERE estado = '';
