package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorSalidas;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Guia;
import modelo.Pasajero;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuCoordinador extends Menu {

    private ControladorCatalogo ctrlCatalogo;
    private ControladorSalidas ctrlSalidas;

    public MenuCoordinador(ControladorCatalogo ctrlCatalogo, ControladorSalidas ctrlSalidas, Scanner scanner) {
        super(scanner);
        this.ctrlCatalogo = ctrlCatalogo;
        this.ctrlSalidas = ctrlSalidas;
    }

    @Override
    public String getTitulo() {
        return "Coordinador de operaciones";
    }

    @Override
    public void mostrar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("--- Coordinador de operaciones ---");
            System.out.println("1. Programar salida");
            System.out.println("2. Asignar guía y vehículo");
            System.out.println("3. Emitir lista de pasajeros");
            System.out.println("4. Iniciar salida");
            System.out.println("5. Cerrar salida");
            System.out.println("6. Cancelar salida");
            System.out.println("7. Controlar plazos (cupo mínimo y saldos)");
            System.out.println("8. Ver salidas");
            System.out.println("0. Volver");
            opcion = leerEntero("Opción: ");
            try {
                switch (opcion) {
                    case 1 -> programarSalida();
                    case 2 -> asignarGuiaYVehiculo();
                    case 3 -> emitirLista();
                    case 4 -> {
                        ctrlSalidas.iniciarSalida(leerTexto("Código de la salida: "));
                        System.out.println("La salida está en curso");
                    }
                    case 5 -> {
                        ctrlSalidas.cerrarSalida(leerTexto("Código de la salida: "));
                        System.out.println("Salida finalizada, ya se puede liquidar al guía");
                    }
                    case 6 -> cancelarSalida();
                    case 7 -> controlarPlazos();
                    case 8 -> verSalidas(ctrlSalidas.listarSalidas());
                    case 0 -> {
                    }
                    default -> System.out.println("Opción inválida");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void programarSalida() throws ReglaNegocioException {
        for (Tour t : ctrlCatalogo.listarTours()) {
            System.out.println("  " + t.getCodigo() + " - " + t.getNombre() + " (" + dias(t.getDuracionDias())
                    + ", cupo " + t.getCupoMinimo() + " a " + t.getCupoMaximo() + ")");
        }
        String codTour = leerTexto("Código del tour: ");
        LocalDate inicio = leerFecha("Fecha de inicio");
        int cupo = leerEntero("Cupo máximo de la salida: ");
        Salida salida = ctrlSalidas.programarSalida(codTour, inicio, cupo);
        System.out.println("Salida " + salida.getCodigo() + " programada del " + salida.getFechaInicio().format(FECHA)
                + " al " + salida.getFechaFin().format(FECHA));
    }

    private void asignarGuiaYVehiculo() throws ReglaNegocioException {
        String codSalida = leerTexto("Código de la salida: ");
        System.out.println("Guías habilitados y libres en esas fechas:");
        for (Guia g : ctrlSalidas.guiasDisponibles(codSalida)) {
            System.out.println("  " + g.getLegajo() + " - " + g.getNombreCompleto() + " (" + String.join(", ", g.getIdiomas())
                    + ", " + usd(g.getHonorarioPorDia()) + " por día)");
        }
        ctrlSalidas.asignarGuia(codSalida, leerEntero("Legajo del guía: "));
        System.out.println("Guía asignado");

        List<Vehiculo> vehiculos = ctrlSalidas.vehiculosDisponibles(codSalida);
        if (vehiculos.isEmpty()) {
            System.out.println("No hay vehículos con capacidad para el cupo de la salida");
            return;
        }
        for (Vehiculo v : vehiculos) {
            System.out.println("  " + v.getPatente() + " - " + v.getTipo().getDescripcion() + " de " + v.getCapacidad()
                    + " lugares");
        }
        ctrlSalidas.asignarVehiculo(codSalida, leerTexto("Patente: ").toUpperCase());
        System.out.println("Vehículo asignado");
    }

    private void emitirLista() throws ReglaNegocioException {
        String codSalida = leerTexto("Código de la salida: ");
        List<Pasajero> lista = ctrlSalidas.emitirListaPasajeros(codSalida);
        for (Pasajero p : lista) {
            String restricciones = p.getRestricciones().isEmpty() ? "sin restricciones" : p.getRestricciones();
            System.out.println("  " + p.getApellido() + ", " + p.getNombre() + " | doc " + p.getDocumento()
                    + " | emergencia: " + p.getContactoEmergencia() + " | " + restricciones);
        }
        System.out.println("Lista emitida, se guardó en data/listas/lista_" + codSalida + ".txt");
    }

    private void cancelarSalida() throws ReglaNegocioException {
        String codSalida = leerTexto("Código de la salida: ");
        String motivo = leerTexto("Motivo: ");
        List<Cancelacion> devoluciones = ctrlSalidas.cancelarSalida(codSalida, motivo);
        if (devoluciones.size() == 1) {
            System.out.println("Salida cancelada, se anuló 1 reserva");
        } else {
            System.out.println("Salida cancelada, se anularon " + devoluciones.size() + " reservas");
        }
        for (Cancelacion c : devoluciones) {
            System.out.println("  Devolver " + usd(c.getImporteDevuelto()) + " (" + medio(c) + ")");
        }
    }

    private void controlarPlazos() throws ReglaNegocioException {
        List<String> procesado = ctrlSalidas.controlarPlazos(LocalDate.now());
        if (procesado.isEmpty()) {
            System.out.println("No hubo cambios");
        }
        for (String linea : procesado) {
            System.out.println("  " + linea);
        }
    }
}
