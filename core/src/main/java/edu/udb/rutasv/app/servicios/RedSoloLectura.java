package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.RedVialTemporal;

/**
 * Copia de la red que se entrega en {@code verRed}: se puede leer, pero los cambios solo entran por
 * la fachada, que exige ADMIN (RN-1). Es una foto tomada bajo el candado de lectura, asi que leerla
 * despues no compite con un {@code agregarTramo} en curso.
 */
final class RedSoloLectura implements RedVial {

    private final RedVial red;

    private RedSoloLectura(RedVial red) {
        this.red = red;
    }

    /** Copia estaciones y tramos de {@code original}. O(V + E). */
    static RedSoloLectura copiaDe(RedVial original) {
        RedVial copia = new RedVialTemporal();
        for (Estacion e : original.estaciones()) {
            copia.agregarEstacion(e);
        }
        for (Estacion e : original.estaciones()) {
            for (Arista a : original.vecinos(e.codigo())) {
                copia.agregarTramo(e.codigo(), a.destinoCodigo(), a.minutos(), a.km());
            }
        }
        return new RedSoloLectura(copia);
    }

    @Override
    public void agregarEstacion(Estacion estacion) {
        throw new AccesoDenegadoException("La red es de solo lectura: usa crearEstacion");
    }

    @Override
    public void agregarTramo(String origenCodigo, String destinoCodigo, int minutos, double km) {
        throw new AccesoDenegadoException("La red es de solo lectura: usa agregarTramo de la fachada");
    }

    @Override
    public Lista<Arista> vecinos(String codigo) {
        return red.vecinos(codigo);
    }

    @Override
    public Lista<Estacion> estaciones() {
        return red.estaciones();
    }
}
