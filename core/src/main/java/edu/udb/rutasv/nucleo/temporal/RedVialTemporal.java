package edu.udb.rutasv.nucleo.temporal;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * TEMPORAL: reemplazar por la implementacion propia.
 * Red respaldada por mapas de {@code java.util}.
 */
public class RedVialTemporal implements RedVial {

    private final Map<String, Estacion> estaciones = new LinkedHashMap<>();
    private final Map<String, Lista<Arista>> adyacencia = new HashMap<>();

    @Override
    public void agregarEstacion(Estacion estacion) {
        if (estacion == null || estacion.codigo() == null) {
            throw new ValidacionException("La estacion y su codigo son obligatorios");
        }
        estaciones.put(estacion.codigo(), estacion);
        adyacencia.computeIfAbsent(estacion.codigo(), k -> new ListaTemporal<>());
    }

    @Override
    public void agregarTramo(String origenCodigo, String destinoCodigo, int minutos, double km) {
        if (minutos <= 0) {
            throw new ValidacionException("Los minutos deben ser mayores que cero");
        }
        if (!estaciones.containsKey(origenCodigo) || !estaciones.containsKey(destinoCodigo)) {
            throw new ValidacionException("Estacion inexistente en el tramo");
        }
        adyacencia.get(origenCodigo).agregar(new Arista(destinoCodigo, minutos, km));
    }

    @Override
    public Lista<Arista> vecinos(String codigo) {
        Lista<Arista> origen = adyacencia.get(codigo);
        Lista<Arista> copia = new ListaTemporal<>();
        if (origen != null) {
            for (Arista a : origen) {
                copia.agregar(a);
            }
        }
        return copia;
    }

    @Override
    public Lista<Estacion> estaciones() {
        Lista<Estacion> resultado = new ListaTemporal<>();
        for (Estacion e : estaciones.values()) {
            resultado.agregar(e);
        }
        return resultado;
    }
}
