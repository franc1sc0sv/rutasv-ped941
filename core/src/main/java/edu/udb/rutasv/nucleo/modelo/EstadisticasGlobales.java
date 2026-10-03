package edu.udb.rutasv.nucleo.modelo;

import java.util.List;

/** Estadisticas globales de busquedas de rutas. */
public record EstadisticasGlobales(long totalBusquedas, String rutaMasConsultada,
                                   long tiempoPromedioNs, List<String> top5) {
}
