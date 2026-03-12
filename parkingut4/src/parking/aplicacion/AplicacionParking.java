/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.aplicacion;

import parking.modelo.*;
import parking.utilidades.Utilidades;

import java.time.LocalDateTime;
import java.util.Scanner;

public class AplicacionParking {

    private static Ticket ticketActivo = null;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        // Crear vehiculos de prueba
        Vehiculo v1 = new Vehiculo("1234ABC", "Ana Lopez", "Seat Arona Azul", false);
        Vehiculo v2 = new Vehiculo("5678XYZ", "Carlos Ruiz", "Ford Focus Blanco", true);

        int opcion;
        do {
            System.out.println("\n--- MENU PARKING ---");
            System.out.println("1. Registrar entrada de vehiculo");
            System.out.println("2. Registrar salida de vehiculo");
            System.out.println("3. Mostrar resumen de facturacion");
            System.out.println("4. Salir");
            System.out.print("Elige una opcion: ");
            opcion = leerEntero();

            switch (opcion) {
                case 1:
                    registrarEntrada();
                    break;
                case 2:
                    registrarSalida();
                    break;
                case 3:
                    mostrarResumen();
                    break;
                case 4:
                    System.out.println("Gracias por usar el sistema de parking.");
                    break;
                default:
                    System.out.println("Opcion no valida.");
            }
        } while (opcion != 4);
    }

    private static void registrarEntrada() {
        if (ticketActivo != null) {
            System.out.println("Ya existe un ticket activo. No se puede crear otro hasta cerrar el actual.");
            return;
        }

        System.out.println("Introduce los datos del vehiculo:");
        System.out.print("Matricula (7 caracteres, 4 digitos + 3 letras mayusculas): ");
        String matricula = scanner.nextLine().trim();
        System.out.print("Propietario: ");
        String propietario = scanner.nextLine().trim();
        System.out.print("Descripcion (marca, modelo, color): ");
        String descripcion = scanner.nextLine().trim();
        System.out.print("Es abonado? (s/n): ");
        boolean abonado = scanner.nextLine().trim().equalsIgnoreCase("s");

        LocalDateTime entrada = Utilidades.leerFechaHora("Fecha/hora de entrada");

        try {
            Vehiculo vehiculo = new Vehiculo(matricula, propietario, descripcion, abonado);
            ticketActivo = new Ticket(vehiculo, entrada); // <-- Ahora se pasa la fecha de entrada
            System.out.println("\n" + ticketActivo.imprimirTicketEntrada());
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void registrarSalida() {
        if (ticketActivo == null) {
            System.out.println("No hay ningun ticket activo. Primero registra una entrada.");
            return;
        }

        LocalDateTime salida = Utilidades.leerFechaHora("Fecha/hora de salida");

        // Validar usando getters públicos
        LocalDateTime entrada = ticketActivo.getFechaEntrada();
        try {
            Utilidades.validarSalida(entrada, salida);
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return;
        }

        FormaPago formaPago = FormaPago.EFECTIVO;
        if (!ticketActivo.getVehiculo().esAbonado()) {
            System.out.println("Forma de pago:");
            System.out.println("1. Efectivo");
            System.out.println("2. Tarjeta");
            System.out.println("3. Mensual");
            int pago = leerEntero();
            switch (pago) {
                case 1:
                    formaPago = FormaPago.EFECTIVO;
                    break;
                case 2:
                    formaPago = FormaPago.TARJETA;
                    break;
                case 3:
                    formaPago = FormaPago.MENSUAL;
                    break;
                default:
                    System.out.println("Opcion invalida. Se usara Efectivo.");
                    formaPago = FormaPago.EFECTIVO;
            }
        }

        try {
            ticketActivo.salir(salida, formaPago);
            System.out.println("\n" + ticketActivo.imprimirTicketSalida());
            ticketActivo = null;
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void mostrarResumen() {
        System.out.println("\n" + Ticket.obtenerResumenFacturacion());
    }

    private static int leerEntero() {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
