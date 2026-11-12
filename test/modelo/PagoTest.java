package modelo;

import enums.MedioPago;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PagoTest {

    private static final LocalDate HOY = LocalDate.of(2027, 3, 1);

    @Test
    void recargoTarjetaEnCuotas() {
        assertEquals(70.0, new Pago(HOY, 1000.0, MedioPago.TARJETA, 3).getRecargo(), 0.001);
        assertEquals(35.0, new Pago(HOY, 500.0, MedioPago.TARJETA, 2).getRecargo(), 0.001);
    }

    @Test
    void tarjetaEnUnaCuotaSinRecargo() {
        assertEquals(0.0, new Pago(HOY, 1000.0, MedioPago.TARJETA, 1).getRecargo(), 0.001);
    }

    @Test
    void otrosMediosSinRecargo() {
        assertEquals(0.0, new Pago(HOY, 1000.0, MedioPago.EFECTIVO, 1).getRecargo(), 0.001);
        assertEquals(0.0, new Pago(HOY, 1000.0, MedioPago.TRANSFERENCIA, 3).getRecargo(), 0.001);
    }
}
