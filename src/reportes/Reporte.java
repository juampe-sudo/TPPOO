package reportes;

import java.util.List;

public interface Reporte {

    String getTitulo();

    List<String> generar();
}
