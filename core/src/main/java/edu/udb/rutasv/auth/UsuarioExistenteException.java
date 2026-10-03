package edu.udb.rutasv.auth;

/** El nombre de usuario ya esta registrado. */
public class UsuarioExistenteException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public UsuarioExistenteException(String mensaje) {
        super(mensaje);
    }
}
