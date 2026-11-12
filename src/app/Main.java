package app;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import estrategias.Promocion;
import estrategias.PromocionGrupo;
import estrategias.PromocionReservaAnticipada;
import modelo.Cliente;
import modelo.Empleado;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;
import persistencia.CargadorDatosIniciales;
import persistencia.RepositorioArchivo;
import reportes.Reporte;
import reportes.ReporteCancelaciones;
import reportes.ReporteIngresosPorTour;
import reportes.ReporteOcupacion;
import reportes.ReporteVentasPorVendedor;
import vista.MenuConsola;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            RepositorioArchivo<Tour> tours = new RepositorioArchivo<>("data/tours.dat", Tour::getCodigo);
            RepositorioArchivo<Proveedor> proveedores = new RepositorioArchivo<>("data/proveedores.dat",
                    Proveedor::getEin);
            RepositorioArchivo<Salida> salidas = new RepositorioArchivo<>("data/salidas.dat", Salida::getCodigo);
            RepositorioArchivo<Reserva> reservas = new RepositorioArchivo<>("data/reservas.dat",
                    r -> String.valueOf(r.getNumero()));
            RepositorioArchivo<Cliente> clientes = new RepositorioArchivo<>("data/clientes.dat",
                    Cliente::getDocumento);
            RepositorioArchivo<Empleado> empleados = new RepositorioArchivo<>("data/empleados.dat",
                    e -> String.valueOf(e.getLegajo()));
            RepositorioArchivo<Vehiculo> vehiculos = new RepositorioArchivo<>("data/vehiculos.dat",
                    Vehiculo::getPatente);

            new CargadorDatosIniciales("data/datos_iniciales.txt").cargar(empleados, vehiculos);
            RepositorioArchivo.vincular(tours, salidas, reservas, clientes, empleados, vehiculos);

            List<Promocion> promociones = new ArrayList<>();
            promociones.add(new PromocionReservaAnticipada(60, 0.15));
            promociones.add(new PromocionGrupo(6, 0.10));

            List<Reporte> reportes = new ArrayList<>();
            reportes.add(new ReporteOcupacion(salidas));
            reportes.add(new ReporteVentasPorVendedor(reservas));
            reportes.add(new ReporteIngresosPorTour(reservas));
            reportes.add(new ReporteCancelaciones(reservas));

            ControladorCatalogo ctrlCatalogo = new ControladorCatalogo(tours, proveedores);
            ControladorReservas ctrlReservas = new ControladorReservas(clientes, salidas, reservas, empleados,
                    promociones);
            ControladorSalidas ctrlSalidas = new ControladorSalidas(tours, salidas, empleados, vehiculos, reservas,
                    "data/listas");
            ControladorReportes ctrlReportes = new ControladorReportes(reportes, "data/reportes");

            new MenuConsola(ctrlCatalogo, ctrlReservas, ctrlSalidas, ctrlReportes, scanner).iniciar();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Se terminó la entrada, se cierra el programa");
        } catch (IllegalStateException e) {
            System.out.println("Error con los archivos de datos: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}
