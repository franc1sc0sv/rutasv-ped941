package edu.udb.rutasv.busqueda.estructuras;

import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.modelo.Estacion;

/**
 * Arbol AVL del autocompletado (rol Busqueda).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class ArbolAvl implements IndiceNombres {

    /** Arbol AVL por nombre normalizado, con rotaciones LL, RR, LR y RL. */
    public ArbolAvl() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void insertar(String nombre, Estacion estacion) {
        throw new NoImplementadoException("TODO: ArbolAvl.insertar");
    }

    @Override
    public void eliminar(String nombre) {
        throw new NoImplementadoException("TODO: ArbolAvl.eliminar");
    }

    @Override
    public Estacion buscar(String nombre) {
        throw new NoImplementadoException("TODO: ArbolAvl.buscar");
    }

    @Override
    public Lista<String> conPrefijo(String texto) {
        throw new NoImplementadoException("TODO: ArbolAvl.conPrefijo");
    }

}
