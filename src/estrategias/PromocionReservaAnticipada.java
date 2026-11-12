package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

import java.time.temporal.ChronoUnit;

public class PromocionReservaAnticipada implements Promocion {

    private static final long serialVersionUID = 1L;

    private int diasMinimos;
    private double porcentaje;

    public PromocionReservaAnticipada(int diasMinimos, double porcentaje) {
        this.diasMinimos = diasMinimos;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(Reserva r) throws ReglaNegocioException {
        long anticipacion = ChronoUnit.DAYS.between(r.getFecha(), r.getSalida().getFechaInicio());
        if (anticipacion > diasMinimos) {
            return r.subtotalTour() * porcentaje;
        }
        return 0.0;
    }
}
