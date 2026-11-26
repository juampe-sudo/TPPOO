package modelo;

import enums.CategoriaTour;
import enums.NivelDificultad;

import java.time.LocalDate;
import java.util.List;

public class TourAventura extends Tour {

    private static final String SEGURO_DE_VIAJE = "Seguro de viaje";

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

    @Override
    public boolean esApto(Pasajero p, LocalDate fecha) {
        return p.edadAl(fecha) >= edadMinima;
    }

    @Override
    public List<ServicioAdicional> getServiciosObligatorios() {
        List<ServicioAdicional> obligatorios = super.getServiciosObligatorios();
        for (ServicioAdicional s : getAdicionales()) {
            if (!s.isObligatorio() && s.getNombre().equalsIgnoreCase(SEGURO_DE_VIAJE)) {
                obligatorios.add(s);
            }
        }
        return obligatorios;
    }
}
