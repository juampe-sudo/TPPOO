package reportes;

import controladores.ControladorReportes;
import enums.MedioPago;
import enums.Temporada;
import enums.TipoPasajero;
import estrategias.PoliticaCancelacionCliente;
import excepciones.ReglaNegocioException;
import modelo.Cliente;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vendedor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import persistencia.RepositorioEnMemoria;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReportesTest {

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234",
            "laura@mail.com", "Argentina");
    private final Vendedor emily = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
    private final Vendedor martin = new Vendedor("Martín", "Sosa", "35678123", 102, "907-555-0187", 0.04);
    private RepositorioEnMemoria<Salida> salidas;
    private RepositorioEnMemoria<Reserva> reservas;
    private int documento = 30000000;

    @BeforeEach
    void cargarDatos() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Anchorage histórico", "Museos", 3, 2, 10);
        tour.agregarTarifa(Temporada.BAJA, hoy, hoy.plusDays(120), 1000.0);
        Salida salida = tour.programarSalida("S001", hoy.plusDays(40), 10);
        salidas = new RepositorioEnMemoria<>(Salida::getCodigo);
        salidas.guardar(salida);
        reservas = new RepositorioEnMemoria<>(r -> String.valueOf(r.getNumero()));

        Reserva primera = reservar(1, salida, emily, 2);
        primera.registrarPago(600.0, MedioPago.EFECTIVO, 1, hoy);
        Reserva segunda = reservar(2, salida, martin, 1);
        segunda.registrarPago(300.0, MedioPago.TARJETA, 3, hoy);
        segunda.cancelar("Problemas de salud", hoy, new PoliticaCancelacionCliente());
        reservar(3, salida, emily, 1);
        reservar(4, salida, martin, 1);
    }

    private Reserva reservar(int numero, Salida salida, Vendedor vendedor, int pasajeros)
            throws ReglaNegocioException {
        Reserva r = new Reserva(numero, cliente, salida, vendedor);
        for (int i = 0; i < pasajeros; i++) {
            documento++;
            r.agregarPasajero(new Pasajero("Ana", "Pérez", String.valueOf(documento), LocalDate.of(1990, 3, 5),
                    TipoPasajero.ADULTO, "Juan Pérez 1155550000", ""));
        }
        r.calcularTotal(new ArrayList<>());
        salida.agregarReserva(r);
        reservas.guardar(r);
        return r;
    }

    @Test
    void ocupacionCuentaSoloReservasActivas() {
        List<String> lineas = new ReporteOcupacion(salidas).generar();

        assertEquals(1, lineas.size());
        assertTrue(lineas.get(0).contains("4/10 lugares"));
    }

    @Test
    void ventasDeMayorAMenorSinAnuladas() {
        List<String> lineas = new ReporteVentasPorVendedor(reservas).generar();

        assertEquals(2, lineas.size());
        assertTrue(lineas.get(0).startsWith("Emily Carter"));
        assertTrue(lineas.get(0).contains("2 reservas"));
        assertTrue(lineas.get(0).contains("comisión USD " + String.format("%.2f", 150.0)));
        assertTrue(lineas.get(1).startsWith("Martín Sosa"));
        assertTrue(lineas.get(1).contains("1 reserva |"));
    }

    @Test
    void ingresosRestanDevolucionesYCancelacionesListadas() {
        List<String> ingresos = new ReporteIngresosPorTour(reservas).generar();
        List<String> cancelaciones = new ReporteCancelaciones(reservas).generar();

        assertEquals(1, ingresos.size());
        assertTrue(ingresos.get(0).contains("cobrado USD " + String.format("%.2f", 921.0)));
        assertTrue(ingresos.get(0).contains("neto USD " + String.format("%.2f", 651.0)));
        assertEquals(1, cancelaciones.size());
        assertTrue(cancelaciones.get(0).contains("reserva 2"));
        assertTrue(cancelaciones.get(0).contains("Tarjeta"));
    }

    @Test
    void controladorEmiteYGuardaElReporte(@TempDir Path carpeta) throws ReglaNegocioException {
        List<Reporte> lista = new ArrayList<>();
        lista.add(new ReporteOcupacion(salidas));
        lista.add(new ReporteCancelaciones(reservas));
        ControladorReportes controlador = new ControladorReportes(lista, carpeta.toString());

        assertEquals("Cancelaciones", controlador.listarReportes().get(1));
        assertEquals(1, controlador.emitir(1).size());
        assertTrue(carpeta.resolve("cancelaciones.txt").toFile().exists());
        assertThrows(ReglaNegocioException.class, () -> controlador.emitir(5));
    }
}
