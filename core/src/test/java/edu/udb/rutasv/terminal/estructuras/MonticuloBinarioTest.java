package edu.udb.rutasv.terminal.estructuras;

import org.junit.jupiter.api.Test;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MonticuloBinarioTest {

    @Test
    void testOrdenPrioridadCP01() {
        MonticuloBinario<Integer> cola = new MonticuloBinario<Integer>(Comparator.naturalOrder());
        cola.insertar(5);
        cola.insertar(3);
        cola.insertar(9);
        cola.insertar(1);
        cola.insertar(7);

        assertEquals(1, cola.extraer());
        assertEquals(3, cola.extraer());
        assertEquals(5, cola.extraer());
        assertEquals(7, cola.extraer());
        assertEquals(9, cola.extraer());
    }

    @Test
    void testComparadorInversoCP02() {
        MonticuloBinario<Integer> cola = new MonticuloBinario<Integer>(Comparator.reverseOrder());
        cola.insertar(2);
        cola.insertar(8);
        cola.insertar(5);

        assertEquals(8, cola.extraer());
        assertEquals(5, cola.extraer());
        assertEquals(2, cola.extraer());
    }

    @Test
    void testColaVaciaCP03() {
        MonticuloBinario<Integer> cola = new MonticuloBinario<Integer>(Comparator.naturalOrder());
        assertTrue(cola.estaVacia());
        assertThrows(ColeccionVaciaException.class, cola::extraer);
        assertThrows(ColeccionVaciaException.class, cola::ver);
    }

    @Test
    void testElementosRepetidosCP04() {
        MonticuloBinario<Integer> cola = new MonticuloBinario<Integer>(Comparator.naturalOrder());
        cola.insertar(2);
        cola.insertar(2);
        cola.insertar(1);
        cola.insertar(1);

        assertEquals(1, cola.extraer());
        assertEquals(1, cola.extraer());
        assertEquals(2, cola.extraer());
        assertEquals(2, cola.extraer());
    }

    @Test
    void testComparacionConJavaPriorityQueueCP06() {
        MonticuloBinario<Integer> miCola = new MonticuloBinario<Integer>(Comparator.naturalOrder());
        PriorityQueue<Integer> javaCola = new PriorityQueue<>(Comparator.naturalOrder());
        Random random = new Random(42);

        for (int i = 0; i < 1000; i++) {
            int num = random.nextInt(5000);
            miCola.insertar(num);
            javaCola.add(num);
        }

        while (!javaCola.isEmpty()) {
            assertEquals(javaCola.poll(), miCola.extraer());
        }
    }
}
