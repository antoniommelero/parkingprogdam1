/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package parking.modelo;

import parking.utilidades.Utilidades;

public class Vehiculo {

    private String matricula;
    private String propietario;
    private String descripcion;
    private boolean esAbonado;

    public Vehiculo(String matricula, String propietario, String descripcion, boolean esAbonado) {
        if (matricula == null || matricula.trim().isEmpty()) {
            throw new IllegalArgumentException("La matricula no puede estar vacia.");
        }
        if (propietario == null || propietario.trim().isEmpty()) {
            throw new IllegalArgumentException("El propietario no puede estar vacio.");
        }
        if (descripcion == null || descripcion.trim().isEmpty()) {
            throw new IllegalArgumentException("La descripcion no puede estar vacia.");
        }
        Utilidades.validarMatricula(matricula);
        this.matricula = matricula;
        this.propietario = propietario;
        this.descripcion = descripcion;
        this.esAbonado = esAbonado;
    }

    public String getMatricula() {
        return matricula;
    }

    public String getPropietario() {
        return propietario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esAbonado() {
        return esAbonado;
    }

    @Override
    public String toString() {
        return "Matricula: " + matricula + ", Propietario: " + propietario +
               ", Descripcion: " + descripcion + ", Abonado: " + (esAbonado ? "Si" : "No");
    }
}