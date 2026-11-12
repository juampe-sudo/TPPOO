package modelo;

public class Vendedor extends Empleado {

    private static final long serialVersionUID = 1L;

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

    public double calcularComision(double monto) {
        return monto * comision;
    }
}
