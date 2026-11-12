package modelo;

import enums.TipoPasajero;

import java.time.LocalDate;
import java.time.Period;

public class Pasajero extends Persona {

    private LocalDate fechaNacimiento;
    private TipoPasajero tipo;
    private String contactoEmergencia;
    private String restricciones;

    public Pasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento,
                    TipoPasajero tipo, String contactoEmergencia, String restricciones) {
        super(nombre, apellido, documento);
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo;
        this.contactoEmergencia = contactoEmergencia;
        this.restricciones = restricciones;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoPasajero getTipo() {
        return tipo;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public int edadAl(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }
}
