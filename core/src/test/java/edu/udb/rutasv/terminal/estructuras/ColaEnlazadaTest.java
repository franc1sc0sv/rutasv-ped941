package edu.udb.rutasv.terminal.estructuras;

import org.junit.jupiter.api.Test;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;

import static org.junit.jupiter.api.Assertions.*;

class ColaEnlazadaTest {

    @Test
    void testOrdenFIFO_FF01() {
        ColaEnlazada<String> cola = new ColaEnlazada<>();
        cola.encolar("a");
        cola.encolar("b");
        cola.encolar("c");

        assertEquals("a", cola.desencolar());
        assertEquals("b", cola.desencolar());
        assertEquals("c", cola.desencolar());
    }

    @Test
    void testColaVacia_FF02() {
        ColaEnlazada<String> cola = new ColaEnlazada<>();
        assertTrue(cola.estaVacia());
        assertThrows(ColeccionVaciaException.class, cola::desencolar);
        assertThrows(ColeccionVaciaException.class, cola::frente);
    }

    @Test
    void testReutilizarTrasVaciar_FF03() {
        ColaEnlazada<String> cola = new ColaEnlazada<>();
        cola.encolar("a");
        assertEquals("a", cola.desencolar());
        assertTrue(cola.estaVacia());

        cola.encolar("b");
        assertEquals("b", cola.desencolar());
    }

    @Test
    void test1000Elementos_FF04() {
        ColaEnlazada<Integer> cola = new ColaEnlazada<>();
        for (int i = 0; i < 1000; i++) {
            cola.encolar(i);
        }
        assertEquals(1000, cola.tamano());
        for (int i = 0; i < 1000; i++) {
            assertEquals(i, cola.desencolar());
        }
        assertTrue(cola.estaVacia());
    }
}
