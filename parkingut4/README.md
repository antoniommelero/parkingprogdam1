# Documentación Técnica — Sistema de Gestión de Parking v4

## Índice
1. [Resumen de la versión](#1-resumen-de-la-versión)
2. [Qué cambia respecto a v3](#2-qué-cambia-respecto-a-v3)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Cambios en el menú de la aplicación](#4-cambios-en-el-menú-de-la-aplicación)
5. [Nuevas operaciones en `Parking`](#5-nuevas-operaciones-en-parking)
6. [Cambios en `Ticket` (Comparable)](#6-cambios-en-ticket-comparable)
7. [Colecciones/Arrays/Streams utilizados (objetivo didáctico)](#7-coleccionesarraysstreams-utilizados-objetivo-didáctico)
8. [Persistencia y compatibilidad](#8-persistencia-y-compatibilidad)
9. [Gestión de errores y casos límite](#9-gestión-de-errores-y-casos-límite)

---

## 1. Resumen de la versión

**Versión v4** amplía el proyecto v3 añadiendo funcionalidades de consulta/estadística y nuevas opciones de menú, incorporando el uso de:

- `Collections` (`max`, `min`, `frequency`, `sort`)
- `Iterator` para borrado seguro en colecciones
- `Streams` (`filter`, `sorted`, `collect`)
- `Arrays` (`sort`, `copyOf`, `copyOfRange`) para análisis y ranking

---

## 2. Qué cambia respecto a v3

| Componente | v3 | v4 |
|---|---|---|
| `AplicacionParking` | Menú con 6 opciones | Menú ampliado a 12 opciones (nuevas consultas y utilidades) |
| `Parking` | Operaciones básicas (entrada/salida/listados/resumen) | Añade estadísticas, búsquedas, ordenaciones, anulación y helpers para arrays |
| `Ticket` | `Serializable` | `Serializable` + `Comparable<Ticket>` (orden natural por importe) |

> Nota: En v4 ya **no se cumple** el requisito de “no modificar `AplicacionParking`” que se mencionaba en v3, porque la aplicación principal se amplía con nuevas opciones.

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

## 4. Cambios en el menú de la aplicación

En **v4**, el menú queda así:

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

## 5. Nuevas operaciones en `Parking`

### 5.1 `obtenerEstadisticas()`
Devuelve un bloque de texto con:
- Ticket más caro (`Collections.max`)
- Ticket más barato (`Collections.min`)
- Número de tickets por forma de pago (`Collections.frequency`)

**Requisito clave:** `Ticket` debe ser comparable para poder usar `max/min` con orden natural.

---

### 5.2 `buscarTicketsCerradosPorMatricula(String matricula)`
Busca en `ticketsCerrados`:
- Filtra por matrícula (ignorando mayúsculas/minúsculas)
- Ordena por fecha de entrada
- Devuelve el listado de tickets encontrados o un mensaje si no hay resultados

Implementación destacable:
- `stream()`
- `filter(...)`
- `sorted(...)`
- `collect(Collectors.toList())`

---

### 5.3 `listarTicketsCerradosOrdenadosPorImporte()`
Devuelve tickets cerrados ordenados por **importe (de mayor a menor)**.

Puntos clave:
- Copia defensiva de la lista para **no modificar el orden original de inserción**
- `Collections.sort(copia, Collections.reverseOrder())`

---

### 5.4 `anularTicketAbierto(String matricula)`
Permite eliminar (anular) un ticket abierto por matrícula.

Puntos clave:
- Se recorre `ticketsAbiertos.entrySet()` con `Iterator`
- Se elimina con `it.remove()` (borrado seguro durante iteración)
- Devuelve `true` si se anuló, `false` si no existía

---

### 5.5 Helpers para Arrays (opciones 10 y 11)
Para facilitar ejercicios con `Arrays` desde `AplicacionParking`:

- `String[] obtenerArrayMatriculas()`  
  Devuelve todas las matrículas de `ticketsCerrados`.

- `double[] obtenerArrayImportes()`  
  Devuelve todos los importes de `ticketsCerrados`.

---

## 6. Cambios en `Ticket` (Comparable)

En v4, `Ticket` implementa:

- `Comparable<Ticket>`
- `compareTo(Ticket otro)` por `importeTotal` (ascendente)

Esto permite:
- `Collections.max(ticketsCerrados)` → ticket más caro
- `Collections.min(ticketsCerrados)` → ticket más barato
- Ordenaciones basadas en el orden natural del ticket

---

## 7. Colecciones/Arrays/Streams utilizados (objetivo didáctico)

- **Collections**
  - `max/min` para extremos
  - `frequency` para contar categorías
  - `sort` + `reverseOrder` para ranking/ordenación

- **Iterator**
  - Eliminación segura de elementos en un `Map` mientras se itera

- **Streams**
  - Búsqueda avanzada y ordenación declarativa sobre listas

- **Arrays**
  - Ordenación y creación de rankings con copias para no alterar datos originales

---

## 8. Persistencia y compatibilidad

La persistencia sigue siendo por **serialización Java** (`parking.dat`).

⚠️ Importante:
- Al cambiar la implementación de `Ticket` (ahora implementa `Comparable`), normalmente **no debería romper** compatibilidad por sí solo si no cambian `serialVersionUID` ni la estructura serializada, pero cualquier cambio de clases serializables puede afectar si se modifican campos/UID.
- Si se producen errores al cargar `parking.dat`, el programa debería recrear el parking (según el manejo que ya existía en versiones previas).

---

## 9. Gestión de errores y casos límite

- **Matrículas**: se validan en la aplicación antes de operar (cuando aplica).
- **Listados/estadísticas**:
  - Si no hay tickets cerrados, las opciones 6/8/10/11 informan de que no hay datos para analizar.
- **Ranking (opción 11)**:
  - Si `n <= 0`, se muestra un mensaje de error.
  - Si `n > número de tickets cerrados`, se ajusta para mostrar todos los disponibles.

---

**Versión documentada:** v4