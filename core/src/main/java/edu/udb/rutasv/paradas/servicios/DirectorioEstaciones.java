package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.estructuras.TablaHash;
import edu.udb.rutasv.modelo.Estacion;
import edu.udb.rutasv.util.TextoUtil;

public class DirectorioEstaciones {

    private TablaHash<String, Estacion> tablaPorCodigo;
    private TablaHash<String, Estacion> tablaPorNombreNormalizado;
    private int totalEstaciones;

    public DirectorioEstaciones() {
        this.tablaPorCodigo = new TablaHash<>();
        this.tablaPorNombreNormalizado = new TablaHash<>();
        this.totalEstaciones = 0;
    }

    public void darDeAlta(String codigo, String nombre, String zona) {
        darDeAlta(codigo, nombre, zona, true);
    }

    public void darDeAlta(String codigo, String nombre, String zona, boolean activa) {
        if (codigo == null || codigo.trim().isEmpty()) {
            throw new IllegalArgumentException("El codigo de la estacion no puede ser nulo ni vacio.");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la estacion no puede ser nulo ni vacio.");
        }

        String codigoLimpio = codigo.trim();
        String nombreLimpio = nombre.trim();
        String zonaLimpia = (zona != null) ? zona.trim() : "";

        if (tablaPorCodigo.contieneClave(codigoLimpio)) {
            throw new IllegalStateException("Ya existe una estacion registrada con el codigo: " + codigoLimpio);
        }

        Estacion nuevaEstacion = new Estacion(codigoLimpio, nombreLimpio, zonaLimpia, activa);
        tablaPorCodigo.insertar(codigoLimpio, nuevaEstacion);

        String nombreNormalizado = TextoUtil.normalizarTexto(nombreLimpio);
        if (nombreNormalizado != null && !nombreNormalizado.isEmpty()) {
            tablaPorNombreNormalizado.insertar(nombreNormalizado, nuevaEstacion);
        }

        totalEstaciones++;
    }

    public Estacion buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.trim().isEmpty()) {
            return null;
        }
        return tablaPorCodigo.obtener(codigo.trim());
    }

    public Estacion buscarPorNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            return null;
        }
        String nombreNormalizado = TextoUtil.normalizarTexto(nombre.trim());
        if (nombreNormalizado == null || nombreNormalizado.isEmpty()) {
            return null;
        }
        return tablaPorNombreNormalizado.obtener(nombreNormalizado);
    }

    public int getTotalEstaciones() {
        return totalEstaciones;
    }
}
