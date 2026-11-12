package persistencia;

import enums.CategoriaTour;
import enums.Temporada;
import enums.TipoPasajero;
import enums.TipoVehiculo;
import excepciones.ReglaNegocioException;
import modelo.Cliente;
import modelo.Coordinador;
import modelo.Empleado;
import modelo.Guia;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.TourCultural;
import modelo.Vehiculo;
import modelo.Vendedor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PersistenciaTest {

    @TempDir
    Path carpeta;

    private final LocalDate hoy = LocalDate.now();

    private String ruta(String nombre) {
        return carpeta.resolve(nombre).toString();
    }

    private RepositorioArchivo<Tour> tours() {
        return new RepositorioArchivo<>(ruta("tours.dat"), Tour::getCodigo);
    }

    private RepositorioArchivo<Salida> salidas() {
        return new RepositorioArchivo<>(ruta("salidas.dat"), Salida::getCodigo);
    }

    private RepositorioArchivo<Reserva> reservas() {
        return new RepositorioArchivo<>(ruta("reservas.dat"), r -> String.valueOf(r.getNumero()));
    }

    private RepositorioArchivo<Cliente> clientes() {
        return new RepositorioArchivo<>(ruta("clientes.dat"), Cliente::getDocumento);
    }

    private RepositorioArchivo<Empleado> empleados() {
        return new RepositorioArchivo<>(ruta("empleados.dat"), e -> String.valueOf(e.getLegajo()));
    }

    private RepositorioArchivo<Vehiculo> vehiculos() {
        return new RepositorioArchivo<>(ruta("vehiculos.dat"), Vehiculo::getPatente);
    }

    @Test
    void guardarYRecargarDesdeArchivo() {
        RepositorioArchivo<Cliente> clientes = clientes();
        clientes.guardar(new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina"));
        clientes.guardar(new Cliente("John", "Miller", "47111222", "907-555-0100", "john@mail.com", "Estados Unidos"));

        RepositorioArchivo<Cliente> recargado = clientes();

        assertEquals(2, recargado.listar().size());
        assertEquals("Laura Gómez", recargado.buscar("28999888").getNombreCompleto());
        assertEquals("Estados Unidos", recargado.buscar("47111222").getPaisResidencia());
        assertNotSame(clientes.buscar("28999888"), recargado.buscar("28999888"));
        assertNull(recargado.buscar("11111111"));
    }

    @Test
    void sinArchivoArrancaVacio() {
        String archivo = ruta("data/vehiculos.dat");
        RepositorioArchivo<Vehiculo> vehiculos = new RepositorioArchivo<>(archivo, Vehiculo::getPatente);

        assertTrue(vehiculos.listar().isEmpty());
        assertNull(vehiculos.buscar("KDF482"));

        vehiculos.guardar(new Vehiculo("KDF482", 8, TipoVehiculo.CAMIONETA));
        RepositorioArchivo<Vehiculo> recargado = new RepositorioArchivo<>(archivo, Vehiculo::getPatente);
        assertTrue(new File(archivo).exists());
        assertEquals(1, recargado.listar().size());
    }

    @Test
    void recargaConReferenciasCompartidas() throws ReglaNegocioException {
        Tour tour = new TourCultural("CUL01", "Herencia nativa de Alaska", "Centro de herencia nativa", 3, 4, 20);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        Salida salida = tour.programarSalida("S001", hoy.plusDays(40), 15);
        List<String> idiomas = new ArrayList<>();
        idiomas.add("Inglés");
        Set<CategoriaTour> categorias = new HashSet<>();
        categorias.add(CategoriaTour.CULTURAL);
        Guia guia = new Guia("Aiyana", "Nanouk", "40112233", 202, "907-555-0133", idiomas, categorias, 250.0);
        Vehiculo combi = new Vehiculo("JTR915", 15, TipoVehiculo.COMBI);
        salida.asignarGuia(guia);
        salida.asignarVehiculo(combi);
        Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234", "laura@mail.com", "Argentina");
        Vendedor vendedor = new Vendedor("Emily", "Carter", "41235678", 101, "907-555-0142", 0.05);
        Reserva reserva = new Reserva(1, cliente, salida, vendedor);
        reserva.agregarPasajero(new Pasajero("Laura", "Gómez", "28999888", LocalDate.of(1988, 6, 10),
                TipoPasajero.ADULTO, "Juan Gómez 1155550000", ""));
        salida.agregarReserva(reserva);

        tours().guardar(tour);
        salidas().guardar(salida);
        reservas().guardar(reserva);
        clientes().guardar(cliente);
        RepositorioArchivo<Empleado> empleadosGuardados = empleados();
        empleadosGuardados.guardar(guia);
        empleadosGuardados.guardar(vendedor);
        vehiculos().guardar(combi);

        RepositorioArchivo<Tour> tours = tours();
        RepositorioArchivo<Salida> salidas = salidas();
        RepositorioArchivo<Reserva> reservas = reservas();
        RepositorioArchivo<Cliente> clientes = clientes();
        RepositorioArchivo<Empleado> empleados = empleados();
        RepositorioArchivo<Vehiculo> vehiculos = vehiculos();
        Salida salidaCargada = salidas.buscar("S001");
        Reserva reservaCargada = reservas.buscar("1");
        assertNotSame(salidaCargada, reservaCargada.getSalida());

        RepositorioArchivo.vincular(tours, salidas, reservas, clientes, empleados, vehiculos);

        assertSame(salidaCargada, reservaCargada.getSalida());
        assertSame(reservaCargada, salidaCargada.getReservas().get(0));
        assertSame(tours.buscar("CUL01"), salidaCargada.getTour());
        assertSame(clientes.buscar("28999888"), reservaCargada.getCliente());
        assertSame(empleados.buscar("101"), reservaCargada.getVendedor());
        assertSame(empleados.buscar("202"), salidaCargada.getGuia());
        assertSame(salidaCargada, salidaCargada.getGuia().getSalidas().get(0));
        assertSame(vehiculos.buscar("JTR915"), salidaCargada.getVehiculo());
    }

    @Test
    void datosInicialesSinDuplicar() {
        CargadorDatosIniciales cargador = new CargadorDatosIniciales("data/datos_iniciales.txt");
        RepositorioEnMemoria<Empleado> empleados = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        RepositorioEnMemoria<Vehiculo> vehiculos = new RepositorioEnMemoria<>(Vehiculo::getPatente);

        cargador.cargar(empleados, vehiculos);

        assertEquals(7, empleados.listar().size());
        assertEquals(3, vehiculos.listar().size());
        assertTrue(((Guia) empleados.buscar("203")).estaHabilitado(CategoriaTour.AVENTURA));
        assertEquals(TipoVehiculo.MICRO, vehiculos.buscar("HMB307").getTipo());

        cargador.cargar(empleados, vehiculos);
        assertEquals(7, empleados.listar().size());

        RepositorioEnMemoria<Empleado> conPersonal = new RepositorioEnMemoria<>(e -> String.valueOf(e.getLegajo()));
        conPersonal.guardar(new Coordinador("Daniel", "Brooks", "33445566", 301, "907-555-0171"));
        RepositorioEnMemoria<Vehiculo> sinVehiculos = new RepositorioEnMemoria<>(Vehiculo::getPatente);

        cargador.cargar(conPersonal, sinVehiculos);

        assertEquals(1, conPersonal.listar().size());
        assertEquals(3, sinVehiculos.listar().size());
    }
}
