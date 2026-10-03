package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;
import org.junit.jupiter.api.Test;

/** Contrato de {@link ColaPrioridad}. La fabrica recibe el comparador. */
public abstract class ColaPrioridadContractTest {

    protected abstract ColaPrioridad<Integer> crear(Comparator<Integer> comparador);

    @Test
    void nuevaColaEstaVacia() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        assertTrue(c.estaVacia());
        assertEquals(0, c.tamano());
    }

    @Test
    void extraeDelMenorAlMayor() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        for (int x : new int[] {5, 1, 4, 2, 3}) {
            c.insertar(x);
        }
        assertEquals(5, c.tamano());
        for (int esperado = 1; esperado <= 5; esperado++) {
            assertEquals(esperado, c.extraer());
        }
        assertTrue(c.estaVacia());
    }

    @Test
    void verNoQuita() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        c.insertar(9);
        c.insertar(3);
        assertEquals(3, c.ver());
        assertEquals(2, c.tamano());
    }

    @Test
    void respetaElComparador() {
        ColaPrioridad<Integer> c = crear(Comparator.reverseOrder());
        c.insertar(1);
        c.insertar(3);
        c.insertar(2);
        assertEquals(3, c.extraer());
        assertEquals(2, c.extraer());
        assertEquals(1, c.extraer());
    }

    @Test
    void aceptaDuplicados() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        c.insertar(2);
        c.insertar(2);
        c.insertar(1);
        assertEquals(1, c.extraer());
        assertEquals(2, c.extraer());
        assertEquals(2, c.extraer());
    }

    @Test
    void manejaMuchosElementosDesordenados() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        for (int i = 0; i < 1000; i++) {
            c.insertar((i * 37) % 1000);
        }
        int anterior = -1;
        while (!c.estaVacia()) {
            int actual = c.extraer();
            assertTrue(actual >= anterior);
            anterior = actual;
        }
    }

    @Test
    void colaVaciaLanzaExcepcion() {
        ColaPrioridad<Integer> c = crear(Comparator.naturalOrder());
        assertThrows(ColeccionVaciaException.class, c::extraer);
        assertThrows(ColeccionVaciaException.class, c::ver);
    }
}
