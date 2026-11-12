package estrategias;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PoliticaCancelacionTest {

    private final PoliticaCancelacion cliente = new PoliticaCancelacionCliente();
    private final PoliticaCancelacion agencia = new PoliticaCancelacionAgencia();

    @Test
    void escalaDevolucionCliente() {
        assertEquals(0.90, cliente.porcentajeDevolucion(60), 0.001);
        assertEquals(0.50, cliente.porcentajeDevolucion(20), 0.001);
        assertEquals(0.20, cliente.porcentajeDevolucion(10), 0.001);
        assertEquals(0.0, cliente.porcentajeDevolucion(3), 0.001);
    }

    @Test
    void bordesEscalaCliente() {
        assertEquals(0.90, cliente.porcentajeDevolucion(30), 0.001);
        assertEquals(0.50, cliente.porcentajeDevolucion(29), 0.001);
        assertEquals(0.50, cliente.porcentajeDevolucion(15), 0.001);
        assertEquals(0.20, cliente.porcentajeDevolucion(14), 0.001);
        assertEquals(0.20, cliente.porcentajeDevolucion(7), 0.001);
        assertEquals(0.0, cliente.porcentajeDevolucion(6), 0.001);
    }

    @Test
    void devolucionAgenciaSiempreTotal() {
        assertEquals(1.0, agencia.porcentajeDevolucion(60), 0.001);
        assertEquals(1.0, agencia.porcentajeDevolucion(10), 0.001);
        assertEquals(1.0, agencia.porcentajeDevolucion(0), 0.001);
    }
}
