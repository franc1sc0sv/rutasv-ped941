package rutasv.terminal;

import org.junit.jupiter.api.Test;
import rutasv.estructuras.ColaPrioridad;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ColaPrioridadTest {

    @Test
    void testOrdenPrioridadCP01() {
        ColaPrioridad<Integer> cola = new ColaPrioridad<>(Comparator.naturalOrder());
        cola.insertar(5);
        cola.insertar(3);
        cola.insertar(9);
        cola.insertar(1);
        cola.insertar(7);

        assertEquals(1, cola.sacarMayor());
        assertEquals(3, cola.sacarMayor());
        assertEquals(5, cola.sacarMayor());
        assertEquals(7, cola.sacarMayor());
        assertEquals(9, cola.sacarMayor());
    }

    @Test
    void testComparadorInversoCP02() {
        ColaPrioridad<Integer> cola = new ColaPrioridad<>(Comparator.reverseOrder());
        cola.insertar(2);
        cola.insertar(8);
        cola.insertar(5);

        assertEquals(8, cola.sacarMayor());
        assertEquals(5, cola.sacarMayor());
        assertEquals(2, cola.sacarMayor());
    }

    @Test
    void testColaVaciaCP03() {
        ColaPrioridad<Integer> cola = new ColaPrioridad<>(Comparator.naturalOrder());
        assertTrue(cola.estaVacia());
        assertNull(cola.sacarMayor());
        assertNull(cola.verMayor());
    }

    @Test
    void testElementosRepetidosCP04() {
        ColaPrioridad<Integer> cola = new ColaPrioridad<>(Comparator.naturalOrder());
        cola.insertar(2);
        cola.insertar(2);
        cola.insertar(1);
        cola.insertar(1);

        assertEquals(1, cola.sacarMayor());
        assertEquals(1, cola.sacarMayor());
        assertEquals(2, cola.sacarMayor());
        assertEquals(2, cola.sacarMayor());
    }

    @Test
    void testComparacionConJavaPriorityQueueCP06() {
        ColaPrioridad<Integer> miCola = new ColaPrioridad<>(Comparator.naturalOrder());
        PriorityQueue<Integer> javaCola = new PriorityQueue<>(Comparator.naturalOrder());
        Random random = new Random(42);

        for (int i = 0; i < 1000; i++) {
            int num = random.nextInt(5000);
            miCola.insertar(num);
            javaCola.add(num);
        }

        while (!javaCola.isEmpty()) {
            assertEquals(javaCola.poll(), miCola.sacarMayor());
        }
    }
}
