package estrategias;

import enums.Temporada;
import enums.TipoPasajero;
import excepciones.ReglaNegocioException;
import modelo.Cliente;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vendedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PromocionTest {

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234",
            "laura@mail.com", "Argentina");
    private final Vendedor vendedor = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);
    private Tour tour;

    @BeforeEach
    void crearTour() throws ReglaNegocioException {
        tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
    }

    private Reserva reservaConAdultos(int diasHastaSalida, int adultos) throws ReglaNegocioException {
        Salida salida = tour.programarSalida("S001", hoy.plusDays(diasHastaSalida), 15);
        Reserva reserva = new Reserva(1, cliente, salida, vendedor);
        for (int i = 0; i < adultos; i++) {
            reserva.agregarPasajero(new Pasajero("Ana", "Pérez", "3000000" + i, LocalDate.of(1990, 3, 5),
                    TipoPasajero.ADULTO, "Juan Pérez 1155550000", ""));
        }
        return reserva;
    }

    @Test
    void descuentoReservaAnticipada() throws ReglaNegocioException {
        Promocion anticipada = new PromocionReservaAnticipada(60, 0.15);

        assertEquals(0.0, anticipada.calcularDescuento(reservaConAdultos(60, 2)), 0.001);
        assertEquals(300.0, anticipada.calcularDescuento(reservaConAdultos(61, 2)), 0.001);
    }

    @Test
    void descuentoGrupo() throws ReglaNegocioException {
        Promocion grupo = new PromocionGrupo(6, 0.10);

        assertEquals(0.0, grupo.calcularDescuento(reservaConAdultos(30, 5)), 0.001);
        assertEquals(600.0, grupo.calcularDescuento(reservaConAdultos(30, 6)), 0.001);
    }

    @Test
    void grupoCuentaMenoresDeTres() throws ReglaNegocioException {
        Reserva reserva = reservaConAdultos(30, 5);
        reserva.agregarPasajero(new Pasajero("Sofía", "Pérez", "60111222", hoy.minusYears(1),
                TipoPasajero.MENOR, "Ana Pérez 1155550000", ""));

        assertEquals(500.0, new PromocionGrupo(6, 0.10).calcularDescuento(reserva), 0.001);
    }
}
