package vista;

import controladores.ControladorCatalogo;
import controladores.ControladorReportes;
import controladores.ControladorReservas;
import controladores.ControladorSalidas;
import enums.CategoriaTour;
import enums.EstadoReserva;
import enums.MedioPago;
import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoPasajero;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Actividad;
import modelo.Cliente;
import modelo.Cancelacion;
import modelo.Guia;
import modelo.Liquidacion;
import modelo.NotaAjuste;
import modelo.Pago;
import modelo.Pasajero;
import modelo.Proveedor;
import modelo.Reserva;
import modelo.Salida;
import modelo.ServicioAdicional;
import modelo.Tarifa;
import modelo.Tour;
import modelo.TourAventura;
import modelo.TourCultural;
import modelo.TourNaturaleza;
import modelo.Vehiculo;
import modelo.Vendedor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class MenuConsola {

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private ControladorCatalogo ctrlCatalogo;
    private ControladorReservas ctrlReservas;
    private ControladorSalidas ctrlSalidas;
    private ControladorReportes ctrlReportes;
    private Scanner scanner;
    private Vendedor vendedor;

    public MenuConsola(ControladorCatalogo ctrlCatalogo, ControladorReservas ctrlReservas,
                       ControladorSalidas ctrlSalidas, ControladorReportes ctrlReportes, Scanner scanner) {
        this.ctrlCatalogo = ctrlCatalogo;
        this.ctrlReservas = ctrlReservas;
        this.ctrlSalidas = ctrlSalidas;
        this.ctrlReportes = ctrlReportes;
        this.scanner = scanner;
    }

    public void iniciar() {
        int opcion;
        do {
            System.out.println();
            System.out.println("=== Rumbo Norte Alaska ===");
            System.out.println("1. Vendedor");
            System.out.println("2. Coordinador de operaciones");
            System.out.println("3. Administrador");
            System.out.println("0. Salir");
            opcion = leerEntero("Elegí tu rol: ");
            switch (opcion) {
                case 1 -> menuVendedor();
                case 2 -> menuCoordinador();
                case 3 -> menuAdministrador();
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
                    case 7 -> verSalidas();
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

    private void menuCoordinador() {
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
                    case 8 -> verSalidas();
                    case 0 -> {
                    }
                    default -> System.out.println("Opción inválida");
                }
            } catch (ReglaNegocioException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (opcion != 0);
    }

    private void menuAdministrador() {
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

    private void verSalidas() {
        List<Salida> salidas = ctrlSalidas.listarSalidas();
        if (salidas.isEmpty()) {
            System.out.println("No hay salidas programadas");
            return;
        }
        for (Salida s : salidas) {
            String guia = s.getGuia() == null ? "sin guía" : "guía " + s.getGuia().getNombreCompleto();
            String vehiculo = s.getVehiculo() == null ? "sin vehículo" : "vehículo " + s.getVehiculo().getPatente();
            System.out.println(s.getCodigo() + " | " + s.getTour().getNombre() + " | " + s.getFechaInicio().format(FECHA)
                    + " al " + s.getFechaFin().format(FECHA) + " | " + s.getEstado().getDescripcion() + " | "
                    + s.lugaresOcupados() + "/" + s.getCupoMaximo() + " lugares | " + guia + " | " + vehiculo);
        }
    }

    private String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return scanner.nextLine().trim();
    }

    private int leerEntero(String mensaje) {
        while (true) {
            try {
                return Integer.parseInt(leerTexto(mensaje));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número entero");
            }
        }
    }

    private double leerDouble(String mensaje) {
        while (true) {
            try {
                return Double.parseDouble(leerTexto(mensaje).replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("Tiene que ser un número, por ejemplo 150.50");
            }
        }
    }

    private LocalDate leerFecha(String mensaje) {
        while (true) {
            try {
                return LocalDate.parse(leerTexto(mensaje + " (dd/mm/aaaa): "), FECHA);
            } catch (DateTimeParseException e) {
                System.out.println("Fecha inválida");
            }
        }
    }

    private LocalTime leerHora(String mensaje) {
        while (true) {
            try {
                return LocalTime.parse(leerTexto(mensaje + " (hh:mm): "));
            } catch (DateTimeParseException e) {
                System.out.println("Hora inválida");
            }
        }
    }

    private boolean leerSiNo(String mensaje) {
        return leerTexto(mensaje + " (s/n): ").equalsIgnoreCase("s");
    }

    private <E extends Enum<E>> E elegir(String mensaje, E[] valores) {
        for (int i = 0; i < valores.length; i++) {
            System.out.println("  " + (i + 1) + ". " + valores[i].name());
        }
        while (true) {
            int opcion = leerEntero(mensaje);
            if (opcion >= 1 && opcion <= valores.length) {
                return valores[opcion - 1];
            }
            System.out.println("Opción inválida");
        }
    }

    private String medio(Cancelacion c) {
        return c.getMedio() == null ? "no tenía pagos" : c.getMedio().getDescripcion();
    }

    private String dias(int n) {
        return n == 1 ? "1 día" : n + " días";
    }

    private String usd(double importe) {
        return "USD " + String.format("%.2f", importe);
    }
}
