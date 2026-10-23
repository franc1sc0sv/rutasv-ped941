package edu.udb.rutasv.busqueda.estructuras;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ArbolAvlTest {

    private static final String[] FIG9 = {"Metrocentro", "Multiplaza", "Plaza Merliot", "Redondel Masferrer",
        "Terminal Occidente", "Terminal Oriente", "Universidad UDB", "Zona Rosa"};

    private static Estacion est(String codigo, String nombre) {
        return new Estacion(codigo, nombre, "Z1", 0.0, 0.0, true);
    }

    private static ArbolAvl conNombres(String... nombres) {
        ArbolAvl arbol = new ArbolAvl();
        for (int i = 0; i < nombres.length; i++) {
            arbol.insertar(nombres[i], est("E" + i, nombres[i]));
        }
        return arbol;
    }

    private static List<String> aJava(Lista<String> lista) {
        List<String> resultado = new ArrayList<>();
        for (String s : lista) {
            resultado.add(s);
        }
        return resultado;
    }

    private static void assertEncuentraTodos(ArbolAvl arbol, String... nombres) {
        for (int i = 0; i < nombres.length; i++) {
            assertEquals("E" + i, arbol.buscar(nombres[i]).codigo());
        }
    }

    @Test
    void arbolVacio() {
        ArbolAvl arbol = new ArbolAvl();
        assertEquals(0, arbol.altura());
        assertNull(arbol.buscar("Metrocentro"));
    }

    @Test
    void rotacionLL() {
        ArbolAvl arbol = conNombres("C", "B", "A");
        assertEquals(2, arbol.altura());
        assertEncuentraTodos(arbol, "C", "B", "A");
    }

    @Test
    void rotacionRR() {
        ArbolAvl arbol = conNombres("A", "B", "C");
        assertEquals(2, arbol.altura());
        assertEncuentraTodos(arbol, "A", "B", "C");
    }

    @Test
    void rotacionLR() {
        ArbolAvl arbol = conNombres("C", "A", "B");
        assertEquals(2, arbol.altura());
        assertEncuentraTodos(arbol, "C", "A", "B");
    }

    @Test
    void rotacionRL() {
        ArbolAvl arbol = conNombres("A", "C", "B");
        assertEquals(2, arbol.altura());
        assertEncuentraTodos(arbol, "A", "C", "B");
    }

    @Test
    void ochoNombresAlfabeticosQuedanBalanceados() {
        ArbolAvl arbol = conNombres(FIG9);
        assertEquals(4, arbol.altura());
        assertEncuentraTodos(arbol, FIG9);
    }

    @Test
    void eliminarConRotacion() {
        ArbolAvl arbol = conNombres("B", "A", "C", "D");
        assertEquals(3, arbol.altura());
        arbol.eliminar("A");
        assertEquals(2, arbol.altura());
        assertNull(arbol.buscar("A"));
        assertEquals("E0", arbol.buscar("B").codigo());
        assertEquals("E2", arbol.buscar("C").codigo());
        assertEquals("E3", arbol.buscar("D").codigo());
    }

    @Test
    void eliminarNodoConDosHijosUsaSucesor() {
        ArbolAvl arbol = conNombres("B", "A", "C");
        arbol.eliminar("b");
        assertNull(arbol.buscar("B"));
        assertEquals("E1", arbol.buscar("A").codigo());
        assertEquals("E2", arbol.buscar("C").codigo());
        assertEquals(2, arbol.altura());
    }

    @Test
    void prefijoTerDaLasDosTerminales() {
        assertEquals(List.of("Terminal Occidente", "Terminal Oriente"), aJava(conNombres(FIG9).conPrefijo("Ter")));
    }

    @Test
    void prefijoMetroDaMetrocentro() {
        assertEquals(List.of("Metrocentro"), aJava(conNombres(FIG9).conPrefijo("metro")));
    }

    @Test
    void prefijoSinCoincidenciasDaListaVacia() {
        assertEquals(List.of(), aJava(conNombres(FIG9).conPrefijo("xyz")));
    }

    @Test
    void prefijoVacioDaTodasEnOrdenAlfabetico() {
        assertEquals(List.of(FIG9), aJava(conNombres(FIG9).conPrefijo("")));
    }
}
