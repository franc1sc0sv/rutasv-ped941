package rutasv.terminal;

import org.junit.jupiter.api.Test;
import rutasv.estructuras.ColaFIFO;

import static org.junit.jupiter.api.Assertions.*;

class ColaFIFOTest {

    @Test
    void testOrdenFIFO_FF01() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        cola.encolar("a");
        cola.encolar("b");
        cola.encolar("c");

        assertEquals("a", cola.desencolar());
        assertEquals("b", cola.desencolar());
        assertEquals("c", cola.desencolar());
    }

    @Test
    void testColaVacia_FF02() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        assertTrue(cola.estaVacia());
        assertNull(cola.desencolar());
        assertNull(cola.verFrente());
    }

    @Test
    void testReutilizarTrasVaciar_FF03() {
        ColaFIFO<String> cola = new ColaFIFO<>();
        cola.encolar("a");
        assertEquals("a", cola.desencolar());
        assertTrue(cola.estaVacia());

        cola.encolar("b");
        assertEquals("b", cola.desencolar());
    }

    @Test
    void test1000Elementos_FF04() {
        ColaFIFO<Integer> cola = new ColaFIFO<>();
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
