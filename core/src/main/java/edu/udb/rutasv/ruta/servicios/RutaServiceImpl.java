package edu.udb.rutasv.ruta.servicios;

import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.ruta.algoritmos.Dijkstra;
import edu.udb.rutasv.ruta.interfaces.RutaService;
import edu.udb.rutasv.ruta.modelo.Camino;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Calcula la ruta con Dijkstra sobre la red y la entrega como {@link RutaCalculada}. */
public class RutaServiceImpl implements RutaService {

    private final RedVial red;
    private final Dijkstra dijkstra;
    private final Clock reloj;

    public RutaServiceImpl(RedVial red, Dijkstra dijkstra) {
        this(red, dijkstra, Clock.systemDefaultZone());
    }

    /** Con un reloj dado, para fijar la fecha en las pruebas. */
    public RutaServiceImpl(RedVial red, Dijkstra dijkstra, Clock reloj) {
        this.red = red;
        this.dijkstra = dijkstra;
        this.reloj = reloj;
    }

    @Override
    public Optional<RutaCalculada> calcular(String origenCodigo, String destinoCodigo, String usuario) {
        exigirEstacion(origenCodigo, "origen");
        exigirEstacion(destinoCodigo, "destino");

        long inicio = System.nanoTime();
        Optional<Camino> camino = dijkstra.calcular(red, origenCodigo, destinoCodigo);
        long tiempoNs = System.nanoTime() - inicio;

        return camino.map(c -> new RutaCalculada(origenCodigo, destinoCodigo, aLista(c), c.minutos(),
                redondear(c.km()), LocalDateTime.now(reloj), tiempoNs, usuario));
    }

    private void exigirEstacion(String codigo, String cual) {
        if (codigo == null || codigo.isBlank()) {
            throw new ValidacionException("Falta la estacion de " + cual);
        }
        for (Estacion e : red.estaciones()) {
            if (e.codigo().equals(codigo)) {
                return;
            }
        }
        throw new ValidacionException("No existe la estacion de " + cual + ": " + codigo);
    }

    // RutaCalculada (nucleo) guarda los tramos como java.util.List inmutable.
    private static List<String> aLista(Camino c) {
        List<String> codigos = new ArrayList<>();
        for (String codigo : c.estaciones()) {
            codigos.add(codigo);
        }
        return List.copyOf(codigos);
    }

    /** Evita 9.499999 al sumar km decimales. */
    private static double redondear(double km) {
        return Math.round(km * 100) / 100.0;
    }
}
