package persistencia;

import modelo.Cliente;
import modelo.Empleado;
import modelo.Guia;
import modelo.Reserva;
import modelo.Salida;
import modelo.Tour;
import modelo.Vehiculo;
import modelo.Vendedor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RepositorioArchivo<T> implements Repositorio<T> {

    private String archivo;
    private Map<String, T> datos;
    private Function<T, String> clave;

    public RepositorioArchivo(String archivo, Function<T, String> clave) {
        this.archivo = archivo;
        this.datos = new LinkedHashMap<>();
        this.clave = clave;
        cargar();
    }

    @Override
    public void guardar(T obj) {
        datos.put(clave.apply(obj), obj);
        escribir();
    }

    @Override
    public T buscar(String clave) {
        return datos.get(clave);
    }

    @Override
    public List<T> listar() {
        return new ArrayList<>(datos.values());
    }

    // cada .dat se lee por separado y trae sus propias copias, aca se reconectan con los objetos de cada repositorio
    public static void vincular(Repositorio<Tour> tours, Repositorio<Salida> salidas, Repositorio<Reserva> reservas,
                                Repositorio<Cliente> clientes, Repositorio<Empleado> empleados,
                                Repositorio<Vehiculo> vehiculos) {
        for (Salida s : salidas.listar()) {
            Tour tour = guardado(tours, s.getTour().getCodigo(), s.getTour());
            Guia guia = (Guia) empleadoGuardado(empleados, s.getGuia());
            Vehiculo vehiculo = s.getVehiculo();
            if (vehiculo != null) {
                vehiculo = guardado(vehiculos, vehiculo.getPatente(), vehiculo);
            }
            List<Reserva> propias = new ArrayList<>();
            for (Reserva r : s.getReservas()) {
                propias.add(guardado(reservas, String.valueOf(r.getNumero()), r));
            }
            s.revincular(tour, guia, vehiculo, propias);
        }
        for (Reserva r : reservas.listar()) {
            Cliente cliente = guardado(clientes, r.getCliente().getDocumento(), r.getCliente());
            Salida salida = guardado(salidas, r.getSalida().getCodigo(), r.getSalida());
            Vendedor vendedor = (Vendedor) empleadoGuardado(empleados, r.getVendedor());
            r.revincular(cliente, salida, vendedor);
        }
        for (Empleado e : empleados.listar()) {
            if (e instanceof Guia) {
                Guia guia = (Guia) e;
                List<Salida> propias = new ArrayList<>();
                for (Salida s : guia.getSalidas()) {
                    propias.add(guardado(salidas, s.getCodigo(), s));
                }
                guia.revincular(propias);
            }
        }
    }

    private static <E> E guardado(Repositorio<E> repositorio, String clave, E copia) {
        E encontrado = repositorio.buscar(clave);
        if (encontrado == null) {
            return copia;
        }
        return encontrado;
    }

    private static Empleado empleadoGuardado(Repositorio<Empleado> empleados, Empleado copia) {
        if (copia == null) {
            return null;
        }
        return guardado(empleados, String.valueOf(copia.getLegajo()), copia);
    }

    @SuppressWarnings("unchecked")
    private void cargar() {
        File origen = new File(archivo);
        if (!origen.exists()) {
            return;
        }
        ObjectInputStream lector = null;
        try {
            lector = new ObjectInputStream(new FileInputStream(origen));
            datos.putAll((Map<String, T>) lector.readObject());
        } catch (IOException | ClassNotFoundException e) {
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

    private void escribir() {
        File destino = new File(archivo);
        File carpeta = destino.getParentFile();
        if (carpeta != null && !carpeta.exists()) {
            carpeta.mkdirs();
        }
        ObjectOutputStream escritor = null;
        try {
            escritor = new ObjectOutputStream(new FileOutputStream(destino));
            escritor.writeObject(datos);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo guardar el archivo " + archivo, e);
        } finally {
            if (escritor != null) {
                try {
                    escritor.close();
                } catch (IOException e) {
                    throw new IllegalStateException("No se pudo cerrar el archivo " + archivo, e);
                }
            }
        }
    }
}
