package edu.udb.rutasv.paradas.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;

/**
 * Tabla hash propia (rol Paradas). Tambien la usa el login.
 * Implementación con encadenamiento y redimension al pasar el factor de carga 0.75.
 */
public class TablaHash<K, V> implements Mapa<K, V> {

    private static class Nodo<K, V> {
        K clave;
        V valor;
        Nodo<K, V> siguiente;

        Nodo(K clave, V valor) {
            this.clave = clave;
            this.valor = valor;
        }
    }

    private Nodo<K, V>[] cubos;
    private int capacidad;
    private int tamanio;
    private static final double FACTOR_CARGA = 0.75;

    @SuppressWarnings("unchecked")
    public TablaHash() {
        this.capacidad = 16;
        this.cubos = new Nodo[capacidad];
        this.tamanio = 0;
    }

    private int obtenerIndice(K clave) {
        if (clave == null) return 0;
        return Math.abs(clave.hashCode()) % capacidad;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        int nuevaCapacidad = capacidad * 2;
        Nodo<K, V>[] nuevosCubos = new Nodo[nuevaCapacidad];
        Nodo<K, V>[] antiguosCubos = cubos;

        cubos = nuevosCubos;
        capacidad = nuevaCapacidad;
        tamanio = 0;

        for (Nodo<K, V> cabeza : antiguosCubos) {
            Nodo<K, V> actual = cabeza;
            while (actual != null) {
                poner(actual.clave, actual.valor);
                actual = actual.siguiente;
            }
        }
    }

    @Override
    public void poner(K clave, V valor) {
        if ((double) (tamanio + 1) / capacidad > FACTOR_CARGA) {
            redimensionar();
        }

        int indice = obtenerIndice(clave);
        Nodo<K, V> actual = cubos[indice];

        while (actual != null) {
            if ((actual.clave == null && clave == null) || (actual.clave != null && actual.clave.equals(clave))) {
                actual.valor = valor;
                return;
            }
            actual = actual.siguiente;
        }

        Nodo<K, V> nuevoNodo = new Nodo<>(clave, valor);
        nuevoNodo.siguiente = cubos[indice];
        cubos[indice] = nuevoNodo;
        tamanio++;
    }

    @Override
    public V obtener(K clave) {
        int indice = obtenerIndice(clave);
        Nodo<K, V> actual = cubos[indice];

        while (actual != null) {
            if ((actual.clave == null && clave == null) || (actual.clave != null && actual.clave.equals(clave))) {
                return actual.valor;
            }
            actual = actual.siguiente;
        }
        return null;
    }

    @Override
    public boolean contiene(K clave) {
        return obtener(clave) != null;
    }

    @Override
    public V eliminar(K clave) {
        int indice = obtenerIndice(clave);
        Nodo<K, V> actual = cubos[indice];
        Nodo<K, V> anterior = null;

        while (actual != null) {
            if ((actual.clave == null && clave == null) || (actual.clave != null && actual.clave.equals(clave))) {
                if (anterior == null) {
                    cubos[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                tamanio--;
                return actual.valor;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    @Override
    public Lista<K> claves() {
        // Retorna las claves recolectadas
        // Nota: Dependiendo de la implementación de Lista en el núcleo, se pueden agregar aquí.
        throw new NoImplementadoException("TODO: TablaHash.claves");
    }

    @Override
    public int tamano() {
        return tamanio;
    }
}
