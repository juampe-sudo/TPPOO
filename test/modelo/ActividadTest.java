package modelo;

import enums.TipoActividad;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ActividadTest {

    private Actividad actividad(int dia, int orden) {
        return new Actividad(dia, orden, LocalTime.of(9, 0), "Actividad " + dia + "-" + orden,
                "Anchorage", TipoActividad.VISITA, null);
    }

    @Test
    void ordenaUnItinerarioDesordenadoPorDiaYOrden() {
        Actividad d1o1 = actividad(1, 1);
        Actividad d1o2 = actividad(1, 2);
        Actividad d2o1 = actividad(2, 1);
        Actividad d3o1 = actividad(3, 1);
        Actividad d3o2 = actividad(3, 2);
        List<Actividad> itinerario = new ArrayList<>(List.of(d3o2, d1o2, d2o1, d3o1, d1o1));

        Collections.sort(itinerario);

        assertEquals(List.of(d1o1, d1o2, d2o1, d3o1, d3o2), itinerario);
    }

    @Test
    void elDiaPesaMasQueElOrden() {
        assertTrue(actividad(1, 5).compareTo(actividad(2, 1)) < 0);
    }

    @Test
    void enElMismoDiaOrdenaPorOrden() {
        assertTrue(actividad(2, 3).compareTo(actividad(2, 1)) > 0);
    }

    @Test
    void mismoDiaYOrdenSonEquivalentes() {
        assertEquals(0, actividad(4, 2).compareTo(actividad(4, 2)));
    }
}
