package edu.udb.rutasv.historial.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;

class ListaDinamicaTest {

    private static List<String> aJava(ListaDinamica<String> lista) {
        List<String> resultado = new ArrayList<>();
        for (int i = 0; i < lista.tamano(); i++) {
            resultado.add(lista.obtener(i));
        }
        return resultado;
    }

    @Test
    void creceAlPasarSuCapacidad() {
        ListaDinamica<Integer> lista = new ListaDinamica<>();
        assertEquals(ListaDinamica.CAPACIDAD_INICIAL, lista.capacidad());
        for (int i = 0; i <= ListaDinamica.CAPACIDAD_INICIAL; i++) {
            lista.agregar(i);
        }
        assertEquals(ListaDinamica.CAPACIDAD_INICIAL * 2, lista.capacidad());
        assertEquals(ListaDinamica.CAPACIDAD_INICIAL + 1, lista.tamano());
        for (int i = 0; i <= ListaDinamica.CAPACIDAD_INICIAL; i++) {
            assertEquals(i, lista.obtener(i));
        }
    }

    @Test
    void eliminarAlInicioAlMedioYAlFinal() {
        ListaDinamica<String> lista = new ListaDinamica<>();
        for (String s : new String[] {"a", "b", "c", "d", "e"}) {
            lista.agregar(s);
        }
        assertEquals("a", lista.eliminar(0));
        assertEquals(List.of("b", "c", "d", "e"), aJava(lista));
        assertEquals("c", lista.eliminar(1));
        assertEquals(List.of("b", "d", "e"), aJava(lista));
        assertEquals("e", lista.eliminar(2));
        assertEquals(List.of("b", "d"), aJava(lista));
    }

    @Test
    void iteradorRecorreEnOrdenYAlFinalLanza() {
        ListaDinamica<String> lista = new ListaDinamica<>();
        lista.agregar("a");
        lista.agregar("b");
        Iterator<String> it = lista.iterator();
        assertEquals("a", it.next());
        assertEquals("b", it.next());
        assertFalse(it.hasNext());
        assertThrows(NoSuchElementException.class, it::next);
    }
}
