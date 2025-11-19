# 🌸 BeautyBooking - Backend API

Sistema de gestión de reservas para centros de estética y salones de belleza.

**Proyecto:** DAW (Desarrollo de Aplicaciones Web) - Ciclo Formativo
**Autor:** Andres Eduardo Parada Prieto
**Tecnologías:** Spring Boot 3.2.5, Java 19, MySQL 8, JWT, Flyway
**Despliegue:** Railway (producción) + H2 (desarrollo local)

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Requisitos](#-requisitos)
- [Instalación Local](#-instalación-local)
- [Configuración](#-configuración)
- [Ejecutar la Aplicación](#-ejecutar-la-aplicación)
- [Endpoints API](#-endpoints-api)
- [Despliegue en Railway](#-despliegue-en-railway)
- [Scripts SQL](#-scripts-sql)
- [Credenciales de Prueba](#-credenciales-de-prueba)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Testing](#-testing)
- [Licencia](#-licencia)

---

## ✨ Características

### Funcionalidades Principales

- ✅ **Autenticación JWT**: Login y registro con tokens seguros
- ✅ **Gestión de Servicios**: CRUD completo de tratamientos
- ✅ **Franjas Horarias**: Sistema flexible de disponibilidad
- ✅ **Reservas Inteligentes**: Control de aforo y solapamientos
- ✅ **Validaciones de Negocio**: Horario 07:00-22:00, prevención de duplicados
- ✅ **Panel de Administración**: Gestión completa del sistema
- ✅ **Multi-perfil**: Dev (H2) + Railway (MySQL)

### Seguridad

- 🔐 Contraseñas hasheadas con BCrypt
- 🔐 Autenticación stateless con JWT
- 🔐 Protección de endpoints por roles (CLIENTE/ADMIN)
- 🔐 CORS configurado para frontend

### Arquitectura

- 🏗️ Patrón MVC + Services + Repositories
- 🏗️ DTOs para request/response
- 🏗️ Exception handling centralizado
- 🏗️ Transacciones ACID con bloqueo pesimista
- 🏗️ Migraciones versionadas con Flyway

---

## 🛠️ Requisitos

### Para desarrollo local:

- **Java 19** o superior ([OpenJDK](https://adoptium.net/))
- **Maven 3.8+** ([Descargar](https://maven.apache.org/download.cgi))
- **IDE:** IntelliJ IDEA, Eclipse o VS Code
- **Git** ([Descargar](https://git-scm.com/))

### Para producción (Railway):

- Cuenta en [Railway.app](https://railway.app/)
- Base de datos MySQL 8 (proporcionada por Railway)

---

## 📦 Instalación Local

### 1. Clonar el repositorio

git clone https://github.com/andresparadaprieto/beautybooking-backend.git
cd beautybooking-backend

### Instalar dependencias

mvn clean install

⚙️ Configuración
Perfiles disponibles:

dev (por defecto): H2 en memoria para desarrollo
railway: MySQL en Railway para producción

# Variables de entorno importantes

| Variable              | Descripción                          | Valor por defecto          |
|-----------------------|--------------------------------------|----------------------------|
| SPRING_PROFILES_ACTIVE| Perfil activo                        | dev                        |
| JWT_SECRET            | Clave secreta JWT (cambiar en prod.) | —                          |
| CORS_ORIGINS          | Orígenes permitidos                  | http://localhost:5173      |
| JDBC_DATABASE_URL     | URL de MySQL (Railway)               | (auto en Railway)          |

---

# application.properties principales

```properties
# Perfil activo
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}


# JWT
jwt.secret=${JWT_SECRET:cambiar-en-produccion}
jwt.expiration-ms=86400000

# CORS
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:5173}
```

## 🚀 Ejecutar la Aplicación

### Desarrollo Local (Base de datos H2 en memoria)

```bash
mvn spring-boot:run
```

La aplicación arranca en: **http://localhost:8080**

#### Consola H2 (solo desarrollo)

- **URL:** http://localhost:8080/h2-console
- **JDBC URL:** `jdbc:h2:mem:beautybooking`
- **Username:** `sa`
- **Password:** _(vacío)_

### Con MySQL Local (opcional)

```bash
# Cambiar perfil a 'local' y configurar MySQL en application-local.properties
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

---

## 📡 Endpoints de la API

### 🔓 Autenticación (Públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/auth/register` | Registrar nuevo usuario |
| `POST` | `/auth/login` | Login y obtener token JWT |

### 💼 Servicios (Públicos)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/servicios` | Listar servicios activos |
| `GET` | `/servicios/{id}` | Obtener servicio por ID |
| `GET` | `/servicios/buscar?nombre=X` | Buscar servicios |

### 📅 Franjas Horarias (Públicas)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/franjas/disponibles?servicioId=X&fecha=YYYY-MM-DD` | Obtener franjas disponibles |

### 📝 Reservas (Autenticadas - Requieren JWT)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/reservas` | Crear nueva reserva |
| `GET` | `/reservas/mis` | Ver mis reservas |
| `GET` | `/reservas/{id}` | Ver detalle de reserva |
| `DELETE` | `/reservas/{id}` | Cancelar reserva |

### 👑 Administración (Solo ADMIN)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/admin/servicios` | Crear servicio |
| `PUT` | `/admin/servicios/{id}` | Actualizar servicio |
| `DELETE` | `/admin/servicios/{id}` | Eliminar servicio |
| `POST` | `/admin/franjas` | Crear franja horaria |
| `DELETE` | `/admin/franjas/{id}` | Eliminar franja |
| `GET` | `/admin/reservas` | Ver todas las reservas |
| `GET` | `/admin/reservas/hoy` | Reservas de hoy |
| `PATCH` | `/admin/reservas/{id}/confirmar` | Confirmar reserva |

---

## 🔐 Ejemplo de Autenticación con JWT

### 1. Realizar Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@beautybooking.com",
    "password": "admin123"
  }'
```

### 2. Respuesta del Login

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "email": "admin@beautybooking.com",
  "rol": "ADMIN"
}
```

### 3. Usar el Token en Peticiones Protegidas

```bash
curl -X GET http://localhost:8080/reservas/mis \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

---

## ☁️ Despliegue en Railway

### Paso 1: Crear Proyecto en Railway

1. Ir a [Railway.app](https://railway.app)
2. Crear nuevo proyecto
3. Añadir servicio **MySQL**
4. Añadir servicio **"Deploy from GitHub"**

### Paso 2: Configurar Variables de Entorno

En el panel de Railway, añade las siguientes variables:

```env
SPRING_PROFILES_ACTIVE=railway
JDBC_DATABASE_URL=jdbc:mysql://...  # Railway lo proporciona automáticamente
JDBC_DATABASE_USERNAME=root         # Railway lo proporciona automáticamente
JDBC_DATABASE_PASSWORD=...          # Railway lo proporciona automáticamente
JWT_SECRET=tu-secreto-super-seguro-minimo-256-bits
CORS_ORIGINS=https://tu-frontend.com
```

### Paso 3: Deploy Automático

Railway detectará el `pom.xml` y automáticamente:

- ✅ Compilará el proyecto con Maven
- ✅ Ejecutará las migraciones de Flyway
- ✅ Iniciará la aplicación Spring Boot

### Paso 4: Verificar Despliegue

Acceder a: `https://tu-app.railway.app/actuator/health`

Debe devolver:

```json
{
  "status": "UP"
}
```

---

## 🗄️ Gestión de Base de Datos

### Exportar Base de Datos

```bash
# MySQL Local
mysqldump -u root -p beautybooking > backup.sql

# Desde Railway
railway run mysqldump beautybooking > railway_backup.sql
```

### Importar Base de Datos

```bash
# MySQL Local
mysql -u root -p beautybooking < backup.sql

# A Railway
railway run mysql beautybooking < backup.sql
```

### Schema Completo

El schema completo se encuentra en:  
📁 `src/main/resources/db/migration/V1__create_schema.sql`

---

## 🔑 Credenciales de Prueba

### 👑 Usuario Administrador

- **Email:** `admin@beautybooking.com`
- **Password:** `admin123`

### 👤 Clientes de Prueba

| Email | Password |
|-------|----------|
| `maria.garcia@example.com` | `password123` |
| `carlos.rodriguez@example.com` | `password123` |

---

## 📦 Tecnologías Utilizadas

- **Backend:** Spring Boot 3.x
- **Seguridad:** Spring Security + JWT
- **Base de Datos:** MySQL / H2 (desarrollo)
- **Migraciones:** Flyway
- **Build:** Maven
