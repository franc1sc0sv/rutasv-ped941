package edu.udb.rutasv.busqueda.servicios;

import edu.udb.rutasv.nucleo.texto.Normalizador;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.impl.ListaDinamica;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;

/**
 * Autocompletado de estaciones y busquedas recientes por usuario (rol Busqueda). El autocompletado
 * usa el arbol AVL; las recientes son las rutas que el usuario calculo, en una tabla hash
 * usuario -> arreglo fijo de {@value #MAX_RECIENTES}, de la mas nueva a la mas vieja.
 */
public class BusquedaService {

    static final int MAX_RECIENTES = 5;

    private final IndiceNombres indice;
    private final Mapa<String, String[]> recientes;

    public BusquedaService(IndiceNombres indice, Mapa<String, String[]> recientes) {
        this.indice = indice;
        this.recientes = recientes;
    }

    /**
     * Estaciones activas cuyo nombre empieza con el texto (sin importar mayusculas ni tildes).
     * Solo consulta el arbol AVL: O(log n + k), con k sugerencias. Lanza ValidacionException si es nulo.
     */
    public Lista<String> sugerir(String texto) {
        if (texto == null) {
            throw new ValidacionException("El texto de busqueda no puede ser nulo");
        }
        return indice.conPrefijo(texto.strip());
    }

    /**
     * Guarda una busqueda hecha por el usuario (por ejemplo "Metrocentro → Soyapango") al inicio de
     * sus recientes. Si ya estaba, sube al inicio sin duplicarse. Se guardan como maximo
     * {@value #MAX_RECIENTES}: arreglo fijo, O(1) en memoria y en tiempo por usuario.
     */
    public void registrarBusqueda(String usuario, String texto) {
        if (usuario == null || texto == null || texto.isBlank()) {
            return;
        }
        String limpio = texto.strip();
        String[] guardadas = recientes.obtener(usuario);
        if (guardadas == null) {
            guardadas = new String[MAX_RECIENTES];
            recientes.poner(usuario, guardadas);
        }
        String clave = Normalizador.normalizar(limpio);
        int posicion = MAX_RECIENTES - 1;
        for (int i = 0; i < MAX_RECIENTES; i++) {
            if (guardadas[i] != null && Normalizador.normalizar(guardadas[i]).equals(clave)) {
                posicion = i;
                break;
            }
        }
        for (int i = posicion; i > 0; i--) {
            guardadas[i] = guardadas[i - 1];
        }
        guardadas[0] = limpio;
    }

    /** Recientes del usuario, de la mas nueva a la mas vieja. */
    public Lista<String> recientes(String usuario) {
        Lista<String> resultado = new ListaDinamica<>();
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
}
