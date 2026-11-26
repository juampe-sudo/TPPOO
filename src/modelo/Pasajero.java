package modelo;

import enums.TipoPasajero;
import excepciones.ReglaNegocioException;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Pasajero extends Persona {

    private LocalDate fechaNacimiento;
    private TipoPasajero tipo;
    private String contactoEmergencia;
    private String restricciones;
    private List<ServicioAdicional> servicios;

    public Pasajero(String nombre, String apellido, String documento, LocalDate fechaNacimiento,
                    TipoPasajero tipo, String contactoEmergencia, String restricciones) {
        super(nombre, apellido, documento);
        this.fechaNacimiento = fechaNacimiento;
        this.tipo = tipo;
        this.contactoEmergencia = contactoEmergencia;
        this.restricciones = restricciones;
        this.servicios = new ArrayList<>();
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public TipoPasajero getTipo() {
        return tipo;
    }

    public String getContactoEmergencia() {
        return contactoEmergencia;
    }

    public String getRestricciones() {
        return restricciones;
    }

    public List<ServicioAdicional> getServicios() {
        return Collections.unmodifiableList(servicios);
    }

    public int edadAl(LocalDate fecha) {
        return Period.between(fechaNacimiento, fecha).getYears();
    }

    public double calcularImporte(double precioBase, LocalDate fecha) {
        int edad = edadAl(fecha);
        if (edad < 3) {
            return 0.0;
        }
        if (edad < 12) {
            return precioBase * 0.60;
        }
        if (tipo == TipoPasajero.JUBILADO) {
            return precioBase * 0.90;
        }
        return precioBase;
    }

    public void agregarServicio(ServicioAdicional s) throws ReglaNegocioException {
        for (ServicioAdicional contratado : servicios) {
            if (contratado.getNombre().equals(s.getNombre())) {
                throw new ReglaNegocioException("El pasajero ya tiene contratado el servicio " + s.getNombre());
            }
        }
        servicios.add(s);
    }

    public double totalServicios() {
        double total = 0;
        for (ServicioAdicional s : servicios) {
            total += s.getPrecio();
        }
        return total;
    }
}
