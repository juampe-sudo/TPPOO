package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

public class PromocionGrupo implements Promocion {

    private static final long serialVersionUID = 1L;

    private int minimoPasajeros;
    private double porcentaje;

    public PromocionGrupo(int minimoPasajeros, double porcentaje) {
        this.minimoPasajeros = minimoPasajeros;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(Reserva r) throws ReglaNegocioException {
        if (r.cantidadPasajeros() >= minimoPasajeros) {
            return r.subtotalTour() * porcentaje;
        }
        return 0.0;
    }
}
