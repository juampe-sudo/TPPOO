package excepciones;

public class CupoInsuficienteException extends ReglaNegocioException {

    public CupoInsuficienteException(int libres) {
        super("No hay lugares suficientes. Lugares libres: " + libres);
    }
}
