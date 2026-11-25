package edu.udb.rutasv.busqueda.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.udb.rutasv.busqueda.estructuras.ArbolAvl;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class BusquedaServiceTest {

    private final BusquedaService servicio;

    BusquedaServiceTest() {
        ArbolAvl indice = new ArbolAvl();
        for (String nombre : new String[] {"Terminal Occidente", "Terminal Oriente", "Metrocentro"}) {
            indice.insertar(nombre, new Estacion("E1", nombre, "Z1", 0, 0, true));
        }
        servicio = new BusquedaService(indice, new TablaHash<>());
    }

    private static List<String> aJava(Lista<String> lista) {
        List<String> resultado = new ArrayList<>();
        for (String s : lista) {
            resultado.add(s);
        }
        return resultado;
    }

    @Test
    void sugerirDevuelveCoincidencias() {
        assertEquals(List.of("Terminal Occidente", "Terminal Oriente"), aJava(servicio.sugerir("ter")));
    }

    @Test
    void sugerirIgnoraEspaciosYMayusculas() {
        assertEquals(List.of("Metrocentro"), aJava(servicio.sugerir("  METRO")));
    }

    @Test
    void sugerirConTextoNuloLanza() {
        assertThrows(ValidacionException.class, () -> servicio.sugerir(null));
    }

    @Test
    void sugerirNoGuardaLoQueSeTeclea() {
        servicio.sugerir("metro");
        assertEquals(List.of(), aJava(servicio.recientes("ana")));
    }

    @Test
    void busquedaEnBlancoNoSeGuarda() {
        servicio.registrarBusqueda("ana", "   ");
        servicio.registrarBusqueda("ana", null);
        assertEquals(List.of(), aJava(servicio.recientes("ana")));
    }

    @Test
    void recientesDeLaMasRecienteALaMasAntigua() {
        servicio.registrarBusqueda("ana", "Metrocentro → Terminal Oriente");
        servicio.registrarBusqueda("ana", "Terminal Occidente → Metrocentro");
        assertEquals(List.of("Terminal Occidente → Metrocentro", "Metrocentro → Terminal Oriente"),
                aJava(servicio.recientes("ana")));
    }

    @Test
    void repetirUnaBusquedaLaSubeSinDuplicarla() {
        servicio.registrarBusqueda("ana", "A → B");
        servicio.registrarBusqueda("ana", "C → D");
        servicio.registrarBusqueda("ana", "a → b");
        assertEquals(List.of("a → b", "C → D"), aJava(servicio.recientes("ana")));
    }

    @Test
    void guardaComoMaximoCinco() {
        for (String texto : new String[] {"a", "b", "c", "d", "e", "f"}) {
            servicio.registrarBusqueda("ana", texto);
        }
        assertEquals(List.of("f", "e", "d", "c", "b"), aJava(servicio.recientes("ana")));
    }

    @Test
    void recientesSonPorUsuario() {
        servicio.registrarBusqueda("ana", "A → B");
        assertEquals(List.of(), aJava(servicio.recientes("bob")));
    }
}
