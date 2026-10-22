package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;

/** Cola FIFO con nodos enlazados (rol Terminal). */
public class ColaEnlazada<T> implements Cola<T> {

    private static class Nodo<E> {
        E dato;
        Nodo<E> siguiente;

        Nodo(E dato) {
            this.dato = dato;
            this.siguiente = null;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamano;

    public ColaEnlazada() {
        this.frente = null;
        this.fin = null;
        this.tamano = 0;
    }

    @Override
    public void encolar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se permiten elementos nulos");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamano++;
    }

    @Override
    public T desencolar() {
        if (estaVacia()) {
            throw new ColeccionVaciaException("La cola esta vacia");
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return dato;
    }

    @Override
    public T frente() {
        if (estaVacia()) {
            throw new ColeccionVaciaException("La cola esta vacia");
        }
        return frente.dato;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public int tamano() {
        return tamano;
    }
}
