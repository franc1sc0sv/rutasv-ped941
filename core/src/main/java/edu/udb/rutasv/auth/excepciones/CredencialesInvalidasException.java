package edu.udb.rutasv.auth.excepciones;

/** Usuario o contrasena incorrectos (mismo mensaje para ambos casos). */
public class CredencialesInvalidasException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
