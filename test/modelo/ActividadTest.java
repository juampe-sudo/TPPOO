package modelo;

import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActividadTest {

    private Actividad actividad(int dia, int orden) {
        return new Actividad(dia, orden, LocalTime.of(9, 0), dia + "-" + orden,
                "Anchorage", TipoActividad.VISITA, null);
    }

    private void agregar(Tour tour, int dia, int orden) throws ReglaNegocioException {
        tour.agregarActividad(dia, orden, LocalTime.of(9, 0), dia + "-" + orden,
                "Anchorage", TipoActividad.VISITA, null);
    }

    @Test
    void ordenaActividadesEnTour() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 4, 20);
        agregar(tour, 3, 2);
        agregar(tour, 1, 2);
        agregar(tour, 2, 1);
        agregar(tour, 3, 1);
        agregar(tour, 1, 1);

        List<Actividad> itinerario = tour.getActividades();

        assertEquals(5, itinerario.size());
        assertEquals("1-1", itinerario.get(0).getDescripcion());
        assertEquals("1-2", itinerario.get(1).getDescripcion());
        assertEquals("2-1", itinerario.get(2).getDescripcion());
        assertEquals("3-1", itinerario.get(3).getDescripcion());
        assertEquals("3-2", itinerario.get(4).getDescripcion());
    }

    @Test
    void rechazaDiaFueraDelTourYOrdenRepetido() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 4, 20);
        agregar(tour, 1, 1);

        assertThrows(ReglaNegocioException.class, () -> agregar(tour, 4, 1));
        assertThrows(ReglaNegocioException.class, () -> agregar(tour, 0, 1));
        assertThrows(ReglaNegocioException.class, () -> agregar(tour, 1, 1));
        assertEquals(1, tour.getActividades().size());
    }

    @Test
    void compararActividades() {
        assertTrue(actividad(1, 5).compareTo(actividad(2, 1)) < 0);
        assertTrue(actividad(2, 3).compareTo(actividad(2, 1)) > 0);
        assertEquals(0, actividad(4, 2).compareTo(actividad(4, 2)));
    }
}
