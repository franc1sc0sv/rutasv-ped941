package edu.udb.rutasv.paradas.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;

/**
 * Tabla hash propia (rol Paradas). Tambien la usa el login.
 * ESQUELETO: reemplaza cada TODO por tu implementacion, sin usar java.util para guardar los datos.
 * Cuando pase el test de contrato, quita el @Disabled de la prueba correspondiente.
 */
public class TablaHash<K, V> implements Mapa<K, V> {

    /** Tabla hash con encadenamiento y redimension al pasar el factor de carga 0.75. */
    public TablaHash() {
        // TODO: inicializa tu estructura
    }

    @Override
    public void poner(K clave, V valor) {
        throw new NoImplementadoException("TODO: TablaHash.poner");
    }

    @Override
    public V obtener(K clave) {
        throw new NoImplementadoException("TODO: TablaHash.obtener");
    }

    @Override
    public boolean contiene(K clave) {
        throw new NoImplementadoException("TODO: TablaHash.contiene");
    }

    @Override
    public V eliminar(K clave) {
        throw new NoImplementadoException("TODO: TablaHash.eliminar");
    }

    @Override
    public Lista<K> claves() {
        throw new NoImplementadoException("TODO: TablaHash.claves");
    }

    @Override
    public int tamano() {
        throw new NoImplementadoException("TODO: TablaHash.tamano");
    }

}
