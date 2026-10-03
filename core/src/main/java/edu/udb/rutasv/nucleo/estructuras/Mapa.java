package edu.udb.rutasv.nucleo.estructuras;

/**
 * Mapa clave-valor (tabla hash propia del equipo).
 *
 * @param <K> tipo de clave
 * @param <V> tipo de valor
 */
public interface Mapa<K, V> {

    /** Asocia la clave con el valor; reemplaza si ya existia. O(1) promedio. */
    void poner(K clave, V valor);

    /** Devuelve el valor de la clave, o {@code null} si no existe. O(1) promedio. */
    V obtener(K clave);

    /** Indica si la clave existe. O(1) promedio. */
    boolean contiene(K clave);

    /** Quita la clave y devuelve su valor, o {@code null} si no existia. O(1) promedio. */
    V eliminar(K clave);

    /** Devuelve todas las claves, sin orden garantizado. O(n). */
    Lista<K> claves();

    /** Cantidad de pares. O(1). */
    int tamano();
}
