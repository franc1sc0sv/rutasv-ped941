package edu.udb.rutasv.terminal.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.terminal.estructuras.ColaEnlazada;
import edu.udb.rutasv.terminal.estructuras.MonticuloBinario;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Terminal con las estructuras propias del rol: monticulo con la regla RN-4 y cola enlazada. */
class TerminalOrdenTest {

    private Terminal terminal;

    @BeforeEach
    void preparar() {
        terminal = new Terminal(
                "Terminal Oriente", new MonticuloBinario<UnidadTransporte>(new ReglaDespacho()), new ColaEnlazada<Pasajero>());
    }

    private static UnidadTransporte unidad(String id, TipoRuta tipo, String hora) {
        return new UnidadTransporte(id, tipo, LocalTime.parse(hora));
    }

    @Test
    void verOrdenDespachoNoVaciaLaCola() {
        terminal.registrarUnidad(unidad("N-1", TipoRuta.NORMAL, "06:00"));
        terminal.registrarUnidad(unidad("X-1", TipoRuta.EXPRES, "09:00"));
        terminal.registrarUnidad(unidad("N-2", TipoRuta.NORMAL, "07:00"));

        Lista<UnidadTransporte> orden = terminal.verOrdenDespacho();
        assertEquals(3, orden.tamano());
        assertEquals("X-1", orden.obtener(0).id());
        assertEquals("N-1", orden.obtener(1).id());
        assertEquals("N-2", orden.obtener(2).id());

        assertEquals("X-1", terminal.despacharUnidad().id());
        assertEquals("N-1", terminal.despacharUnidad().id());
        assertEquals("N-2", terminal.despacharUnidad().id());
    }

    @Test
    void verOrdenDeTerminalVaciaDevuelveListaVacia() {
        assertEquals(0, terminal.verOrdenDespacho().tamano());
    }

    @Test
    void noRegistraLaMismaUnidadDosVeces() {
        terminal.registrarUnidad(unidad("U-1", TipoRuta.NORMAL, "06:00"));
        assertThrows(ValidacionException.class, () -> terminal.registrarUnidad(unidad("U-1", TipoRuta.EXPRES, "07:00")));
    }

    @Test
    void unaUnidadDespachadaPuedeRegistrarseOtraVez() {
        terminal.registrarUnidad(unidad("U-1", TipoRuta.NORMAL, "06:00"));
        terminal.despacharUnidad();
        terminal.registrarUnidad(unidad("U-1", TipoRuta.NORMAL, "08:00"));
        assertEquals(LocalTime.parse("08:00"), terminal.proximaUnidadASalir().horaProgramada());
    }

    @Test
    void verFilaConservaElOrdenDeLlegada() {
        terminal.registrarPasajeroFila(new Pasajero("P-1", "Ana"));
        terminal.registrarPasajeroFila(new Pasajero("P-2", "Beto"));
        terminal.registrarPasajeroFila(new Pasajero("P-3", "Carla"));

        Lista<Pasajero> fila = terminal.verFila();
        assertEquals("P-1", fila.obtener(0).id());
        assertEquals("P-3", fila.obtener(2).id());
        assertEquals(3, terminal.totalPasajerosEnFila());
        assertEquals("P-1", terminal.atenderSiguientePasajero().id());
    }
}
