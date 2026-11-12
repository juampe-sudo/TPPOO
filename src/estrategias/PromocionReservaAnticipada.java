package estrategias;

import modelo.Reserva;

public class PromocionReservaAnticipada implements Promocion {

    private int diasMinimos;
    private double porcentaje;

    public PromocionReservaAnticipada(int diasMinimos, double porcentaje) {
        this.diasMinimos = diasMinimos;
        this.porcentaje = porcentaje;
    }

    @Override
    public double calcularDescuento(Reserva r) {
        return 0;
    }
}
