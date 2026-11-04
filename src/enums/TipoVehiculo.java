package enums;

public enum TipoVehiculo {
    MICRO("Micro"),
    COMBI("Combi"),
    CAMIONETA("Camioneta");

    private final String descripcion;

    TipoVehiculo(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
