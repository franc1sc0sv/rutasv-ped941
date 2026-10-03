package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.modelo.Estacion;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Contrato de {@link IndiceNombres}. Las estaciones son sinteticas. */
public abstract class IndiceNombresContractTest {

    protected abstract IndiceNombres crear();

    private static Estacion est(String codigo, String nombre) {
        return new Estacion(codigo, nombre, "Z1", 0.0, 0.0, true);
    }

    private static List<String> aJava(Lista<String> l) {
        List<String> r = new ArrayList<>();
        for (String s : l) {
            r.add(s);
        }
        return r;
    }

    private IndiceNombres conDatos() {
        IndiceNombres i = crear();
        i.insertar("San Martin", est("E1", "San Martin"));
        i.insertar("San Andres", est("E2", "San Andres"));
        i.insertar("Santa Rosa", est("E3", "Santa Rosa"));
        i.insertar("Terminal Norte", est("E4", "Terminal Norte"));
        return i;
    }

    @Test
    void buscarExacto() {
        IndiceNombres i = conDatos();
        assertEquals("E1", i.buscar("San Martin").codigo());
        assertNull(i.buscar("Inexistente"));
    }

    @Test
    void buscarIgnoraMayusculasYAcentos() {
        IndiceNombres i = crear();
        i.insertar("Estación Perú", est("E9", "Estación Perú"));
        assertEquals("E9", i.buscar("estacion peru").codigo());
        assertEquals("E9", i.buscar("ESTACIÓN PERÚ").codigo());
    }

    @Test
    void prefijoDevuelveOrdenAlfabetico() {
        IndiceNombres i = conDatos();
        assertEquals(List.of("San Andres", "San Martin", "Santa Rosa"), aJava(i.conPrefijo("san")));
        assertEquals(List.of("San Andres", "San Martin"), aJava(i.conPrefijo("San ")));
    }

    @Test
    void prefijoIgnoraMayusculasYAcentos() {
        IndiceNombres i = crear();
        i.insertar("Ágata", est("E1", "Ágata"));
        i.insertar("Agua Fría", est("E2", "Agua Fría"));
        i.insertar("Bosque", est("E3", "Bosque"));
        assertEquals(List.of("Ágata", "Agua Fría"), aJava(i.conPrefijo("AG")));
        assertEquals(List.of("Ágata"), aJava(i.conPrefijo("agá")));
    }

    @Test
    void prefijoSinCoincidenciasDaListaVacia() {
        assertTrue(conDatos().conPrefijo("zzz").estaVacia());
    }

    @Test
    void prefijoVacioDevuelveTodos() {
        assertEquals(4, conDatos().conPrefijo("").tamano());
    }

    @Test
    void indiceVacio() {
        IndiceNombres i = crear();
        assertNull(i.buscar("x"));
        assertTrue(i.conPrefijo("x").estaVacia());
        assertTrue(i.conPrefijo("").estaVacia());
    }

    @Test
    void insertarMismoNombreReemplaza() {
        IndiceNombres i = conDatos();
        i.insertar("san martin", est("E10", "San Martin"));
        assertEquals("E10", i.buscar("San Martin").codigo());
        assertEquals(2, i.conPrefijo("san ").tamano());
    }

    @Test
    void eliminarQuitaDelIndice() {
        IndiceNombres i = conDatos();
        i.eliminar("San Martin");
        assertNull(i.buscar("San Martin"));
        assertEquals(List.of("San Andres", "Santa Rosa"), aJava(i.conPrefijo("san")));
    }

    @Test
    void eliminarInexistenteNoHaceNada() {
        IndiceNombres i = conDatos();
        assertDoesNotThrow(() -> i.eliminar("No existe"));
        assertEquals(4, i.conPrefijo("").tamano());
    }
}
