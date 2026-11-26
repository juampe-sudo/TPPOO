package persistencia;

import enums.CategoriaTour;
import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Actividad;
import modelo.Proveedor;
import modelo.ServicioAdicional;
import modelo.Tarifa;
import modelo.Tour;
import modelo.TourAventura;
import modelo.TourCultural;
import modelo.TourNaturaleza;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RepositorioTourSQL extends RepositorioSQL<Tour> {

    private Repositorio<Proveedor> proveedores;

    public RepositorioTourSQL(Repositorio<Proveedor> proveedores) {
        super(Tour::getCodigo);
        this.proveedores = proveedores;
        cargar();
    }

    @Override
    protected List<Tour> leer() throws SQLException, ReglaNegocioException {
        List<Tour> tours = new ArrayList<>();
        try (PreparedStatement ps = preparar("SELECT * FROM tour ORDER BY codigo");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tours.add(crearTour(rs));
            }
        }
        for (Tour t : tours) {
            leerActividades(t);
            leerTarifas(t);
            leerServicios(t);
        }
        return tours;
    }

    @Override
    protected void escribir(Tour t) throws SQLException {
        String nivel = null;
        Integer edadMinima = null;
        if (t instanceof TourAventura) {
            TourAventura aventura = (TourAventura) t;
            nivel = aventura.getNivelDificultad().name();
            edadMinima = aventura.getEdadMinima();
        }
        ejecutar("INSERT INTO tour (codigo, categoria, nombre, descripcion, duracion_dias, cupo_minimo, cupo_maximo, "
                        + "nivel_dificultad, edad_minima) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) AS nuevo "
                        + "ON DUPLICATE KEY UPDATE nombre = nuevo.nombre, descripcion = nuevo.descripcion, "
                        + "duracion_dias = nuevo.duracion_dias, cupo_minimo = nuevo.cupo_minimo, "
                        + "cupo_maximo = nuevo.cupo_maximo, nivel_dificultad = nuevo.nivel_dificultad, "
                        + "edad_minima = nuevo.edad_minima",
                t.getCodigo(), t.getCategoria().name(), t.getNombre(), t.getDescripcion(), t.getDuracionDias(),
                t.getCupoMinimo(), t.getCupoMaximo(), nivel, edadMinima);
        for (Actividad a : t.getActividades()) {
            String ein = a.getProveedor() == null ? null : a.getProveedor().getEin();
            ejecutar("INSERT INTO actividad (tour, dia, orden, horario, descripcion, lugar, tipo, proveedor) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) AS nuevo ON DUPLICATE KEY UPDATE "
                            + "horario = nuevo.horario, descripcion = nuevo.descripcion, lugar = nuevo.lugar, "
                            + "tipo = nuevo.tipo, proveedor = nuevo.proveedor",
                    t.getCodigo(), a.getDia(), a.getOrden(), a.getHorario(), a.getDescripcion(), a.getLugar(),
                    a.getTipo().name(), ein);
        }
        for (Tarifa tarifa : t.getTarifas()) {
            ejecutar("INSERT INTO tarifa (tour, vigencia_desde, vigencia_hasta, temporada, precio_base) "
                            + "VALUES (?, ?, ?, ?, ?) AS nuevo ON DUPLICATE KEY UPDATE "
                            + "vigencia_hasta = nuevo.vigencia_hasta, temporada = nuevo.temporada, "
                            + "precio_base = nuevo.precio_base",
                    t.getCodigo(), tarifa.getVigenciaDesde(), tarifa.getVigenciaHasta(), tarifa.getTemporada().name(),
                    tarifa.getPrecioBase());
        }
        for (ServicioAdicional s : t.getAdicionales()) {
            ejecutar("INSERT INTO servicio_adicional (tour, nombre, precio, obligatorio) VALUES (?, ?, ?, ?) "
                    + "AS nuevo ON DUPLICATE KEY UPDATE precio = nuevo.precio, obligatorio = nuevo.obligatorio",
                    t.getCodigo(), s.getNombre(), s.getPrecio(), s.isObligatorio());
        }
    }

    private Tour crearTour(ResultSet rs) throws SQLException {
        String codigo = rs.getString("codigo");
        String nombre = rs.getString("nombre");
        String descripcion = rs.getString("descripcion");
        int duracion = rs.getInt("duracion_dias");
        int cupoMinimo = rs.getInt("cupo_minimo");
        int cupoMaximo = rs.getInt("cupo_maximo");
        return switch (CategoriaTour.valueOf(rs.getString("categoria"))) {
            case AVENTURA -> new TourAventura(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo,
                    NivelDificultad.valueOf(rs.getString("nivel_dificultad")), rs.getInt("edad_minima"));
            case CULTURAL -> new TourCultural(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo);
            case NATURALEZA -> new TourNaturaleza(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo);
        };
    }

    private void leerActividades(Tour t) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = preparar("SELECT * FROM actividad WHERE tour = ? ORDER BY dia, orden",
                t.getCodigo());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String ein = rs.getString("proveedor");
                t.agregarActividad(rs.getInt("dia"), rs.getInt("orden"), rs.getObject("horario", LocalTime.class),
                        rs.getString("descripcion"), rs.getString("lugar"), TipoActividad.valueOf(rs.getString("tipo")),
                        ein == null ? null : proveedores.buscar(ein));
            }
        }
    }

    private void leerTarifas(Tour t) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = preparar("SELECT * FROM tarifa WHERE tour = ? ORDER BY vigencia_desde",
                t.getCodigo());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                t.agregarTarifa(Temporada.valueOf(rs.getString("temporada")),
                        rs.getObject("vigencia_desde", LocalDate.class), rs.getObject("vigencia_hasta", LocalDate.class),
                        rs.getDouble("precio_base"));
            }
        }
    }

    private void leerServicios(Tour t) throws SQLException, ReglaNegocioException {
        try (PreparedStatement ps = preparar("SELECT * FROM servicio_adicional WHERE tour = ? ORDER BY nombre",
                t.getCodigo());
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                t.agregarServicio(rs.getString("nombre"), rs.getDouble("precio"), rs.getBoolean("obligatorio"));
            }
        }
    }
}
