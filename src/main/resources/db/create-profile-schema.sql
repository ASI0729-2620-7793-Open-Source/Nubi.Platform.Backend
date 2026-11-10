-- ---------------------------------------------------------------------------
-- Bounded Context: Perfil y Personalización
-- Esquema MySQL aislado: cada BC tiene su propia base de datos y no hay
-- claves foráneas entre contextos.
--
-- Ejecutar una sola vez:
--     mysql -u root -p < src/main/resources/db/create-profile-schema.sql
--
-- También se ejecuta automáticamente al arrancar el backend
-- (spring.sql.init.mode=always) y es idempotente.
-- ---------------------------------------------------------------------------

CREATE DATABASE IF NOT EXISTS `profile`
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `profile`;
