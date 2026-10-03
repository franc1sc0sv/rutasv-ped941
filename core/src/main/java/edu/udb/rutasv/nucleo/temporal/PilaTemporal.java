package edu.udb.rutasv.nucleo.temporal;

import edu.udb.rutasv.nucleo.error.ColeccionVaciaException;
import edu.udb.rutasv.nucleo.estructuras.Pila;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Pila respaldada por {@link ArrayDeque}.
 *
 * @param <T> tipo de elemento
 */
public class PilaTemporal<T> implements Pila<T> {

    private final Deque<T> datos = new ArrayDeque<>();

    @Override
    public void apilar(T elemento) {
        datos.addFirst(elemento);
    }

    @Override
    public T desapilar() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La pila esta vacia");
        }
        return datos.removeFirst();
    }

    @Override
    public T cima() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La pila esta vacia");
        }
        return datos.peekFirst();
    }

    @Override
    public boolean estaVacia() {
        return datos.isEmpty();
    }

    @Override
    public int tamano() {
        return datos.size();
    }
}
