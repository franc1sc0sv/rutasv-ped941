package edu.udb.rutasv.structures;

/**
 * Pila LIFO.
 *
 * @param <T> tipo de elemento
 */
public interface Pila<T> {

    /** Pone el elemento en la cima. O(1). */
    void apilar(T elemento);

    /** Quita y devuelve la cima. Lanza {@link ColeccionVaciaException} si esta vacia. O(1). */
    T desapilar();

    /** Devuelve la cima sin quitarla. Lanza {@link ColeccionVaciaException} si esta vacia. O(1). */
    T cima();

    /** Indica si no hay elementos. O(1). */
    boolean estaVacia();

    /** Cantidad de elementos. O(1). */
    int tamano();
}
