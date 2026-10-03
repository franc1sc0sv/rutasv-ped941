package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
/**
 * Cola FIFO.
 *
 * @param <T> tipo de elemento
 */
public interface Cola<T> {

    /** Agrega al final de la cola. O(1). */
    void encolar(T elemento);

    /** Quita y devuelve el primero. Lanza {@link ColeccionVaciaException} si esta vacia. O(1). */
    T desencolar();

    /** Devuelve el primero sin quitarlo. Lanza {@link ColeccionVaciaException} si esta vacia. O(1). */
    T frente();

    /** Indica si no hay elementos. O(1). */
    boolean estaVacia();

    /** Cantidad de elementos. O(1). */
    int tamano();
}
