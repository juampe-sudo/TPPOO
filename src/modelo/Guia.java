package modelo;

import enums.CategoriaTour;
import enums.EstadoSalida;
import excepciones.ReglaNegocioException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Guia extends Empleado {

    private static final long serialVersionUID = 1L;

    private List<String> idiomas;
    private Set<CategoriaTour> categorias;
    private double honorarioPorDia;
    private List<Salida> salidas;
    private List<Liquidacion> liquidaciones;

    public Guia(String nombre, String apellido, String documento, int legajo, String telefono,
                List<String> idiomas, Set<CategoriaTour> categorias, double honorarioPorDia) {
        super(nombre, apellido, documento, legajo, telefono);
        this.idiomas = new ArrayList<>(idiomas);
        this.categorias = new HashSet<>(categorias);
        this.honorarioPorDia = honorarioPorDia;
        this.salidas = new ArrayList<>();
        this.liquidaciones = new ArrayList<>();
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

    public List<Liquidacion> getLiquidaciones() {
        return Collections.unmodifiableList(liquidaciones);
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

    public Liquidacion liquidar(Salida s) throws ReglaNegocioException {
        if (s == null) {
            throw new ReglaNegocioException("Hay que indicar la salida a liquidar");
        }
        if (s.getEstado() != EstadoSalida.FINALIZADA) {
            throw new ReglaNegocioException("La salida " + s.getCodigo() + " todavía no está finalizada");
        }
        if (s.getGuia() != this) {
            throw new ReglaNegocioException("La salida " + s.getCodigo() + " no la guió " + getNombreCompleto());
        }
        for (Liquidacion l : liquidaciones) {
            if (l.getSalida().getCodigo().equals(s.getCodigo())) {
                return l;
            }
        }
        int dias = s.diasDeDuracion();
        double montoDias = dias * honorarioPorDia;
        double montoAdicionales = s.totalAdicionalesVendidos() * 0.05;
        Liquidacion liquidacion = new Liquidacion(this, s, dias, montoDias, montoAdicionales);
        liquidaciones.add(liquidacion);
        return liquidacion;
    }

    public void revincular(List<Salida> salidas) {
        this.salidas = new ArrayList<>(salidas);
    }

    void agregarSalida(Salida s) {
        if (!salidas.contains(s)) {
            salidas.add(s);
        }
    }
}
