package modelo;

public class ServicioAdicional {

    private String nombre;
    private double precio;
    private boolean obligatorio;

    ServicioAdicional(String nombre, double precio, boolean obligatorio) {
        this.nombre = nombre;
        this.precio = precio;
        this.obligatorio = obligatorio;
    }

    public String getNombre() {
        return nombre;
    }

    public double getPrecio() {
        return precio;
    }

    public boolean isObligatorio() {
        return obligatorio;
    }
}
