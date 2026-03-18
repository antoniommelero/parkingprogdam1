# Documentación Técnica — Sistema de Gestión de Parking v2

## Índice

1. [Enunciado de la Tarea](#1-enunciado-de-la-tarea)
2. [Qué cambia respecto a la versión anterior](#2-qué-cambia-respecto-a-la-versión-anterior)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Modelo de clases y relaciones](#4-modelo-de-clases-y-relaciones)
5. [Especificación de clases](#5-especificación-de-clases)
   - 5.1 [FormaPago](#51-formapago--parkingmodeloformapago)
   - 5.2 [Vehiculo](#52-vehiculo--parkingmodelovehiculo)
   - 5.3 [Ticket](#53-ticket--parkingmodeloticket)
   - 5.4 [Parking](#54-parking--parkingmodeloparking)
   - 5.5 [Utilidades](#55-utilidades--parkingutilidades)
   - 5.6 [AplicacionParking](#56-aplicacionparking--parkingaplicacionaplicacionparking)
6. [Gestión de arrays sin colecciones](#6-gestión-de-arrays-sin-colecciones)
7. [Persistencia de datos](#7-persistencia-de-datos)
8. [Lógica de tarifación](#8-lógica-de-tarifación)
9. [Gestión de excepciones](#9-gestión-de-excepciones)
10. [Decisiones de implementación destacables](#10-decisiones-de-implementación-destacables)

---

## 1. Enunciado de la Tarea

> **Módulo**: Programación — 1.º DAM / DAW  
> **Resultado de Aprendizaje**: RA4 — Diseño y uso de clases en Java  
> **Base**: Proyecto de gestión de parking v1 (ticket único activo)

### Descripción del problema

A partir del proyecto anterior se introduce una nueva clase `Parking` que gestiona **todos los tickets simultáneamente** mediante arrays de objetos. El sistema debe ser capaz de:

- Mantener un array de **tickets abiertos** (vehículos que han entrado y aún no han salido).
- Mantener un array de **tickets cerrados** (estancias ya facturadas).
- Registrar entradas y salidas operando sobre esos arrays: inserción, búsqueda y eliminación de elementos **sin usar colecciones** (`ArrayList`, `List`, etc.).
- Trasladar los acumulados estadísticos desde `Ticket` a `Parking`, donde resulta más coherente mantenerlos.
- Ampliar el menú con la posibilidad de **listar** tanto los vehículos actualmente en el parking como el histórico de tickets cerrados.

### Operaciones sobre arrays requeridas

| Operación | Dónde se aplica | Método responsable |
|---|---|---|
| Inserción al final | `ticketsAbiertos` al registrar entrada | `registrarEntrada()` |
| Búsqueda por matrícula | `ticketsAbiertos` | `buscarIndiceAbiertoPorMatricula()` |
| Eliminación por índice | `ticketsAbiertos` al registrar salida | `eliminarTicketAbierto()` |
| Inserción al final | `ticketsCerrados` al registrar salida | `agregarACerrados()` |

### Funcionalidades adicionales respecto a v1

- Listar todos los vehículos actualmente en el parking (tickets abiertos).
- Listar el histórico de tickets cerrados con fecha de salida e importe.
- **Persistencia**: el estado completo del parking se guarda en disco al salir y se recupera al iniciar.

---

## 2. Qué cambia respecto a la versión anterior

| Aspecto | Versión 1 | Versión 2 |
|---|---|---|
| Tickets simultáneos | Solo uno (`ticketActivo`) | Ilimitados (arrays en `Parking`) |
| Estadísticas globales | Atributos estáticos en `Ticket` | Atributos de instancia en `Parking` |
| Búsqueda de ticket activo | Referencia directa a `ticketActivo` | Búsqueda por matrícula en array |
| Persistencia | No existía | Serialización a fichero `parking.dat` |
| Clases nuevas | — | `Parking` |
| Menú | 4 opciones | 6 opciones (+ listar abiertos y cerrados) |
| `Ticket` | Estadísticas propias (estáticas) | Estadísticas eliminadas (comentadas) |
| `Utilidades.leerFechaHora()` | Crea su propio `Scanner` interno | Recibe el `Scanner` como parámetro |
| `Vehiculo` / `Ticket` | Sin serialización | Implementan `Serializable` |

---

## 3. Estructura del proyecto

```
src/
└── parking/
    ├── aplicacion/
    │   └── AplicacionParking.java   ← Clase principal (main, menú, carga/guardado)
    ├── modelo/
    │   ├── FormaPago.java           ← Enumerado de medios de pago (sin cambios)
    │   ├── Vehiculo.java            ← Entidad vehículo (+ Serializable)
    │   ├── Ticket.java              ← Entidad ticket (estadísticas eliminadas, + Serializable)
    │   └── Parking.java             ← NUEVA: gestiona los dos arrays y las estadísticas
    └── utilidades/
        └── Utilidades.java          ← Validación y lectura (leerFechaHora recibe Scanner)

parking.dat                          ← Fichero de estado serializado (se genera en ejecución)
```

---

## 4. Modelo de clases y relaciones

```
AplicacionParking
    │
    └── tiene ──► Parking  (parking : Parking)
                     │
                     ├── ticketsAbiertos : Ticket[]
                     │       └── cada Ticket contiene ──► Vehiculo
                     │
                     └── ticketsCerrados : Ticket[]
                             └── cada Ticket contiene ──► Vehiculo

Parking  usa ──► FormaPago  (en el switch de estadísticas)
Vehiculo usa ──► Utilidades (validarMatricula en el constructor)
AplicacionParking usa ──► Utilidades (leerFechaHora, validarSalida)
```

### Ciclo de vida de un ticket en v2

```
[ticketsAbiertos]                    [ticketsCerrados]
       │
       │  registrarEntrada(vehiculo, fechaEntrada)
       │  → new Ticket(vehiculo, fechaEntrada)
       │  → insertar al final del array
       ▼
  ticket en posición i
       │
       │  registrarSalida(matricula, salida, formaPago)
       │  → buscarIndiceAbiertoPorMatricula(matricula) → i
       │  → ticket.salir(salida, formaPago)
       │  → eliminarTicketAbierto(i)
       │  → agregarACerrados(ticket)
       │  → actualizar estadísticas en Parking
       ▼
                                [ticket cerrado al final del array]
```

---

## 5. Especificación de clases

---

### 5.1 `FormaPago` — `parking.modelo.FormaPago`

Sin cambios respecto a v1. Enumerado con `EFECTIVO`, `TARJETA` y `MENSUAL`.

---

### 5.2 `Vehiculo` — `parking.modelo.Vehiculo`

Cambio respecto a v1: implementa `Serializable` para permitir la persistencia del estado del parking en fichero.

```java
public class Vehiculo implements Serializable {
    private static final long serialVersionUID = 1L;
    ...
}
```

El resto de atributos, constructor y métodos no varía.

---

### 5.3 `Ticket` — `parking.modelo.Ticket`

#### Cambios respecto a v1

- Implementa `Serializable` (`serialVersionUID = 1L`).
- Los atributos estáticos de estadísticas (`totalEntradas`, `totalSalidas`, `importeTotalFacturado`, etc.) han sido **eliminados** — permanecen en el código comentados como trazabilidad del cambio.
- El método estático `obtenerResumenFacturacion()` también ha sido eliminado de esta clase (comentado) — la responsabilidad pasa a `Parking`.
- Se añaden los getters `getNumeroTicket()`, `getFechaSalida()`, `getFormaPago()` e `getImporteTotal()` que `Parking` necesita para construir los listados y actualizar estadísticas.
- `FORMATO_FECHA_HORA` pasa a ser `public static final` para que `Parking` pueda usarlo al construir los listados sin duplicarlo.

#### Nuevo getter expuesto como `public static final`

```java
public static final DateTimeFormatter FORMATO_FECHA_HORA =
    DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
```

#### Atributos de instancia (sin cambios funcionales)

| Atributo | Tipo | Descripción |
|---|---|---|
| `contadorTickets` | `static int` | Contador global para numeración consecutiva |
| `numeroTicket` | `int` | Número único del ticket |
| `vehiculo` | `Vehiculo` | Vehículo asociado |
| `fechaEntrada` | `LocalDateTime` | Fecha/hora de entrada |
| `fechaSalida` | `LocalDateTime` | Fecha/hora de salida (`null` si abierto) |
| `formaPago` | `FormaPago` | Forma de pago (fijada al cerrar) |
| `importeTotal` | `double` | Importe calculado al cerrar |

#### Métodos públicos

| Método | Retorno | Descripción |
|---|---|---|
| `salir(LocalDateTime, FormaPago)` | `void` | Cierra el ticket y calcula el importe |
| `getFechaEntrada()` | `LocalDateTime` | Getter de fecha de entrada |
| `getFechaSalida()` | `LocalDateTime` | Getter de fecha de salida |
| `getVehiculo()` | `Vehiculo` | Getter del vehículo |
| `getNumeroTicket()` | `int` | Getter del número de ticket |
| `getFormaPago()` | `FormaPago` | Getter de la forma de pago |
| `getImporteTotal()` | `double` | Getter del importe total |
| `imprimirTicketEntrada()` | `String` | Ticket de entrada formateado |
| `imprimirTicketSalida()` | `String` | Ticket completo con importe formateado |

---

### 5.4 `Parking` — `parking.modelo.Parking`

Clase nueva y central de esta versión. Gestiona los dos arrays de tickets y todas las estadísticas de facturación. Implementa `Serializable` para persistencia completa del estado.

#### Atributos

| Atributo | Tipo | Visibilidad | Descripción |
|---|---|---|---|
| `ticketsAbiertos` | `Ticket[]` | `private` | Array de tickets con entrada pero sin salida |
| `ticketsCerrados` | `Ticket[]` | `private` | Array de tickets ya facturados |
| `totalEntradas` | `int` | `private` | Total de entradas registradas |
| `totalSalidas` | `int` | `private` | Total de salidas registradas |
| `importeTotalFacturado` | `double` | `private` | Suma de todos los importes cobrados |
| `importeEfectivo` | `double` | `private` | Acumulado cobrado en efectivo |
| `importeTarjeta` | `double` | `private` | Acumulado cobrado con tarjeta |
| `importeMensual` | `double` | `private` | Acumulado cobrado a abonados |

#### Constructor

```java
public Parking()
```

Inicializa ambos arrays con longitud 0 y todos los acumuladores a cero.

#### Métodos públicos

##### `registrarEntrada(Vehiculo vehiculo, LocalDateTime fechaEntrada)`

```java
public Ticket registrarEntrada(Vehiculo vehiculo, LocalDateTime fechaEntrada)
```

Flujo:

1. Llama a `existeTicketAbierto(matricula)` — si ya hay uno abierto lanza `IllegalArgumentException`.
2. Instancia un nuevo `Ticket(vehiculo, fechaEntrada)`.
3. Crea un nuevo array de longitud `ticketsAbiertos.length + 1`.
4. Copia el array actual con `System.arraycopy()`.
5. Inserta el nuevo ticket en la última posición.
6. Reemplaza la referencia `ticketsAbiertos`.
7. Incrementa `totalEntradas`.
8. Devuelve el nuevo ticket (para que `AplicacionParking` lo imprima).

---

##### `registrarSalida(String matricula, LocalDateTime salida, FormaPago formaPago)`

```java
public Ticket registrarSalida(String matricula, LocalDateTime salida, FormaPago formaPago)
```

Flujo:

1. Llama a `buscarIndiceAbiertoPorMatricula(matricula)` — si devuelve `-1` lanza `IllegalArgumentException`.
2. Recupera el ticket del array en la posición encontrada.
3. Llama a `ticket.salir(salida, formaPago)` para cerrar el ticket.
4. Llama a `eliminarTicketAbierto(indice)` para sacarlo del array de abiertos.
5. Llama a `agregarACerrados(ticket)` para añadirlo al array de cerrados.
6. Actualiza estadísticas: `totalSalidas`, `importeTotalFacturado` y el acumulado de la forma de pago.
7. Devuelve el ticket cerrado.

---

##### `obtenerTicketAbierto(String matricula)`

```java
public Ticket obtenerTicketAbierto(String matricula)
```

Llama a `buscarIndiceAbiertoPorMatricula()`. Devuelve el ticket si lo encuentra o `null` en caso contrario. Usado en `AplicacionParking` para comprobar existencia antes de pedir la fecha de salida.

---

##### `existeTicketAbierto(String matricula)`

```java
public boolean existeTicketAbierto(String matricula)
```

Devuelve `true` si `buscarIndiceAbiertoPorMatricula()` ≥ 0, `false` en caso contrario.

---

##### `obtenerResumenFacturacion()`

```java
public String obtenerResumenFacturacion()
```

Devuelve un `String` formateado con totales de entradas, salidas, importe facturado y desglose por forma de pago.

---

##### `listarTicketsAbiertos()`

```java
public String listarTicketsAbiertos()
```

Recorre `ticketsAbiertos` y construye un `StringBuilder` con una línea por ticket: número, matrícula y fecha/hora de entrada. Devuelve `"No hay tickets abiertos."` si el array está vacío.

---

##### `listarTicketsCerrados()`

```java
public String listarTicketsCerrados()
```

Recorre `ticketsCerrados` y construye un `StringBuilder` con una línea por ticket: número, matrícula, fecha de entrada, fecha de salida e importe. Devuelve `"No hay tickets cerrados."` si el array está vacío.

---

#### Métodos privados

##### `buscarIndiceAbiertoPorMatricula(String matricula)`

```java
private int buscarIndiceAbiertoPorMatricula(String matricula)
```

Recorre `ticketsAbiertos` de principio a fin comparando `getVehiculo().getMatricula().equals(matricula)`. Devuelve el índice si lo encuentra o `-1` si no existe. Es la base de todos los demás métodos de búsqueda.

---

##### `eliminarTicketAbierto(int indice)`

```java
private void eliminarTicketAbierto(int indice)
```

Gestiona dos casos:

- **Caso especial** — solo queda un elemento: reemplaza `ticketsAbiertos` por un array vacío `new Ticket[0]`.
- **Caso general** — crea un array de longitud `ticketsAbiertos.length - 1` y copia en dos tramos con `System.arraycopy()`:
  - Tramo izquierdo: posiciones `0` hasta `indice - 1`.
  - Tramo derecho: posiciones `indice + 1` hasta el final.

---

##### `agregarACerrados(Ticket ticket)`

```java
private void agregarACerrados(Ticket ticket)
```

Crea un array de longitud `ticketsCerrados.length + 1`, copia el array actual con `System.arraycopy()` e inserta el ticket en la última posición.

---

### 5.5 `Utilidades` — `parking.utilidades.Utilidades`

#### Cambio respecto a v1

`leerFechaHora()` ahora recibe el `Scanner` como parámetro en lugar de crear uno propio. Esto evita problemas con múltiples instancias de `Scanner` sobre `System.in` y centraliza la gestión del `Scanner` en `AplicacionParking`.

```java
// v1
public static LocalDateTime leerFechaHora(String mensaje)

// v2
public static LocalDateTime leerFechaHora(String mensaje, Scanner scanner)
```

Los métodos `validarMatricula()` y `validarSalida()` no cambian.

---

### 5.6 `AplicacionParking` — `parking.aplicacion.AplicacionParking`

#### Cambios respecto a v1

- Ya no gestiona un `ticketActivo` individual: toda la lógica de estado se delega a `parking` (instancia de `Parking`).
- Se añaden las opciones 4 (listar abiertos) y 5 (listar cerrados) al menú.
- La opción "Salir" pasa a ser la 6, y antes de salir llama a `guardarParking()`.
- Al iniciar `main()` se llama a `cargarParking()`.
- El `switch` del menú usa la sintaxis de expresiones (`case X -> ...`) en lugar de `case X: ... break`.
- En `registrarSalida()`, antes de pedir la forma de pago, se llama a `listarAbiertos()` para mostrar al usuario qué vehículos están dentro.

#### Atributos de clase

| Atributo | Tipo | Descripción |
|---|---|---|
| `parking` | `static Parking` | Objeto único que centraliza todos los datos |
| `teclado` | `static Scanner` | Instancia única de `Scanner` para toda la E/S |
| `ARCHIVO_DATOS` | `static final String` | Nombre del fichero de persistencia (`parking.dat`) |

#### Flujo del menú

```
Al iniciar → cargarParking()   (lee parking.dat si existe)

┌────────────────────────────────────────┐
│          --- MENU PARKING ---          │
│  1. Registrar entrada de vehiculo      │
│  2. Registrar salida de vehiculo       │
│  3. Mostrar resumen de facturacion     │
│  4. Listar tickets abiertos            │
│  5. Listar tickets cerrados            │
│  6. Salir                              │
└────────────────────────────────────────┘

Opción 1 — Registrar entrada
    → Pedir: matrícula (validar), propietario, descripción, abonado, fecha/hora
    → new Vehiculo(...)
    → parking.registrarEntrada(vehiculo, entrada)
    → Mostrar imprimirTicketEntrada()

Opción 2 — Registrar salida
    → Mostrar listarTicketsAbiertos()    (ayuda visual)
    → Pedir matrícula (validar)
    → parking.obtenerTicketAbierto(matricula)  → null: mensaje de error
    → Pedir fecha/hora de salida
    → Validar con Utilidades.validarSalida()
    → Si NO es abonado: pedir forma de pago
    → parking.registrarSalida(matricula, salida, formaPago)
    → Mostrar imprimirTicketSalida()

Opción 3 → parking.obtenerResumenFacturacion()
Opción 4 → parking.listarTicketsAbiertos()
Opción 5 → parking.listarTicketsCerrados()

Opción 6 → guardarParking()   (escribe parking.dat)
           Fin del bucle do-while
```

#### Método `cargarParking()`

```java
private static void cargarParking()
```

- Comprueba si existe el fichero `parking.dat`.
- Si existe, deserializa el objeto `Parking` con `ObjectInputStream`.
- Si no existe o hay error, crea un `new Parking()` limpio.

#### Método `guardarParking()`

```java
private static void guardarParking()
```

- Serializa el objeto `parking` completo con `ObjectOutputStream` sobre `parking.dat`.
- Si falla, muestra el mensaje de error pero no interrumpe el cierre.

---

## 6. Gestión de arrays sin colecciones

Toda la gestión dinámica de los arrays se realiza manualmente: cada operación de inserción o eliminación crea un nuevo array del tamaño adecuado, copia los elementos con `System.arraycopy()` y reemplaza la referencia anterior. El recolector de basura de Java libera el array antiguo.

### Inserción al final (usado en `registrarEntrada` y `agregarACerrados`)

```
array original:  [ T1 | T2 | T3 ]  (longitud 3)
                          ↓ nuevo array longitud 4
nuevo array:     [ T1 | T2 | T3 | nuevo ]
                   ←── arraycopy ──→  ↑ inserción manual
```

### Eliminación por índice (usado en `eliminarTicketAbierto`)

```
array original:  [ T1 | T2 | T3 | T4 ]  (eliminar índice 1 → T2)
                          ↓ nuevo array longitud 3
                 arraycopy tramo izquierdo: [ T1 ]
                 arraycopy tramo derecho:         [ T3 | T4 ]
nuevo array:     [ T1 | T3 | T4 ]
```

> **Caso especial**: si solo hay un elemento, se asigna directamente `new Ticket[0]` sin llamar a `arraycopy`.

---

## 7. Persistencia de datos

El estado completo del parking (ambos arrays y todos los acumuladores) se persiste entre sesiones mediante **serialización de objetos Java**.

### Clases que implementan `Serializable`

| Clase | `serialVersionUID` |
|---|---|
| `Parking` | `1L` |
| `Ticket` | `1L` |
| `Vehiculo` | `1L` |

### Flujo de persistencia

```
Inicio del programa
    └── ¿existe parking.dat?
         ├── Sí → ObjectInputStream.readObject() → parking
         └── No → new Parking()

Opción 6 — Salir
    └── ObjectOutputStream.writeObject(parking) → parking.dat
```

### Fichero generado

`parking.dat` se crea en el directorio de trabajo del proceso (normalmente la raíz del proyecto). No es un fichero de texto: es formato binario propio de la serialización Java.

---

## 8. Lógica de tarifación

Sin cambios respecto a v1. El método privado `calcularImporte()` permanece en `Ticket` con las mismas constantes y el mismo algoritmo.

### Constantes de tarifación en `Ticket` (`private static final`)

| Constante | Valor |
|---|---|
| `PRECIO_MINIMO` | `2.0` € |
| `PRECIO_POR_MINUTO` | `0.015` € |
| `PRECIO_POR_DIA` | `19.0` € |
| `PRECIO_POR_SEMANA` | `80.0` € |
| `MINUTOS_MINIMOS` | `90` |
| `MINUTOS_POR_DIA` | `1440` |
| `DIAS_POR_SEMANA` | `7` |
| `DESCUENTO_ABONADO` | `0.10` (10 %) |

Para la descripción completa del algoritmo y ejemplos numéricos, consultar la documentación técnica de la versión 1.

---

## 9. Gestión de excepciones

| Excepción | Origen | Motivo |
|---|---|---|
| `IllegalArgumentException` | `Parking.registrarEntrada()` | Ya existe ticket abierto para esa matrícula |
| `IllegalArgumentException` | `Parking.registrarSalida()` | No existe ticket abierto para esa matrícula |
| `IllegalArgumentException` | `Ticket.salir()` | `salida` nula |
| `IllegalStateException` | `Ticket.salir()` | Ticket ya estaba cerrado |
| `IllegalArgumentException` | `Vehiculo` constructor | Matrícula, propietario o descripción inválidos |
| `IllegalArgumentException` | `Utilidades.validarMatricula()` | Formato de matrícula incorrecto |
| `IllegalArgumentException` | `Utilidades.validarSalida()` | Fecha de salida anterior a la de entrada |
| `IOException` | `cargarParking()` / `guardarParking()` | Error de lectura/escritura del fichero |
| `ClassNotFoundException` | `cargarParking()` | El fichero no contiene un objeto `Parking` válido |

Los errores de E/S de fichero se tratan con `System.err` y permiten continuar: si la carga falla, se inicia un parking vacío; si el guardado falla, se informa pero el programa termina igualmente.

---

## 10. Decisiones de implementación destacables

### Traslado de estadísticas a `Parking`

En v1, los acumuladores (`totalEntradas`, `totalSalidas`, etc.) eran atributos estáticos de `Ticket`. En v2 pasan a ser atributos de instancia de `Parking`. Esto es más coherente porque las estadísticas pertenecen al parking en su conjunto, no a la clase que describe un ticket individual. Además, al ser atributos de instancia se serializan junto con el resto del estado.

### `Scanner` como parámetro en `Utilidades.leerFechaHora()`

En v1, `leerFechaHora()` no recibía el `Scanner`. En v2 se pasa como parámetro para evitar crear una segunda instancia de `Scanner` sobre `System.in`, lo que puede causar problemas al leer líneas. Esta mejora centraliza el `Scanner` en `AplicacionParking`.

### Listado de abiertos antes de registrar salida

En `registrarSalida()` se llama a `listarAbiertos()` antes de pedir la matrícula. Esto mejora la usabilidad: el operario ve de un vistazo qué vehículos están dentro y puede introducir la matrícula correcta sin tener que memorizar el estado del parking.

### Arrays de longitud exacta vs arrays con capacidad extra

Se eligió trabajar con arrays de **longitud exacta** (cada inserción y eliminación crea un nuevo array del tamaño justo). Esta decisión es menos eficiente que reservar capacidad extra, pero es la que mejor ilustra la mecánica de `System.arraycopy()` que es el objetivo didáctico de la actividad.

### Sintaxis de expresiones en `switch`

El `switch` del menú principal y el de formas de pago usan la sintaxis moderna de Java (`case X -> ...`, `yield` para expresiones). Esto hace el código más conciso y elimina la posibilidad de olvidar un `break`.
