package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.servicios.Autorizador;
import edu.udb.rutasv.nucleo.estructuras.Cola;
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
        // Escritura: sugerir tambien guarda la busqueda reciente.
        return escribir(() -> busqueda.sugerir(sesion.usuario(), prefijo));
    }

    @Override
    public Lista<String> busquedasRecientes(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        var busqueda = conectado(m.busqueda(), "Busqueda");
        return leer(() -> busqueda.recientes(sesion.usuario()));
    }

    @Override
    public Optional<RutaCalculada> calcularRuta(Sesion sesion, String origenCodigo, String destinoCodigo) {
        autorizador.exigirSesion(sesion);
        // RN-7: cuando Historial este conectado, guardar aqui la ruta (solo si existe, RN-6).
        var ruta = conectado(m.ruta(), "Ruta");
        return leer(() -> ruta.calcular(origenCodigo, destinoCodigo, sesion.usuario()));
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
        escribir(() -> red.agregarTramo(origenCodigo, destinoCodigo, minutos, km));
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
        if (estacion == null || estacion.codigo() == null) {
            throw new ValidacionException("La estacion y su codigo son obligatorios");
        }
        escribir(() -> {
            directorio.darDeAlta(estacion.codigo(), estacion.nombre(), estacion.zona(),
                    estacion.x(), estacion.y(), estacion.activa());
            Estacion guardada = directorio.buscarPorCodigo(estacion.codigo().trim());
            if (m.red() != null) {
                m.red().agregarEstacion(guardada);
            }
            if (m.indice() != null && guardada.activa()) {
                m.indice().insertar(guardada.nombre(), guardada);
            }
        });
    }

    @Override
    public void editarEstacion(Sesion sesion, Estacion estacion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Paradas");
    }

    @Override
    public void activarDesactivarEstacion(Sesion sesion, String codigo, boolean activa) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Paradas");
    }

    @Override
    public void registrarUnidad(Sesion sesion, UnidadTransporte unidad) {
        autorizador.exigirAdmin(sesion);
        var terminal = conectado(m.terminal(), "Terminal");
        if (unidad == null || unidad.id() == null || unidad.tipoRuta() == null || unidad.horaProgramada() == null) {
            throw new ValidacionException("La unidad necesita id, tipo de ruta y hora programada");
        }
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
        Cola<Pasajero> fila = fila(tipo);
        if (pasajero == null || pasajero.id() == null || pasajero.id().isBlank()
                || pasajero.nombre() == null || pasajero.nombre().isBlank()) {
            throw new ValidacionException("El pasajero necesita id y nombre");
        }
        escribir(() -> fila.encolar(pasajero));
    }

    @Override
    public Pasajero atenderPasajero(Sesion sesion, TipoRuta tipo) {
        autorizador.exigirAdmin(sesion);
        Cola<Pasajero> fila = fila(tipo);
        return escribir(fila::desencolar);
    }

    private Cola<Pasajero> fila(TipoRuta tipo) {
        var filas = conectado(m.filas(), "Terminal");
        if (tipo == null) {
            throw new ValidacionException("El tipo de ruta es obligatorio");
        }
        return filas.obtener(tipo);
    }
}
