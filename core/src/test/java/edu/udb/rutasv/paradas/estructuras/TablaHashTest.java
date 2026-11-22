package edu.udb.rutasv.paradas.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Pruebas especificas de la tabla hash: colisiones, redimension, limites y casos borde. */
class TablaHashTest {

    @Test
    void redimensionaAlSuperarElFactorDeCarga() {
        TablaHash<String, Integer> tabla = new TablaHash<>(16);
        assertEquals(16, tabla.capacidad());

        for (int i = 0; i < 12; i++) {
            tabla.poner("k" + i, i);
        }
        // 12/16 = 0.75 exacto: no debe redimensionar (detecta un cambio de > por >=).
        assertEquals(16, tabla.capacidad());
        assertEquals(12, tabla.tamano());

        tabla.poner("k12", 12);
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
    void eliminaCabezaMediaYColaDeCadena() {
        TablaHash<ClaveColision, Integer> tabla = new TablaHash<>(16);
        ClaveColision a = new ClaveColision(1);
        ClaveColision b = new ClaveColision(2);
        ClaveColision c = new ClaveColision(3);

        // Los tres hashCode son multiplos de 16: con capacidad 16 caen en el cubo 0.
        tabla.poner(a, 1);
        tabla.poner(b, 2);
        tabla.poner(c, 3);
        assertEquals(3, tabla.tamano());

        assertEquals(2, tabla.eliminar(b)); // media
        assertEquals(1, tabla.obtener(a));
        assertEquals(3, tabla.obtener(c));

        assertEquals(3, tabla.eliminar(c)); // cabeza
        assertEquals(1, tabla.eliminar(a)); // cola
        assertEquals(0, tabla.tamano());
        assertNull(tabla.obtener(a));
    }

    @Test
    void mezclaBitsAltosEnCapacidadPequena() {
        TablaHash<ClaveConHash, Integer> tabla = new TablaHash<>(16);
        ClaveConHash bajo = new ClaveConHash(1);
        ClaveConHash alto = new ClaveConHash(1 | (1 << 16));

        // Sin mezclar, ambos caerian en el cubo 1; con la mezcla, el segundo cae en el 0.
        assertEquals(1, TablaHash.indiceDe(bajo.hashCode(), 16));
        assertEquals(0, TablaHash.indiceDe(alto.hashCode(), 16));
        assertNotEquals(
                TablaHash.indiceDe(bajo.hashCode(), 16), TablaHash.indiceDe(alto.hashCode(), 16));

        tabla.poner(bajo, 1);
        tabla.poner(alto, 2);
        assertEquals(1, tabla.obtener(bajo));
        assertEquals(2, tabla.obtener(alto));
        assertEquals(16, tabla.capacidad());
    }

    @Test
    void capacidadSeTopaEnDosElevado30() {
        assertEquals(1 << 30, TablaHash.MAX_CAPACIDAD);
        assertEquals(32, TablaHash.siguientePotenciaDeDos(17));
        assertEquals(TablaHash.MAX_CAPACIDAD, TablaHash.siguientePotenciaDeDos(TablaHash.MAX_CAPACIDAD));
        // Con Integer.MAX_VALUE el old while desbordaba y no terminaba.
        assertEquals(TablaHash.MAX_CAPACIDAD, TablaHash.siguientePotenciaDeDos(Integer.MAX_VALUE));
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

    /** Clave con hashCode controlado para forzar colisiones sin depender de String. */
    private static final class ClaveColision {
        private final int id;

        private ClaveColision(int id) {
            this.id = id;
        }

        @Override
        public int hashCode() {
            return id * 16;
        }

        @Override
        public boolean equals(Object otro) {
            return otro instanceof ClaveColision && ((ClaveColision) otro).id == id;
        }
    }

    /** Clave cuyo hashCode se elige a mano para probar la mezcla de bits altos. */
    private static final class ClaveConHash {
        private final int hash;

        private ClaveConHash(int hash) {
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object otro) {
            return otro instanceof ClaveConHash && ((ClaveConHash) otro).hash == hash;
        }
    }
}
