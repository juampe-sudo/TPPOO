package persistencia;

import enums.CategoriaTour;
import enums.TipoVehiculo;
import modelo.Administrador;
import modelo.Coordinador;
import modelo.Empleado;
import modelo.Guia;
import modelo.Vehiculo;
import modelo.Vendedor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CargadorDatosIniciales {

    private String archivo;

    public CargadorDatosIniciales(String archivo) {
        this.archivo = archivo;
    }

    public void cargar(Repositorio<Empleado> empleados, Repositorio<Vehiculo> vehiculos) {
        boolean cargarEmpleados = empleados.listar().isEmpty();
        boolean cargarVehiculos = vehiculos.listar().isEmpty();
        if (!cargarEmpleados && !cargarVehiculos) {
            return;
        }
        BufferedReader lector = null;
        try {
            lector = new BufferedReader(new FileReader(archivo, StandardCharsets.UTF_8));
            String linea = lector.readLine();
            while (linea != null) {
                if (!linea.isBlank()) {
                    String[] campos = linea.split(";");
                    if (campos[0].equals("VEHICULO")) {
                        if (cargarVehiculos) {
                            vehiculos.guardar(new Vehiculo(campos[1], Integer.parseInt(campos[2]),
                                    TipoVehiculo.valueOf(campos[3])));
                        }
                    } else if (cargarEmpleados) {
                        empleados.guardar(crearEmpleado(campos));
                    }
                }
                linea = lector.readLine();
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer el archivo " + archivo, e);
        } finally {
            if (lector != null) {
                try {
                    lector.close();
                } catch (IOException e) {
                    throw new IllegalStateException("No se pudo cerrar el archivo " + archivo, e);
                }
            }
        }
    }

    private Empleado crearEmpleado(String[] campos) {
        String tipo = campos[0];
        String nombre = campos[1];
        String apellido = campos[2];
        String documento = campos[3];
        int legajo = Integer.parseInt(campos[4]);
        String telefono = campos[5];
        if (tipo.equals("VENDEDOR")) {
            return new Vendedor(nombre, apellido, documento, legajo, telefono, Double.parseDouble(campos[6]));
        }
        if (tipo.equals("GUIA")) {
            List<String> idiomas = new ArrayList<>();
            for (String idioma : campos[6].split(",")) {
                idiomas.add(idioma);
            }
            Set<CategoriaTour> categorias = new HashSet<>();
            for (String categoria : campos[7].split(",")) {
                categorias.add(CategoriaTour.valueOf(categoria));
            }
            return new Guia(nombre, apellido, documento, legajo, telefono, idiomas, categorias,
                    Double.parseDouble(campos[8]));
        }
        if (tipo.equals("COORDINADOR")) {
            return new Coordinador(nombre, apellido, documento, legajo, telefono);
        }
        if (tipo.equals("ADMINISTRADOR")) {
            return new Administrador(nombre, apellido, documento, legajo, telefono);
        }
        throw new IllegalStateException("El archivo " + archivo + " tiene un registro de tipo desconocido: " + tipo);
    }
}
