package persistencia;

import enums.TipoVehiculo;
import modelo.Vehiculo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RepositorioVehiculoSQL extends RepositorioSQL<Vehiculo> {

    public RepositorioVehiculoSQL() {
        super(Vehiculo::getPatente);
        cargar();
    }

    @Override
    protected List<Vehiculo> leer() throws SQLException {
        List<Vehiculo> vehiculos = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT patente, capacidad, tipo FROM vehiculo ORDER BY patente");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                vehiculos.add(new Vehiculo(rs.getString("patente"), rs.getInt("capacidad"),
                        TipoVehiculo.valueOf(rs.getString("tipo"))));
            }
        }
        return vehiculos;
    }

    @Override
    protected void escribir(Vehiculo v) throws SQLException {
        ejecutar("INSERT INTO vehiculo (patente, capacidad, tipo) VALUES (?, ?, ?) AS nuevo "
                        + "ON DUPLICATE KEY UPDATE capacidad = nuevo.capacidad, tipo = nuevo.tipo",
                v.getPatente(), v.getCapacidad(), v.getTipo().name());
    }
}
