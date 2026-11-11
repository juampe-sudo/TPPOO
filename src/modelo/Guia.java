package modelo;

import enums.CategoriaTour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Guia extends Empleado {

    private List<String> idiomas;
    private Set<CategoriaTour> categorias;
    private double honorarioPorDia;

    public Guia(String nombre, String apellido, String documento, int legajo, String telefono,
                List<String> idiomas, Set<CategoriaTour> categorias, double honorarioPorDia) {
        super(nombre, apellido, documento, legajo, telefono);
        this.idiomas = new ArrayList<>(idiomas);
        this.categorias = new HashSet<>(categorias);
        this.honorarioPorDia = honorarioPorDia;
    }

    public List<String> getIdiomas() {
        return Collections.unmodifiableList(idiomas);
    }

    public Set<CategoriaTour> getCategorias() {
        return Collections.unmodifiableSet(categorias);
    }

    public double getHonorarioPorDia() {
        return honorarioPorDia;
    }

    @Override
    public String getRol() {
        return "Guía";
    }
}
