package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import java.time.LocalTime;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Datos de demostración para la defensa (todos sintéticos). Se activan con {@code RUTASV_DEMO=1} y
 * necesitan {@code RUTASV_DEMO_PASSWORD} (la contraseña de los pasajeros de prueba: no se guarda en
 * el repositorio). Cargan lo que no vive en los CSV, para que cada estructura tenga algo que mostrar:
 *
 * <ul>
 *   <li>3 pasajeros: {@code ana_01}, {@code luis.m} y {@code sofia-r} (tabla hash de usuarios).
 *   <li>30 rutas calculadas con Dijkstra entre ellos, con conteos desiguales para que el top 5 y su
 *       desempate se vean (lista dinámica del historial y montículo acotado). Solo si el historial
 *       está vacío, para no duplicarlo en cada arranque.
 *   <li>Las 5 unidades de la Fig. 10 del documento en el montículo de despacho.
 *   <li>4 pasajeros en cada fila FIFO (colas enlazadas por tipo de ruta).
 * </ul>
 *
 * <p>Todo pasa por la fachada {@link SistemaTransporte}, con las mismas validaciones que la API.
 */
final class DatosDemo {

    static final String VAR_DEMO = "RUTASV_DEMO";
    static final String VAR_CONTRASENA = "RUTASV_DEMO_PASSWORD";

    private static final Logger LOG = Logger.getLogger(DatosDemo.class.getName());

    private static final String[] PASAJEROS = {"ana_01", "luis.m", "sofia-r"};

    /**
     * Rutas de ejemplo: {usuario, origen, destino, veces}. Los conteos por par quedan 8, 6, 5, 4, 3, 2,
     * 1 y 1 (30 en total), así que el top 5 tiene un ganador claro y un empate que se resuelve por nombre.
     */
    private static final Object[][] RUTAS = {
            {"ana_01", "E01", "E04", 5}, {"luis.m", "E01", "E04", 3},
            {"sofia-r", "E15", "E05", 4}, {"ana_01", "E15", "E05", 2},
            {"luis.m", "E12", "E09", 5},
            {"sofia-r", "E20", "E18", 4},
            {"ana_01", "E07", "E11", 3},
            {"luis.m", "E27", "E10", 2},
            {"sofia-r", "E14", "E13", 1},
            {"ana_01", "E01", "E05", 1},
    };

    private static final UnidadTransporte[] UNIDADES_FIG_10 = {
            new UnidadTransporte("U-009", TipoRuta.NORMAL, LocalTime.of(7, 5)),
            new UnidadTransporte("U-031", TipoRuta.EXPRES, LocalTime.of(7, 30)),
            new UnidadTransporte("U-005", TipoRuta.NORMAL, LocalTime.of(7, 40)),
            new UnidadTransporte("U-014", TipoRuta.EXPRES, LocalTime.of(7, 15)),
            new UnidadTransporte("U-022", TipoRuta.NORMAL, LocalTime.of(7, 20)),
    };

    private static final String[][] FILA_NORMAL = {
            {"P-101", "Ana López"}, {"P-102", "Carlos Rivas"}, {"P-103", "María Pérez"}, {"P-104", "José Martínez"}};
    private static final String[][] FILA_EXPRES = {
            {"P-201", "Lucía Gómez"}, {"P-202", "Diego Hernández"}, {"P-203", "Sofía Ramírez"}, {"P-204", "Andrés Flores"}};

    private DatosDemo() {
    }

    /** Carga los datos si {@code RUTASV_DEMO=1}. Devuelve true si cargó algo. */
    static boolean cargarSiSePide(SistemaTransporte sistema, Map<String, String> entorno) {
        if (!"1".equals(entorno.get(VAR_DEMO))) {
            return false;
        }
        String clave = entorno.get(VAR_CONTRASENA);
        String claveAdmin = entorno.get("RUTASV_ADMIN_PASSWORD");
        if (clave == null || clave.isBlank() || claveAdmin == null || claveAdmin.isBlank()) {
            LOG.warning("Datos de demo NO cargados: faltan " + VAR_CONTRASENA + " o RUTASV_ADMIN_PASSWORD.");
            return false;
        }
        String usuarioAdmin = entorno.getOrDefault("RUTASV_ADMIN_USER", "admin");
        Sesion admin = sistema.iniciarSesion(usuarioAdmin, claveAdmin);

        boolean historialVacio = sistema.estadisticas(admin).totalBusquedas() == 0;
        for (String usuario : PASAJEROS) {
            Sesion s = sesionDe(sistema, usuario, clave);
            if (historialVacio) {
                for (Object[] r : RUTAS) {
                    if (r[0].equals(usuario)) {
                        for (int i = 0; i < (int) r[3]; i++) {
                            sistema.calcularRuta(s, (String) r[1], (String) r[2]);
                        }
                    }
                }
            }
        }
        for (UnidadTransporte u : UNIDADES_FIG_10) {
            sistema.registrarUnidad(admin, u);
        }
        for (String[] p : FILA_NORMAL) {
            sistema.encolarPasajero(admin, TipoRuta.NORMAL, new Pasajero(p[0], p[1]));
        }
        for (String[] p : FILA_EXPRES) {
            sistema.encolarPasajero(admin, TipoRuta.EXPRES, new Pasajero(p[0], p[1]));
        }
        LOG.info("Datos de demo cargados: " + PASAJEROS.length + " pasajeros, "
                + (historialVacio ? "historial de 30 rutas, " : "historial ya existente, ")
                + UNIDADES_FIG_10.length + " unidades y " + (FILA_NORMAL.length + FILA_EXPRES.length)
                + " personas en fila.");
        return true;
    }

    /** Inicia sesión; si el usuario aún no existe, lo registra. */
    private static Sesion sesionDe(SistemaTransporte sistema, String usuario, String clave) {
        try {
            return sistema.iniciarSesion(usuario, clave);
        } catch (CredencialesInvalidasException e) {
            try {
                return sistema.registrar(usuario, clave);
            } catch (ValidacionException ex) {
                throw new ValidacionException("No se pudo crear el pasajero de demo " + usuario + ": " + ex.getMessage());
            }
        }
    }
}
