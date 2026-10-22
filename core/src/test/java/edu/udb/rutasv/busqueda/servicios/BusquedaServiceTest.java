package edu.udb.rutasv.busqueda.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import edu.udb.rutasv.busqueda.estructuras.ArbolAvl;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
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
        servicio = new BusquedaService(indice, new MapaTemporal<>());
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
        assertEquals(List.of("Terminal Occidente", "Terminal Oriente"), aJava(servicio.sugerir("ana", "ter")));
    }

    @Test
    void sugerirIgnoraEspaciosAlInicio() {
        assertEquals(List.of("Metrocentro"), aJava(servicio.sugerir("ana", "  metro")));
    }

    @Test
    void sugerirConTextoNuloLanza() {
        assertThrows(IllegalArgumentException.class, () -> servicio.sugerir("ana", null));
    }

    @Test
    void textoEnBlancoNoSeGuarda() {
        servicio.sugerir("ana", "   ");
        servicio.sugerir("ana", "́");
        assertEquals(List.of(), aJava(servicio.recientes("ana")));
    }

    @Test
    void recientesDeLaMasRecienteALaMasAntigua() {
        servicio.sugerir("ana", "metro");
        servicio.sugerir("ana", "ter");
        assertEquals(List.of("ter", "metro"), aJava(servicio.recientes("ana")));
    }

    @Test
    void escribirLetraPorLetraGuardaUnaSola() {
        servicio.sugerir("ana", "t");
        servicio.sugerir("ana", "te");
        servicio.sugerir("ana", "ter");
        assertEquals(List.of("ter"), aJava(servicio.recientes("ana")));
    }

    @Test
    void borrarLetrasNoGuardaFragmentos() {
        servicio.sugerir("ana", "metrocentro");
        servicio.sugerir("ana", "metrocentr");
        servicio.sugerir("ana", "metrocent");
        servicio.sugerir("ana", "m");
        assertEquals(List.of("metrocentro"), aJava(servicio.recientes("ana")));
    }

    @Test
    void repetirUnaBusquedaLaSubeSinDuplicarla() {
        servicio.sugerir("ana", "metro");
        servicio.sugerir("ana", "ter");
        servicio.sugerir("ana", "Metro");
        assertEquals(List.of("Metro", "ter"), aJava(servicio.recientes("ana")));
    }

    @Test
    void guardaComoMaximoCinco() {
        for (String texto : new String[] {"a", "b", "c", "d", "e", "f"}) {
            servicio.sugerir("ana", texto);
        }
        assertEquals(List.of("f", "e", "d", "c", "b"), aJava(servicio.recientes("ana")));
    }

    @Test
    void recientesSonPorUsuario() {
        servicio.sugerir("ana", "metro");
        assertEquals(List.of(), aJava(servicio.recientes("bob")));
    }
}
