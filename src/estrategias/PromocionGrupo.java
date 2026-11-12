package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

public class PromocionGrupo implements Promocion {

    private int minimoPasajeros;
    private double porcentaje;

    public PromocionGrupo(int minimoPasajeros, double porcentaje) {
        this.minimoPasajeros = minimoPasajeros;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(Reserva r) throws ReglaNegocioException {
        if (r.cantidadPasajeros() >= minimoPasajeros) {
            double factor = porcentaje > 1 ? porcentaje / 100.0 : porcentaje;
            return r.subtotalTour() * factor;
        }
        return 0.0;
    }
}
