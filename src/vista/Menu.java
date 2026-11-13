package vista;

import modelo.Cancelacion;
import modelo.Salida;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public abstract class Menu {

    protected static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    protected Scanner scanner;

    protected Menu(Scanner scanner) {
        this.scanner = scanner;
    }

    public abstract String getTitulo();

    public abstract void mostrar();

    protected void verSalidas(List<Salida> salidas) {
        if (salidas.isEmpty()) {
            System.out.println("No hay salidas programadas");
            return;
        }
        for (Salida s : salidas) {
            String guia = s.getGuia() == null ? "sin guía" : "guía " + s.getGuia().getNombreCompleto();
            String vehiculo = s.getVehiculo() == null ? "sin vehículo" : "vehículo " + s.getVehiculo().getPatente();
            System.out.println(s.getCodigo() + " | " + s.getTour().getNombre() + " | " + s.getFechaInicio().format(FECHA)
                    + " al " + s.getFechaFin().format(FECHA) + " | " + s.getEstado().getDescripcion() + " | "
                    + s.lugaresOcupados() + "/" + s.getCupoMaximo() + " lugares | " + guia + " | " + vehiculo);
        }
    }

    protected String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    protected int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número entero");
            }
        }
    }

    protected double leerDouble(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número, por ejemplo 150.50");
            }
        }
    }

    protected LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje + " (dd/mm/aaaa): "), FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida");
            }
        }
    }

    protected LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(mensaje + " (hh:mm): "));
            } catch (DateTimeParseException e) {
                System.out.println("Hora inválida");
            }
        }
    }

    protected boolean leerSiNo(String mensaje) {
        return leerTexto(mensaje + " (s/n): ").equalsIgnoreCase("s");
    }

    protected <E extends Enum<E>> E elegir(String mensaje, E[] valores) {
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i].name());
        }
        while (true) {
            int opcion = leerEntero(mensaje);
            if (opcion >= 1 && opcion <= valores.length) {
                return valores[opcion - 1];
            }
            System.out.println("Opción inválida");
        }
    }

    protected String medio(Cancelacion c) {
        return c.getMedio() == null ? "no tenía pagos" : c.getMedio().getDescripcion();
    }

    protected String dias(int n) {
        return n == 1 ? "1 día" : n + " días";
    }

    protected String usd(double importe) {
        return "USD " + String.format("%.2f", importe);
    }
}
