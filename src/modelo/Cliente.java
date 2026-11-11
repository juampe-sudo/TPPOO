package modelo;

public class Cliente extends Persona {

    private String telefono;
    private String email;
    private String paisResidencia;

    public Cliente(String nombre, String apellido, String documento,
                   String telefono, String email, String paisResidencia) {
        super(nombre, apellido, documento);
        this.telefono = telefono;
        this.email = email;
        this.paisResidencia = paisResidencia;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getEmail() {
        return email;
    }

    public String getPaisResidencia() {
        return paisResidencia;
    }
}
