package edu.udb.rutasv.auth.excepciones;

/** La sesion es nula o no es valida. */
public class SesionInvalidaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public SesionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
