package modelo;

import enums.EstadoReserva;
import enums.EstadoSalida;
import excepciones.CupoInsuficienteException;
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

    public boolean admiteReservas() {
        return estado == EstadoSalida.PROGRAMADA || estado == EstadoSalida.CONFIRMADA;
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

    public int lugaresOcupados() {
        int ocupados = 0;
        for (Reserva r : reservas) {
            if (reservaActiva(r)) {
                ocupados += r.cantidadPasajeros();
            }
        }
        return ocupados;
    }

    public int lugaresDisponibles() {
        return Math.max(0, cupoMaximo - lugaresOcupados());
    }

    public double getPrecioBase() throws ReglaNegocioException {
        return tour.getTarifaVigente(fechaInicio).getPrecioBase();
    }

    public void agregarReserva(Reserva r) throws ReglaNegocioException {
        if (!admiteReservas()) {
            throw new ReglaNegocioException("La salida " + codigo + " no admite reservas porque está "
                    + estado.getDescripcion().toLowerCase());
        }
        if (r.cantidadPasajeros() > lugaresDisponibles()) {
            throw new CupoInsuficienteException(lugaresDisponibles());
        }
        reservas.add(r);
        actualizarEstado();
    }

    public void actualizarEstado() {
        if (!sinIniciar()) {
            return;
        }
        if (lugaresOcupados() >= cupoMaximo) {
            estado = EstadoSalida.COMPLETA;
            return;
        }
        int confirmados = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() == EstadoReserva.CONFIRMADA) {
                confirmados += r.cantidadPasajeros();
            }
        }
        if (confirmados >= cupoMinimo) {
            estado = EstadoSalida.CONFIRMADA;
        } else {
            estado = EstadoSalida.PROGRAMADA;
        }
    }

    public int diasHastaInicio(LocalDate hoy) {
        return (int) ChronoUnit.DAYS.between(hoy, fechaInicio);
    }

    public boolean seSuperponeCon(Salida otra) {
        return !fechaInicio.isAfter(otra.fechaFin) && !fechaFin.isBefore(otra.fechaInicio);
    }

    public int diasDeDuracion() {
        return (int) ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }

    private boolean sinIniciar() {
        return estado == EstadoSalida.PROGRAMADA || estado == EstadoSalida.CONFIRMADA
                || estado == EstadoSalida.COMPLETA;
    }

    private boolean reservaActiva(Reserva r) {
        return r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA;
    }
}
