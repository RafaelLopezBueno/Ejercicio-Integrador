# CandiTrack — Tracker de Candidaturas Laborales

Aplicación Java para llevar el seguimiento de candidaturas laborales: empresas, ofertas y entrevistas.

- **v1.0** (tag `v1.0`): persistencia en archivos de texto, CLI por argumentos y diseño del prototipo en Figma.
- **v2.0** (esta versión): persistencia en MySQL con JDBC e interfaz gráfica JavaFX siguiendo el prototipo.

Equipo: **ELROKIROKI** y **Samuel Aguilar Gallardo**.

## Tecnologías

- Java 24 o superior (probado con JDK 26)
- Maven (incluye wrapper: `mvnw`)
- MySQL 8.4 (con `docker-compose.yml`) y JDBC (`mysql-connector-j`)
- JavaFX 25 con FXML (archivos `.fxml` en formato compatible con Scene Builder)
- JUnit 5, Mockito y H2 (en modo MySQL) para las pruebas
- Figma (prototipo y manual de estilo)
- Para v3.0 se añadirán Hibernate y Ollama

## Compilar y ejecutar (v2.0)

1. Arrancar la base de datos (necesita Docker):

   ```bash
   docker compose up -d
   ```

2. Crear la configuración de conexión a partir del ejemplo (el archivo `config.properties` no se sube al repositorio):

   ```bash
   cp config.properties.example config.properties
   ```

   Los valores por defecto coinciden con los del `docker-compose.yml`. Cualquier propiedad se puede sustituir por una variable de entorno: `TRACKER_DB_URL`, `TRACKER_DB_USER`, `TRACKER_DB_PASSWORD` y `TRACKER_DB_INIT`. Con `db.init=true` las tablas se crean al arrancar; el script equivalente está en [`db/schema.sql`](db/schema.sql).

3. Ejecutar la aplicación:

   ```bash
   ./mvnw javafx:run
   ```

4. Para cargar los datos de ejemplo, en **Candidaturas** pulsa **Importar CSV** y elige [`data/candidaturas_ejemplo.csv`](data/candidaturas_ejemplo.csv).

### Qué se puede hacer

| Pantalla | Funciones |
|---|---|
| Candidaturas | Tabla con filtros combinables (estado, empresa, título, salario mínimo y máximo, fechas), cambio de estado, borrado e importación de CSV |
| Detalle de candidatura | Datos completos, empresa asociada y lista de entrevistas; editar, eliminar y cambiar estado |
| Formulario de candidatura | Alta y edición con validación de campos |
| Empresas | Listado con número de candidaturas, alta, edición y borrado (en cascada, con confirmación) |
| Entrevistas | Listado y formulario para crear, editar y eliminar |

El panel de estadísticas y el asistente de IA del prototipo corresponden a la v3.0.

### Base de datos

Tablas `empresa`, `oferta` y `entrevista` (ver [`db/schema.sql`](db/schema.sql)), con claves foráneas entre ellas. Las consultas con `JOIN` (ofertas con su empresa, entrevistas con su oferta y empresa, empresas con su número de ofertas) están en `src/main/java/es/tracker/repository`. Las operaciones que afectan a varias tablas (importar el CSV, eliminar una empresa o una oferta con sus dependientes) se ejecutan en una transacción (`Database.enTransaccion`).

## Pruebas

```bash
./mvnw test
```

Las pruebas de repositorios, servicios e importación usan H2 en memoria; las de JavaFX comprueban que los FXML cargan y se enlazan con sus controladores. Además hay pruebas de integración sobre MySQL real que solo se ejecutan si se define una base de datos de pruebas (sus tablas se borran antes de cada test, por eso su nombre debe contener `test`):

```bash
docker exec tracker-mysql mysql -uroot -proot_dev -e "CREATE DATABASE IF NOT EXISTS tracker_test; GRANT ALL ON tracker_test.* TO 'tracker'@'%';"
export TRACKER_TEST_DB_URL="jdbc:mysql://localhost:3308/tracker_test?serverTimezone=Europe/Madrid"
export TRACKER_TEST_DB_PASSWORD=tracker_dev
./mvnw test -Dtest=MySqlIntegracionTest
```

## Datos de prueba

CSV con 15 candidaturas: [`data/candidaturas_ejemplo.csv`](data/candidaturas_ejemplo.csv).

## Diseño

- Prototipo y manual de estilo en Figma: <https://www.figma.com/design/lZskhIE8eGatM7dE0wCzcu> (compartido como solo lectura)
- Pantallas, sistema de diseño y correspondencia con la implementación: [`docs/diseno.md`](docs/diseno.md)

## Uso de IA generativa

Este proyecto se ha desarrollado con asistencia de herramientas de IA generativa. Las instrucciones y prompts empleados están documentados en [`AGENTS.md`](AGENTS.md), [`planning.md`](planning.md) y [`tasks.md`](tasks.md).

## Tabla de contribuciones

| Miembro | v1.0 | v2.0 |
|---|---|---|
| ELROKIROKI | Proyecto Maven, modelo de datos, CLI y `Main`, tests de codec y argumentos, prototipo en Figma y manual de estilo, README y documentación de diseño | Esquema SQL y Docker Compose, repositorios JDBC con filtros combinados y JOIN, aplicación JavaFX (navegación, estilos, listado de candidaturas), gestión de empresas, pruebas de integración sobre MySQL |
| Samuel Aguilar Gallardo | Persistencia en archivo, servicio, CSV de datos de prueba, tests del servicio, `AGENTS.md`, `planning.md` y `tasks.md` | Modelo, configuración externa y transacciones JDBC, servicios con validación e importación a la base de datos, formulario y detalle de candidaturas, gestión de entrevistas, documentación de la v2.0 |
