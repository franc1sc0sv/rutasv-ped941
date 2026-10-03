package edu.udb.rutasv.structures.temporal;

import edu.udb.rutasv.structures.IndiceFueraDeRangoException;
import edu.udb.rutasv.structures.Lista;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Lista respaldada por {@link ArrayList}.
 *
 * @param <T> tipo de elemento
 */
public class ListaTemporal<T> implements Lista<T> {

    private final List<T> datos = new ArrayList<>();

    @Override
    public void agregar(T elemento) {
        datos.add(elemento);
    }

    @Override
    public T obtener(int indice) {
        validar(indice);
        return datos.get(indice);
    }

    @Override
    public T eliminar(int indice) {
        validar(indice);
        return datos.remove(indice);
    }

    @Override
    public int tamano() {
        return datos.size();
    }

    @Override
    public boolean estaVacia() {
        return datos.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        return datos.iterator();
    }

    private void validar(int indice) {
        if (indice < 0 || indice >= datos.size()) {
            throw new IndiceFueraDeRangoException("Indice " + indice + " fuera de rango (tamano " + datos.size() + ")");
        }
    }
}
