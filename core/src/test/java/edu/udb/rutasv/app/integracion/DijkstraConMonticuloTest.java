package edu.udb.rutasv.app.integracion;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.ruta.algoritmos.Dijkstra;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;
import edu.udb.rutasv.ruta.modelo.Camino;
import edu.udb.rutasv.terminal.estructuras.MonticuloBinario;
import org.junit.jupiter.api.Test;

/**
 * Dijkstra con la cola de prioridad propia de Terminal, como lo arma {@code Aplicacion}. Vive en
 * {@code app} porque es la unica que puede juntar dos modulos.
 */
class DijkstraConMonticuloTest {

    private final Dijkstra dijkstra = new Dijkstra(MonticuloBinario::new, MapaTemporal::new);

    @Test
    void rutaDelEjemploCon18Minutos() {
        GrafoListaAdyacencia red = new GrafoListaAdyacencia();
        for (String c : new String[] {"E01", "E02", "E03", "E04", "E06"}) {
            red.agregarEstacion(new Estacion(c, c, "Z", 0, 0, true));
        }
        red.agregarTramo("E01", "E02", 8, 4.0);
        red.agregarTramo("E02", "E03", 8, 4.5);
        red.agregarTramo("E03", "E04", 2, 1.0);
        red.agregarTramo("E02", "E06", 5, 2.0);
        red.agregarTramo("E06", "E04", 20, 2.0);

        Camino c = dijkstra.calcular(red, "E01", "E04").orElseThrow();
        assertEquals(18, c.minutos());
        assertEquals(4, c.estaciones().tamano());
        assertEquals("E03", c.estaciones().obtener(2));
    }

    @Test
    void redGrandeEnCadenaUsaElMonticulo() {
        GrafoListaAdyacencia red = new GrafoListaAdyacencia();
        int n = 500;
        for (int i = 0; i < n; i++) {
            red.agregarEstacion(new Estacion("S" + i, "S" + i, "Z", 0, 0, true));
        }
        for (int i = 0; i + 1 < n; i++) {
            red.agregarTramo("S" + i, "S" + (i + 1), 1, 1.0);
            // Atajos caros que Dijkstra debe descartar.
            if (i + 2 < n) {
                red.agregarTramo("S" + i, "S" + (i + 2), 5, 1.0);
            }
        }
        Camino c = dijkstra.calcular(red, "S0", "S" + (n - 1)).orElseThrow();
        assertEquals(n - 1, c.minutos());
        assertEquals(n, c.estaciones().tamano());
        assertTrue(dijkstra.calcular(red, "S" + (n - 1), "S0").isEmpty());
    }
}
