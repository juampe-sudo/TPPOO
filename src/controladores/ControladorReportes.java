package controladores;

import excepciones.ReglaNegocioException;
import reportes.Reporte;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ControladorReportes {

    private List<Reporte> reportes;
    private String carpeta;

    public ControladorReportes(List<Reporte> reportes, String carpeta) {
        this.reportes = reportes;
        this.carpeta = carpeta;
    }

    public List<String> listarReportes() {
        List<String> titulos = new ArrayList<>();
        for (Reporte r : reportes) {
            titulos.add(r.getTitulo());
        }
        return titulos;
    }

    public List<String> emitir(int indice) throws ReglaNegocioException {
        if (indice < 0 || indice >= reportes.size()) {
            throw new ReglaNegocioException("No existe el reporte " + (indice + 1));
        }
        Reporte reporte = reportes.get(indice);
        List<String> lineas = reporte.generar();
        guardar(reporte.getTitulo(), lineas);
        return lineas;
    }

    private void guardar(String titulo, List<String> lineas) throws ReglaNegocioException {
        File destino = new File(carpeta);
        if (!destino.exists()) {
            destino.mkdirs();
        }
        String nombre = Normalizer.normalize(titulo, Normalizer.Form.NFD).replaceAll("\\p{M}", "")
                .toLowerCase().replace(' ', '_');
        File archivo = new File(destino, nombre + ".txt");
        try (BufferedWriter escritor = new BufferedWriter(new FileWriter(archivo, StandardCharsets.UTF_8))) {
            escritor.write(titulo + " - emitido el " + LocalDate.now());
            escritor.newLine();
            if (lineas.isEmpty()) {
                escritor.write("Sin datos");
                escritor.newLine();
            }
            for (String linea : lineas) {
                escritor.write(linea);
                escritor.newLine();
            }
        } catch (IOException e) {
            throw new ReglaNegocioException("No se pudo guardar el reporte en " + archivo);
        }
    }
}
