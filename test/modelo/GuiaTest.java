package modelo;

import enums.CategoriaTour;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoPasajero;
import enums.TipoVehiculo;
import estrategias.PoliticaCancelacionCliente;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GuiaTest {

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234",
            "laura@mail.com", "Argentina");
    private final Vendedor vendedor = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);
    private Guia guia;
    private Tour tour;

    @BeforeEach
    void crearTour() throws ReglaNegocioException {
        tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        tour.agregarServicio("Kayak en la bahía", 100.0, false);

        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        idiomas.add("Español");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(CategoriaTour.CULTURAL);
        categorias.add(CategoriaTour.NATURALEZA);
        guia = new Guia("Tom", "Reed", "40111222", 201, "9075550000", idiomas, categorias, 150.0);
    }

    private Pasajero pasajero(String documento) {
        return new Pasajero("Ana", "Pérez", documento, LocalDate.of(1990, 3, 5), TipoPasajero.ADULTO,
                "Juan Pérez 1155550000", "");
    }

    @Test
    void disponibilidadYHabilitacionGuia() throws ReglaNegocioException {
        assertTrue(guia.estaHabilitado(CategoriaTour.CULTURAL));
        assertFalse(guia.estaHabilitado(CategoriaTour.AVENTURA));

        Salida primera = tour.programarSalida("S001", hoy.plusDays(40), 10);
        Salida pisada = tour.programarSalida("S002", hoy.plusDays(41), 10);
        Salida libre = tour.programarSalida("S003", hoy.plusDays(50), 10);
        primera.asignarGuia(guia);

        assertTrue(guia.estaDisponible(primera));
        assertFalse(guia.estaDisponible(pisada));
        assertThrows(ReglaNegocioException.class, () -> pisada.asignarGuia(guia));
        assertTrue(guia.estaDisponible(libre));

        primera.cancelar();
        assertTrue(guia.estaDisponible(pisada));
    }

    @Test
    void liquidacionHonorariosGuia() throws ReglaNegocioException {
        Salida salida = tour.programarSalida("S001", hoy.plusDays(40), 10);
        ServicioAdicional kayak = tour.getAdicionales().get(0);

        Reserva grupo = new Reserva(1, cliente, salida, vendedor);
        for (int i = 0; i < 4; i++) {
            grupo.agregarPasajero(pasajero("100" + i));
        }
        grupo.contratarServicio("1000", kayak);
        grupo.contratarServicio("1001", kayak);
        salida.agregarReserva(grupo);

        Reserva anulada = new Reserva(2, cliente, salida, vendedor);
        anulada.agregarPasajero(pasajero("2000"));
        anulada.contratarServicio("2000", kayak);
        salida.agregarReserva(anulada);
        anulada.cancelar("No viaja", hoy, new PoliticaCancelacionCliente());

        grupo.calcularTotal(new ArrayList<>());
        grupo.registrarPago(grupo.getTotal() * 0.30, MedioPago.TRANSFERENCIA, 1, hoy);
        salida.asignarGuia(guia);
        salida.asignarVehiculo(new Vehiculo("AC456EF", 12, TipoVehiculo.MICRO));
        salida.emitirListaPasajeros();
        salida.iniciar();
        assertThrows(ReglaNegocioException.class, () -> guia.liquidar(salida));

        salida.finalizar();
        assertEquals(200.0, salida.totalAdicionalesVendidos(), 0.001);

        Liquidacion liquidacion = guia.liquidar(salida);
        assertEquals(3, liquidacion.getDias());
        assertEquals(450.0, liquidacion.getMontoDias(), 0.001);
        assertEquals(10.0, liquidacion.getMontoAdicionales(), 0.001);
        assertEquals(460.0, liquidacion.getTotal(), 0.001);
        assertSame(liquidacion, guia.liquidar(salida));
        assertEquals(1, guia.getLiquidaciones().size());

        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(CategoriaTour.CULTURAL);
        Guia otro = new Guia("Kate", "Moss", "40333444", 202, "9075552222", idiomas, categorias, 120.0);
        assertThrows(ReglaNegocioException.class, () -> otro.liquidar(salida));
    }
}
