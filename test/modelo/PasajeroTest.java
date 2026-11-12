package modelo;

import enums.TipoPasajero;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasajeroTest {

    private static final LocalDate INICIO_SALIDA = LocalDate.of(2027, 7, 10);

    private Pasajero pasajero(LocalDate nacimiento, TipoPasajero tipo) {
        return new Pasajero("Ana", "Pérez", "30111222", nacimiento, tipo, "Juan Pérez 1155550000", "");
    }

    @Test
    void importeSegunTipoYEdad() {
        assertEquals(1000.0, pasajero(LocalDate.of(1990, 3, 5), TipoPasajero.ADULTO)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(600.0, pasajero(LocalDate.of(2019, 2, 1), TipoPasajero.MENOR)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(0.0, pasajero(LocalDate.of(2025, 9, 15), TipoPasajero.MENOR)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(900.0, pasajero(LocalDate.of(1955, 11, 20), TipoPasajero.JUBILADO)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
    }

    @Test
    void importeEnLosBordesDeEdad() {
        assertEquals(0.0, pasajero(LocalDate.of(2024, 7, 11), TipoPasajero.MENOR)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(600.0, pasajero(LocalDate.of(2024, 7, 10), TipoPasajero.MENOR)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(600.0, pasajero(LocalDate.of(2015, 7, 11), TipoPasajero.MENOR)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
        assertEquals(1000.0, pasajero(LocalDate.of(2015, 7, 10), TipoPasajero.ADULTO)
                .calcularImporte(1000.0, INICIO_SALIDA), 0.001);
    }

    @Test
    void noPermiteServicioRepetido() throws ReglaNegocioException {
        Pasajero ana = pasajero(LocalDate.of(1990, 3, 5), TipoPasajero.ADULTO);
        ana.agregarServicio(new ServicioAdicional("Seguro de viaje", 80.0, true));

        assertThrows(ReglaNegocioException.class,
                () -> ana.agregarServicio(new ServicioAdicional("Seguro de viaje", 80.0, true)));
        assertEquals(1, ana.getServicios().size());
    }

    @Test
    void totalServicios() throws ReglaNegocioException {
        Pasajero ana = pasajero(LocalDate.of(1990, 3, 5), TipoPasajero.ADULTO);
        assertEquals(0.0, ana.totalServicios(), 0.001);

        ana.agregarServicio(new ServicioAdicional("Seguro de viaje", 80.0, true));
        ana.agregarServicio(new ServicioAdicional("Alquiler de crampones", 35.0, false));

        assertEquals(115.0, ana.totalServicios(), 0.001);
    }
}
