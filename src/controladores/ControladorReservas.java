package controladores;

import enums.EstadoReserva;
import enums.MedioPago;
import enums.TipoPasajero;
import estrategias.PoliticaCancelacion;
import estrategias.PoliticaCancelacionAgencia;
import estrategias.PoliticaCancelacionCliente;
import estrategias.Promocion;
import excepciones.CupoInsuficienteException;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Vendedor;
import persistencia.Repositorio;

import java.time.LocalDate;
import java.util.List;

public class ControladorReservas {

    private Repositorio<Cliente> clientes;
    private Repositorio<Salida> salidas;
    private Repositorio<Reserva> reservas;
    private List<Promocion> promociones;
    private Reserva reservaEnCurso;

    public ControladorReservas(Repositorio<Cliente> clientes, Repositorio<Salida> salidas,
                               Repositorio<Reserva> reservas, List<Promocion> promociones) {
        this.clientes = clientes;
        this.salidas = salidas;
        this.reservas = reservas;
        this.promociones = promociones;
        this.reservaEnCurso = null;
    }

    public void registrarCliente(Cliente c) throws ReglaNegocioException {
        Cliente existente = clientes.buscar(c.getDocumento());
        if (existente != null) {
            throw new ReglaNegocioException("El cliente ya está registrado: " + existente);
        }
        clientes.guardar(c);
    }

    public Reserva iniciarReserva(String docCliente, String codSalida, Vendedor vendedor)
            throws ReglaNegocioException {
        Cliente cliente = clientes.buscar(docCliente);
        if (cliente == null) {
            throw new ReglaNegocioException("No hay un cliente registrado con documento " + docCliente);
        }
        Salida salida = salidas.buscar(codSalida);
        if (salida == null) {
            throw new ReglaNegocioException("No existe la salida " + codSalida);
        }
        if (!salida.admiteReservas()) {
            throw new ReglaNegocioException("La salida " + codSalida + " no admite reservas");
        }
        reservaEnCurso = new Reserva(reservas.listar().size() + 1, cliente, salida, vendedor);
        return reservaEnCurso;
    }

    public void agregarPasajero(String nombre, String apellido, String doc, LocalDate nac, TipoPasajero tipo,
                                String contacto, String restricciones) throws ReglaNegocioException {
        verificarReservaEnCurso();
        Pasajero p = new Pasajero(nombre, apellido, doc, nac, tipo, contacto, restricciones);
        reservaEnCurso.agregarPasajero(p);
    }

    public void contratarServicio(String docPasajero, String servicio) throws ReglaNegocioException {
        verificarReservaEnCurso();
        ServicioAdicional elegido = null;
        for (ServicioAdicional s : reservaEnCurso.getSalida().getTour().getAdicionales()) {
            if (s.getNombre().equalsIgnoreCase(servicio)) {
                elegido = s;
            }
        }
        if (elegido == null) {
            throw new ReglaNegocioException("El tour no ofrece el servicio " + servicio);
        }
        reservaEnCurso.contratarServicio(docPasajero, elegido);
    }

    public Reserva confirmarReserva() throws ReglaNegocioException {
        verificarReservaEnCurso();
        if (reservaEnCurso.cantidadPasajeros() == 0) {
            throw new ReglaNegocioException("La reserva tiene que tener al menos un pasajero");
        }
        Salida salida = reservaEnCurso.getSalida();
        int libres = salida.lugaresDisponibles();
        if (libres < reservaEnCurso.cantidadPasajeros()) {
            reservaEnCurso = null;
            throw new CupoInsuficienteException(libres);
        }
        reservaEnCurso.calcularTotal(promociones);
        salida.agregarReserva(reservaEnCurso);
        reservas.guardar(reservaEnCurso);
        salidas.guardar(salida);
        Reserva confirmada = reservaEnCurso;
        reservaEnCurso = null;
        return confirmada;
    }

    public Pago registrarPago(int nro, double importe, MedioPago medio, int cuotas) throws ReglaNegocioException {
        Reserva reserva = buscarReserva(nro);
        Pago pago = reserva.registrarPago(importe, medio, cuotas, LocalDate.now());
        guardar(reserva);
        return pago;
    }

    public void reemplazarPasajero(int nro, String docAnterior, Pasajero nuevo) throws ReglaNegocioException {
        Reserva reserva = buscarReserva(nro);
        reserva.reemplazarPasajero(docAnterior, nuevo, LocalDate.now());
        guardar(reserva);
    }

    public Cancelacion cancelarReserva(int nro, String motivo, boolean porAgencia) throws ReglaNegocioException {
        Reserva reserva = reservas.buscar(String.valueOf(nro));
        if (reserva == null || reserva.getEstado() == EstadoReserva.ANULADA
                || reserva.getEstado() == EstadoReserva.FINALIZADA) {
            throw new ReglaNegocioException("La reserva " + nro + " no existe, está anulada o finalizada");
        }
        PoliticaCancelacion politica;
        if (porAgencia) {
            politica = new PoliticaCancelacionAgencia();
        } else {
            politica = new PoliticaCancelacionCliente();
        }
        Cancelacion c = reserva.cancelar(motivo, LocalDate.now(), politica);
        guardar(reserva);
        return c;
    }

    public Reserva buscarReserva(int nro) throws ReglaNegocioException {
        Reserva reserva = reservas.buscar(String.valueOf(nro));
        if (reserva == null) {
            throw new ReglaNegocioException("No existe la reserva " + nro);
        }
        return reserva;
    }

    private void verificarReservaEnCurso() throws ReglaNegocioException {
        if (reservaEnCurso == null) {
            throw new ReglaNegocioException("No hay una reserva en curso");
        }
    }

    private void guardar(Reserva reserva) {
        reservas.guardar(reserva);
        salidas.guardar(reserva.getSalida());
    }
}
