# Documentación Técnica — Sistema de Gestión de Parking

## Índice

1. [Enunciado de la Tarea](#1-enunciado-de-la-tarea)
2. [Descripción de la solución](#2-descripción-de-la-solución)
3. [Estructura del proyecto](#3-estructura-del-proyecto)
4. [Modelo de clases y relaciones](#4-modelo-de-clases-y-relaciones)
5. [Especificación de clases](#5-especificación-de-clases)
   - 5.1 [FormaPago](#51-formapago--parkingmodeloformapago)
   - 5.2 [Vehiculo](#52-vehiculo--parkingmodelovehiculo)
   - 5.3 [Ticket](#53-ticket--parkingmodeloticket)
   - 5.4 [Utilidades](#54-utilidades--parkingutilidades)
   - 5.5 [AplicacionParking](#55-aplicacionparking--parkingaplicacionaplicacionparking)
6. [Lógica de tarifación](#6-lógica-de-tarifación)
7. [Gestión de excepciones](#7-gestión-de-excepciones)
8. [Decisiones de implementación destacables](#8-decisiones-de-implementación-destacables)

---

## 1. Enunciado de la Tarea

> **Módulo**: Programación — 1.º DAM / DAW  
> **Resultado de Aprendizaje**: RA4 — Diseño y uso de clases en Java  
> **Peso sobre la nota del módulo**: 15 %

### Descripción del problema

Se requiere desarrollar una aplicación en Java que permita registrar las entradas y salidas de vehículos en un parking, calcular el importe a pagar según las reglas establecidas y mostrar estadísticas de facturación.

### Clases requeridas

| Clase / Tipo | Paquete | Descripción |
|---|---|---|
| `FormaPago` | `parking.modelo` | Enumerado con los medios de pago |
| `Vehiculo` | `parking.modelo` | Entidad que representa un vehículo |
| `Ticket` | `parking.modelo` | Registro de entrada/salida y cálculo de importe |
| `Utilidades` | `parking.utilidades` | Métodos estáticos de validación y lectura |
| `AplicacionParking` | `parking.aplicacion` | Clase principal con menú interactivo |

### Tarifas a implementar

| Concepto | Valor |
|---|---|
| Precio mínimo (hasta 90 min) | 2,00 € |
| Precio por minuto adicional | 0,015 € |
| Precio por día completo | 19,00 € |
| Precio por semana completa | 80,00 € |
| Descuento para abonados | 10 % |

### Reglas de negocio principales

- Solo puede haber **un ticket activo** simultáneamente.
- Los vehículos **abonados** tienen la forma de pago `MENSUAL` asignada automáticamente y se les aplica un descuento del 10 %.
- La fecha de salida **no puede ser anterior** a la de entrada.
- La matrícula debe tener **exactamente 7 caracteres**: 4 dígitos seguidos de 3 letras mayúsculas.
- No se permite el uso de arrays, listas ni colecciones.
- **No se usarán literales numéricos** en el código de cálculo de tarifas: todos los valores deben ser constantes con nombre.

### Criterios de evaluación

| Criterio | Descripción |
|---|---|
| (a) Sintaxis y estructura | Clases correctamente estructuradas, diferenciando atributos y métodos |
| (b) Definición de clases | Clases definidas con su estructura básica correcta |
| (c) Propiedades y métodos | Implementan correctamente la funcionalidad requerida |
| (d) Constructores | Inicializan atributos y cumplen las especificaciones |
| (e) Instanciación y uso de objetos | Objetos creados, manipulados y probados desde la clase principal |
| (f) Visibilidad | Uso correcto de `public` / `private` en atributos y métodos |
| (g, i) Clases de la API de Java | Uso correcto de `String`, `LocalDateTime`, `Duration`, `DateTimeFormatter`… |
| (h) Métodos estáticos | Definidos y utilizados adecuadamente |

---

## 2. Descripción de la solución

La aplicación gestiona el ciclo completo de un vehículo en el parking:

- **Entrada**: el usuario introduce los datos del vehículo y la fecha/hora de entrada; se crea un `Ticket` asociado.
- **Salida**: se introduce la fecha/hora de salida y, si el vehículo no es abonado, la forma de pago; se calcula el importe y se muestra el ticket completo.
- **Resumen**: se consultan las estadísticas acumuladas de todos los tickets procesados durante la sesión.

Las clases del modelo (`Vehiculo`, `Ticket`) **no imprimen nada por consola**: devuelven `String` que la clase principal decide cuándo y cómo mostrar. Toda la interacción con el usuario se centraliza en `AplicacionParking`.

---

## 3. Estructura del proyecto

```
src/
└── parking/
    ├── aplicacion/
    │   └── AplicacionParking.java   ← Clase principal (método main + menú)
    ├── modelo/
    │   ├── FormaPago.java           ← Enumerado de medios de pago
    │   ├── Vehiculo.java            ← Entidad vehículo
    │   └── Ticket.java              ← Entidad ticket (entrada/salida/tarifas)
    └── utilidades/
        └── Utilidades.java          ← Validación de matrícula y lectura de fechas
```

---

## 4. Modelo de clases y relaciones

```
AplicacionParking
    │
    ├── tiene ──► Ticket  (ticketActivo : Ticket)
    │                │
    │                ├── contiene ──► Vehiculo
    │                └── usa       ──► FormaPago
    │
    └── usa ──► Utilidades  (métodos estáticos)

Vehiculo
    └── usa ──► Utilidades  (validarMatricula en el constructor)
```

### Ciclo de vida de un ticket

```
ticketActivo = null
        │
        │  Opción 1 — Registrar entrada
        ▼
  new Ticket(vehiculo, fechaEntrada)
  ticketActivo = ticket
        │
        │  Opción 2 — Registrar salida
        ▼
  ticketActivo.salir(fechaSalida, formaPago)
        │
        ▼
  ticketActivo = null
```

---

## 5. Especificación de clases

---

### 5.1 `FormaPago` — `parking.modelo.FormaPago`

Enumerado con los tres medios de pago posibles.

| Valor | Asignación |
|---|---|
| `EFECTIVO` | El usuario la selecciona en el menú |
| `TARJETA` | El usuario la selecciona en el menú |
| `MENSUAL` | Asignada automáticamente por el sistema cuando el vehículo es abonado |

> `MENSUAL` nunca la elige el usuario directamente: aunque la opción aparece en el menú, si `vehiculo.esAbonado()` es `true` el sistema sobreescribe siempre la forma de pago con `MENSUAL`.

---

### 5.2 `Vehiculo` — `parking.modelo.Vehiculo`

Representa un vehículo registrado en el sistema.

#### Atributos

| Atributo | Tipo | Visibilidad | Descripción |
|---|---|---|---|
| `matricula` | `String` | `private` | Formato: 4 dígitos + 3 letras mayúsculas |
| `propietario` | `String` | `private` | Nombre completo; no puede ser vacío ni nulo |
| `descripcion` | `String` | `private` | Marca, modelo, color…; no puede ser vacío ni nulo |
| `esAbonado` | `boolean` | `private` | `true` si tiene tarifa mensual con descuento |

#### Constructor

```java
public Vehiculo(String matricula, String propietario, String descripcion, boolean esAbonado)
```

Validaciones en orden de ejecución:

1. `matricula` no nula ni vacía → `IllegalArgumentException`
2. `propietario` no nulo ni vacío → `IllegalArgumentException`
3. `descripcion` no nula ni vacía → `IllegalArgumentException`
4. Formato de matrícula → delega en `Utilidades.validarMatricula(matricula)`

#### Métodos

| Método | Retorno | Visibilidad | Descripción |
|---|---|---|---|
| `getMatricula()` | `String` | `public` | Devuelve la matrícula |
| `getPropietario()` | `String` | `public` | Devuelve el nombre del propietario |
| `getDescripcion()` | `String` | `public` | Devuelve la descripción del vehículo |
| `esAbonado()` | `boolean` | `public` | `true` si el vehículo tiene tarifa de abonado |
| `toString()` | `String` | `public` | Cadena con todos los datos del vehículo |

#### Ejemplo de salida de `toString()`

```
Matricula: 1234ABC, Propietario: Ana Lopez, Descripcion: Seat Arona Azul, Abonado: No
```

---

### 5.3 `Ticket` — `parking.modelo.Ticket`

Clase central del sistema. Gestiona el registro de cada estancia y acumula las estadísticas globales de facturación.

#### Constantes de tarifación (`private static final`)

Todos los valores numéricos del cálculo de precios están definidos como constantes, centralizando las tarifas en un único punto de modificación.

| Constante | Tipo | Valor | Descripción |
|---|---|---|---|
| `PRECIO_MINIMO` | `double` | `2.0` | Precio base para estancias de hasta 90 min |
| `PRECIO_POR_MINUTO` | `double` | `0.015` | Precio por cada minuto que supera el mínimo |
| `PRECIO_POR_DIA` | `double` | `19.0` | Precio por día completo |
| `PRECIO_POR_SEMANA` | `double` | `80.0` | Precio por semana completa |
| `MINUTOS_MINIMOS` | `int` | `90` | Umbral para aplicar el precio mínimo |
| `MINUTOS_POR_DIA` | `long` | `1440` | Minutos en un día (60 × 24) |
| `DIAS_POR_SEMANA` | `int` | `7` | Días en una semana |
| `DESCUENTO_ABONADO` | `double` | `0.10` | Factor de descuento para abonados (10 %) |

#### Atributos estáticos de estadísticas (`private static`)

| Atributo | Tipo | Descripción |
|---|---|---|
| `contadorTickets` | `int` | Contador para asignar números de ticket consecutivos |
| `totalEntradas` | `int` | Número total de tickets creados |
| `totalSalidas` | `int` | Número total de salidas registradas |
| `importeTotalFacturado` | `double` | Suma de todos los importes cobrados |
| `importeEfectivo` | `double` | Acumulado cobrado en efectivo |
| `importeTarjeta` | `double` | Acumulado cobrado con tarjeta |
| `importeMensual` | `double` | Acumulado cobrado a abonados |

#### Atributos de instancia (`private`)

| Atributo | Tipo | Descripción |
|---|---|---|
| `numeroTicket` | `int` | Número único asignado en el constructor |
| `vehiculo` | `Vehiculo` | Vehículo al que pertenece el ticket |
| `fechaEntrada` | `LocalDateTime` | Fecha/hora de entrada |
| `fechaSalida` | `LocalDateTime` | Fecha/hora de salida (`null` hasta registrar salida) |
| `formaPago` | `FormaPago` | Medio de pago (se fija al registrar la salida) |
| `importeTotal` | `double` | Importe calculado al registrar la salida |

#### Constructor

```java
public Ticket(Vehiculo vehiculo, LocalDateTime fechaEntrada)
```

> **Decisión de implementación**: el enunciado proponía usar `LocalDateTime.now()` dentro del constructor. En esta solución la fecha de entrada se recibe como parámetro, lo que permite introducirla manualmente desde el menú y facilita las pruebas con fechas pasadas.

Acciones en el constructor:

- Valida que `vehiculo` y `fechaEntrada` no sean nulos → `IllegalArgumentException`.
- Incrementa `contadorTickets` y asigna `numeroTicket`.
- Incrementa `totalEntradas`.

#### Métodos públicos

| Método | Retorno | Visibilidad | Descripción |
|---|---|---|---|
| `salir(LocalDateTime, FormaPago)` | `void` | `public` | Registra la salida y calcula el importe |
| `getFechaEntrada()` | `LocalDateTime` | `public` | Devuelve la fecha/hora de entrada |
| `getVehiculo()` | `Vehiculo` | `public` | Devuelve el vehículo asociado |
| `imprimirTicketEntrada()` | `String` | `public` | Ticket de entrada formateado |
| `imprimirTicketSalida()` | `String` | `public` | Ticket completo con importe formateado |
| `obtenerResumenFacturacion()` | `String` | `public static` | Resumen global de facturación |

#### Detalle de `salir()`

```java
public void salir(LocalDateTime salida, FormaPago formaPago)
```

Flujo de ejecución:

1. Valida que `salida` no sea nula → `IllegalArgumentException`.
2. Valida que el ticket no esté ya cerrado → `IllegalStateException`.
3. Si `vehiculo.esAbonado()` es `true`, sobreescribe `formaPago` con `MENSUAL`.
4. Calcula el importe bruto llamando a `calcularImporte(fechaEntrada, fechaSalida)`.
5. Si el vehículo es abonado, aplica el descuento: `importe × (1 - DESCUENTO_ABONADO)`.
6. Actualiza los acumuladores estáticos: `totalSalidas`, `importeTotalFacturado` y el acumulado de la forma de pago correspondiente.

#### Método privado `calcularImporte()`

```java
private double calcularImporte(LocalDateTime entrada, LocalDateTime salida)
```

Calcula los minutos totales con `Duration.between(entrada, salida).toMinutes()` y aplica las reglas de tarifación descritas en la [sección 6](#6-lógica-de-tarifación).

#### Ejemplos de salida

**`imprimirTicketEntrada()`**
```
TICKET DE ENTRADA
Numero: 1
Matricula: 1234ABC, Propietario: Ana Lopez, Descripcion: Seat Arona Azul, Abonado: No
Fecha/Hora de entrada: 18/03/2026 09:00
```

**`imprimirTicketSalida()`**
```
TICKET DE SALIDA
Numero: 1
Matricula: 1234ABC, Propietario: Ana Lopez, Descripcion: Seat Arona Azul, Abonado: No
Fecha/Hora de entrada: 18/03/2026 09:00
Fecha/Hora de salida:  18/03/2026 11:30
Forma de pago: EFECTIVO
Importe total: 4.25 EUR
```

**`obtenerResumenFacturacion()`**
```
RESUMEN DE FACTURACION
Total entradas: 3
Total salidas: 2
Importe total facturado: 27.45 EUR
Desglose por forma de pago:
  Efectivo: 4.25 EUR
  Tarjeta: 21.00 EUR
  Mensual: 2.20 EUR
```

---

### 5.4 `Utilidades` — `parking.utilidades.Utilidades`

Clase de métodos estáticos de uso transversal. No se instancia.

#### `validarMatricula(String matricula)`

```java
public static void validarMatricula(String matricula)
```

Comprobaciones en orden, cada una con su propio mensaje de error:

| Comprobación | Condición de fallo | Excepción |
|---|---|---|
| Longitud exacta | `matricula.length() != 7` | `IllegalArgumentException` |
| 4 primeros son dígitos | `!Character.isDigit(c)` en posiciones 0–3 | `IllegalArgumentException` |
| 3 últimos son mayúsculas | `!Character.isUpperCase(c)` en posiciones 4–6 | `IllegalArgumentException` |

#### `leerFechaHora(String mensaje)`

```java
public static LocalDateTime leerFechaHora(String mensaje)
```

- Muestra el `mensaje` al usuario indicando el formato esperado.
- Lee una cadena con formato `dd/MM/yyyy HH:mm` usando `DateTimeFormatter`.
- Repite en bucle hasta que el formato sea correcto (captura `DateTimeParseException`).
- Devuelve el `LocalDateTime` resultante.

#### `validarSalida(LocalDateTime entrada, LocalDateTime salida)`

```java
public static void validarSalida(LocalDateTime entrada, LocalDateTime salida)
```

- Comprueba que `salida` no sea anterior a `entrada`.
- Si lo es, lanza `IllegalArgumentException` con un mensaje descriptivo.

> En `AplicacionParking` esta validación se llama explícitamente antes de invocar `ticket.salir()`, como capa adicional de control en la interfaz de usuario.

---

### 5.5 `AplicacionParking` — `parking.aplicacion.AplicacionParking`

Clase principal. Gestiona el menú y toda la interacción con el usuario.

#### Atributos de clase

| Atributo | Tipo | Descripción |
|---|---|---|
| `ticketActivo` | `static Ticket` | Ticket en curso; `null` si no hay ninguno abierto |
| `scanner` | `static Scanner` | Instancia única para toda la lectura de consola |

#### Vehículos de prueba (definidos con literales en `main`)

```java
Vehiculo v1 = new Vehiculo("1234ABC", "Ana Lopez",   "Seat Arona Azul",   false);
Vehiculo v2 = new Vehiculo("5678XYZ", "Carlos Ruiz", "Ford Focus Blanco", true);
```

`v1` es un cliente normal; `v2` es abonado y pagará siempre con el 10 % de descuento. Estos objetos se crean al arrancar el programa para verificar que el constructor y las validaciones funcionan correctamente.

#### Flujo del menú

```
┌──────────────────────────────────┐
│       --- MENU PARKING ---       │
│  1. Registrar entrada            │
│  2. Registrar salida             │
│  3. Mostrar resumen              │
│  4. Salir                        │
└──────────────────────────────────┘

Opción 1 — Registrar entrada
    ├── [ticketActivo != null] → Aviso: ya hay ticket activo
    └── [ticketActivo == null] → Pedir: matrícula, propietario,
                                         descripción, abonado (s/n),
                                         fecha/hora de entrada
                               → new Vehiculo(...)     [puede lanzar excepción]
                               → new Ticket(vehiculo, entrada)
                               → ticketActivo = ticket
                               → Mostrar imprimirTicketEntrada()

Opción 2 — Registrar salida
    ├── [ticketActivo == null] → Aviso: no hay ticket activo
    └── [ticketActivo != null] → Pedir fecha/hora de salida
                               → Validar con Utilidades.validarSalida()
                               → Si NO es abonado: pedir forma de pago
                               → ticketActivo.salir(salida, formaPago)
                               → Mostrar imprimirTicketSalida()
                               → ticketActivo = null

Opción 3 — Resumen de facturación
    └── Mostrar Ticket.obtenerResumenFacturacion()

Opción 4 — Salir
    └── Fin del bucle do-while
```

#### Método auxiliar `leerEntero()`

```java
private static int leerEntero()
```

Lee una línea y la convierte a `int`. Si el formato es incorrecto devuelve `-1`, lo que activa el `default` del `switch` y muestra "Opción no válida" sin propagar ninguna excepción.

---

## 6. Lógica de tarifación

### Algoritmo de `calcularImporte()`

El método trabaja siempre en minutos totales obtenidos con `Duration.between(entrada, salida).toMinutes()`.

```
minutosTotales ≤ 0
    └─► 0.00 €  (caso defensivo)

minutosTotales ≤ MINUTOS_MINIMOS (90)
    └─► PRECIO_MINIMO = 2.00 €

minutosTotales > 90
    │
    ├── 1. Calcular semanas completas (si supera 7 días):
    │       semanas          = minutosTotales / (DIAS_POR_SEMANA × MINUTOS_POR_DIA)
    │       minutosRestantes = minutosTotales % (DIAS_POR_SEMANA × MINUTOS_POR_DIA)
    │
    ├── 2. Calcular días completos del resto:
    │       dias             = minutosRestantes / MINUTOS_POR_DIA
    │       minutosRestantes = minutosRestantes % MINUTOS_POR_DIA
    │
    └── 3. Calcular importe:
            importe  = semanas × PRECIO_POR_SEMANA
            importe += dias    × PRECIO_POR_DIA
            si (dias == 0 y semanas == 0):
                importe += PRECIO_MINIMO + (minutosTotales - MINUTOS_MINIMOS) × PRECIO_POR_MINUTO
            si no:
                importe += minutosRestantes × PRECIO_POR_MINUTO
```

### Tabla de ejemplos

| Duración | Abonado | Cálculo | Importe |
|---|---|---|---|
| 45 min | No | Precio mínimo | 2,00 € |
| 2 h (120 min) | No | 2,00 + (30 × 0,015) | 2,45 € |
| 2 h (120 min) | Sí | 2,45 × 0,90 | 2,205 € |
| 1 día exacto | No | 1 × 19,00 | 19,00 € |
| 1 día + 30 min | No | 19,00 + (30 × 0,015) | 19,45 € |
| 1 día + 2 h | No | 19,00 + (120 × 0,015) | 20,80 € |
| 2 días exactos | No | 2 × 19,00 | 38,00 € |
| 1 semana exacta | No | 1 × 80,00 | 80,00 € |
| 1 semana + 1 día | No | 80,00 + 19,00 | 99,00 € |

---

## 7. Gestión de excepciones

| Excepción | Origen | Motivo |
|---|---|---|
| `IllegalArgumentException` | `Vehiculo` constructor | Matrícula nula/vacía, propietario o descripción vacíos |
| `IllegalArgumentException` | `Utilidades.validarMatricula()` | Formato de matrícula incorrecto |
| `IllegalArgumentException` | `Ticket` constructor | `vehiculo` o `fechaEntrada` nulos |
| `IllegalArgumentException` | `Ticket.salir()` | `salida` nula |
| `IllegalArgumentException` | `Utilidades.validarSalida()` | Fecha de salida anterior a la de entrada |
| `IllegalStateException` | `Ticket.salir()` | Ticket ya cerrado (salida ya registrada) |

Todas las excepciones se capturan en `AplicacionParking` con bloques `try-catch` que muestran `e.getMessage()` al usuario y permiten continuar con el menú sin interrumpir el programa.

---

## 8. Decisiones de implementación destacables

### Fecha de entrada como parámetro del constructor

El enunciado indicaba usar `LocalDateTime.now()` dentro del constructor de `Ticket`. En esta solución la fecha se recibe como parámetro, lo que permite al usuario introducirla manualmente. Esto hace la clase más flexible y facilita las pruebas con fechas pasadas.

### Validación en dos niveles

Las validaciones ocurren tanto en `AplicacionParking` (para mostrar mensajes amigables antes de crear objetos) como en los propios constructores (para garantizar la integridad del objeto independientemente de quién lo instancie). Este doble nivel es una práctica de diseño defensivo.

### Separación estricta entre modelo y presentación

Las clases `Vehiculo` y `Ticket` no contienen ninguna llamada a `System.out`. Toda la salida por consola se gestiona desde `AplicacionParking`, lo que facilita la sustitución futura de la interfaz de texto por cualquier otra sin tocar el modelo.

### Ausencia de literales numéricos en los cálculos

Todo el algoritmo de `calcularImporte()` referencia únicamente las constantes `private static final` de la clase, cumpliendo el requisito del enunciado y centralizando las tarifas en un único punto de modificación.
