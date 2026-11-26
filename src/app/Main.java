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
import persistencia.ConexionBD;
import persistencia.Repositorio;
import persistencia.RepositorioClienteSQL;
import persistencia.RepositorioEmpleadoSQL;
import persistencia.RepositorioProveedorSQL;
import persistencia.RepositorioReservaSQL;
import persistencia.RepositorioSalidaSQL;
import persistencia.RepositorioTourSQL;
import persistencia.RepositorioVehiculoSQL;
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
            Repositorio<Proveedor> proveedores = new RepositorioProveedorSQL();
            Repositorio<Tour> tours = new RepositorioTourSQL(proveedores);
            Repositorio<Cliente> clientes = new RepositorioClienteSQL();
            Repositorio<Empleado> empleados = new RepositorioEmpleadoSQL();
            Repositorio<Vehiculo> vehiculos = new RepositorioVehiculoSQL();
            new CargadorDatosIniciales("data/datos_iniciales.txt").cargar(empleados, vehiculos);
            Repositorio<Salida> salidas = new RepositorioSalidaSQL(tours, empleados, vehiculos);
            Repositorio<Reserva> reservas = new RepositorioReservaSQL(clientes, salidas, empleados);

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
            System.out.println("Error con los datos del programa: " + e.getMessage());
        } finally {
            scanner.close();
            ConexionBD.cerrar();
        }
    }
}
