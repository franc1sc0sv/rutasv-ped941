package edu.udb.rutasv.historial.servicios;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Estadisticas globales del historial (rol Historial): total, ruta mas consultada, top 5 y tiempo promedio.
 * El conteo por par usa un {@link Mapa} y el top 5 una {@link ColaPrioridad}, ambos creados por fabricas.
 */
public class EstadisticasService {

    static final int TOP = 5;

    private final HistorialService historial;
    private final Supplier<Mapa<String, Integer>> fabricaMapas;
    private final Function<Comparator<String>, ColaPrioridad<String>> fabricaColas;

    public EstadisticasService(HistorialService historial, Supplier<Mapa<String, Integer>> fabricaMapas,
                               Function<Comparator<String>, ColaPrioridad<String>> fabricaColas) {
        this.historial = historial;
        this.fabricaMapas = fabricaMapas;
        this.fabricaColas = fabricaColas;
    }

    /** Pares {@code "origen -> destino"} con codigos; sin rutas, la mas consultada es {@code null} y el promedio 0. */
    public EstadisticasGlobales calcular() {
        Lista<RutaCalculada> rutas = historial.todas();
        Mapa<String, Integer> conteos = fabricaMapas.get();
        long sumaNs = 0;
        for (RutaCalculada ruta : rutas) {
            sumaNs += ruta.tiempoCalculoNs();
            String par = ruta.origen() + " -> " + ruta.destino();
            Integer veces = conteos.obtener(par);
            conteos.poner(par, veces == null ? 1 : veces + 1);
        }

        ColaPrioridad<String> cola = fabricaColas.apply((a, b) -> {
            int porConteo = Integer.compare(conteos.obtener(b), conteos.obtener(a));
            return porConteo != 0 ? porConteo : a.compareTo(b);
        });
        for (String par : conteos.claves()) {
            cola.insertar(par);
        }
        String[] top = new String[Math.min(TOP, cola.tamano())];
        for (int i = 0; i < top.length; i++) {
            top[i] = cola.extraer();
        }

        int total = rutas.tamano();
        return new EstadisticasGlobales(total, top.length == 0 ? null : top[0],
                total == 0 ? 0 : sumaNs / total, List.of(top));
    }
}
