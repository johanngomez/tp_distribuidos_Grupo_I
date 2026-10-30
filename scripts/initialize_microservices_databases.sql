-- Inicializa las bases independientes del Hito 2/3.
-- Ejecutar despues de script_rentar_V2.sql para conservar los datos iniciales.

CREATE DATABASE IF NOT EXISTS `rentar_clientes`;
CREATE DATABASE IF NOT EXISTS `rentar_vehiculos`;
CREATE DATABASE IF NOT EXISTS `rentar_reservas`;

CREATE TABLE IF NOT EXISTS `rentar_clientes`.`cliente` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `documento` varchar(20) NOT NULL,
  `nombre` varchar(50) NOT NULL,
  `apellido` varchar(50) NOT NULL,
  `email` varchar(100) NOT NULL,
  `telefono` varchar(255) DEFAULT NULL,
  `fecha_nacimiento` date DEFAULT NULL,
  `activo` bit(1) NOT NULL DEFAULT b'1',
  `password` varchar(255) NOT NULL,
  `es_admin` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_cliente_documento` (`documento`),
  UNIQUE KEY `uk_cliente_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS `rentar_reservas`.`reserva` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cliente_id` bigint NOT NULL,
  `vehiculo_id` bigint NOT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime NOT NULL,
  `precio_diario` double NOT NULL,
  `importe_total` double NOT NULL,
  `resultado_mantenimiento` varchar(32) DEFAULT NULL,
  `estado` varchar(32) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_reserva_cliente` (`cliente_id`),
  KEY `idx_reserva_vehiculo` (`vehiculo_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

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
  `tipo` varchar(32) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_vehiculo_patente` (`patente`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Copia datos del esquema monolitico rentar, si existen.
INSERT IGNORE INTO `rentar_clientes`.`cliente`
  (`id`,`documento`,`nombre`,`apellido`,`email`,`telefono`,`fecha_nacimiento`,`activo`,`password`,`es_admin`)
SELECT `id`, CAST(`documento` AS CHAR), `nombre`, `apellido`, `email`, CAST(`telefono` AS CHAR),
       `fecha_nacimiento`, `activo`, `password`, `es_admin`
FROM `rentar`.`cliente`;

INSERT IGNORE INTO `rentar_vehiculos`.`vehiculo`
  (`id`,`patente`,`marca`,`modelo`,`anio`,`color`,`tipo`,`precio_diario`,`estado`,`activo`)
SELECT `id`,`patente`,`marca`,`modelo`,`anio`,`color`,
       `tipo_vehiculo`,`precio_diario`,`estado`,`activo`
FROM `rentar`.`vehiculo`;

INSERT IGNORE INTO `rentar_reservas`.`reserva`
  (`id`,`cliente_id`,`vehiculo_id`,`fecha_inicio`,`fecha_fin`,`precio_diario`,`importe_total`,`estado`)
SELECT `id`,`cliente_id`,`vehiculo_id`,`fecha_inicio`,`fecha_fin`,`precio_diario`,`importe_total`,`estado`
FROM `rentar`.`reserva`;

-- Deja los autoincrementos por encima de los IDs copiados.
SET @next_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM `rentar_clientes`.`cliente`);
SET @sql = CONCAT('ALTER TABLE `rentar_clientes`.`cliente` AUTO_INCREMENT = ', @next_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM `rentar_vehiculos`.`vehiculo`);
SET @sql = CONCAT('ALTER TABLE `rentar_vehiculos`.`vehiculo` AUTO_INCREMENT = ', @next_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @next_id = (SELECT COALESCE(MAX(id), 0) + 1 FROM `rentar_reservas`.`reserva`);
SET @sql = CONCAT('ALTER TABLE `rentar_reservas`.`reserva` AUTO_INCREMENT = ', @next_id);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;
