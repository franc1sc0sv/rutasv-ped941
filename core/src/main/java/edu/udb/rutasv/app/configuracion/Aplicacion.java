package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.app.servicios.Modulos;
import edu.udb.rutasv.app.servicios.SistemaTransporteImpl;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Usuario;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorio;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorioArchivo;
import edu.udb.rutasv.auth.servicios.AuthServiceImpl;
import edu.udb.rutasv.auth.servicios.Autorizador;
import edu.udb.rutasv.auth.servicios.PasswordHasher;
import edu.udb.rutasv.auth.servicios.SeedAdmin;
import edu.udb.rutasv.busqueda.estructuras.ArbolAvl;
import edu.udb.rutasv.busqueda.servicios.BusquedaService;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorioArchivo;
import edu.udb.rutasv.historial.servicios.EstadisticasService;
import edu.udb.rutasv.historial.servicios.HistorialService;
import edu.udb.rutasv.nucleo.estructuras.Cola;
import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.paradas.servicios.CargadorCSV;
import edu.udb.rutasv.paradas.servicios.DirectorioEstaciones;
import edu.udb.rutasv.ruta.algoritmos.Dijkstra;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;
import edu.udb.rutasv.ruta.servicios.RutaServiceImpl;
import edu.udb.rutasv.nucleo.estructuras.impl.ColaEnlazada;
import edu.udb.rutasv.nucleo.estructuras.impl.MonticuloBinario;
import edu.udb.rutasv.terminal.servicios.ReglaDespacho;
import edu.udb.rutasv.terminal.servicios.Terminal;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;


/**
 * Raiz de composicion: arma las piezas y devuelve el sistema. Es el unico lugar que conoce todos los
 * modulos y decide que implementacion concreta recibe cada interfaz de {@code nucleo}.
 */
public final class Aplicacion {

    private static final Logger LOG = Logger.getLogger(Aplicacion.class.getName());

    private Aplicacion() {
    }

    /** Crea el sistema usando las variables de entorno reales. */
    public static SistemaTransporte crear(Path dirDatos) {
        return crear(dirDatos, System.getenv());
    }

    /** Crea el sistema con un mapa de entorno dado (para pruebas). */
    public static SistemaTransporte crear(Path dirDatos, Map<String, String> entorno) {
        UsuarioRepositorio repo = new UsuarioRepositorioArchivo(dirDatos.resolve("usuarios.txt"));
        PasswordHasher hasher = new PasswordHasher();
        Mapa<String, Usuario> usuarios = new TablaHash<>();
        for (Usuario u : repo.cargar()) {
            usuarios.poner(u.nombre(), u);
        }
        new SeedAdmin(usuarios, repo, hasher, entorno).ejecutar();
        AuthService auth = new AuthServiceImpl(usuarios, repo, hasher);

        GrafoListaAdyacencia red = new GrafoListaAdyacencia();
        DirectorioEstaciones directorio = new DirectorioEstaciones();
        IndiceNombres indice = new ArbolAvl();
        cargarRed(dirDatos, red, directorio, indice);

        Dijkstra dijkstra = new Dijkstra(MonticuloBinario::new, TablaHash::new);
        Terminal terminal = new Terminal("Terminal principal");

        HistorialService historial =
                new HistorialService(new HistorialRepositorioArchivo(dirDatos.resolve("historial.txt")));

        Modulos modulos = new Modulos(
                new BusquedaService(indice, new TablaHash<>()),
                indice,
                red,
                new RutaServiceImpl(red, dijkstra),
                directorio,
                terminal,
                historial,
                new EstadisticasService(historial, TablaHash::new, MonticuloBinario::new));
        return new SistemaTransporteImpl(auth, new Autorizador(auth), modulos);
    }

    /**
     * Lee {@code estaciones.csv} y {@code tramos.csv}. Cada estacion valida entra al grafo y al
     * directorio; las activas, al indice de nombres. Los errores de carga se avisan en el log.
     */
    private static void cargarRed(Path dirDatos, GrafoListaAdyacencia red, DirectorioEstaciones directorio,
                                  IndiceNombres indice) {
        CargadorCSV cargador = new CargadorCSV();
        CargadorCSV.ResultadoCargaEstaciones estaciones =
                cargador.cargarEstacionesDesdeCSV(dirDatos.resolve("estaciones.csv").toString());
        for (String error : estaciones.errores) {
            LOG.warning("estaciones.csv: " + error);
        }
        for (Estacion e : estaciones.estacionesValidas) {
            Estacion limpia;
            try {
                limpia = directorio.darDeAlta(e.codigo(), e.nombre(), e.zona(), e.x(), e.y(), e.activa());
            } catch (ValidacionException ex) {
                LOG.warning("estaciones.csv: " + ex.getMessage());
                continue;
            }
            red.agregarEstacion(limpia);
            if (limpia.activa()) {
                indice.insertar(limpia.nombre(), limpia);
            }
        }

        CargadorCSV.ResultadoCargaTramos tramos =
                cargador.cargarTramosDesdeCSV(dirDatos.resolve("tramos.csv").toString());
        for (String error : tramos.errores) {
            LOG.warning("tramos.csv: " + error);
        }
        for (CargadorCSV.TramoCSV t : tramos.tramosValidos) {
            try {
                red.agregarTramo(DirectorioEstaciones.limpiarCodigo(t.origen),
                        DirectorioEstaciones.limpiarCodigo(t.destino), t.minutos, t.km);
            } catch (ValidacionException ex) {
                LOG.warning("tramos.csv " + t.origen + "->" + t.destino + ": " + ex.getMessage());
            }
        }
    }
}
