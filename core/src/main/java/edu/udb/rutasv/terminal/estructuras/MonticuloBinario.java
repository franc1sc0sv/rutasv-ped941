package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Cola de prioridad con monticulo binario sobre arreglo (rol Terminal).
 * El elemento menor segun el comparador sale primero.
 */
public class MonticuloBinario<T> implements ColaPrioridad<T> {
    private static final int CAPACIDAD_INICIAL = 10;

    private Object[] elementos;
    private int tamano;
    private final Comparator<T> comparador;

    public MonticuloBinario(Comparator<T> comparador) {
        if (comparador == null) throw new IllegalArgumentException("El comparador no puede ser nulo");
        this.comparador = comparador;
        this.elementos = new Object[CAPACIDAD_INICIAL];
        this.tamano = 0;
    }

    @Override
    public int tamano() {
        return tamano;
    }

    @Override
    public boolean estaVacia() {
        return tamano == 0;
    }

    @Override
    public void insertar(T elemento) {
        if (elemento == null) throw new IllegalArgumentException("No se permiten nulos");
        asegurarCapacidad();
        elementos[tamano] = elemento;
        subir(tamano);
        tamano++;
    }

    @Override
    public T ver() {
        if (estaVacia()) throw new ColeccionVaciaException("La cola de prioridad esta vacia");
        return dato(0);
    }

    @Override
    public T extraer() {
        if (estaVacia()) throw new ColeccionVaciaException("La cola de prioridad esta vacia");
        T raiz = dato(0);
        elementos[0] = elementos[tamano - 1];
        elementos[tamano - 1] = null;
        tamano--;
        if (tamano > 0) {
            bajar(0);
        }
        return raiz;
    }

    @SuppressWarnings("unchecked")
    private T dato(int i) {
        return (T) elementos[i];
    }

    private void intercambiar(int i, int j) {
        Object temp = elementos[i];
        elementos[i] = elementos[j];
        elementos[j] = temp;
    }

    private void subir(int i) {
        while (i > 0) {
            int padre = (i - 1) / 2;
            if (comparador.compare(dato(i), dato(padre)) < 0) {
                intercambiar(i, padre);
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

            if (der < tamano && comparador.compare(dato(der), dato(izq)) < 0) {
                menor = der;
            }

            if (comparador.compare(dato(menor), dato(i)) < 0) {
                intercambiar(i, menor);
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
