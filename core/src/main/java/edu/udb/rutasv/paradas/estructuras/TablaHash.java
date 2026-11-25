package edu.udb.rutasv.paradas.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;

/**
 * Tabla hash propia (rol Paradas). Tambien la usa el login y la usaran Ruta e Historial.
 *
 * <p>Guarda los pares en un arreglo de cubos con <em>encadenamiento</em>: cada cubo es una lista
 * enlazada de nodos, asi que las colisiones no se pierden ni desplazan a otras claves. La funcion
 * hash mezcla los 16 bits altos del {@code hashCode} con los bajos antes de aplicar la mascara
 * {@code tamano - 1}; el arreglo siempre tiene tamano potencia de dos. Cuando la relacion entre
 * pares y cubos supera {@value #FACTOR_CARGA_MAXIMO}, se duplica la capacidad y se reinsertan
 * todos los nodos (rehash).
 *
 * <p>Ademas, cada nodo pertenece a una segunda lista doblemente enlazada que guarda el
 * <em>orden de insercion</em> (idea del PR #41). Asi {@code claves} recorre solo los pares vivos,
 * en O(n) aunque el arreglo tenga muchos cubos vacios, y devuelve siempre el mismo orden. Borrar
 * sigue siendo O(1) porque el nodo conoce a su anterior y a su siguiente en ese orden.
 *
 * <p>Complejidad: {@code poner}, {@code obtener}, {@code contiene} y {@code eliminar} son O(1) en
 * promedio y O(n) en el peor caso (todas las claves colisionan en el mismo cubo). El rehash es
 * O(n), pero amortizado no cambia el O(1) promedio de {@code poner}. {@code claves} es O(n) y
 * {@code tamano} es O(1).
 *
 * @param <K> tipo de la clave
 * @param <V> tipo del valor
 */
public class TablaHash<K, V> implements Mapa<K, V> {

    /** Capacidad inicial: potencia de dos para poder usar la mascara en vez de {@code %}. */
    private static final int CAPACIDAD_INICIAL = 16;

    /** Factor de carga maximo antes de duplicar el arreglo. */
    private static final double FACTOR_CARGA_MAXIMO = 0.75;

    /**
     * Capacidad maxima: 2^30, la ultima potencia de dos positiva. Duplicarla desbordaria
     * {@code int} (pasaria a negativo o a cero) y echaria a perder la tabla a medias.
     */
    static final int MAX_CAPACIDAD = 1 << 30;

    private Nodo<K, V>[] cubos;
    private int tamano;
    /** Primer y ultimo nodo en orden de insercion. */
    private Nodo<K, V> primero;
    private Nodo<K, V> ultimo;

    /** Nodo de la cadena de un cubo y, a la vez, de la lista de orden de insercion. */
    private static final class Nodo<K, V> {
        private final K clave;
        private V valor;
        private Nodo<K, V> siguiente;
        private Nodo<K, V> anteriorOrden;
        private Nodo<K, V> siguienteOrden;

        private Nodo(K clave, V valor, Nodo<K, V> siguiente) {
            this.clave = clave;
            this.valor = valor;
            this.siguiente = siguiente;
        }
    }

    /** Crea la tabla con la capacidad inicial por defecto. */
    public TablaHash() {
        this(CAPACIDAD_INICIAL);
    }

    /**
     * Crea la tabla con una capacidad inicial concreta (redondeada a potencia de dos). Util para
     * forzar colisiones o redimensiones en pruebas.
     *
     * @param capacidadInicial capacidad deseada, mayor que cero
     */
    @SuppressWarnings("unchecked")
    public TablaHash(int capacidadInicial) {
        if (capacidadInicial <= 0) {
            throw new IllegalArgumentException("La capacidad inicial debe ser mayor que cero");
        }
        this.cubos = (Nodo<K, V>[]) new Nodo[siguientePotenciaDeDos(capacidadInicial)];
        this.tamano = 0;
    }

    /**
     * Asocia la clave con el valor; si ya existia, reemplaza el valor sin aumentar el tamano.
     * O(1) promedio, O(n) peor caso; O(n) el rehash cuando se supera el factor de carga.
     */
    @Override
    public void poner(K clave, V valor) {
        int indice = indice(clave);
        for (Nodo<K, V> nodo = cubos[indice]; nodo != null; nodo = nodo.siguiente) {
            if (iguales(nodo.clave, clave)) {
                nodo.valor = valor;
                return;
            }
        }
        Nodo<K, V> nuevo = new Nodo<>(clave, valor, cubos[indice]);
        cubos[indice] = nuevo;
        enlazarAlFinal(nuevo);
        tamano++;
        if ((double) tamano / cubos.length > FACTOR_CARGA_MAXIMO) {
            redimensionar();
        }
    }

    /** Devuelve el valor de la clave o {@code null} si no existe. O(1) promedio, O(n) peor caso. */
    @Override
    public V obtener(K clave) {
        Nodo<K, V> nodo = buscar(clave);
        return nodo == null ? null : nodo.valor;
    }

    /** Indica si la clave existe, aunque su valor sea {@code null}. O(1) promedio, O(n) peor caso. */
    @Override
    public boolean contiene(K clave) {
        return buscar(clave) != null;
    }

    /** Quita la clave y devuelve su valor, o {@code null} si no existia. O(1) promedio, O(n) peor caso. */
    @Override
    public V eliminar(K clave) {
        int indice = indice(clave);
        Nodo<K, V> anterior = null;
        Nodo<K, V> actual = cubos[indice];
        while (actual != null) {
            if (iguales(actual.clave, clave)) {
                V valorAnterior = actual.valor;
                if (anterior == null) {
                    cubos[indice] = actual.siguiente;
                } else {
                    anterior.siguiente = actual.siguiente;
                }
                desenlazar(actual);
                tamano--;
                return valorAnterior;
            }
            anterior = actual;
            actual = actual.siguiente;
        }
        return null;
    }

    /**
     * Copia de las claves en orden de insercion. O(n): recorre la lista de orden, no los cubos,
     * asi que no depende de la capacidad. Cambiar la copia no cambia la tabla.
     */
    @Override
    public Lista<K> claves() {
        Lista<K> resultado = new ListaTemporal<>();
        for (Nodo<K, V> nodo = primero; nodo != null; nodo = nodo.siguienteOrden) {
            resultado.agregar(nodo.clave);
        }
        return resultado;
    }

    /** Cantidad de pares guardados. O(1). */
    @Override
    public int tamano() {
        return tamano;
    }

    /** Numero actual de cubos; util para comprobar el redimension en pruebas. */
    int capacidad() {
        return cubos.length;
    }

    /** Pone el nodo al final de la lista de orden. O(1). */
    private void enlazarAlFinal(Nodo<K, V> nodo) {
        nodo.anteriorOrden = ultimo;
        if (ultimo == null) {
            primero = nodo;
        } else {
            ultimo.siguienteOrden = nodo;
        }
        ultimo = nodo;
    }

    /** Saca el nodo de la lista de orden. O(1) porque conoce a sus dos vecinos. */
    private void desenlazar(Nodo<K, V> nodo) {
        if (nodo.anteriorOrden == null) {
            primero = nodo.siguienteOrden;
        } else {
            nodo.anteriorOrden.siguienteOrden = nodo.siguienteOrden;
        }
        if (nodo.siguienteOrden == null) {
            ultimo = nodo.anteriorOrden;
        } else {
            nodo.siguienteOrden.anteriorOrden = nodo.anteriorOrden;
        }
        nodo.anteriorOrden = null;
        nodo.siguienteOrden = null;
    }

    private Nodo<K, V> buscar(K clave) {
        for (Nodo<K, V> nodo = cubos[indice(clave)]; nodo != null; nodo = nodo.siguiente) {
            if (iguales(nodo.clave, clave)) {
                return nodo;
            }
        }
        return null;
    }

    /** Mezcla los bits altos y usa mascara porque la capacidad siempre es potencia de dos. */
    private int indice(K clave) {
        int hash = (clave == null) ? 0 : clave.hashCode();
        return indiceDe(hash, cubos.length);
    }

    /** Calculo puro del cubo; separado para poder probar la mezcla de bits altos. */
    static int indiceDe(int hash, int capacidad) {
        hash ^= (hash >>> 16);
        return hash & (capacidad - 1);
    }

    private static boolean iguales(Object a, Object b) {
        return a == b || (a != null && a.equals(b));
    }

    /** Duplica los cubos y reinserta los nodos existentes. O(n). */
    @SuppressWarnings("unchecked")
    private void redimensionar() {
        if (cubos.length >= MAX_CAPACIDAD) {
            // No se puede duplicar 2^30 en un int: se conserva el arreglo y se acepta una cadena mas larga.
            return;
        }
        Nodo<K, V>[] anteriores = cubos;
        cubos = (Nodo<K, V>[]) new Nodo[anteriores.length * 2];
        for (Nodo<K, V> cubo : anteriores) {
            Nodo<K, V> actual = cubo;
            while (actual != null) {
                Nodo<K, V> siguiente = actual.siguiente;
                int indice = indice(actual.clave);
                actual.siguiente = cubos[indice];
                cubos[indice] = actual;
                actual = siguiente;
            }
        }
    }

    static int siguientePotenciaDeDos(int n) {
        if (n > MAX_CAPACIDAD) {
            // Sin este tope, 1 << 31 se desborda a Integer.MIN_VALUE y el while nunca termina.
            return MAX_CAPACIDAD;
        }
        int potencia = 1;
        while (potencia < n) {
            potencia <<= 1;
        }
        return potencia;
    }
}
