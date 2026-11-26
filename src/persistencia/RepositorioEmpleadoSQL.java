package persistencia;

import enums.CategoriaTour;
import modelo.Administrador;
import modelo.Coordinador;
import modelo.Empleado;
import modelo.Guia;
import modelo.Liquidacion;
import modelo.Vendedor;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RepositorioEmpleadoSQL extends RepositorioSQL<Empleado> {

    public RepositorioEmpleadoSQL() {
        super(e -> String.valueOf(e.getLegajo()));
        cargar();
    }

    @Override
    protected List<Empleado> leer() throws SQLException {
        List<Empleado> empleados = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT * FROM empleado ORDER BY legajo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                empleados.add(crearEmpleado(rs));
            }
        }
        return empleados;
    }

    @Override
    protected void escribir(Empleado e) throws SQLException {
        Double comision = null;
        Double honorario = null;
        if (e instanceof Vendedor) {
            comision = ((Vendedor) e).getComision();
        }
        if (e instanceof Guia) {
            honorario = ((Guia) e).getHonorarioPorDia();
        }
        ejecutar("INSERT INTO empleado (legajo, rol, nombre, apellido, documento, telefono, comision, "
                        + "honorario_por_dia) VALUES (?, ?, ?, ?, ?, ?, ?, ?) AS nuevo ON DUPLICATE KEY UPDATE "
                        + "rol = nuevo.rol, nombre = nuevo.nombre, apellido = nuevo.apellido, "
                        + "documento = nuevo.documento, telefono = nuevo.telefono, comision = nuevo.comision, "
                        + "honorario_por_dia = nuevo.honorario_por_dia",
                e.getLegajo(), e.getRol(), e.getNombre(), e.getApellido(), e.getDocumento(), e.getTelefono(), comision,
                honorario);
        if (e instanceof Guia) {
            escribirGuia((Guia) e);
        }
    }

    private void escribirGuia(Guia g) throws SQLException {
        for (String idioma : g.getIdiomas()) {
            ejecutar("INSERT INTO guia_idioma (legajo, idioma) VALUES (?, ?) AS nuevo "
                            + "ON DUPLICATE KEY UPDATE idioma = nuevo.idioma",
                    g.getLegajo(), idioma);
        }
        for (CategoriaTour c : g.getCategorias()) {
            ejecutar("INSERT INTO guia_categoria (legajo, categoria) VALUES (?, ?) AS nuevo "
                            + "ON DUPLICATE KEY UPDATE categoria = nuevo.categoria",
                    g.getLegajo(), c.name());
        }
        for (Liquidacion l : g.getLiquidaciones()) {
            ejecutar("INSERT INTO liquidacion (salida, dias, monto_dias, monto_adicionales) VALUES (?, ?, ?, ?) "
                    + "AS nuevo ON DUPLICATE KEY UPDATE dias = nuevo.dias, monto_dias = nuevo.monto_dias, "
                    + "monto_adicionales = nuevo.monto_adicionales",
                    l.getSalida().getCodigo(), l.getDias(), l.getMontoDias(), l.getMontoAdicionales());
        }
    }

    private Empleado crearEmpleado(ResultSet rs) throws SQLException {
        String nombre = rs.getString("nombre");
        String apellido = rs.getString("apellido");
        String documento = rs.getString("documento");
        int legajo = rs.getInt("legajo");
        String telefono = rs.getString("telefono");
        return switch (rs.getString("rol")) {
            case "Vendedor" -> new Vendedor(nombre, apellido, documento, legajo, telefono, rs.getDouble("comision"));
            case "Guía" -> new Guia(nombre, apellido, documento, legajo, telefono, leerIdiomas(legajo),
                    leerCategorias(legajo), rs.getDouble("honorario_por_dia"));
            case "Coordinador" -> new Coordinador(nombre, apellido, documento, legajo, telefono);
            default -> new Administrador(nombre, apellido, documento, legajo, telefono);
        };
    }

    private List<String> leerIdiomas(int legajo) throws SQLException {
        List<String> idiomas = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT idioma FROM guia_idioma WHERE legajo = ? ORDER BY idioma", legajo);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                idiomas.add(rs.getString("idioma"));
            }
        }
        return idiomas;
    }

    private Set<CategoriaTour> leerCategorias(int legajo) throws SQLException {
        Set<CategoriaTour> categorias = new HashSet<>();
        try (PreparedStatement ps = preparar("SELECT categoria FROM guia_categoria WHERE legajo = ?", legajo);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                categorias.add(CategoriaTour.valueOf(rs.getString("categoria")));
            }
        }
        return categorias;
    }
}
