package edu.udb.rutasv.nucleo.excepciones;

/** Se pidio un elemento a una coleccion vacia. */
public class ColeccionVaciaException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ColeccionVaciaException(String mensaje) {
        super(mensaje);
    }
}
