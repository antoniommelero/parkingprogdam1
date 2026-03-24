/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.modelo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static parking.modelo.FormaPago.EFECTIVO;
import static parking.modelo.FormaPago.MENSUAL;
import static parking.modelo.FormaPago.TARJETA;

/**
 *
 * @author Antonio
 */
public class Parking implements Serializable {
    private static final long serialVersionUID = 2L; // Versión 1 inicial

    // Arrays con tamaño EXACTO Cada inserción supone crear uno nuevo 
    private Map<String, Ticket> ticketsAbiertos;
    private List<Ticket> ticketsCerrados;
    
    // Estadísticas globales (antes estaban en Ticket)
    private int totalEntradas;
    private int totalSalidas;
    private double importeTotalFacturado;
    private double importeEfectivo;
    private double importeTarjeta;
    private double importeMensual;
    
    public Parking() {
        ticketsAbiertos = new HashMap();  // empiezan vacios los arrays
        ticketsCerrados = new ArrayList();   
        totalEntradas = 0;
        totalSalidas = 0;
        importeTotalFacturado = 0.0;
        importeEfectivo = 0.0;
        importeTarjeta = 0.0;
        importeMensual = 0.0;
    }
    
    //  inserta un nuevo elemento en el array
    public Ticket registrarEntrada(Vehiculo vehiculo, LocalDateTime fechaEntrada) {
        // antes comprueba que no tengamos ya una entrada pendiente de ese vehiculo
        if (ticketsAbiertos.containsKey(vehiculo.getMatricula())) {
            throw new IllegalArgumentException("Ya existe un ticket abierto para la matricula " + vehiculo.getMatricula());
        }
        
        Ticket ticket = new Ticket(vehiculo, fechaEntrada);
        
        // nuevo array con un elemento más que el original
        
        ticketsAbiertos.put(vehiculo.getMatricula(), ticket);
        
//        Ticket[] nuevoArray = new Ticket[ticketsAbiertos.length + 1];
//        System.arraycopy(ticketsAbiertos, 0, nuevoArray, 0, ticketsAbiertos.length);
//        nuevoArray[ticketsAbiertos.length] = ticket; // nuevo elemento al final
//        ticketsAbiertos = nuevoArray;
        totalEntradas++;
        return ticket;
    }
    
    //  Registrar salida: busca ticket, lo cierra y lo cambia de lista
    public Ticket registrarSalida(String matricula, LocalDateTime salida, FormaPago formaPago) {
        // localiza si hay un ticket abierto para esa matricula
        Ticket ticket = ticketsAbiertos.get(matricula);
//        int indice = buscarIndiceAbiertoPorMatricula(matricula);
//        if (indice == -1) { // no puede salir si no hay entrada de este coche
//            throw new IllegalArgumentException("No existe un ticket abierto para la matricula " + matricula);
//        }    
//        Ticket ticket = ticketsAbiertos[indice]; // recupero el ticket de entrada
        ticket.salir(salida, formaPago);  // cambia estado del ticket a 'cerrado'     
        // Eliminar del array de abiertos (crear nuevo array con tamaño -1)
        //eliminarTicketAbierto(indice); 
        ticketsAbiertos.remove(matricula);
        // Añadir al array de cerrados (crear nuevo array con tamaño +1)
        ticketsCerrados.add(ticket);
        //agregarACerrados(ticket);  
        
        // Actualizar estadísticas
        totalSalidas++;
        double importe = ticket.getImporteTotal();
        importeTotalFacturado += importe;
        switch (ticket.getFormaPago()) {
            case EFECTIVO -> importeEfectivo += importe;
            case TARJETA -> importeTarjeta += importe;
            case MENSUAL -> importeMensual += importe;
        }
        return ticket;
    }
    
    //  Devuelve el ticket abierto para esa matricula
    public Ticket obtenerTicketAbierto(String matricula) {
//        int indice = buscarIndiceAbiertoPorMatricula(matricula);
//        return (indice != -1) ? ticketsAbiertos[indice] : null;
        return ticketsAbiertos.get(matricula);
    }
    
    // Indica si hay un ticket abierto para esa matrícula
//    public boolean existeTicketAbierto(String matricula) {
//        return ticketsAbiertos.containsKey(matricula);
//    }
    
    //  Resumen de facturación con estadísticas acumuladas
    public String obtenerResumenFacturacion() {
        return "RESUMEN DE FACTURACION\n"
            + "Total entradas: " + totalEntradas + "\n"
            + "Total salidas: " + totalSalidas + "\n"
            + "Importe total facturado: " + String.format("%.2f", importeTotalFacturado) + "\n"
            + "Desglose por forma de pago:\n"
            + "  Efectivo: " + String.format("%.2f", importeEfectivo) + "\n"
            + "  Tarjeta: " + String.format("%.2f", importeTarjeta) + "\n"
            + "  Mensual: " + String.format("%.2f", importeMensual) + "\n";
    }
    
    //  Listar tickets abiertos 
    public String listarTicketsAbiertos() {
        if (ticketsAbiertos.isEmpty()) {
            return "No hay tickets abiertos.";
        }      
        StringBuilder sb = new StringBuilder("TICKETS ABIERTOS:\n");
        for (Ticket ticket : ticketsAbiertos.values()) {
            sb.append("num: ").append(ticket.getNumeroTicket())
              .append(" - Matricula: ").append(ticket.getVehiculo().getMatricula())
              .append(" - Entrada: ")
              .append(ticket.getFechaEntrada().format(Ticket.FORMATO_FECHA_HORA)).append("\n");
        }
        return sb.toString();
    }
    
    //  Listar tickets cerrados 
    public String listarTicketsCerrados() {
        if (ticketsCerrados.isEmpty()) {
            return "No hay tickets cerrados.";
        }        
        StringBuilder sb = new StringBuilder("TICKETS CERRADOS:\n");
        for (Ticket ticket : ticketsCerrados) {
            sb.append("  #").append(ticket.getNumeroTicket())
              .append(" - Matricula: ").append(ticket.getVehiculo().getMatricula())
              .append(" - Entrada: ").append(ticket.getFechaEntrada().format(Ticket.FORMATO_FECHA_HORA))
              .append(" - Salida: ").append(ticket.getFechaSalida().format(Ticket.FORMATO_FECHA_HORA))
              .append(" - Importe: ").append(String.format("%10.2f", ticket.getImporteTotal()))
              .append("\n");
        }
        return sb.toString();
    }
  
    // OPCIÓN 6 – Estadísticas usando Collections.max, min y frequency
    public String obtenerEstadisticas() {
        if (ticketsCerrados.isEmpty()) {
            return "No hay tickets cerrados para mostrar estadísticas.";
        }
 
        Ticket masCaro   = Collections.max(ticketsCerrados);
        Ticket masBarato = Collections.min(ticketsCerrados);
 
        // Lista auxiliar con solo los valores de FormaPago para usar frequency
        List<FormaPago> formasPago = new ArrayList<>();
        for (Ticket t : ticketsCerrados) {
            formasPago.add(t.getFormaPago());
        }
        int numEfectivo = Collections.frequency(formasPago, FormaPago.EFECTIVO);
        int numTarjeta  = Collections.frequency(formasPago, FormaPago.TARJETA);
        int numMensual  = Collections.frequency(formasPago, FormaPago.MENSUAL);
 
        return "ESTADISTICAS DE TICKETS CERRADOS\n"
             + "Ticket mas caro:   #" + masCaro.getNumeroTicket()
             + " - " + masCaro.getVehiculo().getMatricula()
             + " - " + String.format("%10.2f", masCaro.getImporteTotal()) + " EUR\n"
             + "Ticket mas barato: #" + masBarato.getNumeroTicket()
             + " - " + masBarato.getVehiculo().getMatricula()
             + " - " + String.format("%10.2f", masBarato.getImporteTotal()) + " EUR\n"
             + "Pagos con Efectivo: " + numEfectivo + "\n"
             + "Pagos con Tarjeta:  " + numTarjeta  + "\n"
             + "Pagos con Mensual:  " + numMensual  + "\n";
    }
 
    // OPCIÓN 7 – Buscar tickets cerrados por matrícula usando Stream
    public String buscarTicketsCerradosPorMatricula(String matricula) {
        List<Ticket> resultado = ticketsCerrados.stream()
            .filter(t -> t.getVehiculo().getMatricula().equalsIgnoreCase(matricula))
            //.sorted((t1, t2) -> t1.getFechaEntrada().compareTo(t2.getFechaEntrada()))
            .sorted(Comparator.comparing(Ticket::getFechaEntrada))
            .collect(Collectors.toList());
 
        if (resultado.isEmpty()) {
            return "No se encontraron tickets cerrados para la matricula " + matricula;
        }
 
        StringBuilder sb = new StringBuilder("TICKETS CERRADOS PARA " + matricula + ":\n");
        for (Ticket t : resultado) {
            sb.append(" N.").append(t.getNumeroTicket())
              .append(" - Entrada: ").append(t.getFechaEntrada().format(Ticket.FORMATO_FECHA_HORA))
              .append(" - Salida: ").append(t.getFechaSalida().format(Ticket.FORMATO_FECHA_HORA))
              .append(" - Importe: ").append(String.format("%10.2f", t.getImporteTotal())).append(" EUR\n");
        }
        return sb.toString();
    }
 
    // OPCIÓN 8 – Listar tickets cerrados ordenados por importe (mayor a menor)
    public String listarTicketsCerradosOrdenadosPorImporte() {
        if (ticketsCerrados.isEmpty()) {
            return "No hay tickets cerrados.";
        }
 
        // Copia para no alterar el orden original de inserción en la List original
        List<Ticket> copia = new ArrayList<>(ticketsCerrados);
        Collections.sort(copia, Collections.reverseOrder()); // usa Comparable<Ticket>
 
        StringBuilder sb = new StringBuilder("TICKETS CERRADOS POR IMPORTE (mayor a menor):\n");
        sb.append(String.format("%-6s %-10s %-10s %-10s%n", "Num", "Matricula", "Importe", "Forma pago"));
        sb.append("-".repeat(42)).append("\n");
        for (Ticket t : copia) {
            sb.append(String.format("%6d %-10s %10s %-10s%n",
                t.getNumeroTicket(),
                t.getVehiculo().getMatricula(),
                String.format("%10.2f EUR", t.getImporteTotal()),
                t.getFormaPago()));
        }
        return sb.toString();
    }
 
    // OPCIÓN 9 – Anular ticket abierto usando Iterator
    public boolean anularTicketAbierto(String matricula) {
        Iterator<Map.Entry<String, Ticket>> it = ticketsAbiertos.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Ticket> entrada = it.next();
            if (entrada.getKey().equalsIgnoreCase(matricula)) {
                it.remove(); // eliminación segura con Iterator
                return true;
            }
        }
        return false;
    }
 
    // OPCIÓN 10 – Array de matrículas de tickets cerrados (para vehículos recurrentes)
    public String[] obtenerArrayMatriculas() {
        String[] matriculas = new String[ticketsCerrados.size()];
        for (int i = 0; i < ticketsCerrados.size(); i++) {
            matriculas[i] = ticketsCerrados.get(i).getVehiculo().getMatricula();
        }
        return matriculas;
    }
 
    // OPCIÓN 11 – Array de importes de tickets cerrados (para ranking)
    public double[] obtenerArrayImportes() {
        double[] importes = new double[ticketsCerrados.size()];
        for (int i = 0; i < ticketsCerrados.size(); i++) {
            importes[i] = ticketsCerrados.get(i).getImporteTotal();
        }
        return importes;
    }
    // métodos privados para gestion interna
       
    //  Eliminar ticket de abiertos SIN dejar huecos (nuevo array con tamaño -1)
//    private void eliminarTicketAbierto(int indice) {
//        if (ticketsAbiertos.length == 1) {
//            // Caso especial: solo queda un ticket
//            ticketsAbiertos = new Ticket[0];
//        } else {
//            // tendrá un elemento menos
//            Ticket[] nuevoArray = new Ticket[ticketsAbiertos.length - 1];
//            // Copiar elementos antes del índice
//            if (indice > 0) {
//                System.arraycopy(ticketsAbiertos, 0, nuevoArray, 0, indice);
//            }
//            // Copiar elementos después del índice
//            if (indice < ticketsAbiertos.length - 1) {
//                System.arraycopy(ticketsAbiertos, indice + 1, nuevoArray, indice, ticketsAbiertos.length - indice - 1);
//            }
//            ticketsAbiertos = nuevoArray;
//        }
//    }
    
    //  Añadir ticket a cerrados (nuevo array con un elemento más)
//    private void agregarACerrados(Ticket ticket) {
//        Ticket[] nuevoArray = new Ticket[ticketsCerrados.length + 1];
//        System.arraycopy(ticketsCerrados, 0, nuevoArray, 0, ticketsCerrados.length);
//        nuevoArray[ticketsCerrados.length] = ticket;
//        ticketsCerrados = nuevoArray;
//    }
    
    //  Búsqueda POR MATRÍCULA en tickets abiertos
//    private int buscarIndiceAbiertoPorMatricula(String matricula) {
//        for (int i = 0; i < ticketsAbiertos.length; i++) {
//            if (ticketsAbiertos[i].getVehiculo().getMatricula().equals(matricula)) {
//                return i;
//            }
//        }
//        return -1;
//    }
}
