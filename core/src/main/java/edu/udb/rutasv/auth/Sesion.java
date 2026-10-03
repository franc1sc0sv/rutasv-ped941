package edu.udb.rutasv.auth;

/**
 * Sesion activa de un usuario. Solo {@link AuthService} puede crearla: el constructor
 * no es publico y cada sesion lleva un token aleatorio que el servicio recuerda.
 * Una sesion cerrada o inventada no pasa la validacion.
 */
public final class Sesion {

    private final String usuario;
    private final Rol rol;
    private final String token;

    Sesion(String usuario, Rol rol, String token) {
        this.usuario = usuario;
        this.rol = rol;
        this.token = token;
    }

    /** Nombre del usuario. */
    public String usuario() {
        return usuario;
    }

    /** Rol con el que se emitio la sesion (la fuente de verdad es {@link AuthService#validar}). */
    public Rol rol() {
        return rol;
    }

    String token() {
        return token;
    }

    @Override
    public String toString() {
        return "Sesion[usuario=" + usuario + ", rol=" + rol + "]";
    }
}
