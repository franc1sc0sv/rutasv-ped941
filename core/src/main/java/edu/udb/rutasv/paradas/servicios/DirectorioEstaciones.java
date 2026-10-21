package edu.udb.rutasv.paradas;

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
            throw new IllegalArgumentException("El código de la estación no puede estar vacío");
        }
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre de la estación no puede estar vacío");
        }

        String codigoMayus = codigo.trim().toUpperCase();
        String nombreNorm = TextoUtil.normalizar(nombre);

        if (tablaPorCodigo.contiene(codigoMayus)) {
            throw new IllegalArgumentException("Ya existe una estación registrada con el código: " + codigoMayus);
        }
        if (tablaPorNombreNormalizado.contiene(nombreNorm)) {
            throw new IllegalArgumentException("Ya existe una estación registrada con el nombre: " + nombre);
        }

        Estacion estacion = new Estacion(codigoMayus, nombre.trim(), zona, activa);
        tablaPorCodigo.poner(codigoMayus, estacion);
        tablaPorNombreNormalizado.poner(nombreNorm, estacion);
        totalEstaciones++;
    }

    public Estacion buscarPorCodigo(String codigo) {
        if (codigo == null) return null;
        return tablaPorCodigo.obtener(codigo.trim().toUpperCase());
    }

    public Estacion buscarPorNombre(String nombre) {
        if (nombre == null) return null;
        String nombreNorm = TextoUtil.normalizar(nombre);
        return tablaPorNombreNormalizado.obtener(nombreNorm);
    }

    public void desactivar(String codigo) {
        Estacion estacion = buscarPorCodigo(codigo);
        if (estacion != null) {
            estacion.setActiva(false);
        } else {
            throw new IllegalArgumentException("Estación no encontrada para desactivar: " + codigo);
        }
    }

    public void activar(String codigo) {
        Estacion estacion = buscarPorCodigo(codigo);
        if (estacion != null) {
            estacion.setActiva(true);
        } else {
            throw new IllegalArgumentException("Estación no encontrada para activar: " + codigo);
        }
    }

    public int getTotalEstaciones() {
        return totalEstaciones;
    }
}
