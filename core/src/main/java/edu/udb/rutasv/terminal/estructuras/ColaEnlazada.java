package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;

/**
 * Fila FIFO de abordaje (rol Terminal).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class ColaEnlazada<T> implements Cola<T> {

    /** Cola FIFO con nodos enlazados. */
    public ColaEnlazada() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void encolar(T elemento) {
        throw new NoImplementadoException("TODO: ColaEnlazada.encolar");
    }

    @Override
    public T desencolar() {
        throw new NoImplementadoException("TODO: ColaEnlazada.desencolar");
    }

    @Override
    public T frente() {
        throw new NoImplementadoException("TODO: ColaEnlazada.frente");
    }

    @Override
    public boolean estaVacia() {
        throw new NoImplementadoException("TODO: ColaEnlazada.estaVacia");
    }

    @Override
    public int tamano() {
        throw new NoImplementadoException("TODO: ColaEnlazada.tamano");
    }

}
