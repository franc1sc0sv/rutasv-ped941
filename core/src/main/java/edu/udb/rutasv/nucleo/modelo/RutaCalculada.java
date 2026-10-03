package edu.udb.rutasv.nucleo.modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Resultado de calcular una ruta. {@code tramos} son los codigos de estacion
 * en orden, desde el origen hasta el destino. {@code tiempoCalculoNs} es lo que tardo el calculo.
 */
public record RutaCalculada(String origen, String destino, List<String> tramos, int minutos,
                            double km, LocalDateTime fecha, long tiempoCalculoNs, String usuario) {
}
