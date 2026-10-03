package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;

/**
 * Grafo de la red vial (rol Ruta).
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class GrafoListaAdyacencia implements RedVial {

    /** Grafo ponderado y dirigido con lista de adyacencia. Rechaza minutos <= 0 (RN-2); un tramo por sentido (RN-3). */
    public GrafoListaAdyacencia() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void agregarEstacion(Estacion estacion) {
        throw new NoImplementadoException("TODO: GrafoListaAdyacencia.agregarEstacion");
    }

    @Override
    public void agregarTramo(String origenCodigo, String destinoCodigo, int minutos, double km) {
        throw new NoImplementadoException("TODO: GrafoListaAdyacencia.agregarTramo");
    }

    @Override
    public Lista<Arista> vecinos(String codigo) {
        throw new NoImplementadoException("TODO: GrafoListaAdyacencia.vecinos");
    }

    @Override
    public Lista<Estacion> estaciones() {
        throw new NoImplementadoException("TODO: GrafoListaAdyacencia.estaciones");
    }

}
