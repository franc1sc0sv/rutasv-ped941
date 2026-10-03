package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.error.IndiceFueraDeRangoException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Lista de prueba respaldada por ArrayList. */
public final class ListaFake<T> implements Lista<T> {
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
