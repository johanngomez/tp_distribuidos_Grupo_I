-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: rentar
-- ------------------------------------------------------
-- Server version	8.4.11

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `reserva`
--

DROP TABLE IF EXISTS `reserva`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reserva` (
  `id` int NOT NULL AUTO_INCREMENT,
  `cliente_id` int NOT NULL,
  `vehiculo_id` int NOT NULL,
  `fecha_inicio` datetime NOT NULL,
  `fecha_fin` datetime NOT NULL,
  `precio_diario` decimal(10,2) NOT NULL,
  `importe_total` decimal(10,2) NOT NULL,
  `estado` enum('CONFIRMADA','CANCELADA') NOT NULL DEFAULT 'CONFIRMADA',
  PRIMARY KEY (`id`),
  KEY `fk_reserva_usuario` (`cliente_id`),
  KEY `fk_reserva_vehiculo` (`vehiculo_id`),
  CONSTRAINT `fk_reserva_usuario` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_reserva_vehiculo` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserva`
--

LOCK TABLES `reserva` WRITE;
/*!40000 ALTER TABLE `reserva` DISABLE KEYS */;
INSERT INTO `reserva` VALUES (1,3,3,'2026-10-01 10:00:00','2026-10-05 10:00:00',48000.00,192000.00,'CONFIRMADA'),(2,4,6,'2026-10-10 09:00:00','2026-10-14 09:00:00',58000.00,232000.00,'CONFIRMADA'),(3,5,10,'2026-10-15 08:00:00','2026-10-18 08:00:00',80000.00,240000.00,'CONFIRMADA'),(4,6,16,'2026-11-01 12:00:00','2026-11-03 12:00:00',140000.00,280000.00,'CONFIRMADA'),(5,7,1,'2026-10-20 10:00:00','2026-10-22 10:00:00',45000.00,90000.00,'CONFIRMADA'),(6,3,5,'2026-08-01 14:00:00','2026-08-04 14:00:00',60000.00,180000.00,'CANCELADA'),(7,8,9,'2026-08-10 11:00:00','2026-08-15 11:00:00',75000.00,375000.00,'CANCELADA'),(8,9,12,'2026-09-01 09:00:00','2026-09-03 09:00:00',38000.00,76000.00,'CANCELADA'),(9,4,15,'2026-07-15 10:00:00','2026-07-20 10:00:00',120000.00,600000.00,'CANCELADA'),(10,3,4,'2026-06-01 08:00:00','2026-06-05 08:00:00',35000.00,140000.00,'CONFIRMADA'),(11,5,8,'2026-06-10 10:00:00','2026-06-15 10:00:00',65000.00,325000.00,'CONFIRMADA'),(12,6,14,'2026-07-01 09:00:00','2026-07-04 09:00:00',40000.00,120000.00,'CONFIRMADA'),(13,10,2,'2026-05-12 10:00:00','2026-05-15 10:00:00',42000.00,126000.00,'CONFIRMADA');
/*!40000 ALTER TABLE `reserva` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usuario`
--

DROP TABLE IF EXISTS `usuario`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `usuario` (
  `id` int NOT NULL AUTO_INCREMENT,
  `documento` int NOT NULL,
  `nombre` varchar(100) NOT NULL,
  `apellido` varchar(100) NOT NULL,
  `email` varchar(150) NOT NULL,
  `telefono` bigint DEFAULT NULL,
  `fecha_nacimiento` date NOT NULL,
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  `rol` enum('ADMINISTRADOR','CLIENTE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `documento` (`documento`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,10000001,'Admin','Sistema','admin@rentar.com',1100000001,'1980-01-01',1,'ADMINISTRADOR'),(2,20000001,'Juan','Pérez','juan.perez@example.com',1144440001,'1990-03-15',1,'CLIENTE'),(3,20000002,'Maria','González','maria.gonzalez@example.com',1144440002,'1992-07-22',1,'CLIENTE'),(4,20000003,'Lucas','Rodríguez','lucas.rodriguez@example.com',1144440003,'1988-11-05',1,'CLIENTE'),(5,20000004,'Ana','Martínez','ana.martinez@example.com',1144440004,'1995-01-30',1,'CLIENTE'),(6,20000005,'Roberto','Sánchez','roberto.sanchez@example.com',1144440005,'1982-09-18',1,'CLIENTE'),(7,20000006,'Sofia','López','sofia.lopez@example.com',1144440006,'1998-04-12',1,'CLIENTE'),(8,20000007,'Diego','Fernández','diego.fernandez@example.com',1144440007,'1991-12-08',1,'CLIENTE'),(9,20000008,'Laura','Díaz','laura.diaz@example.com',1144440008,'1987-06-25',1,'CLIENTE'),(10,30000001,'Esteban','Quito','esteban.quito@example.com',1155550001,'1993-02-14',0,'CLIENTE'),(11,30000002,'Valeria','Blanco','valeria.blanco@example.com',1155550002,'1996-10-20',0,'CLIENTE');
/*!40000 ALTER TABLE `usuario` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `vehiculo`
--

DROP TABLE IF EXISTS `vehiculo`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `vehiculo` (
  `id` int NOT NULL AUTO_INCREMENT,
  `patente` varchar(20) NOT NULL,
  `marca` varchar(50) NOT NULL,
  `modelo` varchar(50) NOT NULL,
  `anio` int NOT NULL,
  `color` varchar(30) DEFAULT NULL,
  `tipo_vehiculo` enum('SEDAN','SUV','PICKUP','COUPE','HATCHBACK') NOT NULL,
  `precio_diario` decimal(10,2) NOT NULL,
  `estado` enum('DISPONIBLE','RESERVADO','EN_ALQUILER') NOT NULL DEFAULT 'DISPONIBLE',
  `activo` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `patente` (`patente`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculo`
--

LOCK TABLES `vehiculo` WRITE;
/*!40000 ALTER TABLE `vehiculo` DISABLE KEYS */;
INSERT INTO `vehiculo` VALUES (1,'AAA111','Toyota','Corolla',2022,'Gris','SEDAN',45000.00,'DISPONIBLE',1),(2,'AAA112','Chevrolet','Cruze',2021,'Blanco','SEDAN',42000.00,'DISPONIBLE',1),(3,'AAA113','Nissan','Sentra',2023,'Negro','SEDAN',48000.00,'RESERVADO',1),(4,'AAA114','Fiat','Cronos',2020,'Rojo','SEDAN',35000.00,'EN_ALQUILER',1),(5,'BBB221','Volkswagen','Taos',2024,'Blanco','SUV',60000.00,'DISPONIBLE',1),(6,'BBB222','Jeep','Renegade',2022,'Verde','SUV',58000.00,'RESERVADO',1),(7,'BBB223','Honda','HR-V',2023,'Gris','SUV',62000.00,'DISPONIBLE',1),(8,'BBB224','Ford','Territory',2023,'Azul','SUV',65000.00,'EN_ALQUILER',1),(9,'CCC331','Ford','Ranger',2023,'Negro','PICKUP',75000.00,'DISPONIBLE',1),(10,'CCC332','Toyota','Hilux',2024,'Rojo','PICKUP',80000.00,'RESERVADO',1),(11,'CCC333','Volkswagen','Amarok',2022,'Plata','PICKUP',78000.00,'DISPONIBLE',1),(12,'DDD441','Peugeot','208',2021,'Azul','HATCHBACK',38000.00,'DISPONIBLE',1),(13,'DDD442','Renault','Sandero',2020,'Gris','HATCHBACK',32000.00,'DISPONIBLE',1),(14,'DDD443','Volkswagen','Polo',2023,'Negro','HATCHBACK',40000.00,'EN_ALQUILER',1),(15,'EEE551','Ford','Mustang',2020,'Rojo','COUPE',120000.00,'DISPONIBLE',1),(16,'EEE552','BMW','Serie 4',2022,'Negro','COUPE',140000.00,'RESERVADO',1),(17,'ZZZ991','Renault','Clio',2015,'Blanco','HATCHBACK',25000.00,'DISPONIBLE',0),(18,'ZZZ992','Fiat','Siena',2016,'Gris','SEDAN',28000.00,'DISPONIBLE',0);
/*!40000 ALTER TABLE `vehiculo` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-09-11 23:55:18
