# Documentación Técnica — Sistema de Gestión de Parking v5

## Índice
1. [Resumen de la versión](#1-resumen-de-la-versión)
2. [Qué cambia respecto a v4](#2-qué-cambia-respecto-a-v4)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Menú de la aplicación](#4-menú-de-la-aplicación)
5. [Operaciones principales en `Parking` (v4+)](#5-operaciones-principales-en-parking-v4)
6. [Mejoras de implementación en v5](#6-mejoras-de-implementación-en-v5)
7. [Cambios en `Ticket` (Comparable)](#7-cambios-en-ticket-comparable)
8. [Colecciones/Arrays/Streams utilizados (objetivo didáctico)](#8-coleccionesarraysstreams-utilizados-objetivo-didáctico)
9. [Persistencia y compatibilidad](#9-persistencia-y-compatibilidad)
10. [Gestión de errores y casos límite](#10-gestión-de-errores-y-casos-límite)

---

## 1. Resumen de la versión

**Versión v5** consolida la ampliación funcional introducida en v4 y se centra en:
- mejorar la **legibilidad del código** (ordenaciones con `Comparator.comparing(...)`),
- mejorar la **presentación de listados** (alineación de importes/columnas con `String.format`).

---

## 2. Qué cambia respecto a v4

Cambios relevantes observados entre v4 y v5:

- **Ordenación en Streams más idiomática**:
  - En `Parking.buscarTicketsCerradosPorMatricula(...)` se sustituye:
    - `sorted((t1,t2) -> t1.getFechaEntrada().compareTo(t2.getFechaEntrada()))`
    - por `sorted(Comparator.comparing(Ticket::getFechaEntrada))`

- **Mejoras de formato en salida por consola**:
  - Importes alineados con ancho fijo:
    - `String.format("%10.2f", importe)`
  - Ajustes en la tabla de “tickets cerrados por importe” para alinear mejor:
    - número y columna de importe.

---

## 3. Estructura del proyecto

```
parkingut4/
└── src/
    └── parking/
        ├── aplicacion/
        │   └── AplicacionParking.java
        ├── modelo/
        │   ├── FormaPago.java
        │   ├── Vehiculo.java
        │   ├── Ticket.java
        │   └── Parking.java
        └── utilidades/
            └── Utilidades.java

parking.dat   ← fichero serializado con el estado del parking
```

---

## 4. Menú de la aplicación

El menú (introducido en v4) se mantiene:

1. Registrar entrada de vehículo  
2. Registrar salida de vehículo  
3. Mostrar resumen de facturación  
4. Listar tickets abiertos  
5. Listar tickets cerrados  
6. Estadísticas de tickets cerrados  
7. Buscar tickets cerrados por matrícula  
8. Listar tickets cerrados por importe  
9. Anular ticket abierto  
10. Vehículos recurrentes  
11. Ranking de los N tickets más caros  
12. Salir  

---

## 5. Operaciones principales en `Parking` (v4+)

### 5.1 `obtenerEstadisticas()`
Devuelve un bloque de texto con:
- Ticket más caro (`Collections.max`)
- Ticket más barato (`Collections.min`)
- Número de tickets por forma de pago (`Collections.frequency`)

---

### 5.2 `buscarTicketsCerradosPorMatricula(String matricula)`
Busca en `ticketsCerrados`:
- Filtra por matrícula (ignorando mayúsculas/minúsculas)
- Ordena por fecha de entrada
- Devuelve el listado o un mensaje si no hay resultados

---

### 5.3 `listarTicketsCerradosOrdenadosPorImporte()`
Devuelve tickets cerrados ordenados por **importe (de mayor a menor)**:
- copia defensiva + `Collections.sort(..., Collections.reverseOrder())`

---

### 5.4 `anularTicketAbierto(String matricula)`
Elimina un ticket abierto por matrícula:
- recorre `ticketsAbiertos` con `Iterator` y borra con `it.remove()`

---

### 5.5 Helpers para Arrays (opciones 10 y 11)
- `String[] obtenerArrayMatriculas()`
- `double[] obtenerArrayImportes()`

---

## 6. Mejoras de implementación en v5

### 6.1 Uso de `Comparator.comparing(...)`
v5 usa `Comparator.comparing(Ticket::getFechaEntrada)` para ordenar por fecha de entrada:
- más legible
- más mantenible
- más estándar en Java moderno

### 6.2 Alineación de importes y columnas
Se mejora el formateo de importes (ancho fijo) para que las tablas/listados sean más claros:
- `String.format("%10.2f", ...)`
- y ajustes de `String.format` en el listado por importe.

---

## 7. Cambios en `Ticket` (Comparable)

Desde v4, `Ticket` implementa:
- `Comparable<Ticket>`
- `compareTo(...)` por `importeTotal` (asc)

Esto habilita:
- `Collections.max/min` en `ticketsCerrados`
- ordenaciones basadas en orden natural del ticket

---

## 8. Colecciones/Arrays/Streams utilizados (objetivo didáctico)

- **Collections**: `max`, `min`, `frequency`, `sort`, `reverseOrder`
- **Iterator**: borrado seguro en `Map`
- **Streams**: `filter`, `sorted`, `collect`
- **Arrays**: ordenaciones y rankings con copias

---

## 9. Persistencia y compatibilidad

Persistencia por **serialización Java** (`parking.dat`).

Nota: cambios en clases serializables pueden afectar compatibilidad con ficheros antiguos si cambian campos/UID.

---

## 10. Gestión de errores y casos límite

- Si no hay tickets cerrados, las opciones de estadísticas/listados informan.
- En ranking:
  - `n <= 0` → error
  - `n > total` → se ajusta a máximo disponible

---

**Versión documentada:** v5