package rutasv.estructuras;

public class ColaFIFO<T> {

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

    public ColaFIFO() {
        this.frente = null;
        this.fin = null;
        this.tamano = 0;
    }

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

    public T desencolar() {
        if (estaVacia()) {
            return null;
        }
        T dato = frente.dato;
        frente = frente.siguiente;
        if (frente == null) {
            fin = null;
        }
        tamano--;
        return dato;
    }

    public T verFrente() {
        if (estaVacia()) {
            return null;
        }
        return frente.dato;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public int tamano() {
        return tamano;
    }
}
