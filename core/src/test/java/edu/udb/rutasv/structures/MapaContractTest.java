package edu.udb.rutasv.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Contrato de {@link Mapa}. */
public abstract class MapaContractTest {

    protected abstract Mapa<String, Integer> crear();

    @Test
    void nuevoMapaEstaVacio() {
        Mapa<String, Integer> m = crear();
        assertEquals(0, m.tamano());
        assertTrue(m.claves().estaVacia());
    }

    @Test
    void ponerYObtener() {
        Mapa<String, Integer> m = crear();
        m.poner("a", 1);
        m.poner("b", 2);
        assertEquals(1, m.obtener("a"));
        assertEquals(2, m.obtener("b"));
        assertEquals(2, m.tamano());
        assertTrue(m.contiene("a"));
    }

    @Test
    void ausenteDevuelveNull() {
        Mapa<String, Integer> m = crear();
        assertNull(m.obtener("x"));
        assertFalse(m.contiene("x"));
        assertNull(m.eliminar("x"));
    }

    @Test
    void ponerReemplazaSinCrecer() {
        Mapa<String, Integer> m = crear();
        m.poner("a", 1);
        m.poner("a", 99);
        assertEquals(99, m.obtener("a"));
        assertEquals(1, m.tamano());
    }

    @Test
    void eliminarDevuelveElValor() {
        Mapa<String, Integer> m = crear();
        m.poner("a", 1);
        assertEquals(1, m.eliminar("a"));
        assertFalse(m.contiene("a"));
        assertEquals(0, m.tamano());
        assertNull(m.eliminar("a"));
    }

    @Test
    void clavesDevuelveTodasSinOrden() {
        Mapa<String, Integer> m = crear();
        m.poner("a", 1);
        m.poner("b", 2);
        m.poner("c", 3);
        Set<String> vistas = new HashSet<>();
        for (String k : m.claves()) {
            vistas.add(k);
        }
        assertEquals(Set.of("a", "b", "c"), vistas);
        assertEquals(3, m.claves().tamano());
    }

    @Test
    void manejaMuchasClaves() {
        Mapa<String, Integer> m = crear();
        for (int i = 0; i < 2000; i++) {
            m.poner("k" + i, i);
        }
        assertEquals(2000, m.tamano());
        for (int i = 0; i < 2000; i++) {
            assertEquals(i, m.obtener("k" + i));
        }
        for (int i = 0; i < 2000; i += 2) {
            m.eliminar("k" + i);
        }
        assertEquals(1000, m.tamano());
        assertNull(m.obtener("k0"));
        assertEquals(1, m.obtener("k1"));
    }
}
