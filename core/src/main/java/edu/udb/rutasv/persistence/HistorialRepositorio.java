package edu.udb.rutasv.persistence;

import edu.udb.rutasv.model.RutaCalculada;
import edu.udb.rutasv.structures.Lista;

/** Guarda y carga el historial de rutas calculadas. */
public interface HistorialRepositorio {

    /** Carga todo el historial; lista vacia si no hay datos. */
    Lista<RutaCalculada> cargar();

    /** Agrega una ruta al final del historial. */
    void agregar(RutaCalculada ruta);
}
