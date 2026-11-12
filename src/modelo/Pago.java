package modelo;

import enums.MedioPago;

import java.io.Serializable;
import java.time.LocalDate;

public class Pago implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate fecha;
    private double importe;
    private MedioPago medio;
    private int cuotas;

    Pago(LocalDate fecha, double importe, MedioPago medio, int cuotas) {
        this.fecha = fecha;
        this.importe = importe;
        this.medio = medio;
        this.cuotas = cuotas;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public double getImporte() {
        return importe;
    }

    public MedioPago getMedio() {
        return medio;
    }

    public int getCuotas() {
        return cuotas;
    }

    public double getRecargo() {
        if (medio == MedioPago.TARJETA && cuotas > 1) {
            return importe * 0.07;
        }
        return 0.0;
    }
}
