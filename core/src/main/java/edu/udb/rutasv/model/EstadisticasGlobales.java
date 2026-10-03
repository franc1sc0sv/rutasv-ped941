package edu.udb.rutasv.model;

import java.util.List;

/** Estadisticas globales de busquedas de rutas. */
public record EstadisticasGlobales(long totalBusquedas, String rutaMasConsultada,
                                   long tiempoPromedioNs, List<String> top5) {
}
