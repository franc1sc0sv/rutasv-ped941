package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;

/**
 * Directorio de estaciones (rol Paradas): busqueda directa por codigo o por nombre. Codigo y nombre
 * son unicos. Los dos indices son tablas hash que llegan por el constructor como {@link Mapa}; el
 * constructor sin argumentos usa la version temporal hasta que este lista {@code TablaHash}.
 */
public class DirectorioEstaciones {

    private final Mapa<String, Estacion> porCodigo;
    private final Mapa<String, Estacion> porNombreNormalizado;

    public DirectorioEstaciones() {
        this(new MapaTemporal<>(), new MapaTemporal<>());
    }

    public DirectorioEstaciones(Mapa<String, Estacion> porCodigo, Mapa<String, Estacion> porNombreNormalizado) {
        this.porCodigo = porCodigo;
        this.porNombreNormalizado = porNombreNormalizado;
    }

    public void darDeAlta(String codigo, String nombre, String zona, double x, double y) {
        darDeAlta(codigo, nombre, zona, x, y, true);
    }

    public void darDeAlta(String codigo, String nombre, String zona, double x, double y, boolean activa) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new ValidacionException("El codigo de la estacion no puede ser nulo ni vacio");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ValidacionException("El nombre de la estacion no puede ser nulo ni vacio");
        }

        String codigoLimpio = codigo.trim();
        String nombreLimpio = nombre.trim();
        String zonaLimpia = (zona != null) ? zona.trim() : "";
        String nombreNormalizado = nombreLimpio.toLowerCase();

        if (porCodigo.contiene(codigoLimpio)) {
            throw new ValidacionException("Ya existe una estacion registrada con el codigo: " + codigoLimpio);
        }
        if (porNombreNormalizado.contiene(nombreNormalizado)) {
            throw new ValidacionException("Ya existe una estacion registrada con el nombre: " + nombreLimpio);
        }

        Estacion nueva = new Estacion(codigoLimpio, nombreLimpio, zonaLimpia, x, y, activa);
        porCodigo.poner(codigoLimpio, nueva);
        porNombreNormalizado.poner(nombreNormalizado, nueva);
    }

    public Estacion buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return porCodigo.obtener(codigo.trim());
    }

    public Estacion buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        return porNombreNormalizado.obtener(nombre.trim().toLowerCase());
    }

    public int getTotalEstaciones() {
        return porCodigo.tamano();
    }
}
