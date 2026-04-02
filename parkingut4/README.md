# Proyecto Parking (PROGDAM 1º DAM) — v5

## Estado actual
Este repositorio contiene la evolución de un proyecto de **gestión de parking** desarrollado en Java.  
La versión más reciente del código es **v5** (tag `v5`).

La **documentación técnica por versión** se encuentra en:
- `parkingut4/README.md` (cambia según la tag/commit en el que estés)

---

## Ejecución del proyecto

### Opción A: NetBeans (recomendado)
1. Abre el proyecto en NetBeans.
2. Localiza la clase principal:
   - `parkingut4/src/parking/aplicacion/AplicacionParking.java`
3. Ejecuta el proyecto (Run).

> Nota: el programa puede generar/usar `parking.dat` (serialización) como persistencia del estado.

### Opción B: Terminal (javac/java)
Desde la raíz del repo, compila y ejecuta (ajusta si tu estructura de carpetas/clases difiere):

```bash
cd parkingut4/src
javac parking/aplicacion/AplicacionParking.java
java parking.aplicacion.AplicacionParking
```

---

## Cómo navegar por versiones (tags)

Puedes moverte a una versión concreta con Git:

```bash
git fetch --tags
git switch --detach v4
```

Para volver a `master`:

```bash
git switch master
```

### Tags principales
- `v1`: versión inicial (base del enunciado)
- `v2`: ampliación intermedia
- `v3`: refactor de estructuras internas (arrays → colecciones en `Parking`)
- `v4`: ampliación funcional + uso de Collections/Streams/Arrays
- `v5`: mejoras de implementación (Comparator / formato de salida)

---

## Documentación técnica por versión (dentro de cada versión)
En cada tag (por ejemplo `v4` o `v5`) puedes leer:

- `parkingut4/README.md`

Ahí se documenta **el estado y decisiones técnicas** de esa versión en concreto.

---

## Enunciado original (v1) y objetivo del proyecto

> **Módulo**: Programación — 1.º DAM / DAW  
> **Resultado de Aprendizaje**: diseño y uso de clases en Java  
> **Contexto**: aplicación de consola para gestionar un parking (entradas, salidas, facturación y consultas)

### Objetivo general
Implementar una aplicación que gestione:
- Entrada de vehículos (creación de ticket)
- Salida de vehículos (cierre de ticket con forma de pago e importe)
- Listados y resumen de facturación
- Persistencia de datos mediante serialización (`parking.dat`)

---

## Evolución por versiones (resumen)

| Versión | Idea principal | Cambios destacados |
|---|---|---|
| v1 | Versión base | Operaciones básicas de entrada/salida y facturación |
| v2 | Iteración y mejoras | Consolidación del modelo y lógica |
| v3 | **Colecciones** | `ticketsAbiertos`: `Map<String,Ticket>` y `ticketsCerrados`: `List<Ticket>` |
| v4 | **Funcionalidad + técnicas Java** | Menú ampliado y uso de `Collections`, `Streams`, `Arrays`, `Iterator` |
| v5 | **Refactor + salida** | `Comparator.comparing(...)` y alineación de importes en listados |

---

## Notas
- El fichero `parking.dat` puede quedar incompatible entre versiones si cambia la estructura serializada.
- Para ver el comportamiento “exacto” de una versión, usa su **tag** correspondiente.