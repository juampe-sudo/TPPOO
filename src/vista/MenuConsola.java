package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import enums.CategoriaTour;
import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Actividad;
import modelo.Proveedor;
import modelo.ServicioAdicional;
import modelo.Tarifa;
import modelo.Tour;
import modelo.TourAventura;
import modelo.TourCultural;
import modelo.TourNaturaleza;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class MenuConsola {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ControladorCatalogo ctrlCatalogo;
    private ControladorReservas ctrlReservas;
    private ControladorSalidas ctrlSalidas;
    private ControladorReportes ctrlReportes;
    private Scanner scanner;

    public MenuConsola(ControladorCatalogo ctrlCatalogo, ControladorReservas ctrlReservas,
                       ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        this.ctrlCatalogo = ctrlCatalogo;
        this.ctrlReservas = ctrlReservas;
        this.ctrlSalidas = ctrlSalidas;
        this.ctrlReportes = ctrlReportes;
        this.scanner = scanner;
    }

    public void iniciar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Rumbo Norte Alaska ===");
            System.out.println("1. Vendedor");
            System.out.println("2. Coordinador de operaciones");
            System.out.println("3. Administrador");
            System.out.println("0. Salir");
            opcion = leerEntero("Elegí tu rol: ");
            switch (opcion) {
                case 3 -> menuAdministrador();
                case 0 -> System.out.println("Hasta luego");
                default -> System.out.println("Opción inválida");
            }
        } while (opcion != 0);
    }

    private void menuAdministrador() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- Administrador ---");
            System.out.println("1. Dar de alta un tour");
            System.out.println("2. Modificar un tour");
            System.out.println("3. Registrar proveedor");
            System.out.println("4. Ver catálogo");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> altaTour();
                    case 2 -> editarTour(ctrlCatalogo.buscarTour(leerTexto("Código del tour: ")).getCodigo());
                    case 3 -> registrarProveedor(leerTexto("EIN: "));
                    case 4 -> verCatalogo();
                    case 0 -> {
                    }
                    default -> System.out.println("Opción inválida");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void altaTour() throws ReglaNegocioException {
        String codigo = leerTexto("Código: ");
        String nombre = leerTexto("Nombre: ");
        String descripcion = leerTexto("Descripción: ");
        int duracion = leerEntero("Duración en días: ");
        int cupoMinimo = leerEntero("Cupo mínimo: ");
        int cupoMaximo = leerEntero("Cupo máximo: ");
        CategoriaTour categoria = elegir("Categoría: ", CategoriaTour.values());
        Tour tour;
        if (categoria == CategoriaTour.AVENTURA) {
            NivelDificultad nivel = elegir("Dificultad: ", NivelDificultad.values());
            int edadMinima = leerEntero("Edad mínima: ");
            tour = new TourAventura(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo, nivel, edadMinima);
        } else if (categoria == CategoriaTour.CULTURAL) {
            tour = new TourCultural(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo);
        } else {
            tour = new TourNaturaleza(codigo, nombre, descripcion, duracion, cupoMinimo, cupoMaximo);
        }
        ctrlCatalogo.registrarTour(tour);
        System.out.println("Tour " + codigo + " registrado");
        if (tour instanceof TourAventura) {
            double precio = leerDouble("Precio del seguro de viaje (es obligatorio en aventura): ");
            ctrlCatalogo.agregarServicio(codigo, "Seguro de viaje", precio, true);
        }
        System.out.println("Ahora cargá el itinerario, las tarifas y los adicionales");
        editarTour(codigo);
    }

    private void editarTour(String codigo) {
        int opcion;
        do {
            System.out.println();
            System.out.println("Tour " + codigo);
            System.out.println("1. Agregar actividad al itinerario");
            System.out.println("2. Agregar tarifa");
            System.out.println("3. Agregar servicio adicional");
            System.out.println("0. Terminar");
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> agregarActividad(codigo);
                    case 2 -> agregarTarifa(codigo);
                    case 3 -> agregarServicio(codigo);
                    case 0 -> {
                    }
                    default -> System.out.println("Opción inválida");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void agregarActividad(String codigo) throws ReglaNegocioException {
        int dia = leerEntero("Día: ");
        int orden = leerEntero("Orden dentro del día: ");
        LocalTime horario = leerHora("Horario");
        String descripcion = leerTexto("Descripción: ");
        String lugar = leerTexto("Lugar: ");
        TipoActividad tipo = elegir("Tipo: ", TipoActividad.values());
        String ein = leerTexto("EIN del proveedor (enter si no hay): ");
        if (!ein.isEmpty() && ctrlCatalogo.buscarProveedor(ein) == null) {
            System.out.println("Ese proveedor no está registrado, cargá sus datos");
            registrarProveedor(ein);
        }
        ctrlCatalogo.agregarActividad(codigo, dia, orden, horario, descripcion, lugar, tipo, ein);
        System.out.println("Actividad agregada");
    }

    private void agregarTarifa(String codigo) throws ReglaNegocioException {
        Temporada temporada = elegir("Temporada: ", Temporada.values());
        LocalDate desde = leerFecha("Vigente desde");
        LocalDate hasta = leerFecha("Vigente hasta");
        double precio = leerDouble("Precio base por pasajero (USD): ");
        ctrlCatalogo.agregarTarifa(codigo, temporada, desde, hasta, precio);
        System.out.println("Tarifa agregada");
    }

    private void agregarServicio(String codigo) throws ReglaNegocioException {
        String nombre = leerTexto("Nombre del servicio: ");
        double precio = leerDouble("Precio (USD): ");
        boolean obligatorio = leerSiNo("¿Es obligatorio?");
        ctrlCatalogo.agregarServicio(codigo, nombre, precio, obligatorio);
        System.out.println("Servicio agregado");
    }

    private void registrarProveedor(String ein) throws ReglaNegocioException {
        String nombre = leerTexto("Nombre del proveedor: ");
        String contacto = leerTexto("Contacto: ");
        String servicio = leerTexto("Servicio que presta: ");
        ctrlCatalogo.registrarProveedor(new Proveedor(nombre, ein, contacto, servicio));
        System.out.println("Proveedor registrado");
    }

    private void verCatalogo() {
        if (ctrlCatalogo.listarTours().isEmpty()) {
            System.out.println("Todavía no hay tours cargados");
            return;
        }
        for (Tour t : ctrlCatalogo.listarTours()) {
            System.out.println();
            System.out.println(t.getCodigo() + " - " + t.getNombre() + " (" + t.getCategoria().getDescripcion()
                    + ", " + t.getDuracionDias() + " días, cupo " + t.getCupoMinimo() + " a " + t.getCupoMaximo() + ")");
            if (t instanceof TourAventura) {
                TourAventura a = (TourAventura) t;
                System.out.println("  Dificultad " + a.getNivelDificultad().getDescripcion().toLowerCase()
                        + ", edad mínima " + a.getEdadMinima());
            }
            for (Actividad a : t.getActividades()) {
                String proveedor = a.getProveedor() == null ? "" : " - " + a.getProveedor().getNombre();
                System.out.println("  Día " + a.getDia() + " " + a.getHorario() + " " + a.getDescripcion() + " ("
                        + a.getLugar() + ", " + a.getTipo().getDescripcion() + ")" + proveedor);
            }
            for (Tarifa tarifa : t.getTarifas()) {
                System.out.println("  Tarifa " + tarifa.getTemporada().getDescripcion() + " del "
                        + tarifa.getVigenciaDesde().format(FECHA) + " al " + tarifa.getVigenciaHasta().format(FECHA)
                        + ": " + usd(tarifa.getPrecioBase()));
            }
            for (ServicioAdicional s : t.getAdicionales()) {
                System.out.println("  Adicional " + s.getNombre() + ": " + usd(s.getPrecio())
                        + (s.isObligatorio() ? " (obligatorio)" : ""));
            }
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número entero");
            }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número, por ejemplo 150.50");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje + " (dd/mm/aaaa): "), FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida");
            }
        }
    }

    private LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(mensaje + " (hh:mm): "));
            } catch (DateTimeParseException e) {
                System.out.println("Hora inválida");
            }
        }
    }

    private boolean leerSiNo(String mensaje) {
        return leerTexto(mensaje + " (s/n): ").equalsIgnoreCase("s");
    }

    private <E extends Enum<E>> E elegir(String mensaje, E[] valores) {
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

    private String usd(double importe) {
        return "USD " + String.format("%.2f", importe);
    }
}
