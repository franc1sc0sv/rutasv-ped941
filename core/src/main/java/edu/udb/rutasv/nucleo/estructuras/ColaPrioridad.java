package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.error.ColeccionVaciaException;
/**
 * Cola de prioridad. La implementacion recibe un {@code Comparator<T>} en su constructor;
 * el elemento menor segun ese comparador sale primero.
 *
 * @param <T> tipo de elemento
 */
public interface ColaPrioridad<T> {

    /** Inserta un elemento. O(log n). */
    void insertar(T elemento);

    /** Quita y devuelve el menor. Lanza {@link ColeccionVaciaException} si esta vacia. O(log n). */
    T extraer();

    /** Devuelve el menor sin quitarlo. Lanza {@link ColeccionVaciaException} si esta vacia. O(1). */
    T ver();

    /** Indica si no hay elementos. O(1). */
    boolean estaVacia();

    /** Cantidad de elementos. O(1). */
    int tamano();
}
