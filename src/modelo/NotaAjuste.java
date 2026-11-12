package modelo;

import java.time.LocalDate;

public class NotaAjuste {

    private LocalDate fecha;
    private String descripcion;
    private double importe;

    public NotaAjuste(LocalDate fecha, String descripcion, double importe) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.importe = importe;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public double getImporte() {
        return importe;
    }
}
