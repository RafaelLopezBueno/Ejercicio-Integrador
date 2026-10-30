# Planificación v1.0

## Arquitectura

```
es.tracker
├── Main                      punto de entrada (una operación y termina)
├── cli
│   ├── Argumentos            interpreta args: operación, valor posicional y --opciones
│   └── Comandos              ejecuta cada operación y escribe por consola
├── service
│   └── CandidaturaService    filtrado, alta, consulta, borrado e importación
├── repository
│   ├── CandidaturaRepository         contrato de persistencia
│   ├── FileCandidaturaRepository     implementación sobre archivo de texto
│   └── CandidaturaCodec              línea de texto <-> Candidatura
├── model
│   ├── Candidatura           record con los 7 campos
│   └── Estado                pendiente | entrevista | rechazado | aceptado
└── exception
    └── CandidaturaException  errores mostrados al usuario
```

## Decisiones

- **Formato del archivo:** `id;titulo;empresa;estado;fechaPostulacion;enlace;notas`, con cabecera. Si un campo contiene `;` se sustituye por `,` al escribir.
- **Escritura segura:** se escribe en un archivo temporal y se mueve sobre el definitivo para no dejar el archivo a medias si falla la escritura.
- **Archivo de datos:** `data/candidaturas.txt` por defecto; se cambia con la variable de entorno `CANDIDATURAS_FILE`.
- **Importación:** conserva el `id` del CSV salvo que ya esté en uso; las líneas incorrectas se descartan y se informa de ellas.
- **Operaciones:** se aceptan con uno o dos guiones (`-listar` o `--listar`).
- **Filtro de empresa:** busca texto contenido en el nombre, sin distinguir mayúsculas; el de estado es exacto.

## Diseño (Figma)

Prototipo con 8 pantallas navegables, 19 variables de color, estilos de texto y componentes reutilizables (botón, badge de estado, campo, tarjeta KPI, logo). Ver `docs/diseno.md`.
