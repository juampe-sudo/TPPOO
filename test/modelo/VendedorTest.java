package modelo;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VendedorTest {

    @Test
    void comisionVendedor() {
        Vendedor fraccion = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);
        Vendedor porcentaje = new Vendedor("Marta", "Sosa", "26333444", 102, "1144445555", 5);

        assertEquals(50.0, fraccion.calcularComision(1000.0), 0.001);
        assertEquals(50.0, porcentaje.calcularComision(1000.0), 0.001);
        assertEquals(0.0, fraccion.calcularComision(0), 0.001);
    }
}
