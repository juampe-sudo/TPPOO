package persistencia;

import modelo.Cliente;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioClienteSQL extends RepositorioSQL<Cliente> {

    public RepositorioClienteSQL() {
        super(Cliente::getDocumento);
        cargar();
    }

    @Override
    protected List<Cliente> leer() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT documento, nombre, apellido, telefono, email, pais_residencia "
                + "FROM cliente ORDER BY documento");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(new Cliente(rs.getString("nombre"), rs.getString("apellido"), rs.getString("documento"),
                        rs.getString("telefono"), rs.getString("email"), rs.getString("pais_residencia")));
            }
        }
        return clientes;
    }

    @Override
    protected void escribir(Cliente c) throws SQLException {
        ejecutar("INSERT INTO cliente (documento, nombre, apellido, telefono, email, pais_residencia) "
                        + "VALUES (?, ?, ?, ?, ?, ?) AS nuevo ON DUPLICATE KEY UPDATE nombre = nuevo.nombre, "
                        + "apellido = nuevo.apellido, telefono = nuevo.telefono, email = nuevo.email, "
                        + "pais_residencia = nuevo.pais_residencia",
                c.getDocumento(), c.getNombre(), c.getApellido(), c.getTelefono(), c.getEmail(), c.getPaisResidencia());
    }
}
