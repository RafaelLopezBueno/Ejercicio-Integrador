# Tareas v1.0

## Acceso a Datos
- [x] Proyecto Maven con wrapper y `.gitignore`
- [x] Modelo `Candidatura` y enum `Estado`
- [x] Persistencia en archivo de texto (`FileCandidaturaRepository`, `CandidaturaCodec`)
- [x] Servicio: listar con filtros, añadir, ver, borrar, importar CSV
- [x] CLI por argumentos y manejo de errores
- [x] CSV de datos de prueba (`data/candidaturas_ejemplo.csv`)
- [x] Tests unitarios con JUnit 5

## Desarrollo de Interfaces
- [x] Prototipo Figma: listado con filtros, detalle, formulario de candidatura, empresas (listado + formulario), entrevistas, estadísticas, asistente IA
- [x] Navegación entre pantallas (reacciones del prototipo)
- [x] Manual de estilo: portada, paleta, tipografía y logo
- [ ] Manual de estilo: guía de botones, tablas, formularios, tarjetas, navegación e iconografía
- [ ] Manual de estilo: sistema de diseño (layout y componentes reutilizables)

## Entrega
- [x] README con instrucciones y tabla de contribuciones
- [ ] Exportar a PDF el prototipo y el manual de estilo (no se suben al repositorio)
- [ ] Grabar el vídeo de la demostración (no se sube al repositorio)
- [ ] Compartir el prototipo de Figma como «solo lectura» y añadir a ambos como colaboradores del repositorio

# Tareas v2.0

## Acceso a Datos
- [x] Esquema MySQL (`db/schema.sql`) y `docker-compose.yml`
- [x] Configuración externa (`config.properties` / variables de entorno)
- [x] Repositorios JDBC con filtros combinados y consultas JOIN
- [x] Transacciones (importación, borrado de empresa y de oferta)
- [x] CRUD de empresas y entrevistas, cambio de estado, importación del CSV
- [x] Pruebas con H2, Mockito y MySQL real

## Desarrollo de Interfaces
- [x] Listado de ofertas con TableView, filtros y botones de acción
- [x] Detalle de oferta con empresa y entrevistas
- [x] Formularios de ofertas y empresas con validación; gestión de entrevistas
- [x] Navegación con FXMLLoader y estilos del manual
- [ ] Revisar y retocar los FXML en Scene Builder
- [ ] Panel de estadísticas y asistente IA (v3.0)

## Entrega v2.0
- [ ] Tag `v2.0` y push a GitHub
- [ ] Vídeo de la demostración de la aplicación JavaFX (no se sube al repositorio)
