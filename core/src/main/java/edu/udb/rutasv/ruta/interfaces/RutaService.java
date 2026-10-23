package edu.udb.rutasv.ruta.interfaces;

import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.util.Optional;

/** Calculo de la ruta mas rapida sobre la red vial. */
public interface RutaService {

    /**
     * Ruta mas rapida en minutos, con sus tramos, km, fecha y tiempo de calculo (System.nanoTime).
     * Vacio si no hay camino o si el origen o el destino estan inactivos (RN-6).
     * Lanza {@link ValidacionException} si algun codigo es nulo o no existe en la red.
     */
    Optional<RutaCalculada> calcular(String origenCodigo, String destinoCodigo, String usuario);
}
