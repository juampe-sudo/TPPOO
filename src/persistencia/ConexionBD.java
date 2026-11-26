package persistencia;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ConexionBD {

    private static final String CONFIGURACION = "data/bd.properties";
    private static final String ESQUEMA = "data/esquema.sql";

    private static ConexionBD instancia;

    private Connection conexion;

    private ConexionBD() {
        try {
            Properties config = new Properties();
            try (Reader lector = Files.newBufferedReader(Path.of(CONFIGURACION), StandardCharsets.UTF_8)) {
                config.load(lector);
            }
            String url = System.getProperty("agencia.bd", config.getProperty("url"));
            conexion = DriverManager.getConnection(url, config.getProperty("usuario"), config.getProperty("clave"));
            crearTablas();
            conexion.setAutoCommit(false);
        } catch (SQLException | IOException e) {
            throw new IllegalStateException("No se pudo conectar con la base de datos MySQL: " + e.getMessage(), e);
        }
    }

    public static ConexionBD getInstancia() {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    public Connection getConexion() {
        return conexion;
    }

    public static void cerrar() {
        if (instancia == null) {
            return;
        }
        try {
            instancia.conexion.close();
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo cerrar la base de datos", e);
        } finally {
            instancia = null;
        }
    }

    private void crearTablas() throws SQLException, IOException {
        String script = Files.readString(Path.of(ESQUEMA), StandardCharsets.UTF_8);
        try (Statement st = conexion.createStatement()) {
            for (String sentencia : script.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        }
    }
}
