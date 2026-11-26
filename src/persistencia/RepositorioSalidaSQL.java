package persistencia;

import enums.EstadoSalida;
import modelo.Empleado;
import modelo.Guia;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RepositorioSalidaSQL extends RepositorioSQL<Salida> {

    private Repositorio<Tour> tours;
    private Repositorio<Empleado> empleados;
    private Repositorio<Vehiculo> vehiculos;

    public RepositorioSalidaSQL(Repositorio<Tour> tours, Repositorio<Empleado> empleados,
                                Repositorio<Vehiculo> vehiculos) {
        super(Salida::getCodigo);
        this.tours = tours;
        this.empleados = empleados;
        this.vehiculos = vehiculos;
        cargar();
    }

    @Override
    protected List<Salida> leer() throws SQLException {
        List<Salida> salidas = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT * FROM salida ORDER BY codigo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Salida s = new Salida(rs.getString("codigo"), tours.buscar(rs.getString("tour")),
                        rs.getObject("fecha_inicio", LocalDate.class), rs.getObject("fecha_fin", LocalDate.class),
                        rs.getInt("cupo_minimo"), rs.getInt("cupo_maximo"));
                String legajo = rs.getString("guia");
                String patente = rs.getString("vehiculo");
                s.restaurar(EstadoSalida.valueOf(rs.getString("estado")),
                        legajo == null ? null : (Guia) empleados.buscar(legajo),
                        patente == null ? null : vehiculos.buscar(patente), rs.getBoolean("lista_emitida"));
                leerLiquidacion(s);
                salidas.add(s);
            }
        }
        return salidas;
    }

    @Override
    protected void escribir(Salida s) throws SQLException {
        Integer guia = s.getGuia() == null ? null : s.getGuia().getLegajo();
        String vehiculo = s.getVehiculo() == null ? null : s.getVehiculo().getPatente();
        ejecutar("INSERT INTO salida (codigo, tour, fecha_inicio, fecha_fin, cupo_minimo, cupo_maximo, estado, guia, "
                        + "vehiculo, lista_emitida) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) AS nuevo "
                        + "ON DUPLICATE KEY UPDATE estado = nuevo.estado, guia = nuevo.guia, "
                        + "vehiculo = nuevo.vehiculo, lista_emitida = nuevo.lista_emitida",
                s.getCodigo(), s.getTour().getCodigo(), s.getFechaInicio(), s.getFechaFin(), s.getCupoMinimo(),
                s.getCupoMaximo(), s.getEstado().name(), guia, vehiculo, s.isListaEmitida());
    }

    private void leerLiquidacion(Salida s) throws SQLException {
        try (PreparedStatement ps = preparar("SELECT * FROM liquidacion WHERE salida = ?", s.getCodigo());
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                s.getGuia().restaurarLiquidacion(s, rs.getInt("dias"), rs.getDouble("monto_dias"),
                        rs.getDouble("monto_adicionales"));
            }
        }
    }
}
