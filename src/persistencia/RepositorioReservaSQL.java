package persistencia;

import enums.EstadoReserva;
import enums.MedioPago;
import enums.TipoPasajero;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.Empleado;
import modelo.NotaAjuste;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Vendedor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RepositorioReservaSQL extends RepositorioSQL<Reserva> {

    private Repositorio<Cliente> clientes;
    private Repositorio<Salida> salidas;
    private Repositorio<Empleado> empleados;

    public RepositorioReservaSQL(Repositorio<Cliente> clientes, Repositorio<Salida> salidas,
                                 Repositorio<Empleado> empleados) {
        super(r -> String.valueOf(r.getNumero()));
        this.clientes = clientes;
        this.salidas = salidas;
        this.empleados = empleados;
        cargar();
    }

    @Override
    protected List<Reserva> leer() throws SQLException, ReglaNegocioException {
        List<Reserva> reservas = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT * FROM reserva ORDER BY numero");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Reserva r = new Reserva(rs.getInt("numero"), clientes.buscar(rs.getString("cliente")),
                        salidas.buscar(rs.getString("salida")), (Vendedor) empleados.buscar(rs.getString("vendedor")));
                r.restaurar(rs.getObject("fecha", LocalDate.class), EstadoReserva.valueOf(rs.getString("estado")),
                        rs.getDouble("total"));
                reservas.add(r);
            }
        }
        for (Reserva r : reservas) {
            leerPasajeros(r);
            leerPagos(r);
            leerNotas(r);
            leerCancelacion(r);
            r.getSalida().restaurarReserva(r);
        }
        return reservas;
    }

    @Override
    protected void escribir(Reserva r) throws SQLException {
        int numero = r.getNumero();
        ejecutar("INSERT INTO reserva (numero, fecha, estado, total, cliente, salida, vendedor) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?) AS nuevo ON DUPLICATE KEY UPDATE estado = nuevo.estado, "
                        + "total = nuevo.total",
                numero, r.getFecha(), r.getEstado().name(), r.getTotal(), r.getCliente().getDocumento(),
                r.getSalida().getCodigo(), r.getVendedor().getLegajo());
        ejecutar("DELETE FROM pasajero_servicio WHERE reserva = ?", numero);
        ejecutar("DELETE FROM pasajero WHERE reserva = ?", numero);
        ejecutar("DELETE FROM pago WHERE reserva = ?", numero);
        ejecutar("DELETE FROM nota_ajuste WHERE reserva = ?", numero);
        ejecutar("DELETE FROM cancelacion WHERE reserva = ?", numero);

        List<Pasajero> pasajeros = r.getPasajeros();
        for (int i = 0; i < pasajeros.size(); i++) {
            Pasajero p = pasajeros.get(i);
            ejecutar("INSERT INTO pasajero (reserva, documento, nro, nombre, apellido, fecha_nacimiento, tipo, "
                            + "contacto_emergencia, restricciones) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    numero, p.getDocumento(), i + 1, p.getNombre(), p.getApellido(), p.getFechaNacimiento(),
                    p.getTipo().name(), p.getContactoEmergencia(), p.getRestricciones());
            for (ServicioAdicional s : p.getServicios()) {
                ejecutar("INSERT INTO pasajero_servicio (reserva, documento, servicio) VALUES (?, ?, ?)",
                        numero, p.getDocumento(), s.getNombre());
            }
        }
        List<Pago> pagos = r.getPagos();
        for (int i = 0; i < pagos.size(); i++) {
            Pago p = pagos.get(i);
            ejecutar("INSERT INTO pago (reserva, nro, fecha, importe, medio, cuotas) VALUES (?, ?, ?, ?, ?, ?)",
                    numero, i + 1, p.getFecha(), p.getImporte(), p.getMedio().name(), p.getCuotas());
        }
        List<NotaAjuste> notas = r.getNotas();
        for (int i = 0; i < notas.size(); i++) {
            NotaAjuste n = notas.get(i);
            ejecutar("INSERT INTO nota_ajuste (reserva, nro, fecha, descripcion, importe) VALUES (?, ?, ?, ?, ?)",
                    numero, i + 1, n.getFecha(), n.getDescripcion(), n.getImporte());
        }
        Cancelacion c = r.getCancelacion();
        if (c != null) {
            String medio = c.getMedio() == null ? null : c.getMedio().name();
            ejecutar("INSERT INTO cancelacion (reserva, fecha, motivo, importe_devuelto, medio) VALUES (?, ?, ?, ?, ?)",
                    numero, c.getFecha(), c.getMotivo(), c.getImporteDevuelto(), medio);
        }
    }

    private void leerPasajeros(Reserva r) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = preparar("SELECT * FROM pasajero WHERE reserva = ? ORDER BY nro", r.getNumero());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Pasajero p = new Pasajero(rs.getString("nombre"), rs.getString("apellido"), rs.getString("documento"),
                        rs.getObject("fecha_nacimiento", LocalDate.class), TipoPasajero.valueOf(rs.getString("tipo")),
                        rs.getString("contacto_emergencia"), rs.getString("restricciones"));
                leerServicios(r, p);
                r.agregarPasajero(p);
            }
        }
    }

    private void leerServicios(Reserva r, Pasajero p) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = preparar("SELECT servicio FROM pasajero_servicio WHERE reserva = ? AND documento = ? "
                + "ORDER BY servicio", r.getNumero(), p.getDocumento());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                p.agregarServicio(servicioDelTour(r, rs.getString("servicio")));
            }
        }
    }

    private ServicioAdicional servicioDelTour(Reserva r, String nombre) throws ReglaNegocioException {
        for (ServicioAdicional s : r.getSalida().getTour().getAdicionales()) {
            if (s.getNombre().equals(nombre)) {
                return s;
            }
        }
        throw new ReglaNegocioException("El tour de la reserva " + r.getNumero() + " no tiene el servicio " + nombre);
    }

    private void leerPagos(Reserva r) throws SQLException {
        try (PreparedStatement ps = preparar("SELECT * FROM pago WHERE reserva = ? ORDER BY nro", r.getNumero());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                r.restaurarPago(rs.getObject("fecha", LocalDate.class), rs.getDouble("importe"),
                        MedioPago.valueOf(rs.getString("medio")), rs.getInt("cuotas"));
            }
        }
    }

    private void leerNotas(Reserva r) throws SQLException {
        try (PreparedStatement ps = preparar("SELECT * FROM nota_ajuste WHERE reserva = ? ORDER BY nro", r.getNumero());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                r.restaurarNota(rs.getObject("fecha", LocalDate.class), rs.getString("descripcion"),
                        rs.getDouble("importe"));
            }
        }
    }

    private void leerCancelacion(Reserva r) throws SQLException {
        try (PreparedStatement ps = preparar("SELECT * FROM cancelacion WHERE reserva = ?", r.getNumero());
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String medio = rs.getString("medio");
                r.restaurarCancelacion(rs.getObject("fecha", LocalDate.class), rs.getString("motivo"),
                        rs.getDouble("importe_devuelto"), medio == null ? null : MedioPago.valueOf(medio));
            }
        }
    }
}
