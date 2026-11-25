package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.servicios.Autorizador;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.paradas.servicios.DirectorioEstaciones;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;


/**
 * Fachada. Cada metodo valida la autorizacion (RN-1) y delega en el modulo de su rol. Los modulos
 * que aun no estan conectados (ver {@link Modulos}) lanzan {@link NoImplementadoException}.
 *
 * <p>Las estructuras propias no son seguras entre hilos y la API atiende pedidos en paralelo, asi que
 * la fachada las protege con un solo candado: lectura para consultar, escritura para cambiar. El
 * login no lo usa: {@code AuthServiceImpl} tiene el suyo.
 */
public class SistemaTransporteImpl implements SistemaTransporte {

    private final AuthService auth;
    private final Autorizador autorizador;
    private final Modulos m;
    private final ReentrantReadWriteLock candado = new ReentrantReadWriteLock();

    public SistemaTransporteImpl(AuthService auth, Autorizador autorizador, Modulos modulos) {
        this.auth = auth;
        this.autorizador = autorizador;
        this.m = modulos;
    }

    private static NoImplementadoException pendiente(String rol) {
        return new NoImplementadoException("Pendiente: modulo " + rol);
    }

    private static <T> T conectado(T modulo, String rol) {
        if (modulo == null) {
            throw pendiente(rol);
        }
        return modulo;
    }

    private <T> T leer(Supplier<T> consulta) {
        candado.readLock().lock();
        try {
            return consulta.get();
        } finally {
            candado.readLock().unlock();
        }
    }

    private <T> T escribir(Supplier<T> cambio) {
        candado.writeLock().lock();
        try {
            return cambio.get();
        } finally {
            candado.writeLock().unlock();
        }
    }

    private void escribir(Runnable cambio) {
        escribir(() -> {
            cambio.run();
            return null;
        });
    }

    @Override
    public Sesion registrar(String usuario, String contrasena) {
        return auth.registrar(usuario, contrasena);
    }

    @Override
    public Sesion iniciarSesion(String usuario, String contrasena) {
        return auth.iniciarSesion(usuario, contrasena);
    }

    @Override
    public void cerrarSesion(Sesion sesion) {
        auth.cerrarSesion(sesion);
    }

    @Override
    public Lista<String> sugerirEstaciones(Sesion sesion, String prefijo) {
        autorizador.exigirSesion(sesion);
        var busqueda = conectado(m.busqueda(), "Busqueda");
        return leer(() -> busqueda.sugerir(prefijo));
    }

    @Override
    public Lista<String> busquedasRecientes(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        var busqueda = conectado(m.busqueda(), "Busqueda");
        return leer(() -> busqueda.recientes(sesion.usuario()));
    }

    /**
     * Si hay ruta, la guarda en el historial del usuario (RN-7) y en sus busquedas recientes; si no
     * hay, no guarda nada (RN-6).
     */
    @Override
    public Optional<RutaCalculada> calcularRuta(Sesion sesion, String origenCodigo, String destinoCodigo) {
        autorizador.exigirSesion(sesion);
        var ruta = conectado(m.ruta(), "Ruta");
        var historial = m.historial();
        return escribir(() -> {
            Optional<RutaCalculada> calculada = ruta.calcular(origenCodigo, destinoCodigo, sesion.usuario());
            calculada.ifPresent(r -> {
                if (historial != null) {
                    historial.registrar(r);
                }
                if (m.busqueda() != null) {
                    m.busqueda().registrarBusqueda(sesion.usuario(), nombre(r.origen()) + " → " + nombre(r.destino()));
                }
            });
            return calculada;
        });
    }

    /** Nombre de la estacion para mostrar; si no esta en la red, el codigo. */
    private String nombre(String codigo) {
        Estacion e = m.red() == null ? null : m.red().estacion(codigo);
        return e == null ? codigo : e.nombre();
    }

    @Override
    public Lista<RutaCalculada> miHistorial(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        var historial = conectado(m.historial(), "Historial");
        return leer(() -> historial.deUsuario(sesion.usuario()));
    }

    @Override
    public RedVial verRed(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        var red = conectado(m.red(), "Ruta");
        return leer(() -> RedSoloLectura.copiaDe(red));
    }

    @Override
    public void agregarTramo(Sesion sesion, String origenCodigo, String destinoCodigo, int minutos, double km) {
        autorizador.exigirAdmin(sesion);
        var red = conectado(m.red(), "Ruta");
        escribir(() -> red.agregarTramo(DirectorioEstaciones.limpiarCodigo(origenCodigo),
                DirectorioEstaciones.limpiarCodigo(destinoCodigo), minutos, km));
    }

    @Override
    public EstadisticasGlobales estadisticas(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        var estadisticas = conectado(m.estadisticas(), "Historial");
        return leer(estadisticas::calcular);
    }

    @Override
    public Optional<Estacion> buscarEstacion(Sesion sesion, String texto) {
        autorizador.exigirSesion(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        return leer(() -> {
            Estacion e = directorio.buscarPorNombre(texto);
            return Optional.ofNullable(e != null ? e : directorio.buscarPorCodigo(texto));
        });
    }

    /** Da de alta la estacion en el directorio, en la red vial y, si esta activa, en el autocompletado. */
    @Override
    public void crearEstacion(Sesion sesion, Estacion estacion) {
        autorizador.exigirAdmin(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        if (estacion == null) {
            throw new ValidacionException("La estacion es obligatoria");
        }
        escribir(() -> {
            Estacion guardada = directorio.darDeAlta(estacion.codigo(), estacion.nombre(), estacion.zona(),
                    estacion.x(), estacion.y(), estacion.activa());
            sincronizar(null, guardada);
        });
    }

    /** Cambia nombre, zona, coordenadas o estado; el directorio, la red y el autocompletado quedan iguales. */
    @Override
    public void editarEstacion(Sesion sesion, Estacion estacion) {
        autorizador.exigirAdmin(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        escribir(() -> {
            Estacion anterior = directorio.editar(estacion);
            sincronizar(anterior, directorio.buscarPorCodigo(anterior.codigo()));
        });
    }

    /** Una estacion inactiva sigue en la red, pero Dijkstra la salta y no sale en el autocompletado. */
    @Override
    public void activarDesactivarEstacion(Sesion sesion, String codigo, boolean activa) {
        autorizador.exigirAdmin(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        escribir(() -> {
            Estacion anterior = directorio.cambiarEstado(codigo, activa);
            sincronizar(anterior, directorio.buscarPorCodigo(anterior.codigo()));
        });
    }

    /**
     * Lleva un cambio del directorio a la red vial (grafo) y al arbol AVL del autocompletado. En el
     * arbol solo viven las estaciones activas, guardadas por nombre. O(log n) por el AVL.
     */
    private void sincronizar(Estacion anterior, Estacion nueva) {
        if (m.red() != null) {
            m.red().agregarEstacion(nueva);
        }
        if (m.indice() != null) {
            if (anterior != null && anterior.activa()) {
                m.indice().eliminar(anterior.nombre());
            }
            if (nueva.activa()) {
                m.indice().insertar(nueva.nombre(), nueva);
            }
        }
    }

    @Override
    public void registrarUnidad(Sesion sesion, UnidadTransporte unidad) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        escribir(() -> terminal.registrarUnidad(unidad));
    }

    @Override
    public UnidadTransporte despacharSiguiente(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        return escribir(terminal::despacharUnidad);
    }

    @Override
    public Lista<UnidadTransporte> verOrdenDespacho(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        return leer(terminal::verOrdenDespacho);
    }

    @Override
    public void encolarPasajero(Sesion sesion, TipoRuta tipo, Pasajero pasajero) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        escribir(() -> terminal.encolarPasajero(tipo, pasajero));
    }

    @Override
    public Pasajero atenderPasajero(Sesion sesion, TipoRuta tipo) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        return escribir(() -> terminal.atenderPasajero(tipo));
    }

    @Override
    public Lista<Pasajero> verFila(Sesion sesion, TipoRuta tipo) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        // Escritura: verFila da una vuelta a la cola (desencola y vuelve a encolar).
        return escribir(() -> terminal.verFila(tipo));
    }
}
