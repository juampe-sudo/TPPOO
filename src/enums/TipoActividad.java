package enums;

public enum TipoActividad {
    VISITA("Visita"),
    TRASLADO("Traslado"),
    COMIDA("Comida"),
    ALOJAMIENTO("Alojamiento");

    private final String descripcion;

    TipoActividad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
