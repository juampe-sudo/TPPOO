package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

public interface Promocion {

    double calcularDescuento(Reserva r) throws ReglaNegocioException;
}
