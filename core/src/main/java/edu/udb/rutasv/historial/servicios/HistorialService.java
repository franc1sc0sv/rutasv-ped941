package edu.udb.rutasv.historial.servicios;

import edu.udb.rutasv.historial.estructuras.ListaDinamica;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorio;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.util.List;

/**
 * Historial de rutas calculadas por usuario (rol Historial, RN-7).
 * Carga el historial al crearse y guarda cada ruta nueva en el repositorio.
 */
public class HistorialService {

    private final HistorialRepositorio repositorio;
    private final Lista<RutaCalculada> rutas = new ListaDinamica<>();

    public HistorialService(HistorialRepositorio repositorio) {
        this.repositorio = repositorio;
        for (RutaCalculada ruta : repositorio.cargar()) {
            rutas.agregar(ruta);
        }
    }

    /** Guarda una ruta calculada; solo se llama cuando hubo ruta (RN-6). */
    public void registrar(RutaCalculada ruta) {
        RutaCalculada copia = new RutaCalculada(ruta.origen(), ruta.destino(), List.copyOf(ruta.tramos()),
                ruta.minutos(), ruta.km(), ruta.fecha(), ruta.tiempoCalculoNs(), ruta.usuario());
        repositorio.agregar(copia);
        rutas.agregar(copia);
    }

    /** Copia de todo el historial, en el orden en que se registro. */
    public Lista<RutaCalculada> todas() {
        Lista<RutaCalculada> copia = new ListaDinamica<>();
        for (RutaCalculada ruta : rutas) {
            copia.agregar(ruta);
        }
        return copia;
    }

    /** Rutas del usuario en el orden en que se registraron. */
    public Lista<RutaCalculada> deUsuario(String usuario) {
        Lista<RutaCalculada> resultado = new ListaDinamica<>();
        for (RutaCalculada ruta : rutas) {
            if (ruta.usuario().equals(usuario)) {
                resultado.agregar(ruta);
            }
        }
        return resultado;
    }
}
