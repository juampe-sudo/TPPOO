package modelo;

public class Coordinador extends Empleado {

    public Coordinador(String nombre, String apellido, String documento, int legajo, String telefono) {
        super(nombre, apellido, documento, legajo, telefono);
    }

    @Override
    public String getRol() {
        return "Coordinador";
    }
}
