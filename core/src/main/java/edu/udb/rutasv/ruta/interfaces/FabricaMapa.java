package edu.udb.rutasv.ruta.interfaces;

import edu.udb.rutasv.nucleo.estructuras.Mapa;

/**
 * Crea tablas hash vacias para los pesos y anteriores de Dijkstra. Desde {@code app} se pasa
 * {@code MapaTemporal::new} y, cuando este lista, {@code TablaHash::new} (rol Paradas).
 */
@FunctionalInterface
public interface FabricaMapa {

    <K, V> Mapa<K, V> crear();
}
