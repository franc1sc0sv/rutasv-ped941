package edu.udb.rutasv.ruta.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import org.junit.jupiter.api.Test;

/** Reglas propias del grafo, ademas del contrato de RedVial. */
class GrafoListaAdyacenciaTest {

    private final GrafoListaAdyacencia red = new GrafoListaAdyacencia();

    GrafoListaAdyacenciaTest() {
        for (String c : new String[] {"C", "A", "B"}) {
            red.agregarEstacion(new Estacion(c, "Estacion " + c, "Z1", 0, 0, true));
        }
    }

    @Test
    void repetirUnTramoLoReemplazaRn3() {
        red.agregarTramo("A", "B", 5, 2.0);
        red.agregarTramo("A", "B", 7, 3.0);
        assertEquals(1, red.vecinos("A").tamano());
        assertEquals(7, red.vecinos("A").obtener(0).minutos());
    }

    @Test
    void dobleSentidoSonDosTramosIndependientesRn3() {
        red.agregarTramo("A", "B", 5, 2.0);
        red.agregarTramo("B", "A", 9, 2.0);
        assertEquals(5, red.vecinos("A").obtener(0).minutos());
        assertEquals(9, red.vecinos("B").obtener(0).minutos());
    }

    @Test
    void estacionesEnOrdenDeAlta() {
        assertEquals("C", red.estaciones().obtener(0).codigo());
        assertEquals("A", red.estaciones().obtener(1).codigo());
        assertEquals("B", red.estaciones().obtener(2).codigo());
    }

    @Test
    void reemplazarEstacionConservaSusTramos() {
        red.agregarTramo("A", "B", 5, 2.0);
        red.agregarEstacion(new Estacion("A", "Estacion A", "Z1", 0, 0, false));
        assertFalse(red.estacion("A").activa());
        assertEquals(1, red.vecinos("A").tamano());
    }

    @Test
    void vecinosEsUnaCopia() {
        red.agregarTramo("A", "B", 5, 2.0);
        red.vecinos("A").eliminar(0);
        assertEquals(1, red.vecinos("A").tamano());
    }

    @Test
    void rechazaLazoYKmNegativos() {
        assertThrows(ValidacionException.class, () -> red.agregarTramo("A", "A", 5, 1.0));
        assertThrows(ValidacionException.class, () -> red.agregarTramo("A", "B", 5, -1.0));
        assertThrows(ValidacionException.class, () -> red.agregarEstacion(null));
    }

    @Test
    void rechazaTramosDeMasDeUnDiaYKmNoFinitos() {
        assertThrows(ValidacionException.class,
                () -> red.agregarTramo("A", "B", GrafoListaAdyacencia.MAX_MINUTOS_TRAMO + 1, 1.0));
        assertThrows(ValidacionException.class, () -> red.agregarTramo("A", "B", 5, Double.NaN));
        assertThrows(ValidacionException.class, () -> red.agregarTramo("A", "B", 5, Double.POSITIVE_INFINITY));
        red.agregarTramo("A", "B", GrafoListaAdyacencia.MAX_MINUTOS_TRAMO, 1.0);
        assertEquals(1, red.vecinos("A").tamano());
    }

    @Test
    void estacionDesconocidaEsNula() {
        assertNull(red.estacion("ZZ"));
    }
}
