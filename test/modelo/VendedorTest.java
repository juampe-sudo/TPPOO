package modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VendedorTest {

    @Test
    void comisionVendedor() {
        Vendedor vendedor = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);

        assertEquals(50.0, vendedor.calcularComision(1000.0), 0.001);
        assertEquals(0.0, vendedor.calcularComision(0), 0.001);
    }
}
