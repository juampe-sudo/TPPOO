package modelo;

import enums.CategoriaTour;
import enums.EstadoReserva;
import enums.EstadoSalida;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoPasajero;
import enums.TipoVehiculo;
import estrategias.PoliticaCancelacionCliente;
import excepciones.CupoInsuficienteException;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SalidaTest {

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234",
            "laura@mail.com", "Argentina");
    private final Vendedor vendedor = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);
    private Tour tour;
    private Salida salida;

    @BeforeEach
    void crearSalida() throws ReglaNegocioException {
        tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        salida = tour.programarSalida("S001", hoy.plusDays(40), 10);
    }

    private Reserva reserva(int numero, int pasajeros, String apellido) throws ReglaNegocioException {
        Reserva r = new Reserva(numero, cliente, salida, vendedor);
        for (int i = 0; i < pasajeros; i++) {
            r.agregarPasajero(new Pasajero("Ana", apellido, numero + "00" + i, LocalDate.of(1990, 3, 5),
                    TipoPasajero.ADULTO, "Juan Pérez 1155550000", ""));
        }
        return r;
    }

    private void pagarSena(Reserva r) throws ReglaNegocioException {
        r.calcularTotal(new ArrayList<>());
        r.registrarPago(r.getTotal() * 0.30, MedioPago.EFECTIVO, 1, hoy);
    }

    private Guia guia(int legajo, CategoriaTour categoria) {
        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(categoria);
        return new Guia("Tom", "Reed", "40111" + legajo, legajo, "9075550000", idiomas, categorias, 150.0);
    }

    @Test
    void duracionYSuperposicionDeFechas() throws ReglaNegocioException {
        assertEquals(3, salida.diasDeDuracion());
        assertEquals(40, salida.diasHastaInicio(hoy));

        Salida pegada = tour.programarSalida("S002", hoy.plusDays(42), 10);
        Salida anterior = tour.programarSalida("S003", hoy.plusDays(38), 10);
        Salida separada = tour.programarSalida("S004", hoy.plusDays(43), 10);

        assertTrue(salida.seSuperponeCon(pegada));
        assertTrue(salida.seSuperponeCon(anterior));
        assertTrue(anterior.seSuperponeCon(salida));
        assertFalse(salida.seSuperponeCon(separada));
    }

    @Test
    void cuposYTransicionesDeEstado() throws ReglaNegocioException {
        assertTrue(salida.admiteReservas());

        Reserva primera = reserva(1, 3, "Pérez");
        salida.agregarReserva(primera);
        assertEquals(3, salida.lugaresOcupados());
        assertEquals(7, salida.lugaresDisponibles());
        pagarSena(primera);
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());

        Reserva segunda = reserva(2, 2, "López");
        salida.agregarReserva(segunda);
        pagarSena(segunda);
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());

        Reserva tercera = reserva(3, 5, "Díaz");
        salida.agregarReserva(tercera);
        assertEquals(EstadoSalida.COMPLETA, salida.getEstado());
        assertEquals(0, salida.lugaresDisponibles());
        assertFalse(salida.admiteReservas());

        tercera.cancelar("Cambio de planes", hoy, new PoliticaCancelacionCliente());
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());
        segunda.cancelar("Cambio de planes", hoy, new PoliticaCancelacionCliente());
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
        assertEquals(3, salida.lugaresOcupados());
    }

    @Test
    void agregarReservaSinLugar() throws ReglaNegocioException {
        salida.agregarReserva(reserva(1, 8, "Pérez"));

        CupoInsuficienteException e = assertThrows(CupoInsuficienteException.class,
                () -> salida.agregarReserva(reserva(2, 3, "López")));
        assertEquals("No hay lugares suficientes. Lugares libres: 2", e.getMessage());
        assertEquals(1, salida.getReservas().size());

        salida.cancelar();
        assertEquals(EstadoSalida.CANCELADA, salida.getEstado());
        assertThrows(ReglaNegocioException.class, () -> salida.agregarReserva(reserva(3, 1, "Díaz")));
    }

    @Test
    void asignacionGuiaYVehiculo() throws ReglaNegocioException {
        Guia soloAventura = guia(201, CategoriaTour.AVENTURA);
        Guia cultural = guia(202, CategoriaTour.CULTURAL);

        assertThrows(ReglaNegocioException.class, () -> salida.asignarGuia(soloAventura));
        salida.asignarGuia(cultural);
        assertSame(cultural, salida.getGuia());
        assertTrue(cultural.getSalidas().contains(salida));

        assertThrows(ReglaNegocioException.class,
                () -> salida.asignarVehiculo(new Vehiculo("AB123CD", 8, TipoVehiculo.COMBI)));
        Vehiculo micro = new Vehiculo("AC456EF", 10, TipoVehiculo.MICRO);
        salida.asignarVehiculo(micro);
        assertSame(micro, salida.getVehiculo());
    }

    @Test
    void emitirListaOrdenadaPorApellido() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class, () -> salida.emitirListaPasajeros());
        salida.asignarGuia(guia(202, CategoriaTour.CULTURAL));
        assertThrows(ReglaNegocioException.class, () -> salida.emitirListaPasajeros());

        Reserva anulada = reserva(2, 1, "Adams");
        Reserva bruno = new Reserva(4, cliente, salida, vendedor);
        bruno.agregarPasajero(new Pasajero("Bruno", "Johnson", "4001", LocalDate.of(1985, 6, 1),
                TipoPasajero.ADULTO, "Lisa Johnson 9075551111", "Celíaco"));
        salida.agregarReserva(reserva(1, 1, "Smith"));
        salida.agregarReserva(anulada);
        salida.agregarReserva(bruno);
        salida.agregarReserva(reserva(3, 1, "Johnson"));
        anulada.cancelar("No viaja", hoy, new PoliticaCancelacionCliente());

        List<Pasajero> lista = salida.emitirListaPasajeros();

        assertEquals(3, lista.size());
        assertEquals("Ana Johnson", lista.get(0).getNombreCompleto());
        assertEquals("Bruno Johnson", lista.get(1).getNombreCompleto());
        assertEquals("Ana Smith", lista.get(2).getNombreCompleto());
        assertTrue(salida.isListaEmitida());
    }

    @Test
    void cicloDeVidaSalida() throws ReglaNegocioException {
        Reserva grupo = reserva(1, 4, "Pérez");
        salida.agregarReserva(grupo);
        assertThrows(ReglaNegocioException.class, () -> salida.iniciar());

        pagarSena(grupo);
        assertEquals(EstadoSalida.CONFIRMADA, salida.getEstado());
        assertThrows(ReglaNegocioException.class, () -> salida.iniciar());

        salida.asignarGuia(guia(202, CategoriaTour.CULTURAL));
        salida.asignarVehiculo(new Vehiculo("AC456EF", 12, TipoVehiculo.MICRO));
        assertThrows(ReglaNegocioException.class, () -> salida.iniciar());

        salida.emitirListaPasajeros();
        salida.iniciar();
        assertEquals(EstadoSalida.EN_CURSO, salida.getEstado());
        assertThrows(ReglaNegocioException.class, () -> salida.cancelar());

        salida.finalizar();
        assertEquals(EstadoSalida.FINALIZADA, salida.getEstado());
        assertEquals(EstadoReserva.FINALIZADA, grupo.getEstado());
        assertThrows(ReglaNegocioException.class, () -> salida.finalizar());
    }
}
