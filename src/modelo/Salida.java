package modelo;

import enums.EstadoSalida;
import excepciones.ReglaNegocioException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<Reserva> reservas;
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
        this.reservas = new ArrayList<>();
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

    public List<Reserva> getReservas() {
        return Collections.unmodifiableList(reservas);
    }

    public boolean isListaEmitida() {
        return listaEmitida;
    }

    public boolean admitePasajero(Pasajero p) throws ReglaNegocioException {
        if (!tour.esApto(p, fechaInicio)) {
            return false;
        }
        for (ServicioAdicional s : tour.getServiciosObligatorios()) {
            if (!p.getServicios().contains(s)) {
                p.agregarServicio(s);
            }
        }
        return true;
    }

    public double getPrecioBase() throws ReglaNegocioException {
        return tour.getTarifaVigente(fechaInicio).getPrecioBase();
    }

    public int diasHastaInicio(LocalDate hoy) {
        return (int) ChronoUnit.DAYS.between(hoy, fechaInicio);
    }

    public void actualizarEstado() {
    }
}
