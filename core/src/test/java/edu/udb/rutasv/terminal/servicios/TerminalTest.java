package edu.udb.rutasv.terminal.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import edu.udb.rutasv.nucleo.temporal.ColaTemporal;
import java.time.LocalTime;
import java.util.Comparator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TerminalTest {

    private Terminal terminal;

    @BeforeEach
    void preparar() {
        // Mientras llegan las colas propias (PR de cola de prioridad y de cola FIFO) se usan las temporales.
        terminal = new Terminal(
                "Terminal Occidente",
                new ColaPrioridadTemporal<>(Comparator.comparing(UnidadTransporte::horaProgramada)),
                new ColaTemporal<>());
    }

    private static UnidadTransporte unidad(String id, String hora) {
        return new UnidadTransporte(id, TipoRuta.NORMAL, LocalTime.parse(hora));
    }

    @Test
    void nombreVacioSeRechaza() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Terminal(" ", new ColaPrioridadTemporal<>(Comparator.comparing(UnidadTransporte::id)), new ColaTemporal<>()));
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
        assertThrows(ColeccionVaciaException.class, terminal::atenderSiguientePasajero);
    }

    @Test
    void filaDePasajerosEsFifo() {
        terminal.registrarPasajeroFila(new Pasajero("P-1", "Ana"));
        terminal.registrarPasajeroFila(new Pasajero("P-2", "Beto"));
        assertEquals(2, terminal.totalPasajerosEnFila());
        assertEquals("P-1", terminal.atenderSiguientePasajero().id());
        assertEquals("P-2", terminal.atenderSiguientePasajero().id());
        assertFalse(terminal.hayPasajerosEnFila());
    }

    @Test
    void rechazaNulos() {
        assertThrows(IllegalArgumentException.class, () -> terminal.registrarUnidad(null));
        assertThrows(IllegalArgumentException.class, () -> terminal.registrarPasajeroFila(null));
        assertTrue(terminal.getNombre().startsWith("Terminal"));
    }
}
