# 🌸 BeautyBooking - Backend API

Sistema de gestión de reservas para centros de estética y salones de belleza.

**Proyecto:** DAW (Desarrollo de Aplicaciones Web) - Ciclo Formativo
**Autor:** Andres Eduardo Parada Prieto
**Tecnologías:** Spring Boot 3.2.5, Java 17, MySQL 8, JWT, Flyway
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

- **Java 17** o superior ([OpenJDK](https://adoptium.net/))
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

### Configurar variables de entorno (opcional)

cp .env.example .env

⚙️ Configuración
Perfiles disponibles:

dev (por defecto): H2 en memoria para desarrollo
railway: MySQL en Railway para producción

Variables de entorno importantes:
VariableDescripciónValor por defectoSPRING_PROFILES_ACTIVEPerfil activodevJWT_SECRETClave secreta JWT(cambiar en producción)CORS_ORIGINSOrígenes permitidoshttp://localhost:5173JDBC_DATABASE_URLURL de MySQL (Railway)(auto en Railway)
application.properties principales:
properties# Perfil activo
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}

# JWT
jwt.secret=${JWT_SECRET:cambiar-en-produccion}
jwt.expiration-ms=86400000

# CORS
app.cors.allowed-origins=${CORS_ORIGINS:http://localhost:5173}

🚀 Ejecutar la Aplicación
Desarrollo local (H2):
bashmvn spring-boot:run
La aplicación arranca en: http://localhost:8080
Ver base de datos H2 (solo dev):
Acceder a: http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:beautybooking
Username: sa
Password: (vacío)

Con MySQL local (opcional):
bash# Cambiar perfil a 'local' y configurar MySQL en application-local.properties
mvn spring-boot:run -Dspring-boot.run.profiles=local

📡 Endpoints API
Autenticación (públicos)
MétodoEndpointDescripciónPOST/auth/registerRegistrar nuevo usuarioPOST/auth/loginLogin y obtener token JWT
Servicios (públicos)
MétodoEndpointDescripciónGET/serviciosListar servicios activosGET/servicios/{id}Obtener servicio por IDGET/servicios/buscar?nombre=XBuscar servicios
Franjas (públicas)
MétodoEndpointDescripciónGET/franjas/disponibles?servicioId=X&fecha=YYYY-MM-DDFranjas disponibles
Reservas (autenticadas)
MétodoEndpointDescripciónPOST/reservasCrear reservaGET/reservas/misMis reservasGET/reservas/{id}Ver reservaDELETE/reservas/{id}Cancelar reserva
Administración (solo ADMIN)
MétodoEndpointDescripciónPOST/admin/serviciosCrear servicioPUT/admin/servicios/{id}Actualizar servicioDELETE/admin/servicios/{id}Eliminar servicioPOST/admin/franjasCrear franjaDELETE/admin/franjas/{id}Eliminar franjaGET/admin/reservasVer todas las reservasGET/admin/reservas/hoyReservas de hoyPATCH/admin/reservas/{id}/confirmarConfirmar reserva
Ejemplo de petición con JWT:
bash# Login
curl -X POST http://localhost:8080/auth/login \
-H "Content-Type: application/json" \
-d '{"email":"admin@beautybooking.com","password":"admin123"}'

# Respuesta:
{
"token": "eyJhbGciOiJIUzI1NiJ9...",
"type": "Bearer",
"email": "admin@beautybooking.com",
"rol": "ADMIN"
}

# Usar el token en peticiones protegidas:
curl -X GET http://localhost:8080/reservas/mis \
-H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."

☁️ Despliegue en Railway
1. Crear proyecto en Railway

Ir a Railway.app
Crear nuevo proyecto
Añadir servicio MySQL
Añadir servicio "Deploy from GitHub"

2. Configurar variables de entorno en Railway
   envSPRING_PROFILES_ACTIVE=railway
   JDBC_DATABASE_URL=jdbc:mysql://...  (Railway lo proporciona)
   JDBC_DATABASE_USERNAME=root  (Railway lo proporciona)
   JDBC_DATABASE_PASSWORD=...  (Railway lo proporciona)
   JWT_SECRET=tu-secreto-super-seguro-minimo-256-bits
   CORS_ORIGINS=
3. Deploy automático
   Railway detectará el pom.xml y:

Compilará con Maven
Ejecutará Flyway (migraciones SQL)
Iniciará la aplicación

4. Verificar despliegue
   Acceder a: 
   Debe devolver: {"status":"UP"}

🗄️ Scripts SQL
Exportar base de datos:
bash# MySQL
mysqldump -u root -p beautybooking > backup.sql

# Desde Railway
railway run mysqldump beautybooking > railway_backup.sql
Importar base de datos:
bash# MySQL local
mysql -u root -p beautybooking < backup.sql

# A Railway
railway run mysql beautybooking < backup.sql
Script completo de schema:
Ver: src/main/resources/db/migration/V1__create_schema.sql

🔑 Credenciales de Prueba
Usuario Administrador:

Email: admin@beautybooking.com
Password: admin123

Clientes de Prueba:

Email: maria.garcia@example.com | Password: password123
Email: carlos.rodriguez@example.com | Password: password123

IMPORTANTE: Cambiar credenciales en producción.

📁 Estructura del Proyecto
beautybooking/
├── src/main/java/com/beautybooking/
│   ├── BeautybookingApplication.java
│   ├── config/
│   │   ├── CorsConfig.java
│   │   ├── SecurityConfig.java
│   │   └── DataLoader.java
│   ├── model/
│   │   ├── Usuario.java
│   │   ├── Servicio.java
│   │   ├── FranjaHoraria.java
│   │   ├── Reserva.java
│   │   └── enums/
│   ├── repository/
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   ├── service/
│   ├── controller/
│   ├── security/
│   │   ├── JwtUtil.java
│   │   ├── JwtAuthFilter.java
│   │   └── UserDetailsServiceImpl.java
│   └── exception/
├── src/main/resources/
│   ├── application.properties
│   ├── application-dev.properties
│   ├── application-railway.properties
│   └── db/migration/
│       └── V1__create_schema.sql
└── pom.xml



📄 Licencia
Este proyecto es parte de un Trabajo de Fin de Ciclo (DAW).
Autor: Andres Eduardo Parada Prieto
Año: 2025


📞 Contacto

Email: andres.parada.18@gmail.com
GitHub: @andresparadaprieto


🙏 Agradecimientos

FOC (Fomento Ocupacional) - Centro de formación
Spring Framework - Framework de desarrollo
Railway - Plataforma de despliegue