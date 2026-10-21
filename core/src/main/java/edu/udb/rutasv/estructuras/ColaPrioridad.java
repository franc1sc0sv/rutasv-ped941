package edu.udb.rutasv.estructuras;

import java.util.Comparator;

public class ColaPrioridad<T> {
    private T[] arreglo;
    private int tamanio;
    private final Comparator<T> comparador;

    @SuppressWarnings("unchecked")
    public ColaPrioridad(Comparator<T> comparador) {
        if (comparador == null) {
            throw new IllegalArgumentException("El comparador no puede ser nulo.");
        }
        this.comparador = comparador;
        this.arreglo = (T[]) new Object[16];
        this.tamanio = 0;
    }

    public void insertar(T elemento) {
        if (elemento == null) {
            throw new IllegalArgumentException("Elemento nulo no permitido.");
        }
        if (tamanio == arreglo.length) {
            redimensionar();
        }
        arreglo[tamanio] = elemento;
        flotar(tamanio);
        tamanio++;
    }

    public T sacarMayor() {
        if (estaVacia()) return null;
        T max = arreglo[0];
        arreglo[0] = arreglo[tamanio - 1];
        arreglo[tamanio - 1] = null;
        tamanio--;
        hundir(0);
        return max;
    }

    public T sacarMenor() {
        return sacarMayor();
    }

    public T verMayor() {
        return estaVacia() ? null : arreglo[0];
    }

    public boolean estaVacia() {
        return tamanio == 0;
    }

    public int tamanio() {
        return tamanio;
    }

    @SuppressWarnings("unchecked")
    public ColaPrioridad<T> copia() {
        ColaPrioridad<T> nueva = new ColaPrioridad<>(this.comparador);
        nueva.arreglo = (T[]) new Object[this.arreglo.length];
        System.arraycopy(this.arreglo, 0, nueva.arreglo, 0, this.tamanio);
        nueva.tamanio = this.tamanio;
        return nueva;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        T[] nuevo = (T[]) new Object[arreglo.length * 2];
        System.arraycopy(arreglo, 0, nuevo, 0, tamanio);
        arreglo = nuevo;
    }

    private void flotar(int k) {
        while (k > 0) {
            int padre = (k - 1) / 2;
            if (comparador.compare(arreglo[k], arreglo[padre]) >= 0) break;
            intercambiar(k, padre);
            k = padre;
        }
    }

    private void hundir(int k) {
        while (2 * k + 1 < tamanio) {
            int j = 2 * k + 1;
            if (j + 1 < tamanio && comparador.compare(arreglo[j + 1], arreglo[j]) < 0) j++;
            if (comparador.compare(arreglo[k], arreglo[j]) <= 0) break;
            intercambiar(k, j);
            k = j;
        }
    }

    private void intercambiar(int i, int j) {
        T temp = arreglo[i];
        arreglo[i] = arreglo[j];
        arreglo[j] = temp;
    }
}
