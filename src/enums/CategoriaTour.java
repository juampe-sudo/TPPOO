package enums;

public enum CategoriaTour {
    AVENTURA("Aventura"),
    CULTURAL("Cultural"),
    NATURALEZA("Naturaleza");

    private final String descripcion;

    CategoriaTour(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
