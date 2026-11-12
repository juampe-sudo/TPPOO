package modelo;

import enums.EstadoReserva;
import enums.MedioPago;
import estrategias.PoliticaCancelacion;
import estrategias.Promocion;
import excepciones.ReglaNegocioException;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

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

    public void agregarPasajero(Pasajero p) throws ReglaNegocioException {
        verificarDocumentoLibre(p.getDocumento());
        if (!salida.admitePasajero(p)) {
            throw new ReglaNegocioException("El pasajero " + p.getNombreCompleto() + " no cumple la edad mínima del tour");
        }
        pasajeros.add(p);
    }

    public int cantidadPasajeros() {
        return pasajeros.size();
    }

    public double subtotalTour() throws ReglaNegocioException {
        double precioBase = salida.getPrecioBase();
        double subtotal = 0;
        for (Pasajero p : pasajeros) {
            subtotal += p.calcularImporte(precioBase, salida.getFechaInicio());
        }
        return subtotal;
    }

    public double calcularTotal(List<Promocion> promociones) throws ReglaNegocioException {
        double subtotal = subtotalTour();
        double adicionales = 0;
        for (Pasajero p : pasajeros) {
            adicionales += p.totalServicios();
        }
        double mayorDescuento = 0;
        for (Promocion promo : promociones) {
            double descuento = promo.calcularDescuento(this);
            if (descuento > mayorDescuento) {
                mayorDescuento = descuento;
            }
        }
        total = subtotal - mayorDescuento + adicionales;
        return total;
    }

    public double montoPagado() {
        double pagado = 0;
        for (Pago p : pagos) {
            pagado += p.getImporte();
        }
        return pagado;
    }

    public double saldo() {
        return Math.max(0.0, total - montoPagado());
    }

    public Pago registrarPago(double importe, MedioPago medio, int cuotas, LocalDate fecha)
            throws ReglaNegocioException {
        verificarActiva();
        if (importe <= 0 || importe > saldo()) {
            throw new ReglaNegocioException("El importe tiene que ser mayor a 0 y no superar el saldo de USD "
                    + String.format("%.2f", saldo()));
        }
        if (cuotas < 1) {
            throw new ReglaNegocioException("El pago tiene que tener al menos una cuota");
        }
        Pago pago = new Pago(fecha, importe, medio, cuotas);
        pagos.add(pago);
        if (estado == EstadoReserva.PENDIENTE && montoPagado() >= total * 0.30) {
            estado = EstadoReserva.CONFIRMADA;
            salida.actualizarEstado();
        }
        return pago;
    }

    public Cancelacion cancelar(String motivo, LocalDate hoy, PoliticaCancelacion politica)
            throws ReglaNegocioException {
        verificarActiva();
        int dias = salida.diasHastaInicio(hoy);
        double porcentaje = politica.porcentajeDevolucion(dias);
        double importe = montoPagado() * porcentaje;
        MedioPago medioSena = pagos.isEmpty() ? null : pagos.get(0).getMedio();
        estado = EstadoReserva.ANULADA;
        cancelacion = new Cancelacion(hoy, motivo, importe, medioSena);
        salida.actualizarEstado();
        return cancelacion;
    }

    public void agregarNota(String descripcion, double importe) {
        notas.add(new NotaAjuste(LocalDate.now(), descripcion, importe));
    }

    public void contratarServicio(String doc, ServicioAdicional s) throws ReglaNegocioException {
        verificarActiva();
        Pasajero p = buscarPasajero(doc);
        p.agregarServicio(s);
        total += s.getPrecio();
        if (!pagos.isEmpty()) {
            agregarNota("Adicional " + s.getNombre() + " para " + p.getNombreCompleto(), s.getPrecio());
        }
    }

    public void reemplazarPasajero(String docAnterior, Pasajero nuevo, LocalDate hoy) throws ReglaNegocioException {
        verificarActiva();
        if (salida.diasHastaInicio(hoy) < 3) {
            throw new ReglaNegocioException("Los pasajeros se pueden reemplazar hasta 3 días antes de la salida");
        }
        Pasajero anterior = buscarPasajero(docAnterior);
        verificarDocumentoLibre(nuevo.getDocumento());
        if (!salida.admitePasajero(nuevo)) {
            throw new ReglaNegocioException("El pasajero " + nuevo.getNombreCompleto() + " no cumple la edad mínima del tour");
        }
        pasajeros.set(pasajeros.indexOf(anterior), nuevo);
        agregarNota("Reemplazo de " + anterior.getNombreCompleto() + " por " + nuevo.getNombreCompleto(), 0.0);
    }

    public void revincular(Cliente cliente, Salida salida, Vendedor vendedor) {
        this.cliente = cliente;
        this.salida = salida;
        this.vendedor = vendedor;
    }

    void finalizar() {
        estado = EstadoReserva.FINALIZADA;
    }

    private void verificarActiva() throws ReglaNegocioException {
        if (estado == EstadoReserva.ANULADA || estado == EstadoReserva.FINALIZADA) {
            throw new ReglaNegocioException("La reserva " + numero + " está " + estado.getDescripcion().toLowerCase());
        }
    }

    private void verificarDocumentoLibre(String doc) throws ReglaNegocioException {
        for (Pasajero p : pasajeros) {
            if (p.getDocumento().equals(doc)) {
                throw new ReglaNegocioException("El pasajero con documento " + doc + " ya está en la reserva");
            }
        }
    }

    private Pasajero buscarPasajero(String doc) throws ReglaNegocioException {
        for (Pasajero p : pasajeros) {
            if (p.getDocumento().equals(doc)) {
                return p;
            }
        }
        throw new ReglaNegocioException("La reserva " + numero + " no tiene un pasajero con documento " + doc);
    }
}
