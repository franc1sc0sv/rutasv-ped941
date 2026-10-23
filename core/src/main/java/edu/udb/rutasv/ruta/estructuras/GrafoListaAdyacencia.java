package edu.udb.rutasv.ruta.estructuras;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;

/**
 * Grafo ponderado y dirigido de la red vial con lista de adyacencia (rol Ruta).
 * Cada estacion tiene su lista de tramos salientes; el acceso por codigo es por tabla hash.
 * Rechaza minutos <= 0 (RN-2) y guarda un tramo por sentido (RN-3): repetir A->B lo reemplaza.
 */
public class GrafoListaAdyacencia implements RedVial {

    private final Mapa<String, Estacion> estaciones;
    private final Mapa<String, Lista<Arista>> adyacencia;
    /** Codigos en orden de alta, para listar la red siempre en el mismo orden. */
    private final Lista<String> orden = nuevaLista();

    /** Usa las tablas hash temporales hasta que llegue {@code TablaHash} (rol Paradas). */
    public GrafoListaAdyacencia() {
        this(new MapaTemporal<>(), new MapaTemporal<>());
    }

    /** Recibe las dos tablas hash vacias: estaciones por codigo y tramos salientes por codigo. */
    public GrafoListaAdyacencia(Mapa<String, Estacion> estaciones, Mapa<String, Lista<Arista>> adyacencia) {
        this.estaciones = estaciones;
        this.adyacencia = adyacencia;
    }

    // Cuando llegue ListaDinamica (rol Historial) basta con cambiar esta linea.
    private static <T> Lista<T> nuevaLista() {
        return new ListaTemporal<>();
    }

    @Override
    public void agregarEstacion(Estacion estacion) {
        if (estacion == null || estacion.codigo() == null || estacion.codigo().isBlank()) {
            throw new ValidacionException("La estacion y su codigo son obligatorios");
        }
        String codigo = estacion.codigo();
        if (!estaciones.contiene(codigo)) {
            orden.agregar(codigo);
            adyacencia.poner(codigo, nuevaLista());
        }
        estaciones.poner(codigo, estacion);
    }

    @Override
    public void agregarTramo(String origenCodigo, String destinoCodigo, int minutos, double km) {
        if (minutos <= 0) {
            throw new ValidacionException("Los minutos del tramo deben ser mayores que cero (RN-2)");
        }
        if (km < 0) {
            throw new ValidacionException("Los km del tramo no pueden ser negativos");
        }
        if (!estaciones.contiene(origenCodigo)) {
            throw new ValidacionException("No existe la estacion de origen: " + origenCodigo);
        }
        if (!estaciones.contiene(destinoCodigo)) {
            throw new ValidacionException("No existe la estacion de destino: " + destinoCodigo);
        }
        if (origenCodigo.equals(destinoCodigo)) {
            throw new ValidacionException("Un tramo no puede salir y llegar a la misma estacion");
        }
        Lista<Arista> salientes = adyacencia.obtener(origenCodigo);
        for (int i = 0; i < salientes.tamano(); i++) {
            if (salientes.obtener(i).destinoCodigo().equals(destinoCodigo)) {
                salientes.eliminar(i);
                break;
            }
        }
        salientes.agregar(new Arista(destinoCodigo, minutos, km));
    }

    /** Copia de los tramos salientes: quien la recibe no puede cambiar el grafo. */
    @Override
    public Lista<Arista> vecinos(String codigo) {
        Lista<Arista> copia = nuevaLista();
        Lista<Arista> salientes = codigo == null ? null : adyacencia.obtener(codigo);
        if (salientes != null) {
            for (Arista a : salientes) {
                copia.agregar(a);
            }
        }
        return copia;
    }

    @Override
    public Lista<Estacion> estaciones() {
        Lista<Estacion> resultado = nuevaLista();
        for (String codigo : orden) {
            resultado.agregar(estaciones.obtener(codigo));
        }
        return resultado;
    }

    /** Estacion por codigo, o {@code null} si no existe. O(1) promedio. */
    public Estacion estacion(String codigo) {
        return codigo == null ? null : estaciones.obtener(codigo);
    }
}
