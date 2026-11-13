package controladores;

import enums.NivelDificultad;
import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Proveedor;
import modelo.Tour;
import modelo.TourAventura;
import modelo.TourCultural;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import persistencia.RepositorioEnMemoria;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ControladorCatalogoTest {

    private final LocalDate hoy = LocalDate.now();
    private RepositorioEnMemoria<Tour> tours;
    private ControladorCatalogo catalogo;

    @BeforeEach
    void crearControlador() throws ReglaNegocioException {
        tours = new RepositorioEnMemoria<>(Tour::getCodigo);
        catalogo = new ControladorCatalogo(tours, new RepositorioEnMemoria<>(Proveedor::getEin));
        catalogo.registrarTour(new TourCultural("CUL01", "Anchorage histórico", "Museos y centro histórico", 3, 4, 20));
    }

    @Test
    void registrarTourRechazaCodigoRepetidoYCuposInvalidos() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class, () -> catalogo.registrarTour(
                new TourCultural("CUL01", "Otro", "Otro tour", 1, 2, 10)));
        assertThrows(ReglaNegocioException.class, () -> catalogo.registrarTour(
                new TourCultural("CUL02", "Cupos al revés", "Mínimo mayor al máximo", 1, 12, 10)));
        assertThrows(ReglaNegocioException.class, () -> catalogo.registrarTour(
                new TourCultural("CUL03", "Sin días", "Duración en cero", 0, 2, 10)));

        catalogo.registrarTour(new TourAventura("AVE01", "Glaciar Matanuska", "Caminata sobre hielo", 2, 4, 12,
                NivelDificultad.ALTO, 16));

        assertEquals(2, catalogo.listarTours().size());
        assertNull(tours.buscar("CUL02"));
    }

    @Test
    void cargaItinerarioTarifasYServicios() throws ReglaNegocioException {
        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarActividad("CUL01", 1, 1,
                LocalTime.of(9, 0), "Museo de Anchorage", "Anchorage", TipoActividad.VISITA, "92-1234567"));

        Proveedor museo = new Proveedor("Anchorage Museum", "92-1234567", "907-555-0101", "Entradas");
        catalogo.registrarProveedor(museo);
        catalogo.agregarActividad("CUL01", 1, 2, LocalTime.of(13, 0), "Almuerzo", "Centro", TipoActividad.COMIDA, "");
        catalogo.agregarActividad("CUL01", 1, 1, LocalTime.of(9, 0), "Museo de Anchorage", "Anchorage",
                TipoActividad.VISITA, "92-1234567");
        catalogo.agregarTarifa("CUL01", Temporada.ALTA, hoy, hoy.plusDays(60), 900.0);
        catalogo.agregarServicio("CUL01", "Cena de despedida", 80.0, false);

        Tour tour = tours.buscar("CUL01");
        assertEquals("Museo de Anchorage", tour.getActividades().get(0).getDescripcion());
        assertSame(museo, tour.getActividades().get(0).getProveedor());
        assertNull(tour.getActividades().get(1).getProveedor());
        assertEquals(900.0, tour.getTarifaVigente(hoy.plusDays(10)).getPrecioBase(), 0.001);
        assertEquals("Cena de despedida", tour.getAdicionales().get(0).getNombre());

        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarTarifa("CUL01", Temporada.MEDIA,
                hoy.plusDays(30), hoy.plusDays(90), 700.0));
        assertThrows(ReglaNegocioException.class, () -> catalogo.registrarProveedor(museo));
    }

    @Test
    void rechazaOperacionesSobreUnTourInexistente() {
        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarTarifa("NAT09", Temporada.BAJA,
                hoy, hoy.plusDays(30), 500.0));
        assertThrows(ReglaNegocioException.class, () -> catalogo.agregarServicio("NAT09", "Kayak", 100.0, false));
    }
}
