package persistencia;

import excepciones.ReglaNegocioException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public abstract class RepositorioSQL<T> implements Repositorio<T> {

    private Map<String, T> datos;
    private Function<T, String> clave;

    protected RepositorioSQL(Function<T, String> clave) {
        this.datos = new LinkedHashMap<>();
        this.clave = clave;
    }

    @Override
    public void guardar(T obj) {
        Connection conexion = ConexionBD.getInstancia().getConexion();
        try {
            escribir(obj);
            conexion.commit();
        } catch (SQLException e) {
            try {
                conexion.rollback();
            } catch (SQLException otra) {
                e.addSuppressed(otra);
            }
            throw new IllegalStateException("No se pudo guardar " + clave.apply(obj) + " en la base de datos", e);
        }
        datos.put(clave.apply(obj), obj);
    }

    @Override
    public T buscar(String clave) {
        return datos.get(clave);
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(datos.values());
    }

    protected void cargar() {
        try {
            for (T obj : leer()) {
                datos.put(clave.apply(obj), obj);
            }
        } catch (SQLException | ReglaNegocioException e) {
            throw new IllegalStateException("No se pudieron leer los datos de la base", e);
        }
    }

    protected PreparedStatement preparar(String sql, Object... valores) throws SQLException {
        PreparedStatement ps = ConexionBD.getInstancia().getConexion().prepareStatement(sql);
        try {
            for (int i = 0; i < valores.length; i++) {
                ps.setObject(i + 1, valores[i]);
            }
        } catch (SQLException e) {
            ps.close();
            throw e;
        }
        return ps;
    }

    protected void ejecutar(String sql, Object... valores) throws SQLException {
        try (PreparedStatement ps = preparar(sql, valores)) {
            ps.executeUpdate();
        }
    }

    protected abstract List<T> leer() throws SQLException, ReglaNegocioException;

    protected abstract void escribir(T obj) throws SQLException;
}
