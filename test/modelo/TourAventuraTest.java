package modelo;

import enums.NivelDificultad;
import enums.TipoPasajero;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

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
    void esAptoSiSuperaLaEdadMinima() {
        assertTrue(aventura.esApto(pasajeroNacidoEl(LocalDate.of(1990, 3, 5)), INICIO_SALIDA));
    }

    @Test
    void esAptoSiCumpleLaEdadMinimaElMismoDiaDeLaSalida() {
        assertTrue(aventura.esApto(pasajeroNacidoEl(LocalDate.of(2011, 7, 10)), INICIO_SALIDA));
    }

    @Test
    void noEsAptoSiCumpleLaEdadMinimaUnDiaDespuesDeLaSalida() {
        assertFalse(aventura.esApto(pasajeroNacidoEl(LocalDate.of(2011, 7, 11)), INICIO_SALIDA));
    }

    @Test
    void laEdadSeCalculaALaFechaDeLaSalidaYNoAHoy() {
        Pasajero pasajero = pasajeroNacidoEl(LocalDate.of(2011, 1, 1));

        assertFalse(aventura.esApto(pasajero, LocalDate.of(2026, 12, 31)));
        assertTrue(aventura.esApto(pasajero, LocalDate.of(2027, 1, 1)));
    }

    @Test
    void unTourQueNoEsDeAventuraAceptaCualquierEdad() {
        Tour cultural = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 4, 20);

        assertTrue(cultural.esApto(pasajeroNacidoEl(LocalDate.of(2026, 1, 1)), INICIO_SALIDA));
    }
}
