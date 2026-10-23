package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;

/**
 * Vista de la red que se entrega en {@code verRed}: se puede leer, pero los cambios solo entran por
 * la fachada, que exige ADMIN (RN-1).
 */
final class RedSoloLectura implements RedVial {

    private final RedVial red;

    RedSoloLectura(RedVial red) {
        this.red = red;
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
