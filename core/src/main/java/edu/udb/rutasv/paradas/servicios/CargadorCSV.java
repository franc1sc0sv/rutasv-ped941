package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.nucleo.modelo.Estacion;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Carga de los CSV de datos (rol Paradas). Separador {@code ;}, como en {@code data/}:
 * {@code estaciones.csv} es codigo;nombre;zona;x;y (y opcional activa) y {@code tramos.csv} es
 * origen;destino;minutos;km. Una linea mala no detiene la carga: se anota con su numero.
 */
public class CargadorCSV {

    private static final String SEPARADOR = ";";

    public static class ResultadoCargaEstaciones {
        public List<Estacion> estacionesValidas = new ArrayList<>();
        public List<String> errores = new ArrayList<>();
    }

    public static class TramoCSV {
        public String origen;
        public String destino;
        public double minutos;
        public double km;

        public TramoCSV(String origen, String destino, double minutos, double km) {
            this.origen = origen;
            this.destino = destino;
            this.minutos = minutos;
            this.km = km;
        }
    }

    public static class ResultadoCargaTramos {
        public List<TramoCSV> tramosValidos = new ArrayList<>();
        public List<String> errores = new ArrayList<>();
    }

    public ResultadoCargaEstaciones cargarEstacionesDesdeCSV(String rutaArchivo) {
        ResultadoCargaEstaciones resultado = new ResultadoCargaEstaciones();

        try (BufferedReader br = Files.newBufferedReader(Path.of(rutaArchivo), StandardCharsets.UTF_8)) {
            String linea;
            boolean primeraLinea = true;
            int numeroLinea = 0;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                if (primeraLinea) {
                    primeraLinea = false;
                    String lower = linea.toLowerCase();
                    if (lower.contains("codigo") || lower.contains("nombre")) {
                        continue;
                    }
                }

                String[] partes = linea.split(SEPARADOR, -1);
                if (partes.length < 5) {
                    resultado.errores.add("Línea " + numeroLinea + ": Formato inválido, se esperaban al menos 5 campos (codigo;nombre;zona;x;y).");
                    continue;
                }

                String codigo = partes[0].trim();
                String nombre = partes[1].trim();
                String zona = partes[2].trim();

                if (codigo.isEmpty() || nombre.isEmpty()) {
                    resultado.errores.add("Línea " + numeroLinea + ": Código o Nombre vacío.");
                    continue;
                }

                try {
                    double x = Double.parseDouble(partes[3].trim());
                    double y = Double.parseDouble(partes[4].trim());
                    boolean activa = partes.length < 6 || partes[5].trim().isEmpty() || Boolean.parseBoolean(partes[5].trim());
                    resultado.estacionesValidas.add(new Estacion(codigo, nombre, zona, x, y, activa));
                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numeroLinea + ": Error numérico en las coordenadas x o y.");
                }
            }
        } catch (IOException e) {
            resultado.errores.add("Error al leer el archivo CSV de estaciones: " + e.getMessage());
        }

        return resultado;
    }

    public ResultadoCargaTramos cargarTramosDesdeCSV(String rutaArchivo) {
        ResultadoCargaTramos resultado = new ResultadoCargaTramos();

        try (BufferedReader br = Files.newBufferedReader(Path.of(rutaArchivo), StandardCharsets.UTF_8)) {
            String linea;
            boolean primeraLinea = true;
            int numeroLinea = 0;

            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                if (linea.trim().isEmpty()) continue;

                if (primeraLinea) {
                    primeraLinea = false;
                    String lower = linea.toLowerCase();
                    if (lower.contains("origen") || lower.contains("destino")) {
                        continue;
                    }
                }

                String[] partes = linea.split(SEPARADOR, -1);
                if (partes.length < 4) {
                    resultado.errores.add("Línea " + numeroLinea + ": Formato de tramo inválido (origen;destino;minutos;km).");
                    continue;
                }

                try {
                    String origen = partes[0].trim();
                    String destino = partes[1].trim();
                    double minutos = Double.parseDouble(partes[2].trim());
                    double km = Double.parseDouble(partes[3].trim());

                    if (origen.isEmpty() || destino.isEmpty()) {
                        resultado.errores.add("Línea " + numeroLinea + ": Origen o destino vacíos.");
                        continue;
                    }
                    if (minutos <= 0) {
                        resultado.errores.add("Línea " + numeroLinea + ": Los minutos deben ser mayores que 0 (RN-2).");
                        continue;
                    }

                    resultado.tramosValidos.add(new TramoCSV(origen, destino, minutos, km));
                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numeroLinea + ": Error numérico en minutos o kilómetros.");
                }
            }
        } catch (IOException e) {
            resultado.errores.add("Error al leer el archivo CSV de tramos: " + e.getMessage());
        }

        return resultado;
    }
}
