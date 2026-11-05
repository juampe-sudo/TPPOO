package enums;

public enum EstadoSalida {
    PROGRAMADA("Programada"),
    CONFIRMADA("Confirmada"),
    COMPLETA("Completa"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String descripcion;

    EstadoSalida(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
