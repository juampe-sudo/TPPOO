package modelo;

public class Coordinador extends Empleado {

    private static final long serialVersionUID = 1L;

    public Coordinador(String nombre, String apellido, String documento, int legajo, String telefono) {
        super(nombre, apellido, documento, legajo, telefono);
    }

    @Override
    public String getRol() {
        return "Coordinador";
    }
}
