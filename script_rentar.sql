-- MySQL dump 10.13  Distrib 8.0.46, for Win64 (x86_64)
--
-- Host: localhost    Database: rentar
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
  `fechaInicio` date NOT NULL,
  `fechaFin` date NOT NULL,
  `precioDiario` decimal(10,2) NOT NULL,
  `importeTotal` decimal(10,2) NOT NULL,
  `estado` enum('PENDIENTE','ACTIVA','COMPLETADA','CANCELADA') NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK_Reserva_Usuario` (`cliente_id`),
  KEY `FK_Reserva_Vehiculo` (`vehiculo_id`),
  CONSTRAINT `FK_Reserva_Usuario` FOREIGN KEY (`cliente_id`) REFERENCES `usuario` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `FK_Reserva_Vehiculo` FOREIGN KEY (`vehiculo_id`) REFERENCES `vehiculo` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reserva`
--

LOCK TABLES `reserva` WRITE;
/*!40000 ALTER TABLE `reserva` DISABLE KEYS */;
INSERT INTO `reserva` VALUES (1,2,1,'2026-09-10','2026-09-15',40000.00,200000.00,'ACTIVA'),(2,3,2,'2026-09-20','2026-09-23',55000.00,165000.00,'PENDIENTE'),(3,4,3,'2026-09-01','2026-09-07',80000.00,480000.00,'COMPLETADA'),(4,5,5,'2026-09-12','2026-09-14',35000.00,70000.00,'PENDIENTE'),(5,2,4,'2026-10-05','2026-10-06',120000.00,120000.00,'CANCELADA');
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
  `fecha_of_nacimiento` date NOT NULL,
  `activo_inactivo` tinyint(1) DEFAULT '1',
  `rol` enum('ADMINISTRADOR','CLIENTE') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `documento` (`documento`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usuario`
--

LOCK TABLES `usuario` WRITE;
/*!40000 ALTER TABLE `usuario` DISABLE KEYS */;
INSERT INTO `usuario` VALUES (1,11222333,'Carlos','Gómez','carlos.gomez@example.com',1155551111,'1985-03-12',1,'ADMINISTRADOR'),(2,22333444,'María','López','maria.lopez@example.com',1155552222,'1992-07-25',1,'CLIENTE'),(3,33444555,'Juan','Martínez','juan.martinez@example.com',1155553333,'1998-11-05',1,'CLIENTE'),(4,44555666,'Ana','Rodríguez','ana.rodriguez@example.com',1155554444,'1990-01-30',1,'CLIENTE'),(5,55666777,'Diego','Fernández','diego.fernandez@example.com',1155555555,'2001-04-18',1,'CLIENTE'),(6,66777888,'Laura','Pérez','laura.perez@example.com',1155556666,'1995-09-14',0,'CLIENTE');
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
  `tipo_de_vehiculo` enum('SEDAN','SUV','PICKUP','COUPE','HATCHBACK') NOT NULL,
  `precio_diario` decimal(10,2) NOT NULL,
  `estado` enum('DISPONIBLE','RESERVADO','EN_ALQUILER') NOT NULL,
  `activo_inactivo` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `patente` (`patente`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `vehiculo`
--

LOCK TABLES `vehiculo` WRITE;
/*!40000 ALTER TABLE `vehiculo` DISABLE KEYS */;
INSERT INTO `vehiculo` VALUES (1,'ABC123A','Toyota','Corolla',2021,'Blanco','SEDAN',40000.00,'DISPONIBLE',1),(2,'DEF456B','Honda','CR-V',2020,'Gris','SUV',55000.00,'RESERVADO',1),(3,'GHI789C','Volkswagen','Amarok',2023,'Negro','PICKUP',80000.00,'EN_ALQUILER',1),(4,'JKL012D','Ford','Mustang',2019,'Rojo','COUPE',120000.00,'DISPONIBLE',1),(5,'MNO345E','Chevrolet','Onix',2022,'Azul','HATCHBACK',35000.00,'DISPONIBLE',1),(6,'PQR678F','Renault','Duster',2018,'Verde','SUV',48000.00,'DISPONIBLE',0);
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

-- Dump completed on 2026-09-08 22:07:34
