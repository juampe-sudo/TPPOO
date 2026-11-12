package modelo;

import java.io.Serializable;

public class Liquidacion implements Serializable {

    private static final long serialVersionUID = 1L;

    private Guia guia;
    private Salida salida;
    private int dias;
    private double montoDias;
    private double montoAdicionales;

    public Liquidacion(Guia guia, Salida salida, int dias, double montoDias, double montoAdicionales) {
        this.guia = guia;
        this.salida = salida;
        this.dias = dias;
        this.montoDias = montoDias;
        this.montoAdicionales = montoAdicionales;
    }

    public Guia getGuia() {
        return guia;
    }

    public Salida getSalida() {
        return salida;
    }

    public int getDias() {
        return dias;
    }

    public double getMontoDias() {
        return montoDias;
    }

    public double getMontoAdicionales() {
        return montoAdicionales;
    }

    public double getTotal() {
        return montoDias + montoAdicionales;
    }
}
