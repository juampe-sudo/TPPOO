package persistencia;

import modelo.Proveedor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioProveedorSQL extends RepositorioSQL<Proveedor> {

    public RepositorioProveedorSQL() {
        super(Proveedor::getEin);
        cargar();
    }

    @Override
    protected List<Proveedor> leer() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT ein, nombre, contacto, servicio FROM proveedor ORDER BY ein");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                proveedores.add(new Proveedor(rs.getString("nombre"), rs.getString("ein"), rs.getString("contacto"),
                        rs.getString("servicio")));
            }
        }
        return proveedores;
    }

    @Override
    protected void escribir(Proveedor p) throws SQLException {
        ejecutar("INSERT INTO proveedor (ein, nombre, contacto, servicio) VALUES (?, ?, ?, ?) AS nuevo "
                        + "ON DUPLICATE KEY UPDATE nombre = nuevo.nombre, contacto = nuevo.contacto, "
                        + "servicio = nuevo.servicio",
                p.getEin(), p.getNombre(), p.getContacto(), p.getServicio());
    }
}
