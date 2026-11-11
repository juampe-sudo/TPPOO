package modelo;

public class Administrador extends Empleado {

    public Administrador(String nombre, String apellido, String documento, int legajo, String telefono) {
        super(nombre, apellido, documento, legajo, telefono);
    }

    @Override
    public String getRol() {
        return "Administrador";
    }
}
