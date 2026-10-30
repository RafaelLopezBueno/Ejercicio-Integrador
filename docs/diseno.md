# Diseño de CandiTrack (v1.0)

Prototipo interactivo en Figma: <https://www.figma.com/design/lZskhIE8eGatM7dE0wCzcu>

El archivo tiene dos páginas: **Manual de estilo** y **Prototipo**. Los PDF exportados del prototipo y del manual y el vídeo de la demostración se entregan aparte y no forman parte de este repositorio.

## Pantallas del prototipo

| Pantalla | Contenido | Navegación |
|---|---|---|
| 01 Listado de candidaturas | Tabla con filtros por estado, empresa y fecha | Fila → detalle · Nueva candidatura → formulario |
| 02 Detalle de candidatura | Datos, notas, empresa asociada y entrevistas | Editar → formulario · Ver empresa → empresas · Gestionar entrevistas → entrevistas |
| 03 Formulario de candidatura | Alta/edición con campos obligatorios | Guardar/Cancelar → detalle |
| 04 Gestión de empresas | Listado de empresas | Fila / Nueva empresa → formulario |
| 05 Formulario de empresa | Alta/edición de empresa | Guardar/Cancelar → empresas |
| 06 Gestión de entrevistas | Listado y formulario de programación | Menú lateral |
| 07 Panel de estadísticas (v3.0) | KPIs y gráficos por estado, empresa y mes | Menú lateral |
| 08 Asistente IA (v3.0) | Diálogo superpuesto con conversación | Se abre desde el menú lateral; Cerrar |

El flujo comienza en la pantalla 01. El menú lateral y el logotipo están enlazados en todas las pantallas.

## Sistema de diseño

- **Colores:** 19 variables en la colección «Colores» (principal, secundario, fondo, texto, bordes y 4 colores de estado con su fondo). Todas las combinaciones de texto cumplen WCAG AA (4.5:1).
- **Espaciado y radios:** colección «Espaciado y radios» (4, 8, 12, 16, 24, 32 px; radios 6, 10 y 16 px).
- **Tipografía:** Inter (títulos y cuerpo) y JetBrains Mono (monoespaciado), con 7 estilos de texto.
- **Componentes:** Botón (primario, secundario, peligro), Badge de estado (4), Campo (normal, foco, error), Tarjeta KPI y Logo (isotipo y horizontal).
- **Layout:** menú lateral de 240 px y contenido con 32 px de margen y 24 px entre bloques, sobre un lienzo de 1440 × 900.
