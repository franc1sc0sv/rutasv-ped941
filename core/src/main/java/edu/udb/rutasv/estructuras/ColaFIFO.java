package edu.udb.rutasv.estructuras;

import java.util.ArrayList;
import java.util.List;

public class ColaFIFO<T> {

    private static class Nodo<T> {
        T dato;
        Nodo<T> siguiente;

        Nodo(T dato) {
            this.dato = dato;
        }
    }

    private Nodo<T> frente;
    private Nodo<T> fin;
    private int tamanio;

    public ColaFIFO() {
        this.frente = null;
        this.fin = null;
        this.tamanio = 0;
    }

    public void encolar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("No se permiten elementos nulos.");
        }
        Nodo<T> nuevo = new Nodo<>(elemento);
        if (estaVacia()) {
            frente = nuevo;
        } else {
            fin.siguiente = nuevo;
        }
        fin = nuevo;
        tamanio++;
    }

    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamanio--;
        return dato;
    }

    public T verFrente() {
        return estaVacia() ? null : frente.dato;
    }

    public boolean estaVacia() {
        return frente == null;
    }

    public int tamanio() {
        return tamanio;
    }

    public List<T> aLista() {
        List<T> lista = new ArrayList<>();
        Nodo<T> actual = frente;
        while (actual != null) {
            lista.add(actual.dato);
            actual = actual.siguiente;
        }
        return lista;
    }
}
