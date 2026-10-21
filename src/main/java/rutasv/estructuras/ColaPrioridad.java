package rutasv.estructuras;

import java.util.Comparator;
import java.util.Arrays;

public class ColaPrioridad<T> {
    private Object[] elementos;
    private int tamano;
    private Comparator<T> comparador;

    @SuppressWarnings("unchecked")
    public ColaPrioridad(Comparator<T> comparador) {
        this.comparador = comparador;
        this.elementos = new Object[10];
        this.tamano = 0;
    }

    public int tamano() {
        return tamano;
    }

    public boolean estaVacia() {
        return tamano == 0;
    }

    public void insertar(T elemento) {
        if (elemento == null) throw new IllegalArgumentException("No se permiten nulos");
        asegurarCapacidad();
        elementos[tamano] = elemento;
        subir(tamano);
        tamano++;
    }

    @SuppressWarnings("unchecked")
    public T verMayor() {
        if (estaVacia()) return null;
        return (T) elementos[0];
    }

    @SuppressWarnings("unchecked")
    public T sacarMayor() {
        if (estaVacia()) return null;
        T raiz = (T) elementos[0];
        elementos[0] = elementos[tamano - 1];
        elementos[tamano - 1] = null;
        tamano--;
        if (tamano > 0) {
            bajar(0);
        }
        return raiz;
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            @SuppressWarnings("unchecked")
            T hijoVal = (T) elementos[i];
            @SuppressWarnings("unchecked")
            T padreVal = (T) elementos[padre];

            if (comparador.compare(hijoVal, padreVal) < 0) {
                Object temp = elementos[i];
                elementos[i] = elementos[padre];
                elementos[padre] = temp;
                i = padre;
            } else {
                break;
            }
        }
    }

    private void bajar(int i) {
        while (2 * i + 1 < tamano) {
            int izq = 2 * i + 1;
            int der = izq + 1;
            int menor = izq;

            if (der < tamano) {
                @SuppressWarnings("unchecked")
                T izqVal = (T) elementos[izq];
                @SuppressWarnings("unchecked")
                T derVal = (T) elementos[der];
                if (comparador.compare(derVal, izqVal) < 0) {
                    menor = der;
                }
            }

            @SuppressWarnings("unchecked")
            T actualVal = (T) elementos[i];
            @SuppressWarnings("unchecked")
            T menorVal = (T) elementos[menor];

            if (comparador.compare(menorVal, actualVal) < 0) {
                Object temp = elementos[i];
                elementos[i] = elementos[menor];
                elementos[menor] = temp;
                i = menor;
            } else {
                break;
            }
        }
    }

    private void asegurarCapacidad() {
        if (tamano >= elementos.length) {
            elementos = Arrays.copyOf(elementos, elementos.length * 2);
        }
    }
}
