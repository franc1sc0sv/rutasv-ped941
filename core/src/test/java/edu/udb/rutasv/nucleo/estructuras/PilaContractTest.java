package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.error.ColeccionVaciaException;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/** Contrato de {@link Pila}. */
public abstract class PilaContractTest {

    protected abstract Pila<Integer> crear();

    @Test
    void nuevaPilaEstaVacia() {
        Pila<Integer> p = crear();
        assertTrue(p.estaVacia());
        assertEquals(0, p.tamano());
    }

    @Test
    void saleEnOrdenInverso() {
        Pila<Integer> p = crear();
        p.apilar(1);
        p.apilar(2);
        p.apilar(3);
        assertEquals(3, p.tamano());
        assertEquals(3, p.desapilar());
        assertEquals(2, p.desapilar());
        assertEquals(1, p.desapilar());
        assertTrue(p.estaVacia());
    }

    @Test
    void cimaNoQuita() {
        Pila<Integer> p = crear();
        p.apilar(7);
        p.apilar(8);
        assertEquals(8, p.cima());
        assertEquals(8, p.cima());
        assertEquals(2, p.tamano());
    }

    @Test
    void sePuedeReutilizarTrasVaciar() {
        Pila<Integer> p = crear();
        p.apilar(1);
        p.desapilar();
        p.apilar(2);
        assertEquals(2, p.cima());
        assertEquals(1, p.tamano());
    }

    @Test
    void pilaVaciaLanzaExcepcion() {
        Pila<Integer> p = crear();
        assertThrows(ColeccionVaciaException.class, p::desapilar);
        assertThrows(ColeccionVaciaException.class, p::cima);
        p.apilar(1);
        p.desapilar();
        assertThrows(ColeccionVaciaException.class, p::desapilar);
    }
}
