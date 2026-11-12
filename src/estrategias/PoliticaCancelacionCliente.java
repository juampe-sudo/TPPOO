package estrategias;

public class PoliticaCancelacionCliente implements PoliticaCancelacion {

    private static final long serialVersionUID = 1L;

    @Override
    public double porcentajeDevolucion(int dias) {
        if (dias >= 30) {
            return 0.90;
        }
        if (dias >= 15) {
            return 0.50;
        }
        if (dias >= 7) {
            return 0.20;
        }
        return 0.0;
    }
}
