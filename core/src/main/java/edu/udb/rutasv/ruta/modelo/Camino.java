package edu.udb.rutasv.ruta.modelo;

import edu.udb.rutasv.nucleo.estructuras.Lista;

/** Resultado de Dijkstra: codigos de estacion de origen a destino, minutos y km totales. */
public record Camino(Lista<String> estaciones, int minutos, double km) {
}
