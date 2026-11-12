package modelo;

import enums.Temporada;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TarifaTest {

    private final Tarifa tarifa = new Tarifa(Temporada.ALTA,
            LocalDate.of(2026, 12, 1), LocalDate.of(2027, 2, 28), 1500.0);

    @Test
    void estaVigenteEnUnaFechaIntermedia() {
        assertTrue(tarifa.estaVigente(LocalDate.of(2027, 1, 15)));
    }

    @Test
    void estaVigenteElPrimerDia() {
        assertTrue(tarifa.estaVigente(LocalDate.of(2026, 12, 1)));
    }

    @Test
    void estaVigenteElUltimoDia() {
        assertTrue(tarifa.estaVigente(LocalDate.of(2027, 2, 28)));
    }

    @Test
    void noEstaVigenteElDiaAnteriorAlInicio() {
        assertFalse(tarifa.estaVigente(LocalDate.of(2026, 11, 30)));
    }

    @Test
    void noEstaVigenteElDiaPosteriorAlFin() {
        assertFalse(tarifa.estaVigente(LocalDate.of(2027, 3, 1)));
    }
}
