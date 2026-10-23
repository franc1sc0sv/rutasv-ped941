package edu.udb.rutasv.historial.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.IndiceFueraDeRangoException;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Lista respaldada por un arreglo que duplica su capacidad al llenarse (rol Historial).
 * Agregar al final es O(1) amortizado; obtener por indice, O(1).
 */
public class ListaDinamica<T> implements Lista<T> {

    static final int CAPACIDAD_INICIAL = 8;

    private Object[] datos;
    private int tamano;

    /** Lista respaldada por un arreglo que crece al llenarse. */
    public ListaDinamica() {
        datos = new Object[CAPACIDAD_INICIAL];
    }

    @Override
    public void agregar(T elemento) {
        if (tamano == datos.length) {
            crecer();
        }
        datos[tamano++] = elemento;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T obtener(int indice) {
        validar(indice);
        return (T) datos[indice];
    }

    @Override
    public T eliminar(int indice) {
        T eliminado = obtener(indice);
        for (int i = indice; i < tamano - 1; i++) {
            datos[i] = datos[i + 1];
        }
        datos[--tamano] = null;
        return eliminado;
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
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private int siguiente;

            @Override
            public boolean hasNext() {
                return siguiente < tamano;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return obtener(siguiente++);
            }
        };
    }

    int capacidad() {
        return datos.length;
    }

    private void crecer() {
        Object[] nuevos = new Object[datos.length * 2];
        for (int i = 0; i < tamano; i++) {
            nuevos[i] = datos[i];
        }
        datos = nuevos;
    }

    private void validar(int indice) {
        if (indice < 0 || indice >= tamano) {
            throw new IndiceFueraDeRangoException("Indice " + indice + " fuera de rango (tamano " + tamano + ")");
        }
    }
}
