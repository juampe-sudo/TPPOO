package estrategias;

public class PoliticaCancelacionAgencia implements PoliticaCancelacion {

    private static final long serialVersionUID = 1L;

    @Override
    public double porcentajeDevolucion(int dias) {
        return 1.0;
    }
}
