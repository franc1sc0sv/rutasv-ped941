package edu.udb.rutasv.terminal.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.terminal.estructuras.ColaEnlazada;
import edu.udb.rutasv.terminal.estructuras.MonticuloBinario;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Las 5 unidades de la Fig. 10 deben salir U-014, U-031, U-009, U-022, U-005 (RN-4).
 *
 * <p>Las horas son ilustrativas (el documento no las trae en texto), pero elegidas para que el caso
 * sea exigente: U-009 es NORMAL y sale a las 07:05, antes que la EXPRES U-031 de las 07:30, asi que
 * ordenar solo por hora falla; y las unidades se registran desordenadas, asi que el orden de llegada
 * tampoco sirve.
 */
class DespachoFigura10Test {

    private static final String[] ORDEN_FIG_10 = {"U-014", "U-031", "U-009", "U-022", "U-005"};

    private Terminal terminal;

    @BeforeEach
    void registrarUnidadesDeLaFigura() {
        terminal = new Terminal("Terminal Occidente",
                new MonticuloBinario<UnidadTransporte>(new ReglaDespacho()), new ColaEnlazada<Pasajero>());
        terminal.registrarUnidad(unidad("U-009", TipoRuta.NORMAL, "07:05"));
        terminal.registrarUnidad(unidad("U-031", TipoRuta.EXPRES, "07:30"));
        terminal.registrarUnidad(unidad("U-005", TipoRuta.NORMAL, "07:40"));
        terminal.registrarUnidad(unidad("U-014", TipoRuta.EXPRES, "07:15"));
        terminal.registrarUnidad(unidad("U-022", TipoRuta.NORMAL, "07:20"));
    }

    private static UnidadTransporte unidad(String id, TipoRuta tipo, String hora) {
        return new UnidadTransporte(id, tipo, LocalTime.parse(hora));
    }

    @Test
    void verOrdenDespachoMuestraElOrdenDeLaFigura() {
        Lista<UnidadTransporte> orden = terminal.verOrdenDespacho();
        assertEquals(ORDEN_FIG_10.length, orden.tamano());
        for (int i = 0; i < ORDEN_FIG_10.length; i++) {
            assertEquals(ORDEN_FIG_10[i], orden.obtener(i).id(), "posicion " + (i + 1));
        }
    }

    @Test
    void despacharSacaLasUnidadesEnElOrdenDeLaFigura() {
        for (String esperado : ORDEN_FIG_10) {
            assertEquals(esperado, terminal.despacharUnidad().id());
        }
        assertFalse(terminal.hayUnidadesDisponibles());
        assertThrows(ColeccionVaciaException.class, terminal::despacharUnidad);
    }

    @Test
    void verElOrdenAntesDeDespacharNoCambiaLaSalida() {
        terminal.verOrdenDespacho();
        terminal.verOrdenDespacho();
        for (String esperado : ORDEN_FIG_10) {
            assertEquals(esperado, terminal.despacharUnidad().id());
        }
    }

    @Test
    void unaNormalTempranaNoAdelantaAUnaExpres() {
        // U-009 (NORMAL 07:05) es la hora mas temprana de todas, pero sale tercera.
        assertEquals("U-014", terminal.despacharUnidad().id());
        assertEquals("U-031", terminal.despacharUnidad().id());
        assertEquals("U-009", terminal.proximaUnidadASalir().id());
    }
}
