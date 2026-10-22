package edu.udb.rutasv.busqueda.estructuras;

import edu.udb.rutasv.busqueda.servicios.Normalizador;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;

/**
 * Arbol AVL del autocompletado (rol Busqueda), ordenado por nombre normalizado.
 * Insertar, buscar y eliminar son O(L log n), con L = largo del nombre; conPrefijo es O(L log n + k).
 */
public class ArbolAvl implements IndiceNombres {

    private static final class Nodo {
        String clave;
        String nombre;
        Estacion estacion;
        int altura = 1;
        Nodo izq;
        Nodo der;

        Nodo(String clave, String nombre, Estacion estacion) {
            this.clave = clave;
            this.nombre = nombre;
            this.estacion = estacion;
        }
    }

    private Nodo raiz;

    /** Arbol AVL por nombre normalizado, con rotaciones LL, RR, LR y RL. */
    public ArbolAvl() {
        raiz = null;
    }

    @Override
    public void insertar(String nombre, Estacion estacion) {
        raiz = insertar(raiz, Normalizador.normalizar(nombre), nombre, estacion);
    }

    @Override
    public void eliminar(String nombre) {
        raiz = eliminar(raiz, Normalizador.normalizar(nombre));
    }

    @Override
    public Estacion buscar(String nombre) {
        String clave = Normalizador.normalizar(nombre);
        Nodo actual = raiz;
        while (actual != null) {
            int cmp = clave.compareTo(actual.clave);
            if (cmp == 0) {
                return actual.estacion;
            }
            actual = cmp < 0 ? actual.izq : actual.der;
        }
        return null;
    }

    @Override
    public Lista<String> conPrefijo(String texto) {
        Lista<String> resultado = new ListaTemporal<>();
        conPrefijo(raiz, Normalizador.normalizar(texto), resultado);
        return resultado;
    }

    int altura() {
        return altura(raiz);
    }

    /** Inorden acotado: solo baja a las ramas que pueden tener claves con el prefijo. */
    private void conPrefijo(Nodo nodo, String prefijo, Lista<String> resultado) {
        if (nodo == null) {
            return;
        }
        int cmp = nodo.clave.compareTo(prefijo);
        boolean coincide = nodo.clave.startsWith(prefijo);
        if (cmp > 0) {
            conPrefijo(nodo.izq, prefijo, resultado);
        }
        if (coincide) {
            resultado.agregar(nodo.nombre);
        }
        if (coincide || cmp < 0) {
            conPrefijo(nodo.der, prefijo, resultado);
        }
    }

    private Nodo insertar(Nodo nodo, String clave, String nombre, Estacion estacion) {
        if (nodo == null) {
            return new Nodo(clave, nombre, estacion);
        }
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izq = insertar(nodo.izq, clave, nombre, estacion);
        } else if (cmp > 0) {
            nodo.der = insertar(nodo.der, clave, nombre, estacion);
        } else {
            nodo.nombre = nombre;
            nodo.estacion = estacion;
            return nodo;
        }
        return balancear(nodo);
    }

    private Nodo eliminar(Nodo nodo, String clave) {
        if (nodo == null) {
            return null;
        }
        int cmp = clave.compareTo(nodo.clave);
        if (cmp < 0) {
            nodo.izq = eliminar(nodo.izq, clave);
        } else if (cmp > 0) {
            nodo.der = eliminar(nodo.der, clave);
        } else {
            if (nodo.izq == null) {
                return nodo.der;
            }
            if (nodo.der == null) {
                return nodo.izq;
            }
            Nodo sucesor = nodo.der;
            while (sucesor.izq != null) {
                sucesor = sucesor.izq;
            }
            nodo.clave = sucesor.clave;
            nodo.nombre = sucesor.nombre;
            nodo.estacion = sucesor.estacion;
            nodo.der = eliminar(nodo.der, sucesor.clave);
        }
        return balancear(nodo);
    }

    private Nodo balancear(Nodo nodo) {
        actualizarAltura(nodo);
        int balance = factorBalance(nodo);
        if (balance > 1) {
            if (factorBalance(nodo.izq) < 0) {
                nodo.izq = rotarIzquierda(nodo.izq);
            }
            return rotarDerecha(nodo);
        }
        if (balance < -1) {
            if (factorBalance(nodo.der) > 0) {
                nodo.der = rotarDerecha(nodo.der);
            }
            return rotarIzquierda(nodo);
        }
        return nodo;
    }

    private Nodo rotarDerecha(Nodo y) {
        Nodo x = y.izq;
        y.izq = x.der;
        x.der = y;
        actualizarAltura(y);
        actualizarAltura(x);
        return x;
    }

    private Nodo rotarIzquierda(Nodo x) {
        Nodo y = x.der;
        x.der = y.izq;
        y.izq = x;
        actualizarAltura(x);
        actualizarAltura(y);
        return y;
    }

    private static int altura(Nodo nodo) {
        return nodo == null ? 0 : nodo.altura;
    }

    private static int factorBalance(Nodo nodo) {
        return altura(nodo.izq) - altura(nodo.der);
    }

    private static void actualizarAltura(Nodo nodo) {
        nodo.altura = 1 + Math.max(altura(nodo.izq), altura(nodo.der));
    }
}
