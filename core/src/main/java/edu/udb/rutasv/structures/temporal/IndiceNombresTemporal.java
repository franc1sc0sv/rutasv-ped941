package edu.udb.rutasv.structures.temporal;

import edu.udb.rutasv.model.Estacion;
import edu.udb.rutasv.structures.IndiceNombres;
import edu.udb.rutasv.structures.Lista;
import java.text.Normalizer;
import java.util.Locale;
import java.util.TreeMap;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Indice respaldado por {@link TreeMap}; la clave se normaliza sin acentos ni mayusculas.
 */
public class IndiceNombresTemporal implements IndiceNombres {

    private record Entrada(String nombre, Estacion estacion) {
    }

    private final TreeMap<String, Entrada> datos = new TreeMap<>();

    @Override
    public void insertar(String nombre, Estacion estacion) {
        datos.put(normalizar(nombre), new Entrada(nombre, estacion));
    }

    @Override
    public void eliminar(String nombre) {
        datos.remove(normalizar(nombre));
    }

    @Override
    public Estacion buscar(String nombre) {
        Entrada e = datos.get(normalizar(nombre));
        return e == null ? null : e.estacion();
    }

    @Override
    public Lista<String> conPrefijo(String texto) {
        String prefijo = normalizar(texto);
        Lista<String> resultado = new ListaTemporal<>();
        for (var par : datos.tailMap(prefijo, true).entrySet()) {
            if (!par.getKey().startsWith(prefijo)) {
                break;
            }
            resultado.agregar(par.getValue().nombre());
        }
        return resultado;
    }

    private static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String sinAcentos = Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}+", "");
        return sinAcentos.toLowerCase(Locale.ROOT);
    }
}
