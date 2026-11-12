package modelo;

public class Administrador extends Empleado {

    private static final long serialVersionUID = 1L;

    public Administrador(String nombre, String apellido, String documento, int legajo, String telefono) {
        super(nombre, apellido, documento, legajo, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}
