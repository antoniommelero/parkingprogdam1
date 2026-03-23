/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package parking.aplicacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Scanner;
import parking.modelo.FormaPago;
import parking.modelo.Parking;
import parking.modelo.Ticket;
import parking.modelo.Vehiculo;
import parking.utilidades.Utilidades;

/**
 *
 * @author Antonio
 */
public class AplicacionParking {
    /**
     * @param args the command line arguments
     */
    private static Parking parking = new Parking(); // un solo objeto para todos los datos
    private static Scanner teclado = new Scanner(System.in);
    private static final String ARCHIVO_DATOS = "parking.dat"; // Nombre del archivo de guardado

    public static void main(String[] args) {
        cargarParking(); // Carga automática al iniciar

        int opcion;
        do {
            System.out.println("\n--- MENU PARKING ---");
            System.out.println(" 1. Registrar entrada de vehiculo");
            System.out.println(" 2. Registrar salida de vehiculo");
            System.out.println(" 3. Mostrar resumen de facturacion");
            System.out.println(" 4. Listar tickets abiertos");
            System.out.println(" 5. Listar tickets cerrados");
            System.out.println(" 6. Estadisticas de tickets cerrados");
            System.out.println(" 7. Buscar tickets cerrados por matricula");
            System.out.println(" 8. Listar tickets cerrados por importe");
            System.out.println(" 9. Anular ticket abierto");
            System.out.println("10. Vehiculos recurrentes");
            System.out.println("11. Ranking de los N tickets mas caros");
            System.out.println("12. Salir");
            System.out.print("Elige una opcion: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1 ->
                    registrarEntrada();
                case 2 ->
                    registrarSalida();
                case 3 ->
                    mostrarResumen();
                case 4 ->
                    listarAbiertos();
                case 5 ->
                    listarCerrados();
                case 6  -> mostrarEstadisticas();
                case 7  -> buscarPorMatricula();
                case 8  -> listarPorImporte();
                case 9  -> anularTicket();
                case 10 -> vehiculosRecurrentes();
                case 11 -> rankingTicketsCaros();
                case 12 -> {
                    guardarParking();
                    System.out.println("Gracias por usar el sistema de parking.");
                }
                default -> System.out.println("Opcion no valida.");
            }
        } while (opcion != 12);

        teclado.close();
    }
    
    // Método para cargar el estado guardado al iniciar
    private static void cargarParking() {
        File archivo = new File(ARCHIVO_DATOS);
        if (archivo.exists()) {
            try (ObjectInputStream ficheroDatos = new ObjectInputStream(new FileInputStream(archivo))) {
                parking = (Parking) ficheroDatos.readObject();
                System.out.println("Estado anterior del parking cargado correctamente.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error al cargar el archivo. Iniciando parking nuevo.");
                parking = new Parking();
            }
        } else {
            parking = new Parking();
            System.out.println("ℹ No existe archivo de guardado. Iniciando parking nuevo.");
        }
    }

    // Método para guardar el estado actual al salir
    private static void guardarParking() {
        try (ObjectOutputStream ficheroDatos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_DATOS))) {
            ficheroDatos.writeObject(parking);
            System.out.println("Estado del parking guardado correctamente.");
        } catch (IOException e) {
            System.err.println("Error al guardar el estado: " + e.getMessage());
        }
    }

    private static void registrarEntrada() {
        System.out.println("Introduce los datos del vehiculo:");
        System.out.print("Matricula (7 caracteres, 4 digitos + 3 letras mayusculas): ");
        String matricula = teclado.nextLine().trim();

        try {
            Utilidades.validarMatricula(matricula);
        } catch (IllegalArgumentException e) {
            System.out.println("Error en matricula: " + e.getMessage());
            return;
        }

        System.out.print("Propietario: ");
        String propietario = teclado.nextLine().trim();
        if (propietario.isEmpty()) {
            System.out.println("El propietario no puede estar vacio.");
            return;
        }

        System.out.print("Descripcion (marca, modelo, color): ");
        String descripcion = teclado.nextLine().trim();
        if (descripcion.isEmpty()) {
            System.out.println("La descripcion no puede estar vacia.");
            return;
        }

        System.out.print("Es abonado? (s/n): ");
        boolean abonado = teclado.nextLine().trim().equalsIgnoreCase("s");

        LocalDateTime entrada = Utilidades.leerFechaHora("Fecha/hora de entrada", teclado);

        try {
            Vehiculo vehiculo = new Vehiculo(matricula, propietario, descripcion, abonado);
            Ticket ticket = parking.registrarEntrada(vehiculo, entrada);
            System.out.println("\n" + ticket.imprimirTicketEntrada());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registrarSalida() {
        listarAbiertos(); // para ayudar a saber los coches que hay dentro 
        System.out.print("Matricula del vehiculo que sale: ");
        String matricula = teclado.nextLine().trim();

        try {
            Utilidades.validarMatricula(matricula);
        } catch (IllegalArgumentException e) {
            System.out.println("Error en matricula: " + e.getMessage());
            return;
        }

        Ticket ticket = parking.obtenerTicketAbierto(matricula);
        if (ticket == null) {
            System.out.println("No existe un ticket abierto para la matricula " + matricula);
            return;
        }

        LocalDateTime salida = Utilidades.leerFechaHora("Fecha/hora de salida", teclado);

        try {
            Utilidades.validarSalida(ticket.getFechaEntrada(), salida);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        FormaPago formaPago = FormaPago.EFECTIVO;
        if (!ticket.getVehiculo().esAbonado()) {
            System.out.println("Forma de pago:");
            System.out.println("1. Efectivo");
            System.out.println("2. Tarjeta");
            System.out.println("3. Mensual");
            int pago = leerEntero();
            formaPago = switch (pago) {
                case 1 ->
                    FormaPago.EFECTIVO;
                case 2 ->
                    FormaPago.TARJETA;
                case 3 ->
                    FormaPago.MENSUAL;
                default -> {
                    System.out.println("Opcion invalida. Se usara Efectivo.");
                    yield FormaPago.EFECTIVO;
                }
            };
        } else {
            formaPago = FormaPago.TARJETA;
        }

        try {
            Ticket ticketCerrado = parking.registrarSalida(matricula, salida, formaPago);
            System.out.println("\n" + ticketCerrado.imprimirTicketSalida());
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void mostrarResumen() {
        System.out.println("\n" + parking.obtenerResumenFacturacion());
    }

    private static void listarAbiertos() {
        System.out.println("\n" + parking.listarTicketsAbiertos());
    }

    private static void listarCerrados() {
        System.out.println("\n" + parking.listarTicketsCerrados());
    }

    // Opción 6 – Estadísticas
    private static void mostrarEstadisticas() {
        System.out.println("\n" + parking.obtenerEstadisticas());
    }
 
    // Opción 7 – Buscar por matrícula
    private static void buscarPorMatricula() {
        System.out.print("Introduce la matricula a buscar: ");
        String matricula = teclado.nextLine().trim();
        try {
            Utilidades.validarMatricula(matricula);
        } catch (IllegalArgumentException e) {
            System.out.println("Error en matricula: " + e.getMessage());
            return;
        }
        System.out.println("\n" + parking.buscarTicketsCerradosPorMatricula(matricula));
    }
 
    // Opción 8 – Listar por importe
    private static void listarPorImporte() {
        System.out.println("\n" + parking.listarTicketsCerradosOrdenadosPorImporte());
    }
 
    // Opción 9 – Anular ticket abierto
    private static void anularTicket() {
        listarAbiertos();
        System.out.print("Introduce la matricula del ticket a anular: ");
        String matricula = teclado.nextLine().trim();
        try {
            Utilidades.validarMatricula(matricula);
        } catch (IllegalArgumentException e) {
            System.out.println("Error en matricula: " + e.getMessage());
            return;
        }
        boolean anulado = parking.anularTicketAbierto(matricula);
        if (anulado) {
            System.out.println("Ticket de la matricula " + matricula + " anulado correctamente.");
        } else {
            System.out.println("No existe un ticket abierto para la matricula " + matricula);
        }
    }
 
    // Opción 10 – Vehículos recurrentes
    private static void vehiculosRecurrentes() {
        String[] matriculas = parking.obtenerArrayMatriculas();
 
        if (matriculas.length == 0) {
            System.out.println("No hay tickets cerrados para analizar.");
            return;
        }
 
        Arrays.sort(matriculas); // agrupa repetidas en posiciones consecutivas
        System.out.println("\nArray de matriculas ordenado: " + Arrays.toString(matriculas));
 
        // Filtra las que aparecen más de una vez usando Arrays.stream y contador interno
        String[] recurrentes = Arrays.stream(matriculas)
            .filter(m -> Arrays.stream(matriculas).filter(m::equals).count() > 1)
            .distinct()
            .toArray(String[]::new);
 
        if (recurrentes.length == 0) {
            System.out.println("No se han detectado vehiculos recurrentes.");
        } else {
            System.out.println("Vehiculos recurrentes: " + Arrays.toString(recurrentes));
        }
    }
 
    // Opción 11 – Ranking de los N tickets más caros
    private static void rankingTicketsCaros() {
        double[] importes = parking.obtenerArrayImportes();
 
        if (importes.length == 0) {
            System.out.println("No hay tickets cerrados para generar el ranking.");
            return;
        }
 
        System.out.print("¿Cuantos tickets quieres ver en el ranking? ");
        int n = leerEntero();
        if (n <= 0) {
            System.out.println("El numero debe ser mayor que 0.");
            return;
        }
 
        double[] copia = Arrays.copyOf(importes, importes.length); // nunca modificar el original
        Arrays.sort(copia); // orden ascendente
 
        int disponibles = copia.length;
        if (n > disponibles) {
            System.out.println("Solo hay " + disponibles + " tickets cerrados. Se muestran todos.");
            n = disponibles;
        }
 
        // Los N mayores están al final del array ordenado ascendente
        double[] ranking = Arrays.copyOfRange(copia, disponibles - n, disponibles);
 
        // Invertir para mostrar de mayor a menor
        for (int i = 0; i < ranking.length / 2; i++) {
            double tmp = ranking[i];
            ranking[i] = ranking[ranking.length - 1 - i];
            ranking[ranking.length - 1 - i] = tmp;
        }
 
        System.out.println("\nRanking de los " + n + " tickets mas caros (EUR):");
        System.out.println(Arrays.toString(ranking));
    }
    // Utilidades
    private static int leerEntero() {
        try {
            return Integer.parseInt(teclado.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
