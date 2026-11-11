package modelo;

import enums.TipoActividad;

import java.time.LocalTime;

public class Actividad {

    private int dia;
    private int orden;
    private LocalTime horario;
    private String descripcion;
    private String lugar;
    private TipoActividad tipo;
    private Proveedor proveedor;

    Actividad(int dia, int orden, LocalTime horario, String descripcion, String lugar,
              TipoActividad tipo, Proveedor proveedor) {
        this.dia = dia;
        this.orden = orden;
        this.horario = horario;
        this.descripcion = descripcion;
        this.lugar = lugar;
        this.tipo = tipo;
        this.proveedor = proveedor;
    }

    public int getDia() {
        return dia;
    }

    public int getOrden() {
        return orden;
    }

    public LocalTime getHorario() {
        return horario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getLugar() {
        return lugar;
    }

    public TipoActividad getTipo() {
        return tipo;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }
}
