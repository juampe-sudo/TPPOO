package persistencia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RepositorioEnMemoria<T> implements Repositorio<T> {

    private Map<String, T> datos;
    private Function<T, String> clave;

    public RepositorioEnMemoria(Function<T, String> clave) {
        this.datos = new LinkedHashMap<>();
        this.clave = clave;
    }

    @Override
    public void guardar(T obj) {
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
}
