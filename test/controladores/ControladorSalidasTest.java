package controladores;

import enums.CategoriaTour;
import enums.EstadoReserva;
import enums.EstadoSalida;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoPasajero;
import enums.TipoVehiculo;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Guia;
import modelo.Liquidacion;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vehiculo;
import modelo.Vendedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.RepositorioEnMemoria;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControladorSalidasTest {

    @TempDir
    Path carpeta;

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com",
            "Argentina");
    private final Vendedor vendedor = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
    private RepositorioEnMemoria<Salida> salidas;
    private RepositorioEnMemoria<Reserva> reservas;
    private Guia guia;
    private ControladorSalidas controlador;

    @BeforeEach
    void crearControlador() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        tour.agregarServicio("Cena de despedida", 100.0, false);
        RepositorioEnMemoria<Tour> tours = new RepositorioEnMemoria<>(Tour::getCodigo);
        tours.guardar(tour);

        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(CategoriaTour.CULTURAL);
        guia = new Guia("Aiyana", "Nanouk", "40112233", 202, "907-555-0133", idiomas, categorias, 250.0);
        RepositorioEnMemoria<Empleado> empleados = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        empleados.guardar(guia);
        empleados.guardar(vendedor);

        RepositorioEnMemoria<Vehiculo> vehiculos = new RepositorioEnMemoria<>(Vehiculo::getPatente);
        vehiculos.guardar(new Vehiculo("KDF482", 8, TipoVehiculo.CAMIONETA));
        vehiculos.guardar(new Vehiculo("JTR915", 15, TipoVehiculo.COMBI));

        salidas = new RepositorioEnMemoria<>(Salida::getCodigo);
        reservas = new RepositorioEnMemoria<>(r -> String.valueOf(r.getNumero()));
        controlador = new ControladorSalidas(tours, salidas, empleados, vehiculos, reservas, carpeta.toString());
    }

    private Reserva reservar(int numero, Salida salida, int pasajeros, double porcentajePagado)
            throws ReglaNegocioException {
        Reserva r = new Reserva(numero, cliente, salida, vendedor);
        for (int i = 0; i < pasajeros; i++) {
            r.agregarPasajero(new Pasajero("Ana", "Pérez", numero + "00" + i, LocalDate.of(1990, 3, 5),
                    TipoPasajero.ADULTO, "Juan Pérez 1155550000", ""));
        }
        r.calcularTotal(new ArrayList<>());
        salida.agregarReserva(r);
        if (porcentajePagado > 0) {
            r.registrarPago(r.getTotal() * porcentajePagado, MedioPago.EFECTIVO, 1, hoy);
        }
        reservas.guardar(r);
        return r;
    }

    @Test
    void programaSalidasConCodigoCorrelativo() throws ReglaNegocioException {
        Salida primera = controlador.programarSalida("CUL01", hoy.plusDays(30), 10);
        Salida segunda = controlador.programarSalida("CUL01", hoy.plusDays(60), 12);

        assertEquals("S001", primera.getCodigo());
        assertEquals("S002", segunda.getCodigo());
        assertSame(segunda, salidas.buscar("S002"));
        assertEquals(EstadoSalida.PROGRAMADA, primera.getEstado());
        assertThrows(ReglaNegocioException.class, () -> controlador.programarSalida("AVE99", hoy.plusDays(30), 10));
        assertThrows(ReglaNegocioException.class, () -> controlador.programarSalida("CUL01", hoy.plusDays(30), 30));
        assertEquals(2, salidas.listar().size());
    }

    @Test
    void asignaGuiaYVehiculoPorLegajoYPatente() throws ReglaNegocioException {
        Salida salida = controlador.programarSalida("CUL01", hoy.plusDays(30), 10);
        controlador.programarSalida("CUL01", hoy.plusDays(31), 10);

        assertEquals(1, controlador.guiasDisponibles("S001").size());
        assertEquals(1, controlador.vehiculosDisponibles("S001").size());
        assertThrows(ReglaNegocioException.class, () -> controlador.asignarGuia("S001", 101));
        assertThrows(ReglaNegocioException.class, () -> controlador.asignarGuia("S009", 202));
        controlador.asignarGuia("S001", 202);
        assertSame(guia, salida.getGuia());
        assertThrows(ReglaNegocioException.class, () -> controlador.guiasDisponibles("S002"));

        assertThrows(ReglaNegocioException.class, () -> controlador.asignarVehiculo("S001", "KDF482"));
        assertThrows(ReglaNegocioException.class, () -> controlador.asignarVehiculo("S001", "ZZZ000"));
        controlador.asignarVehiculo("S001", "JTR915");
        assertEquals("JTR915", salida.getVehiculo().getPatente());
    }

    @Test
    void cancelarSalidaDevuelveTodoLoPagado() throws ReglaNegocioException {
        Salida salida = controlador.programarSalida("CUL01", hoy.plusDays(5), 10);
        Reserva conSena = reservar(1, salida, 4, 0.30);
        Reserva sinPagos = reservar(2, salida, 1, 0);

        List<Cancelacion> devoluciones = controlador.cancelarSalida("S001", "Tormenta de nieve");

        assertEquals(2, devoluciones.size());
        assertEquals(EstadoSalida.CANCELADA, salida.getEstado());
        assertEquals(EstadoReserva.ANULADA, conSena.getEstado());
        assertEquals(1200.0, conSena.getCancelacion().getImporteDevuelto(), 0.001);
        assertEquals("Tormenta de nieve", sinPagos.getCancelacion().getMotivo());
        assertEquals(0.0, sinPagos.getCancelacion().getImporteDevuelto(), 0.001);
        assertThrows(ReglaNegocioException.class, () -> controlador.cancelarSalida("S001", "Otra vez"));
    }

    @Test
    void controlarPlazosCancelaSalidasYAnulaReservasImpagas() throws ReglaNegocioException {
        Salida sinMinimo = controlador.programarSalida("CUL01", hoy.plusDays(6), 10);
        Reserva sola = reservar(1, sinMinimo, 2, 0.30);
        Salida confirmada = controlador.programarSalida("CUL01", hoy.plusDays(9), 10);
        Reserva conSaldo = reservar(2, confirmada, 4, 0.30);
        Reserva pagada = reservar(3, confirmada, 1, 1.0);
        Salida lejana = controlador.programarSalida("CUL01", hoy.plusDays(40), 10);
        Reserva lejos = reservar(4, lejana, 1, 0);

        List<String> procesado = controlador.controlarPlazos(hoy);

        assertEquals(2, procesado.size());
        assertEquals("Reserva 2 anulada por falta de pago. Se devuelven USD " + String.format("%.2f", 240.0), procesado.get(1));
        assertEquals(EstadoSalida.CANCELADA, sinMinimo.getEstado());
        assertEquals("No alcanzó el cupo mínimo", sola.getCancelacion().getMotivo());
        assertEquals(600.0, sola.getCancelacion().getImporteDevuelto(), 0.001);

        assertEquals(EstadoReserva.ANULADA, conSaldo.getEstado());
        assertEquals(240.0, conSaldo.getCancelacion().getImporteDevuelto(), 0.001);
        assertEquals(EstadoReserva.CONFIRMADA, pagada.getEstado());
        assertEquals(EstadoSalida.PROGRAMADA, confirmada.getEstado());

        assertEquals(EstadoSalida.PROGRAMADA, lejana.getEstado());
        assertEquals(EstadoReserva.PENDIENTE, lejos.getEstado());
        assertTrue(controlador.controlarPlazos(hoy).isEmpty());
    }

    @Test
    void cicloCompletoYLiquidacionDelGuia() throws ReglaNegocioException, IOException {
        Salida futura = controlador.programarSalida("CUL01", hoy.plusDays(20), 10);
        Salida salida = controlador.programarSalida("CUL01", hoy, 10);
        Reserva grupo = reservar(1, salida, 4, 0.30);
        grupo.contratarServicio("1000", salida.getTour().getAdicionales().get(0));
        controlador.asignarGuia("S002", 202);
        controlador.asignarVehiculo("S002", "JTR915");

        assertThrows(ReglaNegocioException.class, () -> controlador.iniciarSalida("S002"));
        List<Pasajero> lista = controlador.emitirListaPasajeros("S002");
        assertEquals(4, lista.size());
        List<String> lineas = Files.readAllLines(carpeta.resolve("lista_S002.txt"));
        assertEquals(5, lineas.size());
        assertEquals("Pérez, Ana | 1000 | Juan Pérez 1155550000 | sin restricciones", lineas.get(1));
        assertThrows(ReglaNegocioException.class, () -> controlador.iniciarSalida(futura.getCodigo()));
        controlador.iniciarSalida("S002");
        assertEquals(EstadoSalida.EN_CURSO, salida.getEstado());
        assertThrows(ReglaNegocioException.class, () -> controlador.liquidarHonorarios("S002"));

        controlador.cerrarSalida("S002");
        assertEquals(EstadoSalida.FINALIZADA, salida.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, grupo.getEstado());

        Liquidacion liquidacion = controlador.liquidarHonorarios("S002");
        assertSame(guia, liquidacion.getGuia());
        assertEquals(750.0, liquidacion.getMontoDias(), 0.001);
        assertEquals(5.0, liquidacion.getMontoAdicionales(), 0.001);
        assertEquals(755.0, liquidacion.getTotal(), 0.001);
        assertThrows(ReglaNegocioException.class, () -> controlador.liquidarHonorarios("S001"));
    }
}
