package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.error.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;

/** Red de estaciones como grafo dirigido con lista de adyacencia. */
public interface RedVial {

    /** Agrega o reemplaza una estacion. O(1) promedio. */
    void agregarEstacion(Estacion estacion);

    /**
     * Agrega un tramo dirigido. Lanza {@link ValidacionException} si {@code minutos <= 0} (RN-2)
     * o si alguna estacion no existe. O(1) promedio.
     */
    void agregarTramo(String origenCodigo, String destinoCodigo, int minutos, double km);

    /** Tramos que salen de la estacion; lista vacia si no tiene. O(1) promedio. */
    Lista<Arista> vecinos(String codigo);

    /** Todas las estaciones de la red. O(n). */
    Lista<Estacion> estaciones();
}
