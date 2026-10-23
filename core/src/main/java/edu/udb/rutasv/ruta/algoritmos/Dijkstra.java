package edu.udb.rutasv.ruta.algoritmos;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.Pila;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.ruta.estructuras.PilaEnlazada;
import edu.udb.rutasv.ruta.interfaces.FabricaColaPrioridad;
import edu.udb.rutasv.ruta.interfaces.FabricaMapa;
import edu.udb.rutasv.ruta.modelo.Camino;
import java.util.Comparator;
import java.util.Optional;

/**
 * Ruta mas rapida (en minutos) entre dos estaciones, con el algoritmo de Dijkstra.
 *
 * <ol>
 *   <li>Inicio: peso(origen) = 0 y el origen entra a la cola de prioridad.
 *   <li>Ciclo: sale la estacion pendiente con menor peso; si ya se cerro, se ignora. Cada tramo hacia
 *       una estacion activa que mejora su peso actualiza peso y anterior y vuelve a la cola.
 *   <li>Reconstruccion: desde el destino se sigue "anterior" apilando cada estacion; al desapilar
 *       salen de origen a destino.
 * </ol>
 *
 * Complejidad: O((V + E) log V) con el montículo binario; memoria O(V + E).
 * Las estaciones inactivas no se visitan. Si el destino no se alcanza no hay ruta (RN-6).
 */
public class Dijkstra {

    /** Estacion pendiente en la cola, con el peso con que se encolo. */
    private record Pendiente(String codigo, int minutos) {
    }

    private static final Comparator<Pendiente> MENOR_PESO =
            Comparator.comparingInt(Pendiente::minutos).thenComparing(Pendiente::codigo);

    private final FabricaColaPrioridad colas;
    private final FabricaMapa mapas;

    public Dijkstra(FabricaColaPrioridad colas, FabricaMapa mapas) {
        this.colas = colas;
        this.mapas = mapas;
    }

    /**
     * Camino de menor tiempo de {@code origen} a {@code destino}. Vacio si no hay camino o si alguna
     * de las dos estaciones no existe o esta inactiva. Si origen y destino son iguales, el camino
     * tiene una sola estacion y 0 minutos.
     */
    public Optional<Camino> calcular(RedVial red, String origen, String destino) {
        Mapa<String, Estacion> porCodigo = mapas.crear();
        for (Estacion e : red.estaciones()) {
            porCodigo.poner(e.codigo(), e);
        }
        if (!activa(porCodigo, origen) || !activa(porCodigo, destino)) {
            return Optional.empty();
        }

        Mapa<String, Integer> peso = mapas.crear();
        Mapa<String, Double> km = mapas.crear();
        Mapa<String, String> anterior = mapas.crear();
        Mapa<String, Boolean> cerrada = mapas.crear();
        ColaPrioridad<Pendiente> cola = colas.crear(MENOR_PESO);

        peso.poner(origen, 0);
        km.poner(origen, 0.0);
        cola.insertar(new Pendiente(origen, 0));

        while (!cola.estaVacia()) {
            Pendiente actual = cola.extraer();
            if (cerrada.contiene(actual.codigo())) {
                continue;
            }
            cerrada.poner(actual.codigo(), true);
            if (actual.codigo().equals(destino)) {
                break;
            }
            for (Arista tramo : red.vecinos(actual.codigo())) {
                String vecino = tramo.destinoCodigo();
                if (cerrada.contiene(vecino) || !activa(porCodigo, vecino)) {
                    continue;
                }
                int nuevoPeso = actual.minutos() + tramo.minutos();
                Integer pesoVecino = peso.obtener(vecino);
                if (pesoVecino == null || nuevoPeso < pesoVecino) {
                    peso.poner(vecino, nuevoPeso);
                    km.poner(vecino, km.obtener(actual.codigo()) + tramo.km());
                    anterior.poner(vecino, actual.codigo());
                    cola.insertar(new Pendiente(vecino, nuevoPeso));
                }
            }
        }

        if (!peso.contiene(destino)) {
            return Optional.empty();
        }
        return Optional.of(new Camino(reconstruir(anterior, destino), peso.obtener(destino), km.obtener(destino)));
    }

    /** Sigue "anterior" desde el destino apilando, y desapila para obtener el orden origen -> destino. */
    private static Lista<String> reconstruir(Mapa<String, String> anterior, String destino) {
        Pila<String> pila = new PilaEnlazada<>();
        for (String codigo = destino; codigo != null; codigo = anterior.obtener(codigo)) {
            pila.apilar(codigo);
        }
        Lista<String> ruta = new ListaTemporal<>();
        while (!pila.estaVacia()) {
            ruta.agregar(pila.desapilar());
        }
        return ruta;
    }

    private static boolean activa(Mapa<String, Estacion> porCodigo, String codigo) {
        Estacion e = codigo == null ? null : porCodigo.obtener(codigo);
        return e != null && e.activa();
    }
}
