package edu.udb.rutasv.historial.servicios;

import edu.udb.rutasv.nucleo.estructuras.impl.ListaDinamica;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorio;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.util.List;

/**
 * Historial de rutas calculadas por usuario (rol Historial, RN-7).
 * Carga el historial al crearse y guarda cada ruta nueva en el repositorio.
 *
 * <p>Guarda las rutas dos veces, por referencia: una lista dinamica con todas (para las
 * estadisticas) y una tabla hash usuario -> lista (para "mi historial"). Asi consultar el historial
 * de un usuario es O(1) promedio + O(k) para copiar sus k rutas, sin recorrer las de los demas.
 */
public class HistorialService {

    private final HistorialRepositorio repositorio;
    private final Lista<RutaCalculada> rutas = new ListaDinamica<>();
    private final Mapa<String, Lista<RutaCalculada>> porUsuario = new TablaHash<>();

    public HistorialService(HistorialRepositorio repositorio) {
        this.repositorio = repositorio;
        for (RutaCalculada ruta : repositorio.cargar()) {
            indexar(ruta);
        }
    }

    private void indexar(RutaCalculada ruta) {
        rutas.agregar(ruta);
        Lista<RutaCalculada> delUsuario = porUsuario.obtener(ruta.usuario());
        if (delUsuario == null) {
            delUsuario = new ListaDinamica<>();
            porUsuario.poner(ruta.usuario(), delUsuario);
        }
        delUsuario.agregar(ruta);
    }

    /** Guarda una ruta calculada; solo se llama cuando hubo ruta (RN-6). */
    public void registrar(RutaCalculada ruta) {
        RutaCalculada copia = new RutaCalculada(ruta.origen(), ruta.destino(), List.copyOf(ruta.tramos()),
                ruta.minutos(), ruta.km(), ruta.fecha(), ruta.tiempoCalculoNs(), ruta.usuario());
        repositorio.agregar(copia);
        indexar(copia);
    }

    /** Copia de todo el historial, en el orden en que se registro. */
    public Lista<RutaCalculada> todas() {
        Lista<RutaCalculada> copia = new ListaDinamica<>();
        for (RutaCalculada ruta : rutas) {
            copia.agregar(ruta);
        }
        return copia;
    }

    /** Rutas del usuario en el orden en que se registraron. O(1) promedio para hallarlas + O(k) para copiarlas. */
    public Lista<RutaCalculada> deUsuario(String usuario) {
        Lista<RutaCalculada> resultado = new ListaDinamica<>();
        Lista<RutaCalculada> delUsuario = usuario == null ? null : porUsuario.obtener(usuario);
        if (delUsuario != null) {
            for (RutaCalculada ruta : delUsuario) {
                resultado.agregar(ruta);
            }
        }
        return resultado;
    }
}
