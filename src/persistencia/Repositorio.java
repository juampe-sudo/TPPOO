package persistencia;

import java.util.List;

public interface Repositorio<T> {

    void guardar(T obj);

    T buscar(String clave);

    List<T> listar();
}
