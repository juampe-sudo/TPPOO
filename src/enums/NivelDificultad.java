package enums;

public enum NivelDificultad {
    BAJO("Bajo"),
    MEDIO("Medio"),
    ALTO("Alto");

    private final String descripcion;

    NivelDificultad(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
