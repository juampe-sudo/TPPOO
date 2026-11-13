package reportes;

import enums.EstadoReserva;
import modelo.Reserva;
import modelo.Vendedor;
import persistencia.Repositorio;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReporteVentasPorVendedor implements Reporte {

    private Repositorio<Reserva> reservas;

    public ReporteVentasPorVendedor(Repositorio<Reserva> reservas) {
        this.reservas = reservas;
    }

    @Override
    public String getTitulo() {
        return "Ventas por vendedor";
    }

    @Override
    public List<String> generar() {
        Map<Integer, Vendedor> vendedores = new HashMap<>();
        Map<Integer, Integer> cantidades = new HashMap<>();
        Map<Integer, Double> totales = new HashMap<>();
        for (Reserva r : reservas.listar()) {
            if (r.getEstado() != EstadoReserva.ANULADA) {
                int legajo = r.getVendedor().getLegajo();
                vendedores.put(legajo, r.getVendedor());
                cantidades.put(legajo, cantidades.getOrDefault(legajo, 0) + 1);
                totales.put(legajo, totales.getOrDefault(legajo, 0.0) + r.getTotal());
            }
        }
        List<Vendedor> ordenados = new ArrayList<>(vendedores.values());
        Comparator<Vendedor> porTotal = Comparator.comparing(v -> totales.get(v.getLegajo()));
        ordenados.sort(porTotal.reversed());

        List<String> lineas = new ArrayList<>();
        for (Vendedor v : ordenados) {
            double total = totales.get(v.getLegajo());
            int cant = cantidades.get(v.getLegajo());
            lineas.add(v.getNombreCompleto() + " (legajo " + v.getLegajo() + ") | "
                    + cant + (cant == 1 ? " reserva" : " reservas") + " | total USD " + String.format("%.2f", total)
                    + " | comisión USD " + String.format("%.2f", v.calcularComision(total)));
        }
        return lineas;
    }
}
