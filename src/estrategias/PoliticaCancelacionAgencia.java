package estrategias;

public class PoliticaCancelacionAgencia implements PoliticaCancelacion {

    @Override
    public double porcentajeDevolucion(int dias) {
        return 1.0;
    }
}
