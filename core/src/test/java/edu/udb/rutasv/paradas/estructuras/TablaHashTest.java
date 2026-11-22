package edu.udb.rutasv.paradas.estructuras;

import static org.junit.jupiter.api.Assertions.*;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.IndiceFueraDeRangoException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Random;
import org.junit.jupiter.api.Test;

class TablaHashTest {
    private record Clave(int id, int hash) {
        @Override public int hashCode() { return hash; }
    }

    @Test void creceSoloAlSuperarTresCuartosYConservaMilClaves() {
        TablaHash<Integer, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 12; i++) tabla.poner(i, i);
        assertEquals(16, tabla.capacidad());
        tabla.poner(0, 99);
        assertEquals(16, tabla.capacidad());
        tabla.poner(12, 12);
        assertEquals(32, tabla.capacidad());
        for (int i = 13; i < 1000; i++) tabla.poner(i, i);
        assertEquals(1000, tabla.tamano());
        for (int i = 0; i < 1000; i++) assertEquals(i == 0 ? 99 : i, tabla.obtener(i));
    }

    @Test void eliminaCabezaMedioYColaDeUnaColision() {
        TablaHash<Clave, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 4; i++) tabla.poner(new Clave(i, -1), i);
        for (int i : new int[] {3, 1, 0, 2}) {
            assertEquals(i, tabla.eliminar(new Clave(i, -1)));
            assertFalse(tabla.contiene(new Clave(i, -1)));
        }
        assertNull(tabla.eliminar(new Clave(8, -1)));
        assertEquals(0, tabla.tamano());
        assertTrue(tabla.claves().estaVacia());
    }

    @Test void colisionesPersistenTrasVariosRehashYHashMinimo() {
        TablaHash<Clave, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 1000; i++) tabla.poner(new Clave(i, Integer.MIN_VALUE), i);
        for (int i = 0; i < 1000; i++) assertEquals(i, tabla.obtener(new Clave(i, Integer.MIN_VALUE)));
        tabla.poner(new Clave(500, Integer.MIN_VALUE), -7);
        assertEquals(1000, tabla.tamano());
        assertEquals(-7, tabla.obtener(new Clave(500, Integer.MIN_VALUE)));
    }

    @Test void nullEsUnaClaveYUnValorValido() {
        TablaHash<String, Integer> tabla = new TablaHash<>();
        tabla.poner(null, null);
        assertTrue(tabla.contiene(null));
        assertNull(tabla.obtener(null));
        tabla.poner(null, 7);
        tabla.poner("nulo", null);
        assertEquals(2, tabla.tamano());
        assertEquals(7, tabla.eliminar(null));
        assertNull(tabla.eliminar("nulo"));
        assertFalse(tabla.contiene("nulo"));
        assertEquals(0, tabla.tamano());
    }

    @Test void copiaDeClavesEsIndependienteYRespetaContratoLista() {
        TablaHash<Integer, Integer> tabla = new TablaHash<>();
        for (int i = 0; i < 30; i++) tabla.poner(i, i);
        Lista<Integer> copia = tabla.claves();
        Integer primera = copia.eliminar(0);
        assertTrue(tabla.contiene(primera));
        copia.agregar(99);
        assertFalse(tabla.contiene(99));
        tabla.poner(100, 100);
        assertEquals(30, copia.tamano());
        assertThrows(IndiceFueraDeRangoException.class, () -> copia.obtener(-1));
        assertThrows(IndiceFueraDeRangoException.class, () -> copia.eliminar(30));
        var iterador = copia.iterator();
        while (iterador.hasNext()) iterador.next();
        assertThrows(NoSuchElementException.class, iterador::next);
    }

    @Test void operacionesMezcladasCoincidenConModeloDeReferencia() {
        TablaHash<Integer, Integer> tabla = new TablaHash<>();
        HashMap<Integer, Integer> modelo = new HashMap<>();
        Random azar = new Random(941);
        for (int i = 0; i < 20000; i++) {
            Integer clave = azar.nextInt(100) == 0 ? null : azar.nextInt(500);
            Integer valor = azar.nextBoolean() ? null : azar.nextInt();
            switch (azar.nextInt(3)) {
                case 0 -> { tabla.poner(clave, valor); modelo.put(clave, valor); }
                case 1 -> assertEquals(modelo.remove(clave), tabla.eliminar(clave));
                default -> {
                    assertEquals(modelo.get(clave), tabla.obtener(clave));
                    assertEquals(modelo.containsKey(clave), tabla.contiene(clave));
                }
            }
            assertEquals(modelo.size(), tabla.tamano());
            if (i % 100 == 0) {
                HashSet<Integer> claves = new HashSet<>();
                for (Integer k : tabla.claves()) claves.add(k);
                assertEquals(modelo.keySet(), claves);
                assertEquals(modelo.size(), tabla.claves().tamano());
            }
        }
    }
}
