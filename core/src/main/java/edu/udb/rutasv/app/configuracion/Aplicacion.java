package edu.udb.rutasv.app.configuracion;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
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
import edu.udb.rutasv.nucleo.estructuras.IndiceNombres;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.paradas.servicios.CargadorCSV;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;


/** Raiz de composicion: arma las piezas y devuelve el sistema. */
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
        Mapa<String, Usuario> usuarios = new MapaTemporal<>();
        for (Usuario u : repo.cargar()) {
            usuarios.poner(u.nombre(), u);
        }
        new SeedAdmin(usuarios, repo, hasher, entorno).ejecutar();
        AuthService auth = new AuthServiceImpl(usuarios, repo, hasher);
        BusquedaService busqueda = new BusquedaService(cargarIndice(dirDatos), new MapaTemporal<>());
        return new SistemaTransporteImpl(auth, new Autorizador(auth), busqueda);
    }

    /** Indice de nombres con las estaciones activas de {@code estaciones.csv}; los errores de carga se avisan en el log. */
    private static IndiceNombres cargarIndice(Path dirDatos) {
        IndiceNombres indice = new ArbolAvl();
        String ruta = dirDatos.resolve("estaciones.csv").toString();
        CargadorCSV.ResultadoCargaEstaciones carga = new CargadorCSV().cargarEstacionesDesdeCSV(ruta);
        for (String error : carga.errores) {
            LOG.warning("estaciones.csv: " + error);
        }
        for (Estacion e : carga.estacionesValidas) {
            if (e.activa()) {
                indice.insertar(e.nombre(), e);
            }
        }
        return indice;
    }
}
