package modelo;

public class Vendedor extends Empleado {

    private double comision;

    public Vendedor(String nombre, String apellido, String documento, int legajo, String telefono,
                    double comision) {
        super(nombre, apellido, documento, legajo, telefono);
        this.comision = comision;
    }

    public double getComision() {
        return comision;
    }

    @Override
    public String getRol() {
        return "Vendedor";
    }
}
