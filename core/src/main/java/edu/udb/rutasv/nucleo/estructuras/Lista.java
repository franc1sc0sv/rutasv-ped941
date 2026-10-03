package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.excepciones.IndiceFueraDeRangoException;
/**
 * Lista dinamica de elementos.
 *
 * @param <T> tipo de elemento
 */
public interface Lista<T> extends Iterable<T> {

    /** Agrega al final. O(1) amortizado. */
    void agregar(T elemento);

    /** Devuelve el elemento en la posicion dada (base 0). Lanza {@link IndiceFueraDeRangoException} si no existe. */
    T obtener(int indice);

    /** Quita y devuelve el elemento en la posicion dada. Lanza {@link IndiceFueraDeRangoException} si no existe. */
    T eliminar(int indice);

    /** Cantidad de elementos. O(1). */
    int tamano();

    /** Indica si no hay elementos. O(1). */
    boolean estaVacia();
}
