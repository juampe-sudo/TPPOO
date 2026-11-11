package modelo;

import enums.CategoriaTour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Tour {

    private String codigo;
    private String nombre;
    private String descripcion;
    private int duracionDias;
    private int cupoMinimo;
    private int cupoMaximo;
    private List<Actividad> actividades;
    private List<Tarifa> tarifas;
    private List<ServicioAdicional> adicionales;

    protected Tour(String codigo, String nombre, String descripcion, int duracionDias,
                   int cupoMinimo, int cupoMaximo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionDias = duracionDias;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
        this.actividades = new ArrayList<>();
        this.tarifas = new ArrayList<>();
        this.adicionales = new ArrayList<>();
    }

    public abstract CategoriaTour getCategoria();

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Actividad> getActividades() {
        return Collections.unmodifiableList(actividades);
    }

    public List<Tarifa> getTarifas() {
        return Collections.unmodifiableList(tarifas);
    }

    public List<ServicioAdicional> getAdicionales() {
        return Collections.unmodifiableList(adicionales);
    }
}
