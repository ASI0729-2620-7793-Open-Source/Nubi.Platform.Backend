# Despliegue del backend

## Despliegue actual: Render con base en memoria

```
Firebase Hosting (frontend)  ──https──▶  Render (Spring Boot, Docker, perfil "render" con H2 en memoria)
```

- `render.yaml` activa `SPRING_PROFILES_ACTIVE=render` (`application-render.properties`): H2 en modo MySQL,
  sin base de datos externa. **Los datos no son persistentes**: se reinician cuando Render duerme o redespliega
  el servicio, y al arrancar se vuelven a crear el catálogo y la cuenta demo `demo@nubi.app` / `nubi123`.
- Swagger: `https://<servicio>.onrender.com/swagger-ui/index.html`.
- Para crearlo: en Render, **New → Blueprint** (o **Web Service** con runtime Docker) sobre este repositorio,
  rama `main`, variable `SPRING_PROFILES_ACTIVE=render`.

## Opción con datos persistentes: Render + MySQL en la VM de Azure

```
Firebase Hosting (frontend)  ──https──▶  Render (Spring Boot, Docker)  ──3306──▶  VM Azure 68.155.147.143 (MySQL 8)
```

Quita `SPRING_PROFILES_ACTIVE` en Render y define `DB_URL`, `DB_USERNAME` y `DB_PASSWORD` como se explica abajo.

- **Backend:** Render, servicio web gratuito con Docker (`Dockerfile` y `render.yaml` en la raíz). Render da HTTPS.
- **Base de datos:** MySQL 8 en la VM de Azure. Los datos viven en el disco de la VM, así que **son persistentes**:
  no se pierden al reiniciar ni al volver a desplegar el backend.
- Las tablas las crea el propio backend al arrancar (`ddl-auto=update`) junto con el catálogo inicial y la cuenta
  demo `demo@nubi.app` / `nubi123`.

## 1. Preparar MySQL en la VM (una sola vez)

Conéctate a la VM:

```bash
ssh azureuser@68.155.147.143
```

Crea la base y un usuario propio de Nubi (cambia `CAMBIA_ESTA_CLAVE` por una contraseña larga):

```bash
sudo mysql -e "CREATE DATABASE IF NOT EXISTS nubi_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci; CREATE USER IF NOT EXISTS 'nubi_user'@'%' IDENTIFIED BY 'CAMBIA_ESTA_CLAVE'; GRANT ALL PRIVILEGES ON nubi_db.* TO 'nubi_user'@'%'; FLUSH PRIVILEGES;"
```

(Es lo mismo que `docs/deploy/azure-mysql-setup.sql`.)

Permite que MySQL acepte conexiones desde fuera de la VM (Render no está en la misma red):

```bash
sudo sed -i 's/^bind-address.*/bind-address = 0.0.0.0/' /etc/mysql/mysql.conf.d/mysqld.cnf
sudo systemctl restart mysql
sudo ufw status | grep -q active && sudo ufw allow 3306/tcp
```

BioTrack sigue funcionando igual: su usuario es local y no queda expuesto.

## 2. Abrir el puerto 3306 en Azure

En el portal de Azure: **Máquina virtual → Redes → Agregar regla de puerto de entrada**

| Campo | Valor |
| --- | --- |
| Puerto de destino | `3306` |
| Protocolo | TCP |
| Origen | Any (Render usa IPs variables en el plan gratuito) |
| Nombre | `MySQL-Nubi` |

Como el puerto queda abierto, usa una contraseña larga para `nubi_user`; ese usuario solo puede ver `nubi_db`.

## 3. Crear el servicio en Render

1. En https://dashboard.render.com: **New → Blueprint** y elige el repositorio `Nubi.Platform.Backend`.
2. Render lee `render.yaml` y pide estas variables:

| Variable | Valor |
| --- | --- |
| `DB_URL` | `jdbc:mysql://68.155.147.143:3306/nubi_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC` |
| `DB_USERNAME` | `nubi_user` |
| `DB_PASSWORD` | la contraseña del paso 1 |

3. **Apply**. El primer build tarda unos minutos. Al terminar, el backend queda en una URL como
   `https://nubi-platform-backend.onrender.com` (Render puede agregarle un sufijo).
4. Prueba: abre `https://<tu-url>.onrender.com/api/v1/calming-resources`; debe responder una lista JSON.

> El plan gratuito de Render "duerme" el servicio tras 15 minutos sin uso: la primera llamada después tarda
> alrededor de un minuto. Los datos no se pierden porque están en MySQL, no en Render.

## 4. Conectar el frontend

En el frontend, `Nubi/src/environments/environment.ts`:

```ts
apiBaseUrl: 'https://<tu-url>.onrender.com/api/v1',
```

Luego `npm run build` y `firebase deploy --only hosting`.

## Copias de seguridad (recomendado)

En la VM, un respaldo diario a las 3:00 a. m.:

```bash
sudo mkdir -p /var/backups/nubi
(sudo crontab -l 2>/dev/null; echo "0 3 * * * mysqldump nubi_db | gzip > /var/backups/nubi/nubi_\$(date +\%F).sql.gz") | sudo crontab -
```
