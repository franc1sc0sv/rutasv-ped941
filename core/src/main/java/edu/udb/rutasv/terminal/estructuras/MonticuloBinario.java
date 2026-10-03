package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import java.util.Comparator;

/**
 * Cola de prioridad con monticulo (rol Terminal).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class MonticuloBinario<T> implements ColaPrioridad<T> {

    /** Monticulo binario sobre arreglo; el comparador define quien sale primero (RN-4). */
    public MonticuloBinario(Comparator<T> comparador) {
        // TODO: inicializa tu estructura
    }

    @Override
    public void insertar(T elemento) {
        throw new NoImplementadoException("TODO: MonticuloBinario.insertar");
    }

    @Override
    public T extraer() {
        throw new NoImplementadoException("TODO: MonticuloBinario.extraer");
    }

    @Override
    public T ver() {
        throw new NoImplementadoException("TODO: MonticuloBinario.ver");
    }

    @Override
    public boolean estaVacia() {
        throw new NoImplementadoException("TODO: MonticuloBinario.estaVacia");
    }

    @Override
    public int tamano() {
        throw new NoImplementadoException("TODO: MonticuloBinario.tamano");
    }

}
