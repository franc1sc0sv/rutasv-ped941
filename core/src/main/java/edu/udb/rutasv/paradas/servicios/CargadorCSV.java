package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.modelo.Estacion;
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

    public static ResultadoCargaEstaciones cargarEstaciones(String rutaArchivo) {
        ResultadoCargaEstaciones resultado = new ResultadoCargaEstaciones();
        int numeroLinea = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();
                
                // Ignorar líneas vacías o comentarios
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 3) {
                    resultado.errores.add("Línea " + numeroLinea + ": formato incorrecto, se esperan al menos 3 columnas.");
                    continue;
                }

                String codigo = partes[0].trim();
                String nombre = partes[1].trim();
                String zona = partes[2].trim();
                boolean activa = true;

                if (partes.length >= 4) {
                    String estadoStr = partes[3].trim().toLowerCase();
                    if (estadoStr.equals("inactiva")) {
                        activa = false;
                    } else if (!estadoStr.equals("activa")) {
                        resultado.errores.add("Línea " + numeroLinea + ": estado inválido '" + partes[3] + "' (debe ser activa o inactiva).");
                        continue;
                    }
                }

                if (codigo.isEmpty() || nombre.isEmpty()) {
                    resultado.errores.add("Línea " + numeroLinea + ": el código y el nombre no pueden estar vacíos.");
                    continue;
                }

                resultado.estacionesValidas.add(new Estacion(codigo, nombre, zona, activa));
            }
        } catch (IOException e) {
            resultado.errores.add("Error al leer el archivo de estaciones: " + e.getMessage());
        }

        return resultado;
    }

    public static ResultadoCargaTramos cargarTramos(String rutaArchivo) {
        ResultadoCargaTramos resultado = new ResultadoCargaTramos();
        int numeroLinea = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                numeroLinea++;
                linea = linea.trim();

                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }

                String[] partes = linea.split(",");
                if (partes.length < 4) {
                    resultado.errores.add("Línea " + numeroLinea + ": formato incorrecto para tramo (origen, destino, minutos, km).");
                    continue;
                }

                try {
                    String origen = partes[0].trim();
                    String destino = partes[1].trim();
                    double minutos = Double.parseDouble(partes[2].trim());
                    double km = Double.parseDouble(partes[3].trim());

                    if (minutos <= 0 || km <= 0) {
                        resultado.errores.add("Línea " + numeroLinea + ": los minutos y kilómetros deben ser mayores que 0 (RN-2).");
                        continue;
                    }

                    if (origen.equals(destino)) {
                        resultado.errores.add("Línea " + numeroLinea + ": el origen y el destino no pueden ser iguales.");
                        continue;
                    }

                    resultado.tramosValidos.add(new TramoCSV(origen, destino, minutos, km));
                } catch (NumberFormatException e) {
                    resultado.errores.add("Línea " + numeroLinea + ": valor numérico inválido en minutos o kilómetros.");
                }
            }
        } catch (IOException e) {
            resultado.errores.add("Error al leer el archivo de tramos: " + e.getMessage());
        }

        return resultado;
    }
}
