package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import enums.EstadoReserva;
import enums.MedioPago;
import enums.TipoPasajero;
import excepciones.ReglaNegocioException;
import modelo.Cancelacion;
import modelo.Cliente;
import modelo.NotaAjuste;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Vendedor;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class MenuConsola extends Menu {

    private ControladorReservas ctrlReservas;
    private ControladorSalidas ctrlSalidas;
    private Vendedor vendedor;
    private MenuCoordinador coordinador;
    private MenuAdministrador administrador;

    public MenuConsola(ControladorCatalogo ctrlCatalogo, ControladorReservas ctrlReservas,
                       ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        super(scanner);
        this.ctrlReservas = ctrlReservas;
        this.ctrlSalidas = ctrlSalidas;
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
                case 1 -> menuVendedor();
                case 2 -> coordinador.mostrar();
                case 3 -> administrador.mostrar();
                case 0 -> System.out.println("Hasta luego");
                default -> System.out.println("Opción inválida");
            }
        } while (opcion != 0);
    }

    private void menuVendedor() {
        try {
            vendedor = ctrlReservas.buscarVendedor(leerEntero("Tu legajo: "));
        } catch (ReglaNegocioException e) {
            System.out.println(e.getMessage());
            return;
        }
        System.out.println("Hola " + vendedor.getNombre() + "!");
        int op;
        do {
            System.out.println();
            System.out.println("--- Vendedor ---");
            System.out.println("1. Registrar cliente");
            System.out.println("2. Crear reserva");
            System.out.println("3. Registrar pago");
            System.out.println("4. Modificar reserva");
            System.out.println("5. Cancelar reserva");
            System.out.println("6. Ver reserva");
            System.out.println("7. ver salidas");
            System.out.println("0. Volver");
            op = leerEntero("Opción: ");
            try {
                switch (op) {
                    case 1 -> registrarCliente(leerTexto("Documento: "));
                    case 2 -> crearReserva();
                    case 3 -> registrarPago();
                    case 4 -> modificarReserva();
                    case 5 -> cancelarReserva();
                    case 6 -> verReserva();
                    case 7 -> verSalidas(ctrlSalidas.listarSalidas());
                    case 0 -> {
                    }
                    default -> System.out.println("Opcion invalida");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (op != 0);
    }

    private void registrarCliente(String doc) throws ReglaNegocioException {
        Cliente existe = ctrlReservas.buscarCliente(doc);
        if (existe != null) {
            System.out.println("Ese documento ya esta registrado: " + existe);
            return;
        }
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String tel = leerTexto("Teléfono: ");
        String mail = leerTexto("Correo: ");
        String pais = leerTexto("País de residencia: ");
        ctrlReservas.registrarCliente(new Cliente(nombre, apellido, doc, tel, mail, pais));
        System.out.println("Cliente registrado");
    }

    private void crearReserva() throws ReglaNegocioException {
        String doc = leerTexto("Documento del cliente: ");
        if (ctrlReservas.buscarCliente(doc) == null) {
            System.out.println("El cliente no está registrado, cargalo primero");
            registrarCliente(doc);
        }
        System.out.println("Salidas que admiten reservas:");
        for (Salida s : ctrlSalidas.listarSalidas()) {
            if (s.admiteReservas()) {
                System.out.println("  " + s.getCodigo() + " - " + s.getTour().getNombre() + " el " + s.getFechaInicio().format(FECHA)
                        + " (" + s.lugaresDisponibles() + " lugares libres)");
            }
        }
        Reserva res = ctrlReservas.iniciarReserva(doc, leerTexto("Código de la salida: "), vendedor);

        do {
            try {
                String nom = leerTexto("Nombre del pasajero: ");
                String ape = leerTexto("Apellido: ");
                String dni = leerTexto("Documento: ");
                LocalDate nac = leerFecha("Fecha de nacimiento");
                TipoPasajero tipo = elegir("Tipo: ", TipoPasajero.values());
                String contacto = leerTexto("Contacto de emergencia: ");
                String restr = leerTexto("Restricciones alimentarias o de salud (enter si no tiene): ");
                ctrlReservas.agregarPasajero(nom, ape, dni, nac, tipo, contacto, restr);
                System.out.println("pasajero agregado");
            } catch (ReglaNegocioException e) {
                System.out.println("No se agregó: " + e.getMessage());
            }
        } while (leerSiNo("¿Otro pasajero?"));

        List<ServicioAdicional> adicionales = res.getSalida().getTour().getAdicionales();
        if (!adicionales.isEmpty()) {
            System.out.println("Adicionales del tour:");
            for (ServicioAdicional sa : adicionales) {
                System.out.println("  " + sa.getNombre() + " " + usd(sa.getPrecio()) + (sa.isObligatorio() ? " (ya incluido, es obligatorio)" : ""));
            }
            while (leerSiNo("¿Contratar un adicional?")) {
                try {
                    ctrlReservas.contratarServicio(leerTexto("Documento del pasajero: "), leerTexto("Servicio: "));
                    System.out.println("Agregado");
                } catch (ReglaNegocioException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }

        if (!leerSiNo("¿Confirmar la reserva?")) {
            System.out.println("Reserva descartada");
            return;
        }
        Reserva confirmada = ctrlReservas.confirmarReserva();
        System.out.println("Reserva " + confirmada.getNumero() + " creada. Total " + usd(confirmada.getTotal())
                + ", seña a pagar (30%): " + usd(confirmada.getTotal() * 0.30));
    }

    private void registrarPago() throws ReglaNegocioException {
        int nro = leerEntero("Número de reserva: ");
        Reserva r = ctrlReservas.buscarReserva(nro);
        System.out.println("Total " + usd(r.getTotal()) + " | pagado " + usd(r.montoPagado()) + " | saldo " + usd(r.saldo()));
        double importe = leerDouble("Importe: ");
        MedioPago medio = elegir("Medio de pago: ", MedioPago.values());
        int cuotas = 1;
        if (medio == MedioPago.TARJETA) {
            cuotas = leerEntero("Cuotas: ");
        }
        Pago pago = ctrlReservas.registrarPago(nro, importe, medio, cuotas);
        if (pago.getRecargo() > 0) {
            System.out.println("Recargo por pagar en cuotas (7%): " + usd(pago.getRecargo()));
        }
        System.out.println("Pago registrado. La reserva esta " + r.getEstado().getDescripcion().toLowerCase()
                + " y el saldo es " + usd(r.saldo()));
    }

    private void modificarReserva() throws ReglaNegocioException {
        int nro = leerEntero("Número de reserva: ");
        ctrlReservas.buscarReserva(nro);
        System.out.println("1. Reemplazar un pasajero");
        System.out.println("2. Contratar un adicional");
        int opc = leerEntero("Opción: ");
        if (opc == 1) {
            String docViejo = leerTexto("Documento del pasajero que sale: ");
            System.out.println("Datos del pasajero nuevo");
            String nombre = leerTexto("Nombre: ");
            String apellido = leerTexto("Apellido: ");
            String doc = leerTexto("Documento: ");
            LocalDate nac = leerFecha("Fecha de nacimiento");
            TipoPasajero tipo = elegir("Tipo: ", TipoPasajero.values());
            String contacto = leerTexto("Contacto de emergencia: ");
            String restricciones = leerTexto("Restricciones (enter si no tiene): ");
            Pasajero nuevo = new Pasajero(nombre, apellido, doc, nac, tipo, contacto, restricciones);
            ctrlReservas.reemplazarPasajero(nro, docViejo, nuevo);
            System.out.println("Pasajero reemplazado sin cargo, quedó una nota de ajuste");
        } else if (opc == 2) {
            ctrlReservas.contratarServicio(nro, leerTexto("Documento del pasajero: "), leerTexto("Servicio: "));
            Reserva r = ctrlReservas.buscarReserva(nro);
            System.out.println("Adicional contratado, el total quedó en " + usd(r.getTotal()));
        } else {
            System.out.println("Opción inválida");
        }
    }

    private void cancelarReserva() throws ReglaNegocioException {
        int nro = leerEntero("Número de reserva: ");
        String motivo = leerTexto("Motivo: ");
        boolean agencia = leerSiNo("¿La causa es de la agencia?");
        Cancelacion c = ctrlReservas.cancelarReserva(nro, motivo, agencia);
        System.out.println("Reserva anulada. Hay que devolver " + usd(c.getImporteDevuelto()) + " (" + medio(c) + ")");
    }

    private void verReserva() throws ReglaNegocioException {
        Reserva r = ctrlReservas.buscarReserva(leerEntero("Numero de reserva: "));
        Salida sal = r.getSalida();
        System.out.println("Reserva " + r.getNumero() + " del " + r.getFecha().format(FECHA) + " - " + r.getEstado().getDescripcion());
        System.out.println("Cliente: " + r.getCliente() + " | Vendedor: " + r.getVendedor().getNombreCompleto());
        System.out.println("Salida " + sal.getCodigo() + " - " + sal.getTour().getNombre() + " el " + sal.getFechaInicio().format(FECHA));
        System.out.println("Pasajeros:");
        for (Pasajero p : r.getPasajeros()) {
            String extras = "";
            for (ServicioAdicional sa : p.getServicios()) {
                extras += ", " + sa.getNombre();
            }
            System.out.println("  " + p + " - " + p.getTipo().getDescripcion() + extras);
        }
        if (r.getEstado() == EstadoReserva.ANULADA) {
            System.out.println("Total " + usd(r.getTotal()) + " | pagado " + usd(r.montoPagado()));
        } else {
            System.out.println("Total " + usd(r.getTotal()) + " | pagado " + usd(r.montoPagado()) + " | saldo " + usd(r.saldo()));
        }
        for (Pago pg : r.getPagos()) {
            String cuotas = pg.getCuotas() > 1 ? " en " + pg.getCuotas() + " cuotas" : "";
            System.out.println("  Pago del " + pg.getFecha().format(FECHA) + ": " + usd(pg.getImporte()) + " "
                    + pg.getMedio().getDescripcion().toLowerCase() + cuotas);
        }
        for (NotaAjuste n : r.getNotas()) {
            System.out.println("  Nota del " + n.getFecha().format(FECHA) + ": " + n.getDescripcion()
                    + (n.getImporte() != 0 ? " (" + usd(n.getImporte()) + ")" : ""));
        }
        if (r.getCancelacion() != null) {
            Cancelacion c = r.getCancelacion();
            System.out.println("Cancelada el " + c.getFecha().format(FECHA) + " por " + c.getMotivo() + ". Se devolvió "
                    + usd(c.getImporteDevuelto()) + " (" + medio(c) + ")");
        }
    }
}
