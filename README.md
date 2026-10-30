# CandiTrack — Tracker de Candidaturas Laborales

Aplicación Java para llevar el seguimiento de candidaturas laborales. Esta es la **entrega v1.0**: persistencia en archivos de texto, interfaz de línea de comandos por argumentos y diseño del prototipo visual en Figma.

Equipo: **ELROKIROKI** y **Samuel Aguilar Gallardo**.

## Tecnologías

- Java 24 o superior
- Maven (incluye wrapper: `mvnw`)
- JUnit 5 para las pruebas
- Figma (prototipo y manual de estilo)
- Persistencia: archivo de texto con campos separados por `;` (sin base de datos)
- Para v2.0 y v3.0 se añadirán JDBC, Hibernate, JavaFX, Scene Builder y Ollama

## Compilar y ejecutar (v1.0)

```bash
./mvnw package
```

Genera `target/tracker-candidaturas-1.0.jar`. Cada ejecución realiza una operación y termina:

```bash
java -jar target/tracker-candidaturas-1.0.jar -importar data/candidaturas_ejemplo.csv
java -jar target/tracker-candidaturas-1.0.jar -listar
java -jar target/tracker-candidaturas-1.0.jar -listar --estado pendiente --empresa Indra
java -jar target/tracker-candidaturas-1.0.jar -añadir --titulo "Dev Java" --empresa Indra --estado pendiente --fecha 30/10/2026
java -jar target/tracker-candidaturas-1.0.jar -ver 13
java -jar target/tracker-candidaturas-1.0.jar -borrar 13
```

| Operación | Descripción |
|---|---|
| `-listar [--estado X] [--empresa Y]` | Lista las candidaturas, con filtros opcionales |
| `-añadir --titulo X --empresa Y --estado Z --fecha dd/MM/yyyy` | Añade una candidatura (`--enlace` y `--notas` son opcionales) |
| `-ver ID` | Muestra todos los detalles de una candidatura |
| `-borrar ID` | Elimina una candidatura |
| `-importar archivo.csv` | Importa candidaturas desde un CSV |

Estados válidos: `pendiente`, `entrevista`, `rechazado`, `aceptado`.

Los datos se guardan en `data/candidaturas.txt`. Se puede usar otro archivo con la variable de entorno `CANDIDATURAS_FILE`. Los errores (argumentos faltantes, archivo no encontrado, ID inexistente, estado o fecha no válidos) muestran un mensaje claro y terminan con código de salida 1.

### Pruebas

```bash
./mvnw test
```

## Datos de prueba

CSV con 15 candidaturas: [`data/candidaturas_ejemplo.csv`](data/candidaturas_ejemplo.csv).

## Diseño

- Prototipo y manual de estilo en Figma: <https://www.figma.com/design/lZskhIE8eGatM7dE0wCzcu> (compartido como solo lectura)
- Descripción de pantallas y sistema de diseño: [`docs/diseno.md`](docs/diseno.md)

## Uso de IA generativa

Este proyecto se ha desarrollado con asistencia de herramientas de IA generativa. Las instrucciones y prompts empleados están documentados en [`AGENTS.md`](AGENTS.md), [`planning.md`](planning.md) y [`tasks.md`](tasks.md).

## Tabla de contribuciones

| Miembro | v1.0 |
|---|---|
| ELROKIROKI | Proyecto Maven, modelo de datos, CLI y `Main`, tests de codec/repositorio/argumentos, prototipo en Figma y manual de estilo (en curso, ver `tasks.md`), README y documentación de diseño |
| Samuel Aguilar Gallardo | Persistencia en archivo, servicio (filtrado, alta, borrado, importación), CSV de datos de prueba, tests del servicio, `AGENTS.md`, `planning.md` y `tasks.md` |
