package edu.udb.rutasv.paradas.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.IndiceFueraDeRangoException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Tabla propia con encadenamiento separado y carga maxima de 0.75.
 * Admite clave y valores null, igual que el adaptador anterior.
 * Buscar/eliminar: O(1) esperado, O(n) peor caso; insertar: O(1) esperado
 * amortizado, O(n) al redimensionar o con colisiones. Hash/equals se suponen O(1).
 * No es segura para acceso concurrente sin sincronizacion externa.
 */
public class TablaHash<K, V> implements Mapa<K, V> {
    private static final class Nodo<K, V> {
        final K clave;
        V valor;
        Nodo<K, V> siguiente;
        Nodo<K, V> anteriorOrden;
        Nodo<K, V> siguienteOrden;
        Nodo(K clave, V valor, Nodo<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    private Nodo<K, V>[] cubetas = crearCubetas(16);
    private int tamano;
    private Nodo<K, V> primero;

    @SuppressWarnings("unchecked")
    private static <K, V> Nodo<K, V>[] crearCubetas(int capacidad) {
        return (Nodo<K, V>[]) new Nodo<?, ?>[capacidad];
    }

    private int indice(K clave) {
        int hash = clave == null ? 0 : clave.hashCode();
        return (hash ^ (hash >>> 16)) & (cubetas.length - 1);
    }

    private Nodo<K, V> buscar(K clave) {
        for (Nodo<K, V> n = cubetas[indice(clave)]; n != null; n = n.siguiente) {
            if (Objects.equals(clave, n.clave)) return n;
        }
        return null;
    }

    @Override
    public void poner(K clave, V valor) {
        Nodo<K, V> existente = buscar(clave);
        if (existente != null) {
            existente.valor = valor;
            return;
        }
        if (tamano + 1 > cubetas.length - cubetas.length / 4) crecer();
        int i = indice(clave);
        Nodo<K, V> nuevo = new Nodo<>(clave, valor, cubetas[i]);
        cubetas[i] = nuevo;
        nuevo.siguienteOrden = primero;
        if (primero != null) primero.anteriorOrden = nuevo;
        primero = nuevo;
        tamano++;
    }

    private void crecer() {
        Nodo<K, V>[] anteriores = cubetas;
        cubetas = crearCubetas(anteriores.length * 2);
        for (Nodo<K, V> cabeza : anteriores) {
            for (Nodo<K, V> n = cabeza; n != null;) {
                Nodo<K, V> siguiente = n.siguiente;
                int i = indice(n.clave);
                n.siguiente = cubetas[i];
                cubetas[i] = n;
                n = siguiente;
            }
        }
    }

    @Override
    public V obtener(K clave) {
        Nodo<K, V> n = buscar(clave);
        return n == null ? null : n.valor;
    }

    @Override
    public boolean contiene(K clave) { return buscar(clave) != null; }

    @Override
    public V eliminar(K clave) {
        int i = indice(clave);
        Nodo<K, V> anterior = null;
        for (Nodo<K, V> n = cubetas[i]; n != null; n = n.siguiente) {
            if (Objects.equals(clave, n.clave)) {
                if (anterior == null) cubetas[i] = n.siguiente;
                else anterior.siguiente = n.siguiente;
                if (n.anteriorOrden == null) primero = n.siguienteOrden;
                else n.anteriorOrden.siguienteOrden = n.siguienteOrden;
                if (n.siguienteOrden != null) n.siguienteOrden.anteriorOrden = n.anteriorOrden;
                tamano--;
                return n.valor;
            }
            anterior = n;
        }
        return null;
    }

    /** Copia independiente O(n), recorriendo solo las entradas vivas. */
    @Override
    public Lista<K> claves() {
        Lista<K> copia = new ListaClaves<>();
        for (Nodo<K, V> n = primero; n != null; n = n.siguienteOrden) copia.agregar(n.clave);
        return copia;
    }

    @Override
    public int tamano() { return tamano; }

    int capacidad() { return cubetas.length; }

    /** Copia con arreglo propio para no acoplar Paradas al modulo Historial. */
    private static final class ListaClaves<T> implements Lista<T> {
        private Object[] datos = new Object[8];
        private int cantidad;
        public void agregar(T elemento) {
            if (cantidad == datos.length) {
                Object[] nuevos = new Object[datos.length * 2];
                System.arraycopy(datos, 0, nuevos, 0, cantidad);
                datos = nuevos;
            }
            datos[cantidad++] = elemento;
        }
        @SuppressWarnings("unchecked")
        public T obtener(int indice) {
            if (indice < 0 || indice >= cantidad) throw new IndiceFueraDeRangoException("Indice fuera de rango: " + indice);
            return (T) datos[indice];
        }
        public T eliminar(int indice) {
            T valor = obtener(indice);
            System.arraycopy(datos, indice + 1, datos, indice, cantidad - indice - 1);
            datos[--cantidad] = null;
            return valor;
        }
        public int tamano() { return cantidad; }
        public boolean estaVacia() { return cantidad == 0; }
        public Iterator<T> iterator() {
            return new Iterator<>() {
                private int siguiente;
                public boolean hasNext() { return siguiente < cantidad; }
                public T next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return obtener(siguiente++);
                }
            };
        }
    }
}
