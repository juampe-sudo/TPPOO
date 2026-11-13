package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorSalidas;
import enums.CategoriaTour;
import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Actividad;
import modelo.Liquidacion;
import modelo.Proveedor;
import modelo.ServicioAdicional;
import modelo.Tarifa;
import modelo.Tour;
import modelo.TourAventura;
import modelo.TourCultural;
import modelo.TourNaturaleza;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Scanner;

public class MenuAdministrador extends Menu {

    private ControladorCatalogo ctrlCatalogo;
    private ControladorSalidas ctrlSalidas;
    private ControladorReportes ctrlReportes;

    public MenuAdministrador(ControladorCatalogo ctrlCatalogo, ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        super(scanner);
        this.ctrlCatalogo = ctrlCatalogo;
        this.ctrlSalidas = ctrlSalidas;
        this.ctrlReportes = ctrlReportes;
    }

    @Override
    public String getTitulo() {
        return "Administrador";
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- Administrador ---");
            System.out.println("1. Dar de alta un tour");
            System.out.println("2. Modificar un tour");
            System.out.println("3. Registrar proveedor");
            System.out.println("4. Ver catálogo");
            System.out.println("5. Liquidar honorarios de un guia");
            System.out.println("6. Reportes");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> altaTour();
                    case 2 -> editarTour(ctrlCatalogo.buscarTour(leerTexto("Código del tour: ")).getCodigo());
                    case 3 -> registrarProveedor(leerTexto("EIN: "));
                    case 4 -> verCatalogo();
                    case 5 -> liquidar();
                    case 6 -> reportes();
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

    private void liquidar() throws ReglaNegocioException {
        Liquidacion liq = ctrlSalidas.liquidarHonorarios(leerTexto("Código de la salida: "));
        System.out.println("Liquidación de " + liq.getGuia().getNombreCompleto() + " por la salida " + liq.getSalida().getCodigo());
        System.out.println("  " + dias(liq.getDias()) + " x " + usd(liq.getGuia().getHonorarioPorDia()) + " = " + usd(liq.getMontoDias()));
        System.out.println("  5% de adicionales vendidos = " + usd(liq.getMontoAdicionales()));
        System.out.println("  Total a pagar: " + usd(liq.getTotal()));
    }

    private void reportes() throws ReglaNegocioException {
        List<String> titulos = ctrlReportes.listarReportes();
        for (int i = 0; i < titulos.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + titulos.get(i));
        }
        int cual = leerEntero("Reporte: ");
        List<String> lineas = ctrlReportes.emitir(cual - 1);
        System.out.println();
        System.out.println(titulos.get(cual - 1).toUpperCase());
        if (lineas.isEmpty()) {
            System.out.println("Sin datos");
        }
        for (String l : lineas) {
            System.out.println(l);
        }
        System.out.println("(guardado en la carpeta data/reportes)");
    }

    private void verCatalogo() {
        if (ctrlCatalogo.listarTours().isEmpty()) {
            System.out.println("Todavía no hay tours cargados");
            return;
        }
        for (Tour t : ctrlCatalogo.listarTours()) {
            System.out.println();
            System.out.println(t.getCodigo() + " - " + t.getNombre() + " (" + t.getCategoria().getDescripcion()
                    + ", " + dias(t.getDuracionDias()) + ", cupo " + t.getCupoMinimo() + " a " + t.getCupoMaximo() + ")");
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
}
