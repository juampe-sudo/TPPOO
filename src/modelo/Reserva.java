package modelo;

import enums.EstadoReserva;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva {

    private int numero;
    private LocalDate fecha;
    private EstadoReserva estado;
    private double total;
    private Cliente cliente;
    private Salida salida;
    private Vendedor vendedor;
    private List<Pasajero> pasajeros;
    private List<Pago> pagos;
    private List<NotaAjuste> notas;
    private Cancelacion cancelacion;

    public Reserva(int numero, Cliente cliente, Salida salida, Vendedor vendedor) {
        this.numero = numero;
        this.fecha = LocalDate.now();
        this.estado = EstadoReserva.PENDIENTE;
        this.total = 0;
        this.cliente = cliente;
        this.salida = salida;
        this.vendedor = vendedor;
        this.pasajeros = new ArrayList<>();
        this.pagos = new ArrayList<>();
        this.notas = new ArrayList<>();
        this.cancelacion = null;
    }

    public int getNumero() {
        return numero;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public double getTotal() {
        return total;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Salida getSalida() {
        return salida;
    }

    public Vendedor getVendedor() {
        return vendedor;
    }

    public List<Pasajero> getPasajeros() {
        return Collections.unmodifiableList(pasajeros);
    }

    public List<Pago> getPagos() {
        return Collections.unmodifiableList(pagos);
    }

    public List<NotaAjuste> getNotas() {
        return Collections.unmodifiableList(notas);
    }

    public Cancelacion getCancelacion() {
        return cancelacion;
    }
}
