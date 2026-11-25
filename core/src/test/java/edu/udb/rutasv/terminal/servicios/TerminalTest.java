package edu.udb.rutasv.terminal.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.nucleo.estructuras.impl.MonticuloBinario;
import java.time.LocalTime;
import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalTest {

    private Terminal terminal;

    @BeforeEach
    void preparar() {
        // Se prueba con las colas propias: monticulo binario y cola enlazada.
        terminal = new Terminal(
                "Terminal Occidente",
                new MonticuloBinario<>(Comparator.comparing(UnidadTransporte::horaProgramada)));
    }

    private static UnidadTransporte unidad(String id, String hora) {
        return new UnidadTransporte(id, TipoRuta.NORMAL, LocalTime.parse(hora));
    }

    @Test
    void nombreVacioSeRechaza() {
        assertThrows(
                ValidacionException.class,
                () -> new Terminal(" ", new MonticuloBinario<>(Comparator.comparing(UnidadTransporte::id))));
    }

    @Test
    void despachaLaUnidadDeHoraMasProxima() {
        terminal.registrarUnidad(unidad("U-2", "08:00"));
        terminal.registrarUnidad(unidad("U-1", "07:00"));
        assertEquals("U-1", terminal.proximaUnidadASalir().id());
        assertEquals("U-1", terminal.despacharUnidad().id());
        assertEquals("U-2", terminal.despacharUnidad().id());
        assertFalse(terminal.hayUnidadesDisponibles());
    }

    @Test
    void terminalVaciaLanzaExcepcion() {
        assertThrows(ColeccionVaciaException.class, terminal::despacharUnidad);
        assertThrows(ColeccionVaciaException.class, terminal::proximaUnidadASalir);
        assertThrows(ColeccionVaciaException.class, () -> terminal.atenderPasajero(TipoRuta.NORMAL));
    }

    @Test
    void filaDePasajerosEsFifo() {
        terminal.encolarPasajero(TipoRuta.NORMAL, new Pasajero("P-1", "Ana"));
        terminal.encolarPasajero(TipoRuta.NORMAL, new Pasajero("P-2", "Beto"));
        assertEquals(2, terminal.totalEnFila(TipoRuta.NORMAL));
        assertEquals("P-1", terminal.atenderPasajero(TipoRuta.NORMAL).id());
        assertEquals("P-2", terminal.atenderPasajero(TipoRuta.NORMAL).id());
        assertFalse(terminal.totalEnFila(TipoRuta.NORMAL) > 0);
    }

    @Test
    void unPasajeroNoPuedeEstarEnDosFilas() {
        terminal.encolarPasajero(TipoRuta.NORMAL, new Pasajero("P-1", "Ana"));
        assertThrows(ValidacionException.class,
                () -> terminal.encolarPasajero(TipoRuta.EXPRES, new Pasajero(" P-1 ", "Ana")));
        terminal.atenderPasajero(TipoRuta.NORMAL);
        terminal.encolarPasajero(TipoRuta.EXPRES, new Pasajero("P-1", "Ana"));
        assertEquals(1, terminal.totalEnFila(TipoRuta.EXPRES));
    }

    @Test
    void lasFilasPorTipoSonIndependientes() {
        terminal.encolarPasajero(TipoRuta.NORMAL, new Pasajero("P-1", "Ana"));
        terminal.encolarPasajero(TipoRuta.EXPRES, new Pasajero("P-2", "Beto"));
        assertEquals("P-2", terminal.atenderPasajero(TipoRuta.EXPRES).id());
        assertEquals(1, terminal.totalEnFila(TipoRuta.NORMAL));
        assertThrows(ColeccionVaciaException.class, () -> terminal.atenderPasajero(TipoRuta.EXPRES));
    }

    @Test
    void rechazaNulos() {
        assertThrows(ValidacionException.class, () -> terminal.registrarUnidad(null));
        assertThrows(ValidacionException.class, () -> terminal.encolarPasajero(TipoRuta.NORMAL, null));
        assertThrows(ValidacionException.class, () -> terminal.encolarPasajero(null, new Pasajero("P-1", "Ana")));
        assertThrows(ValidacionException.class,
                () -> terminal.registrarUnidad(new UnidadTransporte("  ", TipoRuta.NORMAL, LocalTime.NOON)));
        assertTrue(terminal.getNombre().startsWith("Terminal"));
    }
}
