package modelo;

public abstract class Empleado extends Persona {

    private static final long serialVersionUID = 1L;

    protected int legajo;
    protected String telefono;

    protected Empleado(String nombre, String apellido, String documento, int legajo, String telefono) {
        super(nombre, apellido, documento);
        this.legajo = legajo;
        this.telefono = telefono;
    }

    public int getLegajo() {
        return legajo;
    }

    public String getTelefono() {
        return telefono;
    }

    public abstract String getRol();
}
