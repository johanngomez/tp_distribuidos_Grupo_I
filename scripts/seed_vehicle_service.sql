-- Inicializa la base separada utilizada por Vehicle Service.
-- Ejecutar después de script_rentar_V2.sql y antes de iniciar los servicios,
-- o después de que Hibernate haya creado la tabla vehiculo.

CREATE DATABASE IF NOT EXISTS `rentar_vehiculos`;

CREATE TABLE IF NOT EXISTS `rentar_vehiculos`.`vehiculo` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `activo` bit(1) DEFAULT NULL,
  `anio` int DEFAULT NULL,
  `color` varchar(255) DEFAULT NULL,
  `estado` varchar(32) DEFAULT NULL,
  `marca` varchar(255) NOT NULL,
  `modelo` varchar(255) NOT NULL,
  `patente` varchar(255) NOT NULL,
  `precio_diario` double NOT NULL,
  `tipo` enum('COUPE','HATCHBACK','PICKUP','SEDAN','SUV','VEHICLE_TYPE_UNSPECIFIED') DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehiculo_patente_hito3` (`patente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Copia los vehículos históricos conservando sus IDs para que las reservas
-- existentes sigan apuntando al mismo vehículo. Si ya existe un ID o patente,
-- se conserva el registro que ya está en la base separada.
INSERT INTO `rentar_vehiculos`.`vehiculo`
  (`id`, `patente`, `marca`, `modelo`, `anio`, `color`, `tipo`, `precio_diario`, `estado`, `activo`)
SELECT
  v.`id`,
  v.`patente`,
  v.`marca`,
  v.`modelo`,
  v.`anio`,
  v.`color`,
  v.`tipo_vehiculo`,
  v.`precio_diario`,
  v.`estado`,
  v.`activo`
FROM `rentar`.`vehiculo` v
LEFT JOIN `rentar_vehiculos`.`vehiculo` existente
  ON existente.`id` = v.`id` OR existente.`patente` = v.`patente`
WHERE existente.`id` IS NULL AND existente.`patente` IS NULL;

-- Deja el próximo ID por encima del mayor ID copiado.
SET @next_vehicle_id = (SELECT COALESCE(MAX(`id`), 0) + 1 FROM `rentar_vehiculos`.`vehiculo`);
SET @sql = CONCAT('ALTER TABLE `rentar_vehiculos`.`vehiculo` AUTO_INCREMENT = ', @next_vehicle_id);
PREPARE set_auto_increment FROM @sql;
EXECUTE set_auto_increment;
DEALLOCATE PREPARE set_auto_increment;

SELECT COUNT(*) AS vehiculos_migrados FROM `rentar_vehiculos`.`vehiculo`;
