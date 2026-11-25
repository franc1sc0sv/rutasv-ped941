package edu.udb.rutasv.paradas.servicios;

import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.texto.Normalizador;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Directorio de estaciones (rol Paradas): alta, edición, activar/desactivar y búsqueda directa por
 * código o por nombre (RN-1). Código y nombre son únicos.
 *
 * <p>Usa dos tablas hash para que toda búsqueda sea O(1) promedio: una por código y otra por nombre
 * normalizado (sin mayúsculas ni tildes, con el mismo {@link Normalizador} que el árbol AVL del
 * autocompletado). Así "Soyapangó" y "Soyapango" cuentan como el mismo nombre en los dos lados.
 */
public class DirectorioEstaciones {

    /** Código: letras, dígitos, guion o guion bajo; se guarda en mayúsculas. */
    private static final Pattern CODIGO = Pattern.compile("[A-Z0-9_-]{1,12}");
    /** Caracteres que romperían los archivos de datos (separador {@code ;}, {@code >} y saltos de línea). */
    private static final Pattern PROHIBIDOS = Pattern.compile("[;>\\r\\n\\t]");
    private static final int MAX_NOMBRE = 60;
    /** Las coordenadas son de un plano local en km; el límite evita valores absurdos en el mapa. */
    public static final double MAX_COORDENADA = 10_000;

    private final Mapa<String, Estacion> porCodigo;
    private final Mapa<String, Estacion> porNombreNormalizado;

    public DirectorioEstaciones() {
        this(new TablaHash<>(), new TablaHash<>());
    }

    public DirectorioEstaciones(Mapa<String, Estacion> porCodigo, Mapa<String, Estacion> porNombreNormalizado) {
        this.porCodigo = porCodigo;
        this.porNombreNormalizado = porNombreNormalizado;
    }

    public void darDeAlta(String codigo, String nombre, String zona, double x, double y) {
        darDeAlta(codigo, nombre, zona, x, y, true);
    }

    /** Da de alta una estación. O(1) promedio. Devuelve la estación ya limpia (código en mayúsculas). */
    public Estacion darDeAlta(String codigo, String nombre, String zona, double x, double y, boolean activa) {
        Estacion nueva = validada(codigo, nombre, zona, x, y, activa);
        if (porCodigo.contiene(nueva.codigo())) {
            throw new ValidacionException("Ya existe una estación con el código " + nueva.codigo());
        }
        Estacion mismoNombre = porNombreNormalizado.obtener(Normalizador.normalizar(nueva.nombre()));
        if (mismoNombre != null) {
            throw nombreRepetido(nueva.nombre(), mismoNombre);
        }
        guardar(nueva);
        return nueva;
    }

    /**
     * Reemplaza nombre, zona, coordenadas y estado de una estación existente (mismo código).
     * El nombre nuevo no puede ser el de otra estación. O(1) promedio.
     *
     * @return la estación como estaba antes del cambio, para que la fachada actualice los otros índices
     */
    public Estacion editar(Estacion cambios) {
        if (cambios == null) {
            throw new ValidacionException("La estación es obligatoria");
        }
        Estacion nueva = validada(cambios.codigo(), cambios.nombre(), cambios.zona(),
                cambios.x(), cambios.y(), cambios.activa());
        Estacion anterior = exigir(nueva.codigo());
        String claveNueva = Normalizador.normalizar(nueva.nombre());
        Estacion conEseNombre = porNombreNormalizado.obtener(claveNueva);
        if (conEseNombre != null && !conEseNombre.codigo().equals(nueva.codigo())) {
            throw nombreRepetido(nueva.nombre(), conEseNombre);
        }
        porNombreNormalizado.eliminar(Normalizador.normalizar(anterior.nombre()));
        guardar(nueva);
        return anterior;
    }

    /** Activa o desactiva una estación (RN-1). O(1) promedio. Devuelve la estación como estaba antes. */
    public Estacion cambiarEstado(String codigo, boolean activa) {
        Estacion anterior = exigir(limpiarCodigo(codigo));
        guardar(new Estacion(anterior.codigo(), anterior.nombre(), anterior.zona(), anterior.x(), anterior.y(), activa));
        return anterior;
    }

    /** Estación por código (sin importar mayúsculas ni espacios), o {@code null}. O(1) promedio. */
    public Estacion buscarPorCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return porCodigo.obtener(limpiarCodigo(codigo));
    }

    /** Estación por nombre (sin importar mayúsculas ni tildes), o {@code null}. O(1) promedio. */
    public Estacion buscarPorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return null;
        }
        return porNombreNormalizado.obtener(Normalizador.normalizar(nombre.strip()));
    }

    public int getTotalEstaciones() {
        return porCodigo.tamano();
    }

    /** Código sin espacios y en mayúsculas, el formato en que se guarda. */
    public static String limpiarCodigo(String codigo) {
        return codigo == null ? "" : codigo.strip().toUpperCase(Locale.ROOT);
    }

    /** El mensaje nombra la estacion que ya existe: "Soyapangó" choca con "Soyapango (E10)". */
    private static ValidacionException nombreRepetido(String pedido, Estacion existente) {
        String detalle = existente.nombre().equals(pedido) ? "" : " (sin contar mayúsculas ni tildes)";
        return new ValidacionException("El nombre " + pedido + " ya lo usa la estación " + existente.nombre()
                + " (" + existente.codigo() + ")" + detalle);
    }

    private Estacion exigir(String codigo) {
        Estacion e = porCodigo.obtener(codigo);
        if (e == null) {
            throw new ValidacionException("No existe la estación " + codigo);
        }
        return e;
    }

    private void guardar(Estacion e) {
        porCodigo.poner(e.codigo(), e);
        porNombreNormalizado.poner(Normalizador.normalizar(e.nombre()), e);
    }

    private static Estacion validada(String codigo, String nombre, String zona, double x, double y, boolean activa) {
        String c = limpiarCodigo(codigo);
        if (c.isEmpty()) {
            throw new ValidacionException("El código de la estación es obligatorio");
        }
        if (!CODIGO.matcher(c).matches()) {
            throw new ValidacionException(
                    "El código solo admite letras, números, guion y guion bajo (máximo 12): " + codigo);
        }
        String n = nombre == null ? "" : nombre.strip();
        if (n.isEmpty()) {
            throw new ValidacionException("El nombre de la estación es obligatorio");
        }
        if (n.length() > MAX_NOMBRE || PROHIBIDOS.matcher(n).find()) {
            throw new ValidacionException("El nombre debe tener hasta " + MAX_NOMBRE + " caracteres y sin ; ni >");
        }
        String z = zona == null ? "" : zona.strip();
        if (PROHIBIDOS.matcher(z).find()) {
            throw new ValidacionException("La zona no puede tener ; ni >");
        }
        if (!Double.isFinite(x) || !Double.isFinite(y)
                || Math.abs(x) > MAX_COORDENADA || Math.abs(y) > MAX_COORDENADA) {
            throw new ValidacionException("Las coordenadas deben estar entre -" + (int) MAX_COORDENADA
                    + " y " + (int) MAX_COORDENADA);
        }
        return new Estacion(c, n, z, x, y, activa);
    }
}
