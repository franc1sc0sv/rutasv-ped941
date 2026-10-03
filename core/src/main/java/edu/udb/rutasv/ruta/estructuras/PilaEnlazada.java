package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Pila;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;

/**
 * Pila para reconstruir la ruta de origen a destino (rol Ruta).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class PilaEnlazada<T> implements Pila<T> {

    /** Pila con nodos enlazados, sin arreglos ni java.util. */
    public PilaEnlazada() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void apilar(T elemento) {
        throw new NoImplementadoException("TODO: PilaEnlazada.apilar");
    }

    @Override
    public T desapilar() {
        throw new NoImplementadoException("TODO: PilaEnlazada.desapilar");
    }

    @Override
    public T cima() {
        throw new NoImplementadoException("TODO: PilaEnlazada.cima");
    }

    @Override
    public boolean estaVacia() {
        throw new NoImplementadoException("TODO: PilaEnlazada.estaVacia");
    }

    @Override
    public int tamano() {
        throw new NoImplementadoException("TODO: PilaEnlazada.tamano");
    }

}
