package edu.udb.rutasv.paradas.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas especificas de la tabla hash: colisiones, redimension y casos borde. */
class TablaHashTest {

    @Test
    void redimensionaAlSuperarElFactorDeCarga() {
        TablaHash<String, Integer> tabla = new TablaHash<>(16);
        assertEquals(16, tabla.capacidad());

        for (int i = 0; i < 13; i++) {
            tabla.poner("k" + i, i);
        }
        // 13/16 = 0.8125 > 0.75: duplica capacidad y reinserta los 13 pares.
        assertEquals(32, tabla.capacidad());
        assertEquals(13, tabla.tamano());
        for (int i = 0; i < 13; i++) {
            assertEquals(i, tabla.obtener("k" + i));
        }
    }

    @Test
    void colisionesSeResuelvenPorEncadenamiento() {
        // "Aa" y "BB" tienen el mismo hashCode en Java: fuerza una colision real.
        TablaHash<String, Integer> tabla = new TablaHash<>(1);
        tabla.poner("Aa", 1);
        tabla.poner("BB", 2);

        assertEquals(2, tabla.tamano());
        assertEquals(1, tabla.obtener("Aa"));
        assertEquals(2, tabla.obtener("BB"));

        tabla.poner("Aa", 99);
        assertEquals(99, tabla.obtener("Aa"));
        assertEquals(2, tabla.obtener("BB"));
        assertEquals(2, tabla.tamano());

        assertEquals(99, tabla.eliminar("Aa"));
        assertNull(tabla.obtener("Aa"));
        assertEquals(2, tabla.obtener("BB"));
        assertEquals(1, tabla.tamano());
    }

    @Test
    void eliminarInexistenteDevuelveNullYNoCambiaElTamano() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        tabla.poner("a", 1);

        assertNull(tabla.eliminar("noExiste"));
        assertEquals(1, tabla.tamano());
        assertEquals(1, tabla.obtener("a"));
    }

    @Test
    void contieneDistingeClavePresenteConValorNulo() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        tabla.poner("a", null);

        assertTrue(tabla.contiene("a"));
        assertNull(tabla.obtener("a"));
        assertFalse(tabla.contiene("b"));
    }

    @Test
    void milClavesSobrevivenAVariasRedimensiones() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 1000; i++) {
            tabla.poner("k" + i, i);
        }
        assertEquals(1000, tabla.tamano());
        assertTrue(tabla.capacidad() >= 2048);

        for (int i = 0; i < 1000; i++) {
            assertEquals(i, tabla.obtener("k" + i));
        }
        assertEquals(1000, tabla.claves().tamano());

        for (int i = 0; i < 1000; i += 2) {
            assertEquals(i, tabla.eliminar("k" + i));
        }
        assertEquals(500, tabla.tamano());
        assertNull(tabla.obtener("k0"));
        assertEquals(1, tabla.obtener("k1"));
    }
}
