package edu.udb.rutasv.structures;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Contrato de {@link Lista}. Cada implementacion agrega una subclase de una linea. */
public abstract class ListaContractTest {

    protected abstract Lista<Integer> crear();

    @Test
    void nuevaListaEstaVacia() {
        Lista<Integer> l = crear();
        assertTrue(l.estaVacia());
        assertEquals(0, l.tamano());
    }

    @Test
    void agregarYObtenerMantienenOrden() {
        Lista<Integer> l = crear();
        l.agregar(10);
        l.agregar(20);
        l.agregar(30);
        assertEquals(3, l.tamano());
        assertFalse(l.estaVacia());
        assertEquals(10, l.obtener(0));
        assertEquals(30, l.obtener(2));
    }

    @Test
    void eliminarDevuelveYDesplaza() {
        Lista<Integer> l = crear();
        l.agregar(1);
        l.agregar(2);
        l.agregar(3);
        assertEquals(2, l.eliminar(1));
        assertEquals(2, l.tamano());
        assertEquals(3, l.obtener(1));
        assertEquals(1, l.eliminar(0));
        assertEquals(3, l.eliminar(0));
        assertTrue(l.estaVacia());
    }

    @Test
    void iteraEnOrden() {
        Lista<Integer> l = crear();
        for (int i = 0; i < 5; i++) {
            l.agregar(i);
        }
        List<Integer> vistos = new ArrayList<>();
        for (int x : l) {
            vistos.add(x);
        }
        assertEquals(List.of(0, 1, 2, 3, 4), vistos);
    }

    @Test
    void iteradorDeListaVaciaNoTieneElementos() {
        assertFalse(crear().iterator().hasNext());
    }

    @Test
    void manejaMuchosElementos() {
        Lista<Integer> l = crear();
        for (int i = 0; i < 1000; i++) {
            l.agregar(i);
        }
        assertEquals(1000, l.tamano());
        assertEquals(999, l.obtener(999));
    }

    @Test
    void indicesInvalidosLanzanExcepcion() {
        Lista<Integer> l = crear();
        assertThrows(IndiceFueraDeRangoException.class, () -> l.obtener(0));
        assertThrows(IndiceFueraDeRangoException.class, () -> l.eliminar(0));
        l.agregar(1);
        assertThrows(IndiceFueraDeRangoException.class, () -> l.obtener(1));
        assertThrows(IndiceFueraDeRangoException.class, () -> l.obtener(-1));
        assertThrows(IndiceFueraDeRangoException.class, () -> l.eliminar(1));
        assertThrows(IndiceFueraDeRangoException.class, () -> l.eliminar(-1));
        assertEquals(1, l.tamano());
    }
}
