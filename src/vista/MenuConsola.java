package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import excepciones.ReglaNegocioException;
import modelo.Vendedor;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuConsola extends Menu {

    private ControladorSalidas ctrlSalidas;
    private MenuVendedor menuVendedor;
    private MenuCoordinador coordinador;
    private MenuAdministrador administrador;

    public MenuConsola(ControladorCatalogo ctrlCatalogo, ControladorReservas ctrlReservas,
                       ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        super(scanner);
        this.ctrlSalidas = ctrlSalidas;
        this.menuVendedor = new MenuVendedor(ctrlReservas, ctrlSalidas, scanner);
        this.coordinador = new MenuCoordinador(ctrlCatalogo, ctrlSalidas, scanner);
        this.administrador = new MenuAdministrador(ctrlCatalogo, ctrlSalidas, ctrlReportes, scanner);
    }

    @Override
    public String getTitulo() {
        return "Rumbo Norte Alaska";
    }

    public void iniciar() {
        try {
            List<String> procesado = ctrlSalidas.controlarPlazos(LocalDate.now());
            if (!procesado.isEmpty()) {
                System.out.println("Control de plazos de hoy:");
                for (String linea : procesado) {
                    System.out.println("  " + linea);
                }
            }
        } catch (ReglaNegocioException e) {
            System.out.println("No se pudo hacer el control de plazos: " + e.getMessage());
        }
        mostrar();
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== " + getTitulo() + " ===");
            System.out.println("1. Vendedor");
            System.out.println("2. Coordinador de operaciones");
            System.out.println("3. Administrador");
            System.out.println("0. Salir");
            opcion = leerEntero("Elegí tu rol: ");
            switch (opcion) {
                case 1 -> menuVendedor.mostrar();
                case 2 -> coordinador.mostrar();
                case 3 -> administrador.mostrar();
                case 0 -> System.out.println("Hasta luego");
                default -> System.out.println("Opción inválida");
            }
        } while (opcion != 0);
    }
}
