package edu.udb.rutasv.auth.excepciones;

/** La sesion no tiene el rol necesario (RN-1). */
public class AccesoDenegadoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public AccesoDenegadoException(String mensaje) {
        super(mensaje);
    }
}
