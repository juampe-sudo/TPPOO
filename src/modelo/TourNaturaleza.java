package modelo;

import enums.CategoriaTour;

public class TourNaturaleza extends Tour {

    public TourNaturaleza(String codigo, String nombre, String descripcion, int duracionDias, int cupoMinimo,
                          int cupoMaximo) {
        super(codigo, nombre, descripcion, duracionDias, cupoMinimo, cupoMaximo);
    }

    @Override
    public CategoriaTour getCategoria() {
        return CategoriaTour.NATURALEZA;
    }
}
