package edu.udb.rutasv.persistence;

import edu.udb.rutasv.auth.Usuario;
import edu.udb.rutasv.structures.Lista;

/** Guarda y carga usuarios. */
public interface UsuarioRepositorio {

    /** Carga todos los usuarios; lista vacia si no hay datos. Falla con error claro si hay lineas mal formadas. */
    Lista<Usuario> cargar();

    /** Reemplaza todos los usuarios guardados, de forma atomica. */
    void guardar(Lista<Usuario> usuarios);
}
