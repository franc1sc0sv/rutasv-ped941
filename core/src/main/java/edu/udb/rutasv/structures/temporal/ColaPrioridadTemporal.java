package edu.udb.rutasv.structures.temporal;

import edu.udb.rutasv.structures.ColaPrioridad;
import edu.udb.rutasv.structures.ColeccionVaciaException;
import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Cola de prioridad respaldada por {@link PriorityQueue}.
 *
 * @param <T> tipo de elemento
 */
public class ColaPrioridadTemporal<T> implements ColaPrioridad<T> {

    private final PriorityQueue<T> datos;

    public ColaPrioridadTemporal(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser null");
        }
        this.datos = new PriorityQueue<>(comparador);
    }

    @Override
    public void insertar(T elemento) {
        datos.add(elemento);
    }

    @Override
    public T extraer() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La cola de prioridad esta vacia");
        }
        return datos.poll();
    }

    @Override
    public T ver() {
        if (datos.isEmpty()) {
            throw new ColeccionVaciaException("La cola de prioridad esta vacia");
        }
        return datos.peek();
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
