package edu.udb.rutasv.historial.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import java.util.Iterator;

/**
 * Lista dinamica propia (rol Historial).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class ListaDinamica<T> implements Lista<T> {

    /** Lista respaldada por un arreglo que crece al llenarse. */
    public ListaDinamica() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void agregar(T elemento) {
        throw new NoImplementadoException("TODO: ListaDinamica.agregar");
    }

    @Override
    public T obtener(int indice) {
        throw new NoImplementadoException("TODO: ListaDinamica.obtener");
    }

    @Override
    public T eliminar(int indice) {
        throw new NoImplementadoException("TODO: ListaDinamica.eliminar");
    }

    @Override
    public int tamano() {
        throw new NoImplementadoException("TODO: ListaDinamica.tamano");
    }

    @Override
    public boolean estaVacia() {
        throw new NoImplementadoException("TODO: ListaDinamica.estaVacia");
    }

    @Override
    public Iterator<T> iterator() {
        throw new NoImplementadoException("TODO: ListaDinamica.iterator");
    }

}
