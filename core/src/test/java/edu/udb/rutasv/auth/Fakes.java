package edu.udb.rutasv.auth;

import edu.udb.rutasv.nucleo.error.IndiceFueraDeRangoException;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.ListaFake;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** Dobles de prueba minimos, sin depender de las clases temporales. */
final class Fakes {
    private Fakes() {
    }

    static final class ListaFake<T> implements Lista<T> {
        private final List<T> datos = new ArrayList<>();

        public void agregar(T e) {
            datos.add(e);
        }

        public T obtener(int i) {
            if (i < 0 || i >= datos.size()) {
                throw new IndiceFueraDeRangoException("indice " + i);
            }
            return datos.get(i);
        }

        public T eliminar(int i) {
            obtener(i);
            return datos.remove(i);
        }

        public int tamano() {
            return datos.size();
        }

        public boolean estaVacia() {
            return datos.isEmpty();
        }

        public Iterator<T> iterator() {
            return datos.iterator();
        }
    }

    static final class MapaFake<K, V> implements Mapa<K, V> {
        private final Map<K, V> datos = new HashMap<>();

        public void poner(K k, V v) {
            datos.put(k, v);
        }

        public V obtener(K k) {
            return datos.get(k);
        }

        public boolean contiene(K k) {
            return datos.containsKey(k);
        }

        public V eliminar(K k) {
            return datos.remove(k);
        }

        public Lista<K> claves() {
            Lista<K> l = new ListaFake<>();
            datos.keySet().forEach(l::agregar);
            return l;
        }

        public int tamano() {
            return datos.size();
        }
    }
}
