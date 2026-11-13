package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import excepciones.ReglaNegocioException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MenuConsola extends Menu {

    private ControladorSalidas ctrlSalidas;
    private List<Menu> menus;

    public MenuConsola(ControladorCatalogo ctrlCatalogo, ControladorReservas ctrlReservas,
                       ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        super(scanner);
        this.ctrlSalidas = ctrlSalidas;
        this.menus = new ArrayList<>();
        menus.add(new MenuVendedor(ctrlReservas, ctrlSalidas, scanner));
        menus.add(new MenuCoordinador(ctrlCatalogo, ctrlSalidas, scanner));
        menus.add(new MenuAdministrador(ctrlCatalogo, ctrlSalidas, ctrlReportes, scanner));
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
            for (int i = 0; i < menus.size(); i++) {
                System.out.println((i + 1) + ". " + menus.get(i).getTitulo());
            }
            System.out.println("0. Salir");
            opcion = leerEntero("Elegí tu rol: ");
            if (opcion == 0) {
                System.out.println("Hasta luego");
            } else if (opcion > 0 && opcion <= menus.size()) {
                menus.get(opcion - 1).mostrar();
            } else {
                System.out.println("Opción inválida");
            }
        } while (opcion != 0);
    }
}
