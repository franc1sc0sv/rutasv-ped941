package edu.udb.rutasv.nucleo.excepciones;

/** El indice esta fuera del rango valido de la lista. */
public class IndiceFueraDeRangoException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public IndiceFueraDeRangoException(String mensaje) {
        super(mensaje);
    }
}
