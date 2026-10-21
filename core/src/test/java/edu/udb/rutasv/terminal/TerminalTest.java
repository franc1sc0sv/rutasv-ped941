package edu.udb.rutasv.terminal;

import edu.udb.rutasv.modelo.UnidadTransporte;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TerminalTest {

    private Terminal terminal;

    @BeforeEach
    public void setUp() {
        terminal = new Terminal("Terminal Central");
    }

    @Test
    public void testTerminalVacia() {
        assertFalse(terminal.hayUnidadesDisponibles());
        assertNull(terminal.proximaUnidadASalir());
    }

    @Test
    public void testRegistroYDespachoPrioridadRN4() {
        UnidadTransporte normal = new UnidadTransporte("U-01", "NORMAL", "08:00");
        UnidadTransporte expres = new UnidadTransporte("U-02", "EXPRES", "07:30");

        terminal.registrarUnidad(normal);
        terminal.registrarUnidad(expres);

        assertTrue(terminal.hayUnidadesDisponibles());
        // Por regla RN-4, EXPRES debe salir primero aunque su hora sea posterior o anterior según prioridad
        UnidadTransporte proxima = terminal.proximaUnidadASalir();
        assertEquals("U-02", proxima.getId());
    }

    @Test
    public void testVerOrdenDespachoSinVaciar() {
        UnidadTransporte u1 = new UnidadTransporte("U-01", "NORMAL", "08:00");
        UnidadTransporte u2 = new UnidadTransporte("U-02", "EXPRES", "07:30");

        terminal.registrarUnidad(u1);
        terminal.registrarUnidad(u2);

        List<UnidadTransporte> orden = terminal.verOrdenDespacho();
        assertEquals(2, orden.size());
        
        // Verificamos que la cola real sigue teniendo las unidades (no se vació)
        assertTrue(terminal.hayUnidadesDisponibles());
    }

    @Test
    public void testGestionFilaPasajerosFIFO_RN5() {
        terminal.registrarPasajeroFila("Pasajero-A");
        terminal.registrarPasajeroFila("Pasajero-B");

        assertTrue(terminal.hayPasajerosEnFila());
        assertEquals(2, terminal.totalPasajerosEnFila());

        // Comportamiento FIFO: el primero en entrar es el primero en salir
        assertEquals("Pasajero-A", terminal.atenderSiguientePasajero());
        assertEquals("Pasajero-B", terminal.atenderSiguientePasajero());
        assertFalse(terminal.hayPasajerosEnFila());
    }
}
