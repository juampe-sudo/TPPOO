package modelo;

import enums.MedioPago;

import java.time.LocalDate;

public class Cancelacion {

    private LocalDate fecha;
    private String motivo;
    private double importeDevuelto;
    private MedioPago medio;

    Cancelacion(LocalDate fecha, String motivo, double importeDevuelto, MedioPago medio) {
        this.fecha = fecha;
        this.motivo = motivo;
        this.importeDevuelto = importeDevuelto;
        this.medio = medio;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public double getImporteDevuelto() {
        return importeDevuelto;
    }

    public MedioPago getMedio() {
        return medio;
    }
}
