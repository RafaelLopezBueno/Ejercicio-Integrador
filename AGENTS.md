# AGENTS.md — Uso de IA generativa en el proyecto

Siguiendo el apartado 7.1 del enunciado, este archivo documenta el uso de herramientas de IA generativa.

## Herramientas utilizadas

| Herramienta | Para qué se usó |
|---|---|
| Asistente de código con IA (Claude Code) | Generación y revisión del código Java de la v1.0, de los tests unitarios y de la documentación |
| Servidor MCP de Figma conectado al asistente | Creación del prototipo, de las variables de diseño y de las páginas del manual de estilo en Figma |
| Asistente de IA (generación de datos) | Generación del CSV de datos de prueba, según el apartado 8 del enunciado |

## Instrucciones dadas al asistente

Resumen de las instrucciones y restricciones indicadas por el equipo:

1. Implementar solo el alcance de la **v1.0**: CLI por argumentos sobre un archivo de texto con campos separados por `;`, y prototipo en Figma con su manual de estilo.
2. Operaciones: `-listar` (con `--estado` y `--empresa`), `-añadir`, `-ver ID`, `-borrar ID` e `-importar archivo.csv`.
3. Sin menú interactivo ni `Scanner`; cada ejecución realiza una operación y termina.
4. Errores (argumentos faltantes, archivo no encontrado, ID inexistente) con un mensaje claro por consola.
5. Java 24 o superior, Maven, código en paquetes, Javadoc y pruebas con JUnit 5.
6. El repositorio no debe contener los PDF del prototipo ni del manual de estilo ni el vídeo de la demostración (excluidos en `.gitignore`).

## Prompt usado para el CSV de datos de prueba

```
Genera un archivo CSV con 15 registros de candidaturas laborales para una aplicación de seguimiento de empleo.
El CSV debe tener las siguientes columnas separadas por punto y coma:
id;titulo;empresa;estado;fechaPostulacion;enlace;notas
Los estados posibles son: pendiente, entrevista, rechazado, aceptado.
Incluye al menos 4 empresas diferentes y al menos 3 ofertas en cada estado.
Las fechas deben estar en formato dd/MM/yyyy y abarcar los últimos 3 meses.
Devuelve solo el CSV, sin explicaciones.
```

## Revisión humana

El código generado se compila, se prueba (`./mvnw test`) y se ejecuta con los comandos del `README.md` antes de incluirse en el repositorio. Cada miembro del equipo debe poder explicar el código de las partes que figuran a su nombre en la tabla de contribuciones.
