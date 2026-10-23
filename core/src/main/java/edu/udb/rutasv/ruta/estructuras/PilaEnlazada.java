package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Pila;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;

/**
 * Pila para reconstruir la ruta de origen a destino (rol Ruta). Cada elemento vive en un nodo
 * que apunta al de abajo; la cima es el ultimo nodo apilado. Todas las operaciones son O(1).
 */
public class PilaEnlazada<T> implements Pila<T> {

    private static final class Nodo<T> {
        private final T dato;
        private final Nodo<T> abajo;

        private Nodo(T dato, Nodo<T> abajo) {
            this.dato = dato;
            this.abajo = abajo;
        }
    }

    private Nodo<T> cima;
    private int tamano;

    /** Pila con nodos enlazados, sin arreglos ni java.util. */
    public PilaEnlazada() {
        cima = null;
        tamano = 0;
    }

    @Override
    public void apilar(T elemento) {
        cima = new Nodo<>(elemento, cima);
        tamano++;
    }

    @Override
    public T desapilar() {
        T dato = cima();
        cima = cima.abajo;
        tamano--;
        return dato;
    }

    @Override
    public T cima() {
        if (cima == null) {
            throw new ColeccionVaciaException("La pila esta vacia");
        }
        return cima.dato;
    }

    @Override
    public boolean estaVacia() {
        return cima == null;
    }

    @Override
    public int tamano() {
        return tamano;
    }
}
