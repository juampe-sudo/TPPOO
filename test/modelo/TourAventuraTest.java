package modelo;

import enums.NivelDificultad;
import enums.TipoPasajero;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TourAventuraTest {

    private static final LocalDate INICIO_SALIDA = LocalDate.of(2027, 7, 10);

    private final TourAventura aventura = new TourAventura("AVE01", "Glaciar Matanuska",
            "Trekking sobre el glaciar", 2, 4, 12, NivelDificultad.ALTO, 16);

    private Pasajero pasajeroNacidoEl(LocalDate nacimiento) {
        return new Pasajero("Ana", "Pérez", "30111222", nacimiento, TipoPasajero.ADULTO,
                "Juan Pérez 1155550000", "");
    }

    @Test
    void edadMinimaAventura() {
        assertTrue(aventura.esApto(pasajeroNacidoEl(LocalDate.of(1990, 3, 5)), INICIO_SALIDA));
        assertTrue(aventura.esApto(pasajeroNacidoEl(LocalDate.of(2011, 7, 10)), INICIO_SALIDA));
        assertFalse(aventura.esApto(pasajeroNacidoEl(LocalDate.of(2011, 7, 11)), INICIO_SALIDA));
    }

    @Test
    void edadALaFechaDeSalida() {
        Pasajero pasajero = pasajeroNacidoEl(LocalDate.of(2011, 1, 1));

        assertFalse(aventura.esApto(pasajero, LocalDate.of(2026, 12, 31)));
        assertTrue(aventura.esApto(pasajero, LocalDate.of(2027, 1, 1)));
    }

    @Test
    void culturalSinEdadMinima() {
        Tour cultural = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 4, 20);

        assertTrue(cultural.esApto(pasajeroNacidoEl(LocalDate.of(2026, 1, 1)), INICIO_SALIDA));
    }

    @Test
    void serviciosObligatoriosAventura() throws ReglaNegocioException {
        aventura.agregarServicio("Seguro de viaje", 80.0, true);
        aventura.agregarServicio("Alquiler de crampones", 35.0, false);
        aventura.agregarServicio("Traslado al glaciar", 50.0, true);

        List<ServicioAdicional> obligatorios = aventura.getServiciosObligatorios();

        assertEquals(2, obligatorios.size());
        assertEquals("Seguro de viaje", obligatorios.get(0).getNombre());
        assertEquals("Traslado al glaciar", obligatorios.get(1).getNombre());
    }

    @Test
    void seguroDeViajeSiempreObligatorioEnAventura() throws ReglaNegocioException {
        aventura.agregarServicio("Seguro de viaje", 80.0, false);
        aventura.agregarServicio("Alquiler de crampones", 35.0, false);

        List<ServicioAdicional> obligatorios = aventura.getServiciosObligatorios();

        assertEquals(1, obligatorios.size());
        assertEquals("Seguro de viaje", obligatorios.get(0).getNombre());
    }

    @Test
    void serviciosObligatoriosOtraCategoria() throws ReglaNegocioException {
        Tour cultural = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 4, 20);
        cultural.agregarServicio("Almuerzo típico", 40.0, false);
        cultural.agregarServicio("Entrada al museo", 25.0, true);

        List<ServicioAdicional> obligatorios = cultural.getServiciosObligatorios();

        assertEquals(1, obligatorios.size());
        assertEquals("Entrada al museo", obligatorios.get(0).getNombre());
    }
}
