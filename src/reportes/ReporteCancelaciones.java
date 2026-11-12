package reportes;

import modelo.Cancelacion;
import modelo.Reserva;
import persistencia.Repositorio;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReporteCancelaciones implements Reporte {

    private Repositorio<Reserva> reservas;

    public ReporteCancelaciones(Repositorio<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String getTitulo() {
        return "Cancelaciones";
    }

    @Override
    public List<String> generar() {
        List<Reserva> canceladas = new ArrayList<>();
        for (Reserva r : reservas.listar()) {
            if (r.getCancelacion() != null) {
                canceladas.add(r);
            }
        }
        canceladas.sort(Comparator.comparing((Reserva r) -> r.getCancelacion().getFecha())
                .thenComparing(Reserva::getNumero));

        DateTimeFormatter formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        List<String> lineas = new ArrayList<>();
        for (Reserva r : canceladas) {
            Cancelacion c = r.getCancelacion();
            String medio = c.getMedio() == null ? "sin pagos" : c.getMedio().getDescripcion();
            lineas.add(c.getFecha().format(formato) + " | reserva " + r.getNumero() + " | salida "
                    + r.getSalida().getCodigo() + " | " + c.getMotivo() + " | devuelto USD "
                    + String.format("%.2f", c.getImporteDevuelto()) + " (" + medio + ")");
        }
        return lineas;
    }
}
