package controladores;

import enums.EstadoReserva;
import enums.EstadoSalida;
import estrategias.PoliticaCancelacionAgencia;
import estrategias.PoliticaCancelacionCliente;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Empleado;
import modelo.Guia;
import modelo.Liquidacion;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;
import persistencia.Repositorio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ControladorSalidas {

    private Repositorio<Tour> tours;
    private Repositorio<Salida> salidas;
    private Repositorio<Empleado> empleados;
    private Repositorio<Vehiculo> vehiculos;
    private Repositorio<Reserva> reservas;
    private String carpetaListas;
    private DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ControladorSalidas(Repositorio<Tour> tours, Repositorio<Salida> salidas, Repositorio<Empleado> empleados,
                              Repositorio<Vehiculo> vehiculos, Repositorio<Reserva> reservas, String carpetaListas) {
        this.tours = tours;
        this.salidas = salidas;
        this.empleados = empleados;
        this.vehiculos = vehiculos;
        this.reservas = reservas;
        this.carpetaListas = carpetaListas;
    }

    public List<Salida> listarSalidas() {
        return salidas.listar();
    }

    public Salida programarSalida(String codTour, LocalDate inicio, int cupo) throws ReglaNegocioException {
        Tour tour = tours.buscar(codTour);
        if (tour == null) {
            throw new ReglaNegocioException("No existe el tour " + codTour);
        }
        String codigo = String.format("S%03d", salidas.listar().size() + 1);
        Salida salida = tour.programarSalida(codigo, inicio, cupo);
        salidas.guardar(salida);
        return salida;
    }

    public List<Guia> guiasDisponibles(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        List<Guia> disponibles = new ArrayList<>();
        for (Empleado e : empleados.listar()) {
            if (e instanceof Guia) {
                Guia g = (Guia) e;
                if (g.estaHabilitado(salida.getTour().getCategoria()) && g.estaDisponible(salida)) {
                    disponibles.add(g);
                }
            }
        }
        if (disponibles.isEmpty()) {
            throw new ReglaNegocioException("No hay guías disponibles para la salida " + codSalida);
        }
        return disponibles;
    }

    public void asignarGuia(String codSalida, int legajo) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        Empleado empleado = empleados.buscar(String.valueOf(legajo));
        if (!(empleado instanceof Guia)) {
            throw new ReglaNegocioException("No hay un guía con legajo " + legajo);
        }
        Guia guia = (Guia) empleado;
        salida.asignarGuia(guia);
        salidas.guardar(salida);
        empleados.guardar(guia);
    }

    public List<Vehiculo> vehiculosDisponibles(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        List<Vehiculo> disponibles = new ArrayList<>();
        for (Vehiculo v : vehiculos.listar()) {
            if (v.getCapacidad() >= salida.getCupoMaximo()) {
                disponibles.add(v);
            }
        }
        return disponibles;
    }

    public void asignarVehiculo(String codSalida, String patente) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        Vehiculo vehiculo = vehiculos.buscar(patente);
        if (vehiculo == null) {
            throw new ReglaNegocioException("No hay un vehículo con patente " + patente);
        }
        salida.asignarVehiculo(vehiculo);
        salidas.guardar(salida);
    }

    public List<Pasajero> emitirListaPasajeros(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        List<Pasajero> lista = salida.emitirListaPasajeros();
        guardarLista(salida, lista);
        salidas.guardar(salida);
        return lista;
    }

    public void iniciarSalida(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        if (!salida.getFechaInicio().equals(LocalDate.now())) {
            throw new ReglaNegocioException("La salida " + codSalida + " empieza el "
                    + salida.getFechaInicio().format(fmt) + ", no hoy");
        }
        salida.iniciar();
        salidas.guardar(salida);
    }

    public void cerrarSalida(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        salida.finalizar();
        guardarConReservas(salida);
    }

    public List<Cancelacion> cancelarSalida(String codSalida, String motivo) throws ReglaNegocioException {
        return cancelar(buscarSalida(codSalida), motivo, LocalDate.now());
    }

    public List<String> controlarPlazos(LocalDate hoy) throws ReglaNegocioException {
        List<String> procesado = new ArrayList<>();
        for (Salida s : salidas.listar()) {
            if (s.getEstado() == EstadoSalida.PROGRAMADA && s.diasHastaInicio(hoy) <= 7) {
                List<Cancelacion> devoluciones = cancelar(s, "No alcanzó el cupo mínimo", hoy);
                procesado.add("Salida " + s.getCodigo() + " cancelada por no alcanzar el cupo mínimo ("
                        + devoluciones.size() + " reservas anuladas)");
            }
        }
        for (Reserva r : reservas.listar()) {
            Salida s = r.getSalida();
            boolean sinIniciar = s.admiteReservas() || s.getEstado() == EstadoSalida.COMPLETA;
            if (sinIniciar && estaActiva(r) && r.saldo() > 0 && s.diasHastaInicio(hoy) <= 10) {
                Cancelacion c = r.cancelar("Falta de pago del saldo", hoy, new PoliticaCancelacionCliente());
                reservas.guardar(r);
                salidas.guardar(s);
                procesado.add("Reserva " + r.getNumero() + " anulada por falta de pago. Se devuelven USD "
                        + String.format("%.2f", c.getImporteDevuelto()));
            }
        }
        return procesado;
    }

    public Liquidacion liquidarHonorarios(String codSalida) throws ReglaNegocioException {
        Salida salida = buscarSalida(codSalida);
        if (salida.getGuia() == null) {
            throw new ReglaNegocioException("La salida " + codSalida + " no tiene guía asignado");
        }
        Liquidacion liquidacion = salida.getGuia().liquidar(salida);
        empleados.guardar(salida.getGuia());
        return liquidacion;
    }

    private List<Cancelacion> cancelar(Salida salida, String motivo, LocalDate hoy) throws ReglaNegocioException {
        salida.cancelar();
        List<Cancelacion> devoluciones = new ArrayList<>();
        for (Reserva r : salida.getReservas()) {
            if (estaActiva(r)) {
                devoluciones.add(r.cancelar(motivo, hoy, new PoliticaCancelacionAgencia()));
            }
        }
        guardarConReservas(salida);
        return devoluciones;
    }

    private void guardarLista(Salida salida, List<Pasajero> lista) {
        File carpeta = new File(carpetaListas);
        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }
        File archivo = new File(carpeta, "lista_" + salida.getCodigo() + ".txt");
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.write("Salida " + salida.getCodigo() + " - " + salida.getTour().getNombre() + " - "
                    + salida.getFechaInicio().format(fmt) + " - Guía: " + salida.getGuia().getNombreCompleto());
            escritor.newLine();
            for (Pasajero p : lista) {
                String restr = p.getRestricciones().isEmpty() ? "sin restricciones" : p.getRestricciones();
                escritor.write(p.getApellido() + ", " + p.getNombre() + " | " + p.getDocumento() + " | "
                        + p.getContactoEmergencia() + " | " + restr);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar la lista en " + archivo, e);
        }
    }

    private boolean estaActiva(Reserva r) {
        return r.getEstado() == EstadoReserva.PENDIENTE || r.getEstado() == EstadoReserva.CONFIRMADA;
    }

    private void guardarConReservas(Salida salida) {
        for (Reserva r : salida.getReservas()) {
            reservas.guardar(r);
        }
        salidas.guardar(salida);
    }

    private Salida buscarSalida(String codSalida) throws ReglaNegocioException {
        Salida salida = salidas.buscar(codSalida);
        if (salida == null) {
            throw new ReglaNegocioException("No existe la salida " + codSalida);
        }
        return salida;
    }
}
