package modelo;

import enums.Temporada;

import java.io.Serializable;
import java.time.LocalDate;

public class Tarifa implements Serializable {

    private static final long serialVersionUID = 1L;

    private Temporada temporada;
    private LocalDate vigenciaDesde;
    private LocalDate vigenciaHasta;
    private double precioBase;

    Tarifa(Temporada temporada, LocalDate vigenciaDesde, LocalDate vigenciaHasta, double precioBase) {
        this.temporada = temporada;
        this.vigenciaDesde = vigenciaDesde;
        this.vigenciaHasta = vigenciaHasta;
        this.precioBase = precioBase;
    }

    public Temporada getTemporada() {
        return temporada;
    }

    public LocalDate getVigenciaDesde() {
        return vigenciaDesde;
    }

    public LocalDate getVigenciaHasta() {
        return vigenciaHasta;
    }

    public double getPrecioBase() {
        return precioBase;
    }

    public boolean estaVigente(LocalDate fecha) {
        return !fecha.isBefore(vigenciaDesde) && !fecha.isAfter(vigenciaHasta);
    }
}
