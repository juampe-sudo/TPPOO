package enums;

public enum TipoPasajero {
    ADULTO("Adulto"),
    MENOR("Menor"),
    JUBILADO("Jubilado");

    private final String descripcion;

    TipoPasajero(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
