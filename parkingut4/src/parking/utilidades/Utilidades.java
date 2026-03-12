/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.utilidades;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class Utilidades {

    private static Scanner scanner = new Scanner(System.in);

    public static void validarMatricula(String matricula) {
        if (matricula == null || matricula.length() != 7) {
            throw new IllegalArgumentException("La matricula debe tener exactamente 7 caracteres.");
        }
        String digitos = matricula.substring(0, 4);
        String letras = matricula.substring(4, 7);

        for (int i = 0; i < 4; i++) {
            if (!Character.isDigit(digitos.charAt(i))) {
                throw new IllegalArgumentException("Los primeros 4 caracteres deben ser digitos.");
            }
        }
        for (int i = 0; i < 3; i++) {
            char c = letras.charAt(i);
            if (c < 'A' || c > 'Z') {
                throw new IllegalArgumentException("Los ultimos 3 caracteres deben ser letras mayusculas.");
            }
        }
    }

    public static LocalDateTime leerFechaHora(String mensaje) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        while (true) {
            System.out.print(mensaje + " (dd/MM/yyyy HH:mm): ");
            String entrada = scanner.nextLine().trim();
            try {
                return LocalDateTime.parse(entrada, formatter);
            } catch (DateTimeParseException e) {
                System.out.println("Formato incorrecto. Intentalo de nuevo.");
            }
        }
    }

    public static void validarSalida(LocalDateTime entrada, LocalDateTime salida) {
        if (salida.isBefore(entrada)) {
            throw new IllegalArgumentException("La fecha/hora de salida no puede ser anterior a la de entrada.");
        }
    }
}