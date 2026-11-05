package enums;

public enum Temporada {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta");

    private final String descripcion;

    Temporada(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
