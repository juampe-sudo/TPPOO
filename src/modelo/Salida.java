package modelo;

import enums.EstadoSalida;

import java.time.LocalDate;

public class Salida {

    private String codigo;
    private Tour tour;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int cupoMinimo;
    private int cupoMaximo;
    private EstadoSalida estado;
    private Guia guia;
    private Vehiculo vehiculo;
    private boolean listaEmitida;

    public Salida(String codigo, Tour tour, LocalDate fechaInicio, LocalDate fechaFin,
                  int cupoMinimo, int cupoMaximo) {
        this.codigo = codigo;
        this.tour = tour;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
        this.estado = EstadoSalida.PROGRAMADA;
        this.guia = null;
        this.vehiculo = null;
        this.listaEmitida = false;
    }

    public String getCodigo() {
        return codigo;
    }

    public Tour getTour() {
        return tour;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public EstadoSalida getEstado() {
        return estado;
    }

    public Guia getGuia() {
        return guia;
    }

    public Vehiculo getVehiculo() {
        return vehiculo;
    }

    public boolean isListaEmitida() {
        return listaEmitida;
    }
}
