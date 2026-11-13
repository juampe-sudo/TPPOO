package controladores;

import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;
import modelo.Proveedor;
import modelo.Tour;
import persistencia.Repositorio;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class ControladorCatalogo {

    private Repositorio<Tour> tours;
    private Repositorio<Proveedor> proveedores;

    public ControladorCatalogo(Repositorio<Tour> tours, Repositorio<Proveedor> proveedores) {
        this.tours = tours;
        this.proveedores = proveedores;
    }

    public void registrarTour(Tour t) throws ReglaNegocioException {
        if (tours.buscar(t.getCodigo()) != null) {
            throw new ReglaNegocioException("Ya existe un tour con el código " + t.getCodigo());
        }
        if (t.getDuracionDias() < 1) {
            throw new ReglaNegocioException("La duración del tour tiene que ser de al menos un día");
        }
        if (t.getCupoMinimo() < 1 || t.getCupoMinimo() > t.getCupoMaximo()) {
            throw new ReglaNegocioException("El cupo mínimo tiene que ser mayor a 0 y no superar al máximo");
        }
        tours.guardar(t);
    }

    public void registrarProveedor(Proveedor p) throws ReglaNegocioException {
        if (proveedores.buscar(p.getEin()) != null) {
            throw new ReglaNegocioException("Ya existe un proveedor con el EIN " + p.getEin());
        }
        proveedores.guardar(p);
    }

    public void agregarActividad(String codTour, int dia, int orden, LocalTime horario, String desc, String lugar,
                                 TipoActividad tipo, String ein) throws ReglaNegocioException {
        Tour tour = buscarTour(codTour);
        Proveedor proveedor = null;
        if (ein != null && !ein.isBlank()) {
            proveedor = proveedores.buscar(ein);
            if (proveedor == null) {
                throw new ReglaNegocioException("No hay un proveedor registrado con el EIN " + ein);
            }
        }
        tour.agregarActividad(dia, orden, horario, desc, lugar, tipo, proveedor);
        tours.guardar(tour);
    }

    public void agregarTarifa(String codTour, Temporada temporada, LocalDate desde, LocalDate hasta, double precio)
            throws ReglaNegocioException {
        Tour tour = buscarTour(codTour);
        tour.agregarTarifa(temporada, desde, hasta, precio);
        tours.guardar(tour);
    }

    public void agregarServicio(String codTour, String nombre, double precio, boolean obligatorio)
            throws ReglaNegocioException {
        Tour tour = buscarTour(codTour);
        tour.agregarServicio(nombre, precio, obligatorio);
        tours.guardar(tour);
    }

    public List<Tour> listarTours() {
        return tours.listar();
    }

    public Proveedor buscarProveedor(String ein) {
        return proveedores.buscar(ein);
    }

    public Tour buscarTour(String codTour) throws ReglaNegocioException {
        Tour tour = tours.buscar(codTour);
        if (tour == null) {
            throw new ReglaNegocioException("No existe el tour " + codTour);
        }
        return tour;
    }
}
