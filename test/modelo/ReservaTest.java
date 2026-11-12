package modelo;

import enums.EstadoReserva;
import enums.MedioPago;
import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoPasajero;
import estrategias.PoliticaCancelacionAgencia;
import estrategias.PoliticaCancelacionCliente;
import estrategias.Promocion;
import estrategias.PromocionGrupo;
import estrategias.PromocionReservaAnticipada;
import excepciones.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReservaTest {

    private final LocalDate hoy = LocalDate.now();
    private final Cliente cliente = new Cliente("Laura", "Gómez", "28999888", "1155551234",
            "laura@mail.com", "Argentina");
    private final Vendedor vendedor = new Vendedor("Pablo", "Ruiz", "25111222", 101, "1144443333", 0.05);
    private TourAventura tour;
    private Salida salida;
    private Reserva reserva;

    @BeforeEach
    void crearReserva() throws ReglaNegocioException {
        tour = new TourAventura("AVE01", "Glaciar Matanuska", "Trekking sobre el glaciar", 2, 4, 12,
                NivelDificultad.ALTO, 16);
        tour.agregarTarifa(Temporada.ALTA, hoy, hoy.plusDays(120), 1000.0);
        tour.agregarServicio("Seguro de viaje", 80.0, true);
        tour.agregarServicio("Alquiler de crampones", 35.0, false);
        salida = tour.programarSalida("S001", hoy.plusDays(40), 10);
        reserva = new Reserva(1, cliente, salida, vendedor);
    }

    private Pasajero pasajero(String nombre, String documento, LocalDate nacimiento) {
        return new Pasajero(nombre, "Pérez", documento, nacimiento, TipoPasajero.ADULTO,
                "Juan Pérez 1155550000", "");
    }

    private void agregarAdultos(Reserva r, int cantidad) throws ReglaNegocioException {
        for (int i = 0; i < cantidad; i++) {
            r.agregarPasajero(pasajero("Ana", "3000000" + i, LocalDate.of(1990, 3, 5)));
        }
    }

    @Test
    void agregarPasajeroAptoYNoApto() throws ReglaNegocioException {
        Pasajero ana = pasajero("Ana", "30111222", LocalDate.of(1990, 3, 5));
        reserva.agregarPasajero(ana);

        assertEquals(1, reserva.cantidadPasajeros());
        assertEquals(1, ana.getServicios().size());
        assertEquals("Seguro de viaje", ana.getServicios().get(0).getNombre());

        Pasajero menor = pasajero("Tomás", "50111222", hoy.minusYears(12));
        assertThrows(ReglaNegocioException.class, () -> reserva.agregarPasajero(menor));
        assertEquals(1, reserva.cantidadPasajeros());
        assertTrue(menor.getServicios().isEmpty());
    }

    @Test
    void calculoTotalConMejorPromocion() throws ReglaNegocioException {
        List<Promocion> promociones = new ArrayList<>();
        promociones.add(new PromocionReservaAnticipada(60, 0.15));
        promociones.add(new PromocionGrupo(6, 0.10));

        agregarAdultos(reserva, 2);
        assertEquals(2160.0, reserva.calcularTotal(promociones), 0.001);

        Reserva anticipada = new Reserva(2, cliente, tour.programarSalida("S002", hoy.plusDays(61), 10), vendedor);
        agregarAdultos(anticipada, 6);
        assertEquals(5580.0, anticipada.calcularTotal(promociones), 0.001);
        assertEquals(5580.0, anticipada.getTotal(), 0.001);
    }

    @Test
    void registroPagosYConfirmacionConSena() throws ReglaNegocioException {
        agregarAdultos(reserva, 2);
        reserva.calcularTotal(Collections.emptyList());

        reserva.registrarPago(500.0, MedioPago.EFECTIVO, 1, hoy);
        assertEquals(EstadoReserva.PENDIENTE, reserva.getEstado());
        assertEquals(1660.0, reserva.saldo(), 0.001);

        Pago pago = reserva.registrarPago(200.0, MedioPago.TARJETA, 3, hoy);
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
        assertEquals(14.0, pago.getRecargo(), 0.001);
        assertEquals(700.0, reserva.montoPagado(), 0.001);
        assertEquals(1460.0, reserva.saldo(), 0.001);

        assertThrows(ReglaNegocioException.class, () -> reserva.registrarPago(0, MedioPago.EFECTIVO, 1, hoy));
        assertThrows(ReglaNegocioException.class, () -> reserva.registrarPago(-50.0, MedioPago.EFECTIVO, 1, hoy));
        assertThrows(ReglaNegocioException.class, () -> reserva.registrarPago(1500.0, MedioPago.EFECTIVO, 1, hoy));
        assertThrows(ReglaNegocioException.class, () -> reserva.registrarPago(100.0, MedioPago.TARJETA, 0, hoy));
        assertEquals(2, reserva.getPagos().size());
    }

    @Test
    void cancelarReservaConDevolucion() throws ReglaNegocioException {
        agregarAdultos(reserva, 2);
        reserva.calcularTotal(Collections.emptyList());
        reserva.registrarPago(1000.0, MedioPago.TRANSFERENCIA, 1, hoy);
        reserva.registrarPago(500.0, MedioPago.TARJETA, 3, hoy);

        Cancelacion cancelacion = reserva.cancelar("Cambio de planes", hoy.plusDays(20), new PoliticaCancelacionCliente());

        assertEquals(750.0, cancelacion.getImporteDevuelto(), 0.001);
        assertEquals(MedioPago.TRANSFERENCIA, cancelacion.getMedio());
        assertEquals(EstadoReserva.ANULADA, reserva.getEstado());
        assertSame(cancelacion, reserva.getCancelacion());
        assertThrows(ReglaNegocioException.class,
                () -> reserva.cancelar("Otra vez", hoy, new PoliticaCancelacionCliente()));
        assertThrows(ReglaNegocioException.class, () -> reserva.registrarPago(100.0, MedioPago.EFECTIVO, 1, hoy));

        Reserva sinPagos = new Reserva(2, cliente, salida, vendedor);
        Cancelacion porAgencia = sinPagos.cancelar("Mal clima", hoy, new PoliticaCancelacionAgencia());
        assertEquals(0.0, porAgencia.getImporteDevuelto(), 0.001);
        assertNull(porAgencia.getMedio());
    }

    @Test
    void contratarServicioConNotaDeAjuste() throws ReglaNegocioException {
        agregarAdultos(reserva, 2);
        reserva.calcularTotal(Collections.emptyList());
        ServicioAdicional crampones = tour.getAdicionales().get(1);

        reserva.contratarServicio("30000000", crampones);
        assertEquals(2195.0, reserva.getTotal(), 0.001);
        assertTrue(reserva.getNotas().isEmpty());

        reserva.registrarPago(700.0, MedioPago.EFECTIVO, 1, hoy);
        reserva.contratarServicio("30000001", crampones);
        assertEquals(2230.0, reserva.getTotal(), 0.001);
        assertEquals(1, reserva.getNotas().size());
        assertEquals(35.0, reserva.getNotas().get(0).getImporte(), 0.001);

        assertThrows(ReglaNegocioException.class, () -> reserva.contratarServicio("30000000", crampones));
        assertThrows(ReglaNegocioException.class, () -> reserva.contratarServicio("99999999", crampones));
    }

    @Test
    void reemplazoPasajeroValidoEInvalido() throws ReglaNegocioException {
        agregarAdultos(reserva, 2);
        reserva.calcularTotal(Collections.emptyList());
        Pasajero martin = pasajero("Martín", "40222333", LocalDate.of(1995, 8, 1));
        Pasajero menor = pasajero("Tomás", "50111222", hoy.minusYears(12));

        assertThrows(ReglaNegocioException.class,
                () -> reserva.reemplazarPasajero("30000000", martin, hoy.plusDays(38)));
        assertThrows(ReglaNegocioException.class, () -> reserva.reemplazarPasajero("99999999", martin, hoy));
        assertThrows(ReglaNegocioException.class, () -> reserva.reemplazarPasajero("30000000", menor, hoy));

        reserva.reemplazarPasajero("30000000", martin, hoy.plusDays(37));

        assertSame(martin, reserva.getPasajeros().get(0));
        assertEquals(2, reserva.cantidadPasajeros());
        assertEquals(2160.0, reserva.getTotal(), 0.001);
        assertEquals(1, reserva.getNotas().size());
        assertEquals(0.0, reserva.getNotas().get(0).getImporte(), 0.001);
        assertEquals("Seguro de viaje", martin.getServicios().get(0).getNombre());
    }
}
