/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.modelo;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {

    // Formato pars marca temporal
    public final static DateTimeFormatter FORMATO_FECHA_HORA
                = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    // Datos fijos para tarificacion
    private static final double PRECIO_MINIMO = 2.0;
    private static final double PRECIO_POR_MINUTO = 0.015;
    private static final double PRECIO_POR_DIA = 19.0;
    private static final double PRECIO_POR_SEMANA = 80.0;
    private static final int MINUTOS_MINIMOS = 90;
    private static final long MINUTOS_POR_DIA = 1440;
    private static final int DIAS_POR_SEMANA = 7;
    private static final double DESCUENTO_ABONADO = 0.10;

    // Estadisticas globales
    private static int totalEntradas = 0;
    private static int totalSalidas = 0;
    private static double importeTotalFacturado = 0.0;
    private static double importeEfectivo = 0.0;
    private static double importeTarjeta = 0.0;
    private static double importeMensual = 0.0;

    // Atributos de instancia
    private static int contadorTickets = 0;
    private int numeroTicket;
    private Vehiculo vehiculo;
    private LocalDateTime fechaEntrada;
    private LocalDateTime fechaSalida;
    private FormaPago formaPago;
    private double importeTotal;

    public Ticket(Vehiculo vehiculo, LocalDateTime fechaEntrada) {
        if (vehiculo == null) {
            throw new IllegalArgumentException("El vehiculo no puede ser nulo.");
        }
        if (fechaEntrada == null) {
            throw new IllegalArgumentException("La fecha de entrada no puede ser nula.");
        }
        contadorTickets++;
        this.numeroTicket = contadorTickets;
        this.vehiculo = vehiculo;
        this.fechaEntrada = fechaEntrada;
        totalEntradas++;
    }

    public void salir(LocalDateTime salida, FormaPago formaPago) {
        if (salida == null) {
            throw new IllegalArgumentException("La fecha de salida no puede ser nula.");
        }
        if (this.fechaSalida != null) {
            throw new IllegalStateException("Este ticket ya ha sido cerrado.");
        }

        this.formaPago = formaPago;
        if (vehiculo.esAbonado()) {
            this.formaPago = FormaPago.MENSUAL;
        }

        this.fechaSalida = salida;
        this.importeTotal = calcularImporte(fechaEntrada, fechaSalida);

        if (vehiculo.esAbonado()) {
            this.importeTotal = this.importeTotal * (1 - DESCUENTO_ABONADO);
        }

        totalSalidas++;
        importeTotalFacturado += this.importeTotal;

        switch (this.formaPago) {
            case EFECTIVO:
                importeEfectivo += this.importeTotal;
                break;
            case TARJETA:
                importeTarjeta += this.importeTotal;
                break;
            case MENSUAL:
                importeMensual += this.importeTotal;
                break;
        }
    }

    public LocalDateTime getFechaEntrada() {
        return fechaEntrada;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    private double calcularImporte(LocalDateTime entrada, LocalDateTime salida) {
        long minutosTotales = java.time.Duration.between(entrada, salida).toMinutes();
        if (minutosTotales <= 0) {
            return 0.0;
        }

        if (minutosTotales <= MINUTOS_MINIMOS) {
            return PRECIO_MINIMO;
        }

        long semanas = 0;
        long diasRestantes = 0;
        long minutosRestantes = minutosTotales;

        if (minutosTotales > DIAS_POR_SEMANA * MINUTOS_POR_DIA) {
            semanas = minutosTotales / (DIAS_POR_SEMANA * MINUTOS_POR_DIA);
            minutosRestantes = minutosTotales % (DIAS_POR_SEMANA * MINUTOS_POR_DIA);
        }
        long dias = 0;
        if (minutosRestantes >= MINUTOS_POR_DIA) {
            dias = minutosRestantes / MINUTOS_POR_DIA;
            minutosRestantes = minutosRestantes % MINUTOS_POR_DIA;
        }

        double importe = semanas * PRECIO_POR_SEMANA;
        importe += dias * PRECIO_POR_DIA;
        if (minutosRestantes > 0) {
            if (semanas == 0 && dias == 0 && minutosTotales <= MINUTOS_MINIMOS) {
                importe += PRECIO_MINIMO;
            } else {
                if (dias == 0 && semanas == 0) {
                    // Menos de un dia, pero mas de 90 minutos
                    importe += PRECIO_MINIMO + (minutosTotales - MINUTOS_MINIMOS) * PRECIO_POR_MINUTO;
                } else {
                    // Minutos adicionales tras dias o semanas
                    importe += minutosRestantes * PRECIO_POR_MINUTO;
                }
            }
        }
        return importe;
    }

    public String imprimirTicketEntrada() {   
        return "TICKET DE ENTRADA\n"
                + "Numero: " + numeroTicket + "\n"
                + vehiculo.toString() + "\n"
                + "Fecha/Hora de entrada: " + fechaEntrada.format(FORMATO_FECHA_HORA);
    }

    public String imprimirTicketSalida() {    
        return "TICKET DE SALIDA\n"
                + "Numero: " + numeroTicket + "\n"
                + vehiculo.toString() + "\n"
                + "Fecha/Hora de entrada: " + fechaEntrada.format(FORMATO_FECHA_HORA) + "\n"
                + "Fecha/Hora de salida: " + fechaSalida.format(FORMATO_FECHA_HORA) + "\n"
                + "Forma de pago: " + formaPago + "\n"
                + "Importe total: " + String.format("%.2f", importeTotal) + " EUR";
    }

    public static String obtenerResumenFacturacion() {
        return "RESUMEN DE FACTURACION\n"
                + "Total entradas: " + totalEntradas + "\n"
                + "Total salidas: " + totalSalidas + "\n"
                + "Importe total facturado: " + String.format("%.2f", importeTotalFacturado) + " EUR\n"
                + "Desglose por forma de pago:\n"
                + "  Efectivo: " + String.format("%.2f", importeEfectivo) + " EUR\n"
                + "  Tarjeta: " + String.format("%.2f", importeTarjeta) + " EUR\n"
                + "  Mensual: " + String.format("%.2f", importeMensual) + " EUR";
    }
}
