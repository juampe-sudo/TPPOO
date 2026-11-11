package modelo;

import enums.CategoriaTour;
import enums.NivelDificultad;

public class TourAventura extends Tour {

    private NivelDificultad nivelDificultad;
    private int edadMinima;

    public TourAventura(String codigo, String nombre, String descripcion, int duracionDias, int cupoMinimo,
                        int cupoMaximo, NivelDificultad nivelDificultad, int edadMinima) {
        super(codigo, nombre, descripcion, duracionDias, cupoMinimo, cupoMaximo);
        this.nivelDificultad = nivelDificultad;
        this.edadMinima = edadMinima;
    }

    @Override
    public CategoriaTour getCategoria() {
        return CategoriaTour.AVENTURA;
    }

    public NivelDificultad getNivelDificultad() {
        return nivelDificultad;
    }

    public int getEdadMinima() {
        return edadMinima;
    }
}
