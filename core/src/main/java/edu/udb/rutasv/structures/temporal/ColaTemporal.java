package edu.udb.rutasv.structures.temporal;

import edu.udb.rutasv.structures.Cola;
import edu.udb.rutasv.structures.ColeccionVaciaException;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Cola respaldada por {@link ArrayDeque}.
 *
 * @param <T> tipo de elemento
 */
public class ColaTemporal<T> implements Cola<T> {

    private final Deque<T> datos = new ArrayDeque<>();

    @Override
    public void encolar(T elemento) {
        datos.addLast(elemento);
    }

    @Override
    public T desencolar() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La cola esta vacia");
        }
        return datos.removeFirst();
    }

    @Override
    public T frente() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La cola esta vacia");
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
