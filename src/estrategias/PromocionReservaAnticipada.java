package estrategias;

import excepciones.ReglaNegocioException;
import modelo.Reserva;

import java.time.temporal.ChronoUnit;

public class PromocionReservaAnticipada implements Promocion {

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
            double factor = porcentaje > 1 ? porcentaje / 100.0 : porcentaje;
            return r.subtotalTour() * factor;
        }
        return 0.0;
    }
}
