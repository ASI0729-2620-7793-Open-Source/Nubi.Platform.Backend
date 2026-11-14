-- Base de datos de Nubi en la VM de Azure (MySQL 8).
-- Ejecutar en la VM con:  sudo mysql < azure-mysql-setup.sql
-- ANTES cambia CAMBIA_ESTA_CLAVE por una contraseña larga (y anótala para Render).

CREATE DATABASE IF NOT EXISTS nubi_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

-- Usuario propio de Nubi: solo ve nubi_db (no toca la base de BioTrack ni otras).
CREATE USER IF NOT EXISTS 'nubi_user'@'%' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE';
GRANT ALL PRIVILEGES ON nubi_db.* TO 'nubi_user'@'%';
FLUSH PRIVILEGES;

-- Las tablas las crea el backend al arrancar (spring.jpa.hibernate.ddl-auto=update).
