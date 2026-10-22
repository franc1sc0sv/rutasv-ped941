package edu.udb.rutasv.busqueda.servicios;

import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;

/**
 * Autocompletado de estaciones y busquedas recientes por usuario (rol Busqueda).
 * Las recientes viven en memoria: un arreglo por usuario, de la mas reciente a la mas antigua.
 */
public class BusquedaService {

    static final int MAX_RECIENTES = 5;

    private final IndiceNombres indice;
    private final Mapa<String, String[]> recientes;

    public BusquedaService(IndiceNombres indice, Mapa<String, String[]> recientes) {
        this.indice = indice;
        this.recientes = recientes;
    }

    /** Estaciones que empiezan con el texto; ignora espacios al inicio. Lanza IllegalArgumentException si es nulo. */
    public Lista<String> sugerir(String usuario, String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El texto de busqueda no puede ser nulo");
        }
        String limpio = texto.stripLeading();
        if (!Normalizador.normalizar(limpio).isEmpty()) {
            guardar(usuario, limpio);
        }
        return indice.conPrefijo(limpio);
    }

    public Lista<String> recientes(String usuario) {
        Lista<String> resultado = new ListaTemporal<>();
        String[] guardadas = recientes.obtener(usuario);
        if (guardadas != null) {
            for (String texto : guardadas) {
                if (texto != null) {
                    resultado.agregar(texto);
                }
            }
        }
        return resultado;
    }

    /**
     * Si el texto es el comienzo de la ultima (se esta borrando) no cambia nada; si la extiende (se esta
     * escribiendo) la reemplaza; si no, entra al inicio y una repetida sube sin duplicarse.
     */
    private void guardar(String usuario, String texto) {
        String[] guardadas = recientes.obtener(usuario);
        if (guardadas == null) {
            guardadas = new String[MAX_RECIENTES];
            recientes.poner(usuario, guardadas);
        }
        String clave = Normalizador.normalizar(texto);
        if (guardadas[0] != null) {
            String ultima = Normalizador.normalizar(guardadas[0]);
            if (ultima.startsWith(clave)) {
                return;
            }
            if (clave.startsWith(ultima)) {
                quitar(guardadas, 0);
            }
        }
        for (int i = 0; i < MAX_RECIENTES; i++) {
            if (guardadas[i] != null && Normalizador.normalizar(guardadas[i]).equals(clave)) {
                quitar(guardadas, i);
                break;
            }
        }
        for (int i = MAX_RECIENTES - 1; i > 0; i--) {
            guardadas[i] = guardadas[i - 1];
        }
        guardadas[0] = texto;
    }

    private static void quitar(String[] guardadas, int posicion) {
        for (int i = posicion; i < MAX_RECIENTES - 1; i++) {
            guardadas[i] = guardadas[i + 1];
        }
        guardadas[MAX_RECIENTES - 1] = null;
    }
}
