# SkillBridge — Backend

API REST del proyecto **SkillBridge**, una plataforma para **medir y fortalecer *power skills*** (habilidades blandas como Pensamiento Crítico, Adaptabilidad, Comunicación, etc.) en estudiantes de ingeniería de la Universidad de Antioquia (UdeA).

Este repositorio contiene **únicamente el backend**. El frontend (Angular 21) vive en un repositorio aparte: [`skillbridge_frontend`](https://github.com/DuvanR0598/skillbridge_frontend).

> Desarrollado como proyecto de **práctica social** de la Facultad de Ingeniería (UdeA).

---

## 📑 Tabla de contenido

- [¿Qué hace este backend?](#-qué-hace-este-backend)
- [Stack tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Requisitos previos](#-requisitos-previos)
- [Cómo ejecutarlo](#-cómo-ejecutarlo)
  - [Opción A — Docker Compose (recomendada, todo incluido)](#opción-a--docker-compose-recomendada-todo-incluido)
  - [Opción B — Local con Maven (necesitas tu propio PostgreSQL)](#opción-b--local-con-maven-necesitas-tu-propio-postgresql)
- [Variables de entorno](#-variables-de-entorno)
- [Endpoints principales](#-endpoints-principales)
- [Roles y seguridad](#-roles-y-seguridad)
- [Comandos útiles](#-comandos-útiles)
- [Solución de problemas](#-solución-de-problemas)

---

## 🎯 ¿Qué hace este backend?

Expone una API REST que soporta:

- **Autenticación y usuarios:** registro, login con JWT (access + refresh token), login con **Google (OAuth2)**, gestión de perfil de usuario (datos personales, académicos, avatar, sede/seccional).
- **Cuestionarios y preguntas:** creación y administración de cuestionarios de evaluación, banco de preguntas con distintos tipos, dimensiones y ramificación condicional.
- **Evaluaciones:** flujo de examen **PRE_TEST / POST_TEST** para estudiantes, con puntuación **por peso** (no hay "opción correcta") a través de un **Motor de Puntuación** y una matriz de puntuación por niveles.
- **Retroalimentación:** planes de fortalecimiento según los resultados obtenidos.
- **Analítica:** dashboard y reportes de resultados por estudiante y por grupo.
- **Exportación:** listados de usuarios/estudiantes a **Excel (XLSX)**.

Todas las respuestas siguen un contrato uniforme `ApiResponse<T>`:

```json
{ "success": true, "message": "...", "data": {}, "errorCode": null, "timestamp": "..." }
```

---

## 🛠 Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | **Java 17** |
| Framework | **Spring Boot 3.5.14** (Web, Data JPA, Validation, Security, OAuth2 Client) |
| Build | **Maven** (con wrapper `mvnw` incluido) |
| Base de datos | **PostgreSQL 16** |
| ORM | Hibernate / Spring Data JPA (`ddl-auto=update`) |
| Mapeo DTO ↔ Entidad | **MapStruct 1.5.5** + Lombok |
| Seguridad | Spring Security **STATELESS + JWT** (`jjwt 0.12.5`) + OAuth2 Google |
| Exportación Excel | **Apache POI 5.3.0** (`poi-ooxml`) |
| Contenedores | **Docker** + **Docker Compose** |

---

## 🏗 Arquitectura

Arquitectura en capas clásica de Spring Boot:

```
Controller  →  Service (interfaz + impl)  →  Repository (JPA)  →  Entity
                     ↓
                 DTOs  (MapStruct para mapear Entity ↔ DTO)
```

Elementos destacados:

- **Contrato uniforme** de respuesta: `ApiResponse<T>`.
- **Validadores por tipo de pregunta** con patrón **Strategy + Factory**.
- **`MotorDePuntuacion`**: calcula el puntaje por peso de cada respuesta.
- **Filtros de búsqueda** vía `JpaSpecificationExecutor` + Specifications.
- **Sin SQL nativo**: todas las consultas personalizadas son **JPQL** (`@Query`).
- **Manejo central de errores**: `GlobalExceptionHandler`.
- **Seguridad por método** (`@EnableMethodSecurity`) y filtro JWT stateless.

---

## 📂 Estructura del proyecto

```
skillbridge_backend/
├── src/main/java/com/udea/skillbridge/
│   ├── SkillbridgeBackendApplication.java   # Clase main (punto de entrada)
│   ├── common/          # Clases comunes (p. ej. ApiResponse)
│   ├── config/          # Configuración general (recursos estáticos /uploads, etc.)
│   ├── controller/      # Controladores de negocio (cuestionarios, preguntas, evaluación, analítica…)
│   ├── dto/             # DTOs request/response
│   ├── entity/          # Entidades JPA
│   ├── enums/           # Enums (SkillTipo, tipos de pregunta, fases…)
│   ├── exception/       # GlobalExceptionHandler y excepciones de negocio
│   ├── mapper/          # Mappers MapStruct
│   ├── repository/      # Repositorios Spring Data JPA
│   ├── service/         # Lógica de negocio (interfaces + impl)
│   ├── validation/      # Validadores de preguntas (Strategy + Factory)
│   └── seguridad/       # Módulo de autenticación / usuarios
│       ├── config/      #   SecurityConfig, CORS
│       ├── controller/  #   AuthController, UsuarioController, UsuarioPerfilController
│       ├── dto/ entity/ enums/ mapper/ repository/ service/
│       ├── filter/      #   Filtro JWT
│       └── oauth2/      #   Handlers de login con Google
├── src/main/resources/
│   └── application.properties
├── docs/figuras/        # Diagramas SVG del proyecto (arquitectura, ER, flujo…)
├── loadtest/            # Kit de pruebas de carga (JMeter)
├── Dockerfile           # Build multi-etapa (Maven → JRE ligero)
├── docker-compose.yml   # PostgreSQL + backend para desarrollo local
├── pom.xml
├── mvnw / mvnw.cmd      # Maven wrapper (no necesitas Maven instalado)
└── README.md
```

Controladores principales: `AuthController`, `UsuarioController`, `UsuarioPerfilController`, `CuestionarioController`, `PreguntaController`, `DimensionController`, `CondicionPreguntaController`, `PuntuacionMatrixController`, `EvaluacionEstudianteController`, `PlanFortalecimientoController`, `AnalyticsController`, `DashboardController`.

---

## ✅ Requisitos previos

Elige **una** de las dos rutas de ejecución:

### Para la Opción A (Docker) — la más simple
- **Git**
- **Docker Desktop** (incluye Docker Compose) — [descargar](https://www.docker.com/products/docker-desktop/)

> Con Docker **no necesitas** instalar Java, Maven ni PostgreSQL: todo corre en contenedores.

### Para la Opción B (Local con Maven)
- **Git**
- **JDK 17** (Eclipse Temurin recomendado) — verifica desde la consola con `java -version`
- **PostgreSQL 16** instalado y corriendo
- Maven **no** es necesario: el repo trae el wrapper (`mvnw` / `mvnw.cmd`)

---

## 🚀 Cómo ejecutarlo

Primero clona el repositorio:

```bash
git clone https://github.com/DuvanR0598/skillbridge_backend.git
cd skillbridge_backend
```

---

### Opción A — Docker Compose (recomendada, todo incluido)

Levanta **la base de datos PostgreSQL y el backend** juntos con un solo comando. El `docker-compose.yml` ya trae valores por defecto listos para desarrollo local (incluidos secretos *dummy* para JWT/Google).

**1. Construir y arrancar todo:**

```bash
docker compose up --build
```

Esto hace:
- Levanta un contenedor **PostgreSQL 16** (`skillbridge-postgres`) con la base `skill_bridge`.
  - Publicado en el host en el puerto **5433** (para no chocar con un PostgreSQL nativo en 5432).
- Construye la imagen del backend (build multi-etapa: Maven compila el JAR y luego se copia a una imagen JRE ligera).
- Arranca el backend (`skillbridge-backend`) **solo después** de que la base de datos esté saludable (`healthcheck`).
- Hibernate crea/actualiza las tablas automáticamente (`ddl-auto=update`).

**2. Verificar que arrancó:**

La API queda disponible en:

```
http://localhost:8083
```

**3. Comandos habituales de Docker:**

```bash
# Arrancar en segundo plano (sin bloquear la terminal)
docker compose up --build -d

# Ver logs del backend
docker compose logs -f backend

# Detener los contenedores (conserva los datos)
docker compose down

# Detener y BORRAR los datos (base de datos + uploads)
docker compose down -v

# Reconstruir tras cambios en el código
docker compose up --build
```

> **Nota sobre datos:** los volúmenes `skillbridge_pgdata` (base de datos) y `skillbridge_uploads` (avatares subidos) **persisten** entre reinicios. Solo se borran con `docker compose down -v`.

> **Nota sobre Google Login:** en Docker se usan credenciales *dummy* para OAuth2. El login por correo/contraseña (JWT) funciona sin problema; el login con Google solo funcionará si reemplazas `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` por credenciales reales en el `docker-compose.yml`.

---

### Opción B — Local con Maven (necesitas tu propio PostgreSQL)

Útil si prefieres correr el backend directamente en tu máquina (por ejemplo, para depurar desde el IDE) usando tu propia instalación de PostgreSQL.

**1. Crear la base de datos en PostgreSQL:**

```sql
CREATE DATABASE skill_bridge;
```

Por defecto la app espera:
- URL: `jdbc:postgresql://localhost:5432/skill_bridge`
- Usuario: `postgres`
- Contraseña: `admin`

Si tu configuración es distinta, ajústala con variables de entorno (ver [Variables de entorno](#-variables-de-entorno)).

**2. Definir los secretos obligatorios.**

El backend requiere `JWT_SECRET_KEY` (mínimo 32 bytes, codificado en Base64). Ejemplo válido para desarrollo:

```bash
# Linux / macOS / Git Bash
export JWT_SECRET_KEY="ZHVtbXktdGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC1hdC1sZWFzdC0zMi1ieXRlcw=="
export GOOGLE_CLIENT_ID="dummy-client-id"
export GOOGLE_CLIENT_SECRET="dummy-client-secret"
```

```powershell
# Windows PowerShell
$env:JWT_SECRET_KEY   = "ZHVtbXktdGVzdC1zZWNyZXQta2V5LWZvci1qdW5pdC1hdC1sZWFzdC0zMi1ieXRlcw=="
$env:GOOGLE_CLIENT_ID = "dummy-client-id"
$env:GOOGLE_CLIENT_SECRET = "dummy-client-secret"
```

**3. Compilar y ejecutar con el Maven wrapper:**

```bash
# Linux / macOS / Git Bash
./mvnw spring-boot:run
```

```powershell
# Windows (CMD o PowerShell)
.\mvnw.cmd spring-boot:run
```

La API queda disponible en `http://localhost:8083`.

**Otros comandos Maven útiles:**

```bash
./mvnw clean package -DskipTests   # Generar el JAR (target/skillbridge_backend-0.0.1-SNAPSHOT.jar)
./mvnw -q -o compile               # Solo compilar (offline, silencioso)
java -jar target/skillbridge_backend-0.0.1-SNAPSHOT.jar   # Ejecutar el JAR generado
```

> Los **tests** están excluidos del empaquetado (`-DskipTests`) porque requieren una base de datos activa.

---

## 🔧 Variables de entorno

Todas tienen un valor por defecto para desarrollo local (excepto los secretos). Se pueden inyectar por entorno o, en Docker, ya vienen en el `docker-compose.yml`.

| Variable | Descripción | Valor por defecto |
|---|---|---|
| `PORT` | Puerto del servidor | `8083` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/skill_bridge` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la BD | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la BD | `admin` |
| `JWT_SECRET_KEY` | **(Obligatoria)** Clave secreta JWT, Base64, mín. 32 bytes | — |
| `GOOGLE_CLIENT_ID` | **(Obligatoria)** Client ID de Google OAuth2 | — |
| `GOOGLE_CLIENT_SECRET` | **(Obligatoria)** Client Secret de Google OAuth2 | — |
| `APP_CORS_ALLOWED_ORIGINS` | Orígenes CORS permitidos (separados por coma) | `http://localhost:4200` |

Configuración adicional (definida en `application.properties`, no suele cambiarse):
- Expiración access token: `900000` ms (15 min).
- Expiración refresh token: `7` días.
- Tamaño máximo de archivo subido (avatares): `5 MB`.

> ⚠️ **Seguridad:** nunca subas secretos reales al repositorio. En producción se inyectan solo en la plataforma de despliegue (p. ej. Render).

---

## 🌐 Endpoints principales

Ejemplos representativos (todos devuelven `ApiResponse<T>`):

**Autenticación (público):**
- `POST /auth/register` — registro de usuario
- `POST /auth/login` — `{ email, password }` → devuelve `data.accessToken`
- `POST /auth/refresh` — renovar el access token
- `POST /auth/logout`
- `GET /oauth2/authorization/google` — iniciar login con Google

**Perfil:**
- `GET /usuarios/me/perfil` — perfil del usuario autenticado
- `PATCH /usuarios/me/perfil` — completar/editar perfil
- `POST /usuarios/me/perfil/avatar` — subir avatar
- `GET /perfil/programas` — catálogo de programas (público)
- `GET /perfil/sedes` — catálogo de sedes/seccionales (público)

**Usuarios (ADMIN/COORD):**
- `GET /usuarios/estudiantes` — listar estudiantes (con filtros `search`, `programa`, `sede`)
- `GET /usuarios/estudiantes/exportar` — exportar estudiantes a XLSX
- `GET /usuarios/exportar` — exportar usuarios a XLSX (ADMIN)

**Flujo de examen (ESTUDIANTE):**
- `GET /cuestionario/listar_cuestionarios_activos`
- `GET /cuestionario/{id}/entregar_cuestionario`
- `POST /evaluacion/cuestionario/{id}/iniciar?phase=PRE_TEST`
- `POST /evaluacion/{idEval}/respuestas`
- `PATCH /evaluacion/{idEval}/completo` — dispara el `MotorDePuntuacion`

**Preguntas (ADMIN/COORD):**
- `POST /preguntas/crear_pregunta`
- `POST /preguntas/subir_imagen`
- `GET /preguntas/paginado`

> Archivos subidos (avatares/imágenes) se sirven bajo `/uploads/**`.

---

## 🔐 Roles y seguridad

- **Autenticación stateless** con JWT (access + refresh). El token se envía en el header `Authorization: Bearer <token>`.
- **Login con Google** vía OAuth2.
- **Roles:**
  - `ROLE_ADMIN` — administración total.
  - `ROLE_COORDINADOR` — docente/coordinador (gestión de cuestionarios, estudiantes, analítica).
  - `ROLE_ESTUDIANTE` — realiza evaluaciones, ve su progreso.
- Autorización a nivel de método con `@EnableMethodSecurity`.

---

## 🧰 Comandos útiles

```bash
# Docker
docker compose up --build          # levantar BD + backend
docker compose up --build -d       # en segundo plano
docker compose logs -f backend     # ver logs del backend
docker compose down                # detener (conserva datos)
docker compose down -v             # detener y borrar datos

# Maven (local)
./mvnw spring-boot:run             # ejecutar
./mvnw clean package -DskipTests   # empaquetar JAR
./mvnw -q -o compile               # solo compilar
```

---

## 🩺 Solución de problemas

| Síntoma | Causa probable | Solución |
|---|---|---|
| El backend no arranca y falla al iniciar sesión con JWT | `JWT_SECRET_KEY` ausente o menor a 32 bytes | Define una clave Base64 válida (ver Opción B, paso 2) |
| `Connection refused` a PostgreSQL | La BD no está corriendo o la URL/credenciales no coinciden | Verifica que PostgreSQL esté activo y revisa `SPRING_DATASOURCE_*` |
| El puerto **5432** está ocupado (Docker) | Tienes un PostgreSQL nativo usando ese puerto | El compose ya publica la BD en **5433** en el host; no necesitas hacer nada |
| El puerto **8083** está ocupado | Otro proceso lo usa | Cambia `PORT` (y el mapeo `ports` en compose) |
| El frontend recibe errores **CORS** | Origen no permitido | Añade el dominio del frontend a `APP_CORS_ALLOWED_ORIGINS` |
| Login con Google falla | Credenciales *dummy* | Configura `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` reales |
| Imágenes/avatares se pierden tras redeploy en la nube | Disco efímero (p. ej. Render free) | Usar almacenamiento persistente (S3/Cloudinary) o volumen |

---

## 📄 Licencia y propiedad intelectual

- El **código de la plataforma** es propiedad del autor (Duván Ferney Ruiz Ocampo) para usufructo comercial.
- El **modelo y contenido de *power skills*** (preguntas, dimensiones) es de la Universidad de Antioquia, para uso académico.

---

**Autor:** Duván Ferney Ruiz Ocampo — Ingeniería de Sistemas, Universidad de Antioquia.
