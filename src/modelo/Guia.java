package modelo;

import enums.CategoriaTour;
import enums.EstadoSalida;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Guia extends Empleado {

    private List<String> idiomas;
    private Set<CategoriaTour> categorias;
    private double honorarioPorDia;
    private List<Salida> salidas;

    public Guia(String nombre, String apellido, String documento, int legajo, String telefono,
                List<String> idiomas, Set<CategoriaTour> categorias, double honorarioPorDia) {
        super(nombre, apellido, documento, legajo, telefono);
        this.idiomas = new ArrayList<>(idiomas);
        this.categorias = new HashSet<>(categorias);
        this.honorarioPorDia = honorarioPorDia;
        this.salidas = new ArrayList<>();
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

    public List<Salida> getSalidas() {
        return Collections.unmodifiableList(salidas);
    }

    @Override
    public String getRol() {
        return "Guía";
    }

    public boolean estaHabilitado(CategoriaTour c) {
        return categorias.contains(c);
    }

    public boolean estaDisponible(Salida s) {
        for (Salida existente : salidas) {
            if (existente != s && existente.getEstado() != EstadoSalida.CANCELADA && existente.seSuperponeCon(s)) {
                return false;
            }
        }
        return true;
    }

    void agregarSalida(Salida s) {
        if (!salidas.contains(s)) {
            salidas.add(s);
        }
    }
}
