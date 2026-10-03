package edu.udb.rutasv.auth.servicios;

import edu.udb.rutasv.auth.modelo.Usuario;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorio;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.ListaFake;

/** Repositorio de prueba que guarda en memoria. */
final class RepoEnMemoria implements UsuarioRepositorio {
    private final Lista<Usuario> guardados = new Fakes.ListaFake<>();
    int guardados() {
        return guardados.tamano();
    }

    @Override
    public Lista<Usuario> cargar() {
        Lista<Usuario> copia = new Fakes.ListaFake<>();
        guardados.forEach(copia::agregar);
        return copia;
    }

    @Override
    public void guardar(Lista<Usuario> usuarios) {
        while (!guardados.estaVacia()) {
            guardados.eliminar(0);
        }
        usuarios.forEach(guardados::agregar);
    }
}
