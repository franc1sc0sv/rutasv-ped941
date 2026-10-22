package edu.udb.rutasv.terminal.servicios;

import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import java.util.Comparator;

/**
 * Regla de despacho RN-4 (rol Terminal): las unidades exprés salen antes que las normales; entre
 * unidades del mismo tipo sale primero la de hora programada más próxima; si todo empata, decide
 * el id para que el orden sea siempre el mismo.
 *
 * <p>Se pasa como {@code Comparator} a la cola de prioridad de la terminal.
 */
public class ReglaDespacho implements Comparator<UnidadTransporte> {

    @Override
    public int compare(UnidadTransporte a, UnidadTransporte b) {
        if (a.tipoRuta() != b.tipoRuta()) {
            return a.tipoRuta() == TipoRuta.EXPRES ? -1 : 1;
        }
        int porHora = a.horaProgramada().compareTo(b.horaProgramada());
        if (porHora != 0) {
            return porHora;
        }
        return a.id().compareTo(b.id());
    }
}
