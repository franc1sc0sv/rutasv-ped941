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
 *
 * <p>Complejidad con R rutas y P pares distintos: contar es O(R) (tabla hash). El top 5 usa un
 * montículo <em>acotado</em> de 5 elementos cuya raiz es el peor de los 5: cada par entra y, si
 * sobran, sale el peor. Cada operacion es O(log 5) = O(1), asi que el top cuesta O(P) en vez de
 * O(P log P) que costaria meter todos los pares a la cola. Total: O(R + P) = O(R).
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

        // Orden del ranking: mas consultas primero; si empatan, orden alfabetico.
        Comparator<String> mejorPrimero = (a, b) -> {
            int porConteo = Integer.compare(conteos.obtener(b), conteos.obtener(a));
            return porConteo != 0 ? porConteo : a.compareTo(b);
        };
        // Montículo acotado: la raiz es el PEOR de los que estan, para sacarlo cuando sobra.
        ColaPrioridad<String> peores = fabricaColas.apply(mejorPrimero.reversed());
        for (String par : conteos.claves()) {
            peores.insertar(par);
            if (peores.tamano() > TOP) {
                peores.extraer();
            }
        }
        String[] top = new String[peores.tamano()];
        for (int i = top.length - 1; i >= 0; i--) {
            top[i] = peores.extraer();
        }

        int total = rutas.tamano();
        return new EstadisticasGlobales(total, top.length == 0 ? null : top[0],
                total == 0 ? 0 : sumaNs / total, List.of(top));
    }
}
