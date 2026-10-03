package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.error.ColeccionVaciaException;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Contrato de {@link Cola}. */
public abstract class ColaContractTest {

    protected abstract Cola<Integer> crear();

    @Test
    void nuevaColaEstaVacia() {
        Cola<Integer> c = crear();
        assertTrue(c.estaVacia());
        assertEquals(0, c.tamano());
    }

    @Test
    void saleEnOrdenDeLlegada() {
        Cola<Integer> c = crear();
        c.encolar(1);
        c.encolar(2);
        c.encolar(3);
        assertEquals(3, c.tamano());
        assertEquals(1, c.desencolar());
        assertEquals(2, c.desencolar());
        assertEquals(3, c.desencolar());
        assertTrue(c.estaVacia());
    }

    @Test
    void frenteNoQuita() {
        Cola<Integer> c = crear();
        c.encolar(5);
        c.encolar(6);
        assertEquals(5, c.frente());
        assertEquals(5, c.frente());
        assertEquals(2, c.tamano());
    }

    @Test
    void intercalarEncolarYDesencolar() {
        Cola<Integer> c = crear();
        c.encolar(1);
        assertEquals(1, c.desencolar());
        c.encolar(2);
        c.encolar(3);
        assertEquals(2, c.desencolar());
        assertEquals(3, c.frente());
    }

    @Test
    void manejaMuchosElementos() {
        Cola<Integer> c = crear();
        for (int i = 0; i < 1000; i++) {
            c.encolar(i);
        }
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, c.desencolar());
        }
        assertTrue(c.estaVacia());
    }

    @Test
    void colaVaciaLanzaExcepcion() {
        Cola<Integer> c = crear();
        assertThrows(ColeccionVaciaException.class, c::desencolar);
        assertThrows(ColeccionVaciaException.class, c::frente);
    }
}
