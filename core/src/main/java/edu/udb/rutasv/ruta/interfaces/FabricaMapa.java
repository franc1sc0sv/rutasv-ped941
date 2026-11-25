package edu.udb.rutasv.ruta.interfaces;

import edu.udb.rutasv.nucleo.estructuras.Mapa;

/**
 * Crea tablas hash vacias para los pesos y anteriores de Dijkstra. Desde {@code app} se pasa
 * {@code TablaHash::new}; en pruebas se puede pasar otra implementacion de {@link Mapa}.
 */
@FunctionalInterface
public interface FabricaMapa {

    <K, V> Mapa<K, V> crear();
}
