package edu.udb.rutasv.model;

/** Un dato de entrada no cumple las reglas de negocio. */
public class ValidacionException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ValidacionException(String mensaje) {
        super(mensaje);
    }
}
