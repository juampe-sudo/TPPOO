package controladores;

import enums.EstadoReserva;
import enums.EstadoSalida;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoPasajero;
import estrategias.Promocion;
import estrategias.PromocionGrupo;
import estrategias.PromocionReservaAnticipada;
import excepciones.CupoInsuficienteException;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vendedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.RepositorioEnMemoria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ControladorReservasTest {

    private final LocalDate hoy = LocalDate.now();
    private final LocalDate nacimiento = LocalDate.of(1990, 3, 5);
    private final Vendedor vendedor = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
    private RepositorioEnMemoria<Reserva> reservas;
    private Salida salida;
    private ControladorReservas controlador;

    @BeforeEach
    void crearControlador() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        tour.agregarServicio("Cena de despedida", 100.0, false);
        salida = tour.programarSalida("S001", hoy.plusDays(40), 5);

        RepositorioEnMemoria<Salida> salidas = new RepositorioEnMemoria<>(Salida::getCodigo);
        salidas.guardar(salida);
        reservas = new RepositorioEnMemoria<>(r -> String.valueOf(r.getNumero()));
        List<Promocion> promociones = new ArrayList<>();
        promociones.add(new PromocionReservaAnticipada(60, 0.15));
        promociones.add(new PromocionGrupo(6, 0.10));
        RepositorioEnMemoria<Empleado> empleados = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        empleados.guardar(vendedor);
        controlador = new ControladorReservas(new RepositorioEnMemoria<>(Cliente::getDocumento), salidas, reservas,
                empleados, promociones);
        controlador.registrarCliente(new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com",
                "Argentina"));
    }

    private Reserva reservar(int pasajeros) throws ReglaNegocioException {
        controlador.iniciarReserva("28999888", "S001", vendedor);
        for (int i = 0; i < pasajeros; i++) {
            controlador.agregarPasajero("Ana", "Pérez", "3000" + i, nacimiento, TipoPasajero.ADULTO,
                    "Juan Pérez 1155550000", "");
        }
        return controlador.confirmarReserva();
    }

    @Test
    void creaUnaReservaConPasajerosYAdicionales() throws ReglaNegocioException {
        Reserva enCurso = controlador.iniciarReserva("28999888", "S001", vendedor);
        controlador.agregarPasajero("Laura", "Gómez", "28999888", nacimiento, TipoPasajero.ADULTO,
                "Juan Gómez 1155550000", "");
        controlador.agregarPasajero("Tomás", "Gómez", "52111222", LocalDate.of(2018, 1, 10), TipoPasajero.MENOR,
                "Laura Gómez 1155551234", "Celíaco");
        controlador.contratarServicio("28999888", "cena de despedida");
        assertThrows(ReglaNegocioException.class, () -> controlador.contratarServicio("28999888", "Kayak"));

        Reserva reserva = controlador.confirmarReserva();

        assertSame(enCurso, reserva);
        assertEquals(1, reserva.getNumero());
        assertEquals(1700.0, reserva.getTotal(), 0.001);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertSame(reserva, reservas.buscar("1"));
        assertSame(reserva, salida.getReservas().get(0));
        assertEquals(3, salida.lugaresDisponibles());
        assertThrows(ReglaNegocioException.class, () -> controlador.confirmarReserva());
    }

    @Test
    void rechazaClienteRepetidoYDatosInexistentes() {
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarCliente(
                new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina")));
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarCliente(
                new Cliente("Pedro", "Ruiz", "30555444", "1155550001", "pedro.mail.com", "Chile")));
        assertThrows(ReglaNegocioException.class, () -> controlador.iniciarReserva("11111111", "S001", vendedor));
        assertThrows(ReglaNegocioException.class, () -> controlador.iniciarReserva("28999888", "S999", vendedor));
        assertThrows(ReglaNegocioException.class, () -> controlador.agregarPasajero("Ana", "Pérez", "30001",
                nacimiento, TipoPasajero.ADULTO, "", ""));
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarPago(7, 100.0, MedioPago.EFECTIVO, 1));
        assertThrows(ReglaNegocioException.class, () -> controlador.buscarVendedor(999));
    }

    @Test
    void noRegistraLaReservaSiNoHayLugar() throws ReglaNegocioException {
        reservar(4);
        controlador.iniciarReserva("28999888", "S001", vendedor);
        controlador.agregarPasajero("Ana", "Díaz", "40001", nacimiento, TipoPasajero.ADULTO, "", "");
        controlador.agregarPasajero("Luis", "Díaz", "40002", nacimiento, TipoPasajero.ADULTO, "", "");

        CupoInsuficienteException e = assertThrows(CupoInsuficienteException.class,
                () -> controlador.confirmarReserva());

        assertEquals("No hay lugares suficientes. Lugares libres: 1", e.getMessage());
        assertEquals(1, reservas.listar().size());
        assertEquals(1, salida.getReservas().size());
    }

    @Test
    void pagaLaSenaYDespuesCancela() throws ReglaNegocioException {
        Reserva reserva = reservar(4);
        assertThrows(ReglaNegocioException.class, () -> controlador.registrarPago(1, 5000.0, MedioPago.EFECTIVO, 1));

        controlador.registrarPago(1, 1200.0, MedioPago.TRANSFERENCIA, 1);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());

        Cancelacion c = controlador.cancelarReserva(1, "Cambio de planes", false);

        assertEquals(1080.0, c.getImporteDevuelto(), 0.001);
        assertEquals(MedioPago.TRANSFERENCIA, c.getMedio());
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
        assertThrows(ReglaNegocioException.class, () -> controlador.cancelarReserva(1, "Otra vez", true));
        assertThrows(ReglaNegocioException.class, () -> controlador.cancelarReserva(9, "No existe", true));
    }

    @Test
    void cancelacionPorLaAgenciaDevuelveTodo() throws ReglaNegocioException {
        reservar(2);
        controlador.registrarPago(1, 600.0, MedioPago.EFECTIVO, 1);

        Cancelacion c = controlador.cancelarReserva(1, "Cierre del parque", true);

        assertEquals(600.0, c.getImporteDevuelto(), 0.001);
    }

    @Test
    void reemplazaUnPasajeroConNotaDeAjuste() throws ReglaNegocioException {
        Reserva reserva = reservar(2);
        Pasajero nuevo = new Pasajero("Carla", "Ruiz", "45999000", nacimiento, TipoPasajero.ADULTO,
                "Pablo Ruiz 1144443333", "");

        assertSame(reserva, controlador.buscarReserva(1));
        controlador.reemplazarPasajero(1, "30000", nuevo);

        assertSame(nuevo, reserva.getPasajeros().get(0));
        assertEquals(1, reserva.getNotas().size());
        assertEquals(2000.0, reserva.getTotal(), 0.001);
        assertThrows(ReglaNegocioException.class, () -> controlador.reemplazarPasajero(1, "99999", nuevo));
        assertTrue(reserva.getNotas().get(0).getDescripcion().contains("Carla Ruiz"));
    }

    @Test
    void contrataUnAdicionalSobreUnaReservaYaPagada() throws ReglaNegocioException {
        Reserva reserva = reservar(2);
        controlador.registrarPago(1, 600.0, MedioPago.EFECTIVO, 1);

        controlador.contratarServicio(1, "30001", "Cena de despedida");

        assertEquals(2100.0, reserva.getTotal(), 0.001);
        assertEquals(1, reserva.getPasajeros().get(1).getServicios().size());
        assertEquals(1, reserva.getNotas().size());
        assertThrows(ReglaNegocioException.class, () -> controlador.contratarServicio(1, "30001", "Kayak"));
        assertThrows(ReglaNegocioException.class, () -> controlador.contratarServicio(5, "30001", "Cena de despedida"));
    }
}
