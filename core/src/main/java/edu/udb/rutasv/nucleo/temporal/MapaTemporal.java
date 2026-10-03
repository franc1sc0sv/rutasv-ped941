package edu.udb.rutasv.nucleo.temporal;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import java.util.HashMap;
import java.util.Map;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Mapa respaldado por {@link HashMap}.
 *
 * @param <K> tipo de clave
 * @param <V> tipo de valor
 */
public class MapaTemporal<K, V> implements Mapa<K, V> {

    private final Map<K, V> datos = new HashMap<>();

    @Override
    public void poner(K clave, V valor) {
        datos.put(clave, valor);
    }

    @Override
    public V obtener(K clave) {
        return datos.get(clave);
    }

    @Override
    public boolean contiene(K clave) {
        return datos.containsKey(clave);
    }

    @Override
    public V eliminar(K clave) {
        return datos.remove(clave);
    }

    @Override
    public Lista<K> claves() {
        Lista<K> resultado = new ListaTemporal<>();
        for (K k : datos.keySet()) {
            resultado.agregar(k);
        }
        return resultado;
    }

    @Override
    public int tamano() {
        return datos.size();
    }
}
