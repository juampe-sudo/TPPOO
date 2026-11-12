package modelo;

import enums.Temporada;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TourTest {

    private Tour tour;

    @BeforeEach
    void crearTour() throws ReglaNegocioException {
        tour = new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, LocalDate.of(2027, 6, 1), LocalDate.of(2027, 8, 31), 2000.0);
    }

    @Test
    void agregarTarifaSinSuperposicion() {
        assertDoesNotThrow(() -> tour.agregarTarifa(Temporada.MEDIA,
                LocalDate.of(2027, 9, 1), LocalDate.of(2027, 11, 30), 1500.0));
        assertEquals(2, tour.getTarifas().size());
    }

    @Test
    void agregarTarifaSuperpuesta() {
        assertThrows(ReglaNegocioException.class, () -> tour.agregarTarifa(Temporada.MEDIA,
                LocalDate.of(2027, 8, 15), LocalDate.of(2027, 10, 31), 1500.0));
        assertThrows(ReglaNegocioException.class, () -> tour.agregarTarifa(Temporada.BAJA,
                LocalDate.of(2027, 4, 1), LocalDate.of(2027, 6, 1), 1000.0));
        assertThrows(ReglaNegocioException.class, () -> tour.agregarTarifa(Temporada.BAJA,
                LocalDate.of(2027, 1, 1), LocalDate.of(2027, 12, 31), 1000.0));
        assertEquals(1, tour.getTarifas().size());
    }

    @Test
    void agregarTarifaFechasInvertidas() {
        assertThrows(ReglaNegocioException.class, () -> tour.agregarTarifa(Temporada.BAJA,
                LocalDate.of(2027, 12, 31), LocalDate.of(2027, 10, 1), 1000.0));
    }

    @Test
    void tarifaVigentePorFecha() throws ReglaNegocioException {
        tour.agregarTarifa(Temporada.MEDIA, LocalDate.of(2027, 9, 1), LocalDate.of(2027, 11, 30), 1500.0);

        Tarifa tarifa = tour.getTarifaVigente(LocalDate.of(2027, 10, 10));

        assertSame(Temporada.MEDIA, tarifa.getTemporada());
        assertEquals(1500.0, tarifa.getPrecioBase());
        assertSame(Temporada.ALTA, tour.getTarifaVigente(LocalDate.of(2027, 8, 31)).getTemporada());
    }

    @Test
    void sinTarifaVigente() {
        assertThrows(ReglaNegocioException.class, () -> tour.getTarifaVigente(LocalDate.of(2027, 9, 1)));
    }
}
