package reportes;

import modelo.Pago;
import modelo.Reserva;
import modelo.Tour;
import persistencia.Repositorio;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ReporteIngresosPorTour implements Reporte {

    private Repositorio<Reserva> reservas;

    public ReporteIngresosPorTour(Repositorio<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String getTitulo() {
        return "Ingresos por tour";
    }

    @Override
    public List<String> generar() {
        Map<String, Tour> tours = new TreeMap<>();
        Map<String, Double> cobrado = new TreeMap<>();
        Map<String, Double> devuelto = new TreeMap<>();
        for (Reserva r : reservas.listar()) {
            Tour tour = r.getSalida().getTour();
            String codigo = tour.getCodigo();
            tours.put(codigo, tour);
            for (Pago p : r.getPagos()) {
                cobrado.merge(codigo, p.getImporte() + p.getRecargo(), Double::sum);
            }
            if (r.getCancelacion() != null) {
                devuelto.merge(codigo, r.getCancelacion().getImporteDevuelto(), Double::sum);
            }
        }
        List<String> lineas = new ArrayList<>();
        for (String codigo : tours.keySet()) {
            double entra = cobrado.getOrDefault(codigo, 0.0);
            double sale = devuelto.getOrDefault(codigo, 0.0);
            lineas.add(codigo + " - " + tours.get(codigo).getNombre() + " | cobrado USD " + String.format("%.2f", entra)
                    + " | devuelto USD " + String.format("%.2f", sale)
                    + " | ingreso neto USD " + String.format("%.2f", entra - sale));
        }
        return lineas;
    }
}
