package persistencia;

import enums.CategoriaTour;
import enums.EstadoReserva;
import enums.EstadoSalida;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoActividad;
import enums.TipoPasajero;
import enums.TipoVehiculo;
import estrategias.PoliticaCancelacionCliente;
import excepciones.ReglaNegocioException;
import modelo.Cliente;
import modelo.Coordinador;
import modelo.Empleado;
import modelo.Guia;
import modelo.Pasajero;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vehiculo;
import modelo.Vendedor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistenciaTest {

    private final LocalDate hoy = LocalDate.now();

    private Repositorio<Proveedor> proveedores;
    private Repositorio<Tour> tours;
    private Repositorio<Cliente> clientes;
    private Repositorio<Empleado> empleados;
    private Repositorio<Vehiculo> vehiculos;
    private Repositorio<Salida> salidas;
    private Repositorio<Reserva> reservas;

    @BeforeEach
    void usarBaseDePrueba() throws SQLException {
        System.setProperty("agencia.bd", "jdbc:mysql://localhost:3306/agencia_prueba?createDatabaseIfNotExist=true");
        try (Statement st = ConexionBD.getInstancia().getConexion().createStatement()) {
            st.execute("DROP DATABASE agencia_prueba");
        }
        ConexionBD.cerrar();
    }

    @AfterEach
    void cerrarBase() {
        ConexionBD.cerrar();
        System.clearProperty("agencia.bd");
    }

    private void abrirRepositorios() {
        proveedores = new RepositorioProveedorSQL();
        tours = new RepositorioTourSQL(proveedores);
        clientes = new RepositorioClienteSQL();
        empleados = new RepositorioEmpleadoSQL();
        vehiculos = new RepositorioVehiculoSQL();
        salidas = new RepositorioSalidaSQL(tours, empleados, vehiculos);
        reservas = new RepositorioReservaSQL(clientes, salidas, empleados);
    }

    private void reabrir() {
        ConexionBD.cerrar();
        abrirRepositorios();
    }

    private Guia guia() {
        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(CategoriaTour.CULTURAL);
        return new Guia("Aiyana", "Nanouk", "40112233", 202, "907-555-0133", idiomas, categorias, 250.0);
    }

    private Pasajero pasajero(String nombre, String doc) {
        return new Pasajero(nombre, "Gómez", doc, LocalDate.of(1988, 6, 10), TipoPasajero.ADULTO,
                "Juan Gómez 1155550000", "");
    }

    @Test
    void guardarYRecargarDesdeLaBase() {
        RepositorioClienteSQL guardados = new RepositorioClienteSQL();
        guardados.guardar(new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina"));
        guardados.guardar(new Cliente("John", "Miller", "47111222", "907-555-0100", "john@mail.com", "Estados Unidos"));

        RepositorioClienteSQL recargado = new RepositorioClienteSQL();

        assertEquals(2, recargado.listar().size());
        assertEquals("Laura Gómez", recargado.buscar("28999888").getNombreCompleto());
        assertEquals("Estados Unidos", recargado.buscar("47111222").getPaisResidencia());
        assertNotSame(guardados.buscar("28999888"), recargado.buscar("28999888"));
        assertNull(recargado.buscar("11111111"));
    }

    @Test
    void recargaConReferenciasCompartidas() throws ReglaNegocioException {
        abrirRepositorios();
        Proveedor proveedor = new Proveedor("Alaska Native Heritage Center", "92-0101010", "907-555-0110", "Entradas");
        Tour tour = new TourCultural("CUL01", "Herencia nativa de Alaska", "Centro de herencia nativa", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        tour.agregarActividad(1, 1, LocalTime.of(9, 30), "Visita al centro", "Anchorage", TipoActividad.VISITA,
                proveedor);
        tour.agregarServicio("Almuerzo típico", 45.0, false);
        tour.agregarServicio("Foto grupal", 20.0, false);
        Salida salida = tour.programarSalida("S001", hoy.plusDays(40), 15);
        Guia guia = guia();
        Vehiculo combi = new Vehiculo("JTR915", 15, TipoVehiculo.COMBI);
        salida.asignarGuia(guia);
        salida.asignarVehiculo(combi);
        Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina");
        Vendedor vendedor = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
        Reserva reserva = new Reserva(1, cliente, salida, vendedor);
        reserva.agregarPasajero(pasajero("Laura", "28999888"));
        reserva.calcularTotal(new ArrayList<>());
        reserva.contratarServicio("28999888", tour.getAdicionales().get(0));
        salida.agregarReserva(reserva);
        reserva.registrarPago(400.0, MedioPago.TRANSFERENCIA, 1, hoy);
        reserva.contratarServicio("28999888", tour.getAdicionales().get(1));

        proveedores.guardar(proveedor);
        tours.guardar(tour);
        clientes.guardar(cliente);
        empleados.guardar(guia);
        empleados.guardar(vendedor);
        vehiculos.guardar(combi);
        salidas.guardar(salida);
        reservas.guardar(reserva);
        reabrir();

        Salida s = salidas.buscar("S001");
        Reserva r = reservas.buscar("1");
        Tour t = tours.buscar("CUL01");
        assertNotSame(salida, s);
        assertSame(s, r.getSalida());
        assertSame(r, s.getReservas().get(0));
        assertSame(t, s.getTour());
        assertSame(proveedores.buscar("92-0101010"), t.getActividades().get(0).getProveedor());
        assertSame(clientes.buscar("28999888"), r.getCliente());
        assertSame(empleados.buscar("101"), r.getVendedor());
        assertSame(empleados.buscar("202"), s.getGuia());
        assertSame(s, s.getGuia().getSalidas().get(0));
        assertSame(vehiculos.buscar("JTR915"), s.getVehiculo());
        assertSame(t.getAdicionales().get(0), r.getPasajeros().get(0).getServicios().get(0));

        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());
        assertEquals(1065.0, r.getTotal(), 0.001);
        assertEquals(665.0, r.saldo(), 0.001);
        assertEquals(MedioPago.TRANSFERENCIA, r.getPagos().get(0).getMedio());
        assertEquals(1, r.getNotas().size());
        assertEquals(2, r.getPasajeros().get(0).getServicios().size());
        assertEquals(1, s.lugaresOcupados());
        assertTrue(((Guia) empleados.buscar("202")).estaHabilitado(CategoriaTour.CULTURAL));
    }

    @Test
    void recuperaCancelacionesYLiquidaciones() throws ReglaNegocioException {
        abrirRepositorios();
        Tour tour = new TourCultural("CUL01", "Herencia nativa de Alaska", "Centro de herencia nativa", 3, 1, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        Salida salida = tour.programarSalida("S001", hoy.plusDays(40), 15);
        Guia guia = guia();
        Vehiculo combi = new Vehiculo("JTR915", 15, TipoVehiculo.COMBI);
        salida.asignarGuia(guia);
        salida.asignarVehiculo(combi);
        Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina");
        Vendedor vendedor = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
        tours.guardar(tour);
        clientes.guardar(cliente);
        empleados.guardar(guia);
        empleados.guardar(vendedor);
        vehiculos.guardar(combi);

        Reserva viaja = new Reserva(1, cliente, salida, vendedor);
        viaja.agregarPasajero(pasajero("Laura", "28999888"));
        viaja.calcularTotal(new ArrayList<>());
        salida.agregarReserva(viaja);
        viaja.registrarPago(1000.0, MedioPago.EFECTIVO, 1, hoy);
        Reserva anulada = new Reserva(2, cliente, salida, vendedor);
        anulada.agregarPasajero(pasajero("Ana", "30111222"));
        anulada.calcularTotal(new ArrayList<>());
        salida.agregarReserva(anulada);
        anulada.cancelar("Cambio de planes", hoy, new PoliticaCancelacionCliente());
        salida.emitirListaPasajeros();
        salida.iniciar();
        salida.finalizar();
        guia.liquidar(salida);

        salidas.guardar(salida);
        reservas.guardar(viaja);
        reservas.guardar(anulada);
        empleados.guardar(guia);
        reabrir();

        Salida s = salidas.buscar("S001");
        Reserva r = reservas.buscar("2");
        Guia g = (Guia) empleados.buscar("202");
        assertEquals(EstadoSalida.FINALIZADA, s.getEstado());
        assertTrue(s.isListaEmitida());
        assertEquals(EstadoReserva.FINALIZADA, reservas.buscar("1").getEstado());
        assertEquals(EstadoReserva.ANULADA, r.getEstado());
        assertEquals("Cambio de planes", r.getCancelacion().getMotivo());
        assertNull(r.getCancelacion().getMedio());
        assertEquals(1, g.getLiquidaciones().size());
        assertSame(s, g.getLiquidaciones().get(0).getSalida());
        assertEquals(750.0, g.getLiquidaciones().get(0).getMontoDias(), 0.001);
        assertSame(g.getLiquidaciones().get(0), g.liquidar(s));
    }

    @Test
    void siFallaNoQuedaNadaAMedias() throws ReglaNegocioException {
        abrirRepositorios();
        Proveedor sinGuardar = new Proveedor("Kenai Kayaks", "92-0202020", "907-555-0120", "Kayaks");
        Tour tour = new TourCultural("CUL01", "Herencia nativa de Alaska", "Centro de herencia nativa", 3, 4, 20);
        tour.agregarActividad(1, 1, LocalTime.of(9, 30), "Visita al centro", "Anchorage", TipoActividad.VISITA,
                sinGuardar);

        assertThrows(IllegalStateException.class, () -> tours.guardar(tour));
        assertNull(tours.buscar("CUL01"));

        reabrir();
        assertTrue(tours.listar().isEmpty());
    }

    @Test
    void datosInicialesSinDuplicar() {
        CargadorDatosIniciales cargador = new CargadorDatosIniciales("data/datos_iniciales.txt");
        RepositorioEnMemoria<Empleado> empleados = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        RepositorioEnMemoria<Vehiculo> vehiculos = new RepositorioEnMemoria<>(Vehiculo::getPatente);

        cargador.cargar(empleados, vehiculos);

        assertEquals(7, empleados.listar().size());
        assertEquals(3, vehiculos.listar().size());
        assertTrue(((Guia) empleados.buscar("203")).estaHabilitado(CategoriaTour.AVENTURA));
        assertEquals(TipoVehiculo.MICRO, vehiculos.buscar("HMB307").getTipo());

        cargador.cargar(empleados, vehiculos);
        assertEquals(7, empleados.listar().size());

        RepositorioEnMemoria<Empleado> conPersonal = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        conPersonal.guardar(new Coordinador("Daniel", "Brooks", "33445566", 301, "907-555-0171"));
        RepositorioEnMemoria<Vehiculo> sinVehiculos = new RepositorioEnMemoria<>(Vehiculo::getPatente);

        cargador.cargar(conPersonal, sinVehiculos);

        assertEquals(1, conPersonal.listar().size());
        assertEquals(3, sinVehiculos.listar().size());
    }
}
