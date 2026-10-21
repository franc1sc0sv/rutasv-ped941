package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.paradas.modelo.Estacion;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CargadorCSV {

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
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
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
                        continue; // Ignorar cabecera
                    }
                }

                String[] partes = linea.split(",");
                if (partes.length < 3) {
                    resultado.errores.add("Línea " + numeroLinea + ": Formato inválido, se esperaban al menos 3 campos.");
                    continue;
                }

                String codigo = partes[0].trim();
                String nombre = partes[1].trim();
                String zona = partes[2].trim();
                boolean activa = true;

                if (partes.length >= 4) {
                    activa = Boolean.parseBoolean(partes[3].trim());
                }

                if (codigo.isEmpty() || nombre.isEmpty()) {
                    resultado.errores.add("Línea " + numeroLinea + ": Código o Nombre vacío.");
                    continue;
                }

                resultado.estacionesValidas.add(new Estacion(codigo, nombre, zona, activa));
            }
        } catch (IOException e) {
            resultado.errores.add("Error al leer el archivo CSV de estaciones: " + e.getMessage());
        }

        return resultado;
    }

    public ResultadoCargaTramos cargarTramosDesdeCSV(String rutaArchivo) {
        ResultadoCargaTramos resultado = new ResultadoCargaTramos();

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
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
                        continue; // Ignorar cabecera
                    }
                }

                String[] partes = linea.split(",");
                if (partes.length < 4) {
                    resultado.errores.add("Línea " + numeroLinea + ": Formato de tramo inválido (se esperan origen, destino, minutos, km).");
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
