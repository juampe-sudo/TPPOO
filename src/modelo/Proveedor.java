package modelo;

public class Proveedor {

    private String nombre;
    private String ein;
    private String contacto;
    private String servicio;

    public Proveedor(String nombre, String ein, String contacto, String servicio) {
        this.nombre = nombre;
        this.ein = ein;
        this.contacto = contacto;
        this.servicio = servicio;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEin() {
        return ein;
    }

    public String getContacto() {
        return contacto;
    }

    public String getServicio() {
        return servicio;
    }
}
