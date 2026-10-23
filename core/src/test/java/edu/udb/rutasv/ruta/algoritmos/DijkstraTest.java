package edu.udb.rutasv.ruta.algoritmos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.ruta.RedDeEjemplo;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;
import edu.udb.rutasv.ruta.modelo.Camino;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class DijkstraTest {

    private final Dijkstra dijkstra = new Dijkstra(ColaPrioridadTemporal::new, MapaTemporal::new);
    private final GrafoListaAdyacencia red = RedDeEjemplo.crear();

    private static List<String> codigos(Lista<String> lista) {
        List<String> out = new ArrayList<>();
        for (String s : lista) {
            out.add(s);
        }
        return out;
    }

    @Test
    void occidenteAMetrocentroEn18Minutos() {
        Camino c = dijkstra.calcular(red, "E01", "E04").orElseThrow();
        assertEquals(List.of("E01", "E02", "E03", "E04"), codigos(c.estaciones()));
        assertEquals(18, c.minutos());
        assertEquals(9.5, c.km(), 1e-9);
    }

    @Test
    void eligeElCaminoMasRapidoAunqueTengaMasTramos() {
        red.agregarTramo("E02", "E04", 20, 3.0);
        Camino c = dijkstra.calcular(red, "E02", "E04").orElseThrow();
        assertEquals(List.of("E02", "E03", "E04"), codigos(c.estaciones()));
        assertEquals(10, c.minutos());
    }

    @Test
    void origenIgualADestino() {
        Camino c = dijkstra.calcular(red, "E03", "E03").orElseThrow();
        assertEquals(List.of("E03"), codigos(c.estaciones()));
        assertEquals(0, c.minutos());
        assertEquals(0.0, c.km());
    }

    @Test
    void sinRutaDevuelveVacioRn6() {
        red.agregarEstacion(new Estacion("E99", "Aislada", "Z", 0, 0, true));
        assertTrue(dijkstra.calcular(red, "E01", "E99").isEmpty());
    }

    @Test
    void tramoDeUnSoloSentidoNoSirveDeVuelta() {
        red.agregarEstacion(new Estacion("E10", "Nueva", "Z", 0, 0, true));
        red.agregarTramo("E01", "E10", 3, 1.0);
        assertTrue(dijkstra.calcular(red, "E01", "E10").isPresent());
        assertTrue(dijkstra.calcular(red, "E10", "E01").isEmpty());
    }

    @Test
    void saltaEstacionesInactivas() {
        red.agregarEstacion(new Estacion("E03", "Plaza Merliot", "Z", 0, 0, false));
        // Sin Plaza Merliot no hay forma de llegar a Metrocentro desde Occidente.
        assertTrue(dijkstra.calcular(red, "E01", "E04").isEmpty());
        // Un desvio por otra estacion activa si funciona.
        red.agregarTramo("E06", "E04", 4, 2.0);
        Camino c = dijkstra.calcular(red, "E01", "E04").orElseThrow();
        assertEquals(List.of("E01", "E02", "E06", "E04"), codigos(c.estaciones()));
        assertEquals(17, c.minutos());
    }

    @Test
    void origenODestinoInactivoNoTieneRuta() {
        red.agregarEstacion(new Estacion("E04", "Metrocentro", "Z", 0, 0, false));
        assertTrue(dijkstra.calcular(red, "E01", "E04").isEmpty());
        assertTrue(dijkstra.calcular(red, "E04", "E01").isEmpty());
    }

    @Test
    void estacionInexistenteNoTieneRuta() {
        assertTrue(dijkstra.calcular(red, "E01", "ZZ").isEmpty());
    }
}
