package modelo;

import enums.CategoriaTour;
import enums.Temporada;
import enums.TipoActividad;
import excepciones.ReglaNegocioException;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Tour {

    private String codigo;
    private String nombre;
    private String descripcion;
    private int duracionDias;
    private int cupoMinimo;
    private int cupoMaximo;
    private List<Actividad> actividades;
    private List<Tarifa> tarifas;
    private List<ServicioAdicional> adicionales;

    protected Tour(String codigo, String nombre, String descripcion, int duracionDias,
                   int cupoMinimo, int cupoMaximo) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.duracionDias = duracionDias;
        this.cupoMinimo = cupoMinimo;
        this.cupoMaximo = cupoMaximo;
        this.actividades = new ArrayList<>();
        this.tarifas = new ArrayList<>();
        this.adicionales = new ArrayList<>();
    }

    public abstract CategoriaTour getCategoria();

    public String getCodigo() {
        return codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getDuracionDias() {
        return duracionDias;
    }

    public int getCupoMinimo() {
        return cupoMinimo;
    }

    public int getCupoMaximo() {
        return cupoMaximo;
    }

    public List<Actividad> getActividades() {
        return Collections.unmodifiableList(actividades);
    }

    public List<Tarifa> getTarifas() {
        return Collections.unmodifiableList(tarifas);
    }

    public List<ServicioAdicional> getAdicionales() {
        return Collections.unmodifiableList(adicionales);
    }

    public void agregarActividad(int dia, int orden, LocalTime horario, String descripcion, String lugar,
                                 TipoActividad tipo, Proveedor proveedor) {
        actividades.add(new Actividad(dia, orden, horario, descripcion, lugar, tipo, proveedor));
        Collections.sort(actividades);
    }

    public void agregarTarifa(Temporada temporada, LocalDate desde, LocalDate hasta, double precio)
            throws ReglaNegocioException {
        if (desde.isAfter(hasta)) {
            throw new ReglaNegocioException("La vigencia de la tarifa empieza después de terminar");
        }
        for (Tarifa t : tarifas) {
            if (!desde.isAfter(t.getVigenciaHasta()) && !hasta.isBefore(t.getVigenciaDesde())) {
                throw new ReglaNegocioException("La vigencia se superpone con otra tarifa del tour");
            }
        }
        tarifas.add(new Tarifa(temporada, desde, hasta, precio));
    }

    public void agregarServicio(String nombre, double precio, boolean obligatorio) {
        adicionales.add(new ServicioAdicional(nombre, precio, obligatorio));
    }

    public Tarifa getTarifaVigente(LocalDate fecha) throws ReglaNegocioException {
        for (Tarifa t : tarifas) {
            if (t.estaVigente(fecha)) {
                return t;
            }
        }
        throw new ReglaNegocioException("El tour " + codigo + " no tiene una tarifa vigente para el " + fecha);
    }

    public boolean esApto(Pasajero p, LocalDate fecha) {
        return true;
    }

    public List<ServicioAdicional> getServiciosObligatorios() {
        List<ServicioAdicional> obligatorios = new ArrayList<>();
        for (ServicioAdicional s : adicionales) {
            if (s.isObligatorio()) {
                obligatorios.add(s);
            }
        }
        return obligatorios;
    }

    public Salida programarSalida(String codigo, LocalDate inicio, int cupo) throws ReglaNegocioException {
        if (inicio.isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("La fecha de inicio de la salida ya pasó");
        }
        getTarifaVigente(inicio);
        if (cupo < cupoMinimo || cupo > cupoMaximo) {
            throw new ReglaNegocioException("El cupo tiene que estar entre " + cupoMinimo + " y " + cupoMaximo);
        }
        LocalDate fin = inicio.plusDays(duracionDias - 1);
        return new Salida(codigo, this, inicio, fin, cupoMinimo, cupo);
    }
}
