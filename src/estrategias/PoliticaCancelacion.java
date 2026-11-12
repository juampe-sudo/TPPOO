package estrategias;

import java.io.Serializable;

public interface PoliticaCancelacion extends Serializable {

    double porcentajeDevolucion(int dias);
}
