package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

import java.io.Serializable;

public interface Promocion extends Serializable {

    double calcularDescuento(Reserva r) throws ReglaNegocioException;
}
