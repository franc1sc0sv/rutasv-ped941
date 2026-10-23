package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.busqueda.servicios.BusquedaService;
import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.paradas.servicios.DirectorioEstaciones;
import edu.udb.rutasv.ruta.interfaces.RutaService;
import edu.udb.rutasv.terminal.servicios.Terminal;

/**
 * Servicios de cada rol que usa la fachada. Un campo {@code null} es un modulo aun no conectado:
 * sus metodos lanzan {@code NoImplementadoException}. Cuando un rol conecta su modulo, agrega su
 * campo aqui y lo arma en {@code Aplicacion}.
 *
 * @param filas fila FIFO de pasajeros por tipo de ruta (RN-5)
 */
public record Modulos(BusquedaService busqueda, RedVial red, RutaService ruta, DirectorioEstaciones directorio,
                      Terminal terminal, Mapa<TipoRuta, Cola<Pasajero>> filas) {

    /** Ningun modulo conectado: solo funciona el login. */
    public static Modulos ninguno() {
        return new Modulos(null, null, null, null, null, null);
    }
}
