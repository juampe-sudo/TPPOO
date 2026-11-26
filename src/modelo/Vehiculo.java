package modelo;

import enums.TipoVehiculo;

public class Vehiculo {

    private String patente;
    private int capacidad;
    private TipoVehiculo tipo;

    public Vehiculo(String patente, int capacidad, TipoVehiculo tipo) {
        this.patente = patente;
        this.capacidad = capacidad;
        this.tipo = tipo;
    }

    public String getPatente() {
        return patente;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public TipoVehiculo getTipo() {
        return tipo;
    }
}
