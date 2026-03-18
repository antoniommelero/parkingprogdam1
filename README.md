# Documentación Técnica — Sistema de Gestión de Parking v3

## Índice

1. [Enunciado de la Tarea](#1-enunciado-de-la-tarea)
2. [Qué cambia respecto a la versión anterior](#2-qué-cambia-respecto-a-la-versión-anterior)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Modelo de clases y relaciones](#4-modelo-de-clases-y-relaciones)
5. [Especificación de la clase Parking](#5-especificación-de-la-clase-parking)
6. [Comparativa de implementación: arrays vs colecciones](#6-comparativa-de-implementación-arrays-vs-colecciones)
7. [Persistencia de datos](#7-persistencia-de-datos)
8. [Gestión de excepciones](#8-gestión-de-excepciones)
9. [Decisiones de implementación destacables](#9-decisiones-de-implementación-destacables)

---

## 1. Enunciado de la Tarea

> **Módulo**: Programación — 1.º DAM / DAW  
> **Resultado de Aprendizaje**: RA4 — Diseño y uso de clases en Java  
> **Base**: Proyecto de gestión de parking v2 (arrays manuales en `Parking`)

### Descripción del problema

A partir de la versión anterior se sustituyen las dos estructuras de datos de la clase `Parking`:

- El array `Ticket[]` de tickets abiertos se reemplaza por un **`Map<String, Ticket>`**, usando la matrícula como clave. Esto hace que la búsqueda de un ticket abierto por matrícula sea directa e inmediata, sin necesidad de recorrer el array ni mantener índices.

- El array `Ticket[]` de tickets cerrados se reemplaza por una **`List<Ticket>`** (`ArrayList`), que gestiona automáticamente la capacidad y elimina la necesidad de crear nuevos arrays en cada inserción.

El objetivo didáctico es contrastar las tres aproximaciones al mismo problema vistas en el módulo: arrays de tamaño fijo, arrays de tamaño dinámico manual y colecciones de la API de Java.

### Requisito de compatibilidad

El enunciado establece explícitamente que **el programa principal (`AplicacionParking`) no debe requerir ningún cambio** al pasar de v2 a v3. La interfaz pública de `Parking` debe mantenerse idéntica; solo cambia su implementación interna.

### Clases afectadas

| Clase | Cambio |
|---|---|
| `Parking` | Sustitución completa de las estructuras internas |
| `AplicacionParking` | Sin cambios |
| `Ticket` | Sin cambios |
| `Vehiculo` | Sin cambios |
| `Utilidades` | Sin cambios |
| `FormaPago` | Sin cambios |

---

## 2. Qué cambia respecto a la versión anterior

| Aspecto | Versión 2 | Versión 3 |
|---|---|---|
| Estructura para tickets abiertos | `Ticket[]` (array de tamaño exacto) | `Map<String, Ticket>` (`HashMap`) |
| Clave de búsqueda en abiertos | Índice entero buscado con bucle | Matrícula directamente como clave |
| Estructura para tickets cerrados | `Ticket[]` (array de tamaño exacto) | `List<Ticket>` (`ArrayList`) |
| Inserción en abiertos | `System.arraycopy` + reemplazo del array | `map.put(matricula, ticket)` |
| Búsqueda en abiertos | Bucle + `buscarIndiceAbiertoPorMatricula()` | `map.get(matricula)` / `map.containsKey()` |
| Eliminación de abiertos | `System.arraycopy` en dos tramos | `map.remove(matricula)` |
| Inserción en cerrados | `System.arraycopy` + reemplazo del array | `list.add(ticket)` |
| Comprobación de vacío | `array.length == 0` | `map.isEmpty()` / `list.isEmpty()` |
| Iteración para listados | Bucle `for` con índice | `for-each` sobre `map.values()` / `list` |
| Métodos privados eliminados | `buscarIndiceAbiertoPorMatricula()`, `eliminarTicketAbierto()`, `agregarACerrados()` | Ya no son necesarios |
| `serialVersionUID` en `Parking` | `1L` | `2L` (estructura interna cambiada) |
| `existeTicketAbierto()` | Método público | Eliminado (su lógica queda implícita en `registrarEntrada` con `containsKey`) |

---

## 3. Estructura del proyecto

Idéntica a v2. El único fichero que cambia en disco es `Parking.java`.

```
src/
└── parking/
    ├── aplicacion/
    │   └── AplicacionParking.java   ← Sin cambios respecto a v2
    ├── modelo/
    │   ├── FormaPago.java           ← Sin cambios
    │   ├── Vehiculo.java            ← Sin cambios
    │   ├── Ticket.java              ← Sin cambios
    │   └── Parking.java             ← MODIFICADA: Map + List en lugar de arrays
    └── utilidades/
        └── Utilidades.java          ← Sin cambios

parking.dat                          ← Fichero de estado serializado
```

> **Nota sobre `parking.dat`**: al cambiar `serialVersionUID` de `1L` a `2L` en `Parking`, cualquier fichero `parking.dat` generado con v2 es **incompatible** con v3 y provocará `InvalidClassException` al cargar. El programa lo gestiona creando un parking nuevo en ese caso.

---

## 4. Modelo de clases y relaciones

Las relaciones entre clases no varían. Lo que cambia es el tipo concreto de las estructuras internas de `Parking`.

```
AplicacionParking
    └── tiene ──► Parking  (parking : Parking)
                     │
                     ├── ticketsAbiertos : Map<String, Ticket>
                     │       clave ──► matrícula (String)
                     │       valor ──► Ticket
                     │                  └── contiene ──► Vehiculo
                     │
                     └── ticketsCerrados : List<Ticket>
                             └── cada Ticket contiene ──► Vehiculo
```

---

## 5. Especificación de la clase Parking

### Atributos

| Atributo | Tipo | Visibilidad | Descripción |
|---|---|---|---|
| `serialVersionUID` | `static final long` | `private` | `2L` — versión de serialización |
| `ticketsAbiertos` | `Map<String, Ticket>` | `private` | Tickets en curso, indexados por matrícula |
| `ticketsCerrados` | `List<Ticket>` | `private` | Histórico de tickets ya facturados |
| `totalEntradas` | `int` | `private` | Total de entradas registradas |
| `totalSalidas` | `int` | `private` | Total de salidas registradas |
| `importeTotalFacturado` | `double` | `private` | Suma de todos los importes cobrados |
| `importeEfectivo` | `double` | `private` | Acumulado cobrado en efectivo |
| `importeTarjeta` | `double` | `private` | Acumulado cobrado con tarjeta |
| `importeMensual` | `double` | `private` | Acumulado cobrado a abonados |

### Constructor

```java
public Parking()
```

Inicializa `ticketsAbiertos` como `new HashMap<>()` y `ticketsCerrados` como `new ArrayList<>()`. Todos los acumuladores a cero.

---

### Métodos públicos

La **signatura de todos los métodos públicos es idéntica a v2**. Solo cambia su implementación interna.

---

#### `registrarEntrada(Vehiculo vehiculo, LocalDateTime fechaEntrada)`

```java
public Ticket registrarEntrada(Vehiculo vehiculo, LocalDateTime fechaEntrada)
```

| Paso | v2 (array) | v3 (Map) |
|---|---|---|
| Comprobar duplicado | `existeTicketAbierto()` → bucle interno | `ticketsAbiertos.containsKey(matricula)` |
| Insertar | `System.arraycopy` + reemplazo | `ticketsAbiertos.put(matricula, ticket)` |

Flujo completo:

1. Si `ticketsAbiertos.containsKey(vehiculo.getMatricula())` → `IllegalArgumentException`.
2. Instancia `new Ticket(vehiculo, fechaEntrada)`.
3. `ticketsAbiertos.put(vehiculo.getMatricula(), ticket)`.
4. Incrementa `totalEntradas`.
5. Devuelve el ticket.

---

#### `registrarSalida(String matricula, LocalDateTime salida, FormaPago formaPago)`

```java
public Ticket registrarSalida(String matricula, LocalDateTime salida, FormaPago formaPago)
```

| Paso | v2 (array) | v3 (Map + List) |
|---|---|---|
| Buscar ticket | `buscarIndiceAbiertoPorMatricula()` → índice | `ticketsAbiertos.get(matricula)` |
| Eliminar de abiertos | `eliminarTicketAbierto(indice)` | `ticketsAbiertos.remove(matricula)` |
| Añadir a cerrados | `agregarACerrados(ticket)` | `ticketsCerrados.add(ticket)` |

Flujo completo:

1. `Ticket ticket = ticketsAbiertos.get(matricula)` — si `null`, lanza `IllegalArgumentException`.
2. `ticket.salir(salida, formaPago)` — cierra el ticket.
3. `ticketsAbiertos.remove(matricula)` — elimina del mapa.
4. `ticketsCerrados.add(ticket)` — añade a la lista.
5. Actualiza acumuladores: `totalSalidas`, `importeTotalFacturado` y el acumulado de la forma de pago.
6. Devuelve el ticket cerrado.

> **Atención**: en v3 `registrarSalida` no lanza explícitamente la excepción cuando no encuentra el ticket — si `get()` devuelve `null` y se llama a `ticket.salir()`, se producirá una `NullPointerException`. En un entorno de producción convendría añadir la comprobación explícita `if (ticket == null) throw new IllegalArgumentException(...)`. En la práctica esto no ocurre porque `AplicacionParking` comprueba primero con `obtenerTicketAbierto()`.

---

#### `obtenerTicketAbierto(String matricula)`

```java
public Ticket obtenerTicketAbierto(String matricula)
```

Devuelve `ticketsAbiertos.get(matricula)`, que es `null` si no existe la clave. Comportamiento externo idéntico a v2.

---

#### `obtenerResumenFacturacion()`

Sin cambios funcionales. Los acumuladores son los mismos; solo varía que ya no dependen de atributos estáticos de `Ticket`.

---

#### `listarTicketsAbiertos()`

```java
public String listarTicketsAbiertos()
```

Usa `ticketsAbiertos.isEmpty()` para la comprobación de vacío y `ticketsAbiertos.values()` para iterar con `for-each`. El orden de los tickets en el listado puede variar entre ejecuciones al usar `HashMap`, cuyo orden de iteración no está garantizado.

---

#### `listarTicketsCerrados()`

```java
public String listarTicketsCerrados()
```

Usa `ticketsCerrados.isEmpty()` y un `for-each` sobre la lista. El orden es de inserción (el mismo en que se registraron las salidas), ya que `ArrayList` preserva el orden.

---

### Métodos privados eliminados en v3

Los tres métodos privados de v2 ya no son necesarios porque las colecciones de la API de Java los sustituyen con una sola llamada:

| Método eliminado (v2) | Sustituido por (v3) |
|---|---|
| `buscarIndiceAbiertoPorMatricula(String)` | `map.get(matricula)` / `map.containsKey(matricula)` |
| `eliminarTicketAbierto(int)` | `map.remove(matricula)` |
| `agregarACerrados(Ticket)` | `list.add(ticket)` |

El método `existeTicketAbierto(String)` público también ha sido eliminado: su único uso era interno en `registrarEntrada()`, donde ahora se usa directamente `containsKey()`.

---

## 6. Comparativa de implementación: arrays vs colecciones

Esta sección resume la evolución de las tres versiones del proyecto sobre la misma operación de ejemplo: **registrar la entrada de un vehículo**.

### Inserción en la estructura de tickets abiertos

**v1** — un solo ticket activo, sin estructura:
```java
ticketActivo = new Ticket(vehiculo, entrada);
```

**v2** — array manual de tamaño exacto:
```java
Ticket[] nuevoArray = new Ticket[ticketsAbiertos.length + 1];
System.arraycopy(ticketsAbiertos, 0, nuevoArray, 0, ticketsAbiertos.length);
nuevoArray[ticketsAbiertos.length] = ticket;
ticketsAbiertos = nuevoArray;
```

**v3** — `Map` de la API de Java:
```java
ticketsAbiertos.put(vehiculo.getMatricula(), ticket);
```

### Búsqueda de un ticket abierto por matrícula

**v2** — recorrido secuencial del array:
```java
for (int i = 0; i < ticketsAbiertos.length; i++) {
    if (ticketsAbiertos[i].getVehiculo().getMatricula().equals(matricula)) {
        return i;  // O(n) en el peor caso
    }
}
return -1;
```

**v3** — acceso directo por clave:
```java
ticketsAbiertos.get(matricula);  // O(1) amortizado
```

### Eliminación de un ticket abierto

**v2** — dos copias parciales del array con `arraycopy`:
```java
Ticket[] nuevoArray = new Ticket[ticketsAbiertos.length - 1];
System.arraycopy(ticketsAbiertos, 0, nuevoArray, 0, indice);
System.arraycopy(ticketsAbiertos, indice + 1, nuevoArray, indice,
                 ticketsAbiertos.length - indice - 1);
ticketsAbiertos = nuevoArray;
```

**v3** — eliminación directa por clave:
```java
ticketsAbiertos.remove(matricula);
```

---

## 7. Persistencia de datos

El mecanismo de persistencia es idéntico al de v2 (serialización Java con `ObjectInputStream` / `ObjectOutputStream`). Los métodos `cargarParking()` y `guardarParking()` en `AplicacionParking` no han cambiado.

Lo que sí cambia es que `Parking` ahora contiene un `HashMap` y un `ArrayList`. Ambas clases implementan `Serializable`, por lo que la serialización funciona correctamente sin ningún cambio adicional.

### Incompatibilidad con ficheros de v2

El `serialVersionUID` de `Parking` ha cambiado de `1L` a `2L`. Un fichero `parking.dat` generado con v2 **no puede cargarse** en v3: Java lanzará `InvalidClassException`, que es capturada por el `catch (IOException | ClassNotFoundException)` de `cargarParking()`, iniciando un parking vacío de forma controlada.

---

## 8. Gestión de excepciones

Sin cambios respecto a v2, con la salvedad indicada en `registrarSalida()`.

| Excepción | Origen | Motivo |
|---|---|---|
| `IllegalArgumentException` | `Parking.registrarEntrada()` | Ya existe ticket abierto para esa matrícula (`containsKey`) |
| `IllegalArgumentException` / `NullPointerException` | `Parking.registrarSalida()` | No existe ticket abierto para esa matrícula |
| `IllegalArgumentException` | `Ticket.salir()` | `salida` nula |
| `IllegalStateException` | `Ticket.salir()` | Ticket ya estaba cerrado |
| `IllegalArgumentException` | `Vehiculo` constructor | Matrícula, propietario o descripción inválidos |
| `IllegalArgumentException` | `Utilidades.validarMatricula()` | Formato de matrícula incorrecto |
| `IllegalArgumentException` | `Utilidades.validarSalida()` | Fecha de salida anterior a la de entrada |
| `IOException` | `cargarParking()` / `guardarParking()` | Error de E/S en el fichero |
| `ClassNotFoundException` / `InvalidClassException` | `cargarParking()` | Fichero incompatible (generado con otra versión) |

---

## 9. Decisiones de implementación destacables

### Principio de encapsulamiento como objetivo de diseño

El enunciado establece como requisito que `AplicacionParking` no necesite ningún cambio al pasar de v2 a v3. Esto se consigue porque la interfaz pública de `Parking` no varía: mismos nombres de métodos, mismas signaturas, mismo comportamiento observable. El cambio es puramente interno. Este es un ejemplo práctico del principio de **encapsulamiento**: los clientes de una clase no deben depender de cómo está implementada, sino solo de qué hace.

### `HashMap` para tickets abiertos

Se elige `HashMap` porque la operación dominante sobre tickets abiertos es la búsqueda por matrícula, y `HashMap` la resuelve en tiempo O(1) amortizado. La restricción de un único ticket abierto por matrícula queda garantizada estructuralmente: una clave en un `Map` no puede tener dos valores simultáneos.

### `ArrayList` para tickets cerrados

Se elige `ArrayList` porque sobre los tickets cerrados solo se realizan inserciones al final e iteraciones completas para listar, que son las operaciones más eficientes de `ArrayList`. No se necesita búsqueda ni eliminación en esta colección.

### Código comentado como trazabilidad del cambio

En el código fuente de v3, la implementación de v2 permanece comentada junto a la nueva. Esto hace visible de forma inmediata qué líneas han sido reemplazadas y por qué, lo que facilita la comprensión del cambio y su revisión en clase.

### `serialVersionUID = 2L`

El cambio de versión de `1L` a `2L` es deliberado: la estructura interna de `Parking` ha cambiado lo suficiente como para que los objetos serializados con v2 no sean compatibles con v3. Declarar explícitamente el nuevo `serialVersionUID` hace que Java detecte la incompatibilidad con un error controlado en lugar de un comportamiento impredecible.
