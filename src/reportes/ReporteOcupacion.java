package reportes;

import enums.EstadoReserva;
import modelo.Reserva;
import modelo.Salida;
import persistencia.Repositorio;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReporteOcupacion implements Reporte {

    private Repositorio<Salida> salidas;

    public ReporteOcupacion(Repositorio<Salida> salidas) {
        this.salidas = salidas;
    }

    @Override
    public String getTitulo() {
        return "Ocupación por salida";
    }

    @Override
    public List<String> generar() {
        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<String> lineas = new ArrayList<>();
        for (Salida s : salidas.listar()) {
            int ocupados = 0;
            for (Reserva r : s.getReservas()) {
                if (r.getEstado() != EstadoReserva.ANULADA) {
                    ocupados += r.cantidadPasajeros();
                }
            }
            double porcentaje = ocupados * 100.0 / s.getCupoMaximo();
            lineas.add(s.getCodigo() + " | " + s.getTour().getNombre() + " | " + s.getFechaInicio().format(formato)
                    + " | " + s.getEstado().getDescripcion() + " | " + ocupados + "/" + s.getCupoMaximo()
                    + " lugares (" + String.format("%.1f", porcentaje) + " %)");
        }
        return lineas;
    }
}
