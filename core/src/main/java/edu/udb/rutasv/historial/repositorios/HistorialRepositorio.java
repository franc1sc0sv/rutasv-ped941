package edu.udb.rutasv.historial.repositorios;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;

/** Guarda y carga el historial de rutas calculadas. */
public interface HistorialRepositorio {

    /** Carga todo el historial; lista vacia si no hay datos. */
    Lista<RutaCalculada> cargar();

    /** Agrega una ruta al final del historial. */
    void agregar(RutaCalculada ruta);
}
