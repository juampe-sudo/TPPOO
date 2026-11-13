package modelo;

import enums.EstadoReserva;
import enums.EstadoSalida;
import excepciones.CupoInsuficienteException;
import excepciones.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Salida implements Serializable {

    private static final long serialVersionUID = 1L;

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
            // las finalizadas tambien cuentan, si no una salida cerrada queda vacia
            if (r.getEstado() != EstadoReserva.ANULADA) {
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

    public void asignarGuia(Guia g) throws ReglaNegocioException {
        if (!sinIniciar()) {
            throw new ReglaNegocioException("No se puede asignar guía a una salida "
                    + estado.getDescripcion().toLowerCase());
        }
        if (g == null) {
            throw new ReglaNegocioException("Hay que indicar el guía de la salida");
        }
        if (!g.estaHabilitado(tour.getCategoria())) {
            throw new ReglaNegocioException("El guía " + g.getNombreCompleto() + " no está habilitado para tours de "
                    + tour.getCategoria().getDescripcion().toLowerCase());
        }
        if (!g.estaDisponible(this)) {
            throw new ReglaNegocioException("El guía " + g.getNombreCompleto() + " tiene otra salida en esas fechas");
        }
        guia = g;
        g.agregarSalida(this);
    }

    public void asignarVehiculo(Vehiculo v) throws ReglaNegocioException {
        if (!sinIniciar()) {
            throw new ReglaNegocioException("No se puede asignar vehículo a una salida "
                    + estado.getDescripcion().toLowerCase());
        }
        if (v == null) {
            throw new ReglaNegocioException("Hay que indicar el vehículo de la salida");
        }
        if (v.getCapacidad() < cupoMaximo) {
            throw new ReglaNegocioException("El vehículo " + v.getPatente() + " tiene capacidad para "
                    + v.getCapacidad() + " y la salida tiene un cupo de " + cupoMaximo);
        }
        vehiculo = v;
    }

    public List<Pasajero> emitirListaPasajeros() throws ReglaNegocioException {
        if (guia == null) {
            throw new ReglaNegocioException("La salida " + codigo + " no tiene guía asignado");
        }
        List<Pasajero> lista = new ArrayList<>();
        for (Reserva r : reservas) {
            if (reservaActiva(r)) {
                lista.addAll(r.getPasajeros());
            }
        }
        if (lista.isEmpty()) {
            throw new ReglaNegocioException("La salida " + codigo + " no tiene pasajeros");
        }
        Comparator<Pasajero> porApellido = Comparator.comparing(Pasajero::getApellido)
                .thenComparing(Pasajero::getNombre);
        Collections.sort(lista, porApellido);
        listaEmitida = true;
        return lista;
    }

    public void iniciar() throws ReglaNegocioException {
        if (estado != EstadoSalida.CONFIRMADA && estado != EstadoSalida.COMPLETA) {
            throw new ReglaNegocioException("La salida " + codigo + " no se puede iniciar porque está "
                    + estado.getDescripcion().toLowerCase());
        }
        if (guia == null) {
            throw new ReglaNegocioException("La salida " + codigo + " no tiene guía asignado");
        }
        if (vehiculo == null) {
            throw new ReglaNegocioException("La salida " + codigo + " no tiene vehículo asignado");
        }
        if (!listaEmitida) {
            throw new ReglaNegocioException("La salida " + codigo + " no tiene la lista de pasajeros emitida");
        }
        estado = EstadoSalida.EN_CURSO;
    }

    public void finalizar() throws ReglaNegocioException {
        if (estado != EstadoSalida.EN_CURSO) {
            throw new ReglaNegocioException("La salida " + codigo + " no está en curso");
        }
        estado = EstadoSalida.FINALIZADA;
        for (Reserva r : reservas) {
            if (reservaActiva(r)) {
                r.finalizar();
            }
        }
    }

    public void cancelar() throws ReglaNegocioException {
        if (!sinIniciar()) {
            throw new ReglaNegocioException("La salida " + codigo + " no se puede cancelar porque está "
                    + estado.getDescripcion().toLowerCase());
        }
        estado = EstadoSalida.CANCELADA;
    }

    public boolean seSuperponeCon(Salida otra) {
        return !fechaInicio.isAfter(otra.fechaFin) && !fechaFin.isBefore(otra.fechaInicio);
    }

    public double totalAdicionalesVendidos() {
        double total = 0;
        for (Reserva r : reservas) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                for (Pasajero p : r.getPasajeros()) {
                    total += p.totalServicios();
                }
            }
        }
        return total;
    }

    public int diasDeDuracion() {
        return (int) ChronoUnit.DAYS.between(fechaInicio, fechaFin) + 1;
    }

    public void revincular(Tour tour, Guia guia, Vehiculo vehiculo, List<Reserva> reservas) {
        this.tour = tour;
        this.guia = guia;
        this.vehiculo = vehiculo;
        this.reservas = new ArrayList<>(reservas);
    }

    private boolean sinIniciar() {
        return estado == EstadoSalida.PROGRAMADA || estado == EstadoSalida.CONFIRMADA
                || estado == EstadoSalida.COMPLETA;
    }

    private boolean reservaActiva(Reserva r) {
        return r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA;
    }
}
