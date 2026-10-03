package edu.udb.rutasv.auth.modelo;

import edu.udb.rutasv.auth.interfaces.AuthService;
/**
 * Sesion activa de un usuario. Solo {@code AuthService} debe crearla. El constructor es
 * publico solo porque la clase vive en otra carpeta, pero eso no la hace falsificable:
 * cada sesion lleva un token aleatorio de 128 bits que el servicio recuerda y valida en
 * cada operacion. Una sesion inventada o cerrada no pasa {@code AuthService#validar}.
 */
public final class Sesion {

    private final String usuario;
    private final Rol rol;
    private final String token;

    public Sesion(String usuario, Rol rol, String token) {
        this.usuario = usuario;
        this.rol = rol;
        this.token = token;
    }

    /** Nombre del usuario. */
    public String usuario() {
        return usuario;
    }

    /** Rol con el que se emitio la sesion (la fuente de verdad es {@code AuthService#validar}). */
    public Rol rol() {
        return rol;
    }

    /** Token secreto de la sesion. No se muestra en {@code toString}. */
    public String token() {
        return token;
    }

    @Override
    public String toString() {
        return "Sesion[usuario=" + usuario + ", rol=" + rol + "]";
    }
}
