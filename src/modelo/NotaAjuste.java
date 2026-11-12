package modelo;

import java.io.Serializable;
import java.time.LocalDate;

public class NotaAjuste implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate fecha;
    private String descripcion;
    private double importe;

    NotaAjuste(LocalDate fecha, String descripcion, double importe) {
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
