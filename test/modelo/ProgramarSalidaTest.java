package modelo;

import enums.EstadoSalida;
import enums.Temporada;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProgramarSalidaTest {

    private final LocalDate hoy = LocalDate.now();
    private Tour tour;

    @BeforeEach
    void crearTour() throws ReglaNegocioException {
        tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy.minusDays(10), hoy.plusDays(90), 2000.0);
    }

    @Test
    void programarSalidaValida() throws ReglaNegocioException {
        LocalDate inicio = hoy.plusDays(30);

        Salida salida = tour.programarSalida("S001", inicio, 15);

        assertEquals("S001", salida.getCodigo());
        assertSame(tour, salida.getTour());
        assertEquals(inicio, salida.getFechaInicio());
        assertEquals(inicio.plusDays(2), salida.getFechaFin());
        assertEquals(4, salida.getCupoMinimo());
        assertEquals(15, salida.getCupoMaximo());
        assertEquals(EstadoSalida.PROGRAMADA, salida.getEstado());
        assertNull(salida.getGuia());
        assertNull(salida.getVehiculo());
        assertTrue(salida.getReservas().isEmpty());
    }

    @Test
    void programarSalidaHoyYCuposLimite() throws ReglaNegocioException {
        assertEquals(hoy, tour.programarSalida("S001", hoy, 15).getFechaInicio());
        assertEquals(4, tour.programarSalida("S002", hoy.plusDays(30), 4).getCupoMaximo());
        assertEquals(20, tour.programarSalida("S003", hoy.plusDays(30), 20).getCupoMaximo());
    }

    @Test
    void programarSalidaFechaPasada() {
        assertThrows(ReglaNegocioException.class, () -> tour.programarSalida("S001", hoy.minusDays(1), 15));
    }

    @Test
    void programarSalidaSinTarifa() {
        assertThrows(ReglaNegocioException.class, () -> tour.programarSalida("S001", hoy.plusDays(120), 15));
    }

    @Test
    void programarSalidaCupoInvalido() {
        assertThrows(ReglaNegocioException.class, () -> tour.programarSalida("S001", hoy.plusDays(30), 3));
        assertThrows(ReglaNegocioException.class, () -> tour.programarSalida("S002", hoy.plusDays(30), 21));
    }
}
