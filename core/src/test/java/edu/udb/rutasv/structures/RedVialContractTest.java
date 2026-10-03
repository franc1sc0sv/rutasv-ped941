package edu.udb.rutasv.structures;

import static org.junit.jupiter.api.Assertions.*;

import edu.udb.rutasv.model.Arista;
import edu.udb.rutasv.model.Estacion;
import edu.udb.rutasv.model.ValidacionException;
import org.junit.jupiter.api.Test;

/** Contrato de {@link RedVial}. Datos sinteticos. */
public abstract class RedVialContractTest {

    protected abstract RedVial crear();

    private static Estacion est(String codigo) {
        return new Estacion(codigo, "Estacion " + codigo, "Z1", 1.0, 2.0, true);
    }

    private RedVial conTresEstaciones() {
        RedVial r = crear();
        r.agregarEstacion(est("A"));
        r.agregarEstacion(est("B"));
        r.agregarEstacion(est("C"));
        return r;
    }

    @Test
    void redNuevaEstaVacia() {
        RedVial r = crear();
        assertTrue(r.estaciones().estaVacia());
        assertTrue(r.vecinos("A").estaVacia());
    }

    @Test
    void agregarEstaciones() {
        RedVial r = conTresEstaciones();
        assertEquals(3, r.estaciones().tamano());
    }

    @Test
    void agregarMismaEstacionReemplaza() {
        RedVial r = conTresEstaciones();
        r.agregarEstacion(new Estacion("A", "Nueva", "Z2", 0, 0, false));
        assertEquals(3, r.estaciones().tamano());
    }

    @Test
    void tramoEsDirigido() {
        RedVial r = conTresEstaciones();
        r.agregarTramo("A", "B", 5, 2.5);
        r.agregarTramo("A", "C", 8, 4.0);
        assertEquals(2, r.vecinos("A").tamano());
        Arista primera = r.vecinos("A").obtener(0);
        assertEquals("B", primera.destinoCodigo());
        assertEquals(5, primera.minutos());
        assertEquals(2.5, primera.km());
        assertTrue(r.vecinos("B").estaVacia());
    }

    @Test
    void estacionSinTramosTieneVecinosVacios() {
        assertTrue(conTresEstaciones().vecinos("C").estaVacia());
    }

    @Test
    void estacionDesconocidaTieneVecinosVacios() {
        assertTrue(conTresEstaciones().vecinos("ZZ").estaVacia());
    }

    @Test
    void minutosNoPositivosSeRechazan() {
        RedVial r = conTresEstaciones();
        assertThrows(ValidacionException.class, () -> r.agregarTramo("A", "B", 0, 1.0));
        assertThrows(ValidacionException.class, () -> r.agregarTramo("A", "B", -3, 1.0));
        assertTrue(r.vecinos("A").estaVacia());
    }

    @Test
    void tramoConEstacionInexistenteSeRechaza() {
        RedVial r = conTresEstaciones();
        assertThrows(ValidacionException.class, () -> r.agregarTramo("A", "X", 5, 1.0));
        assertThrows(ValidacionException.class, () -> r.agregarTramo("X", "A", 5, 1.0));
    }
}
