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


/**
 * Fachada. Cada metodo valida la autorizacion (RN-1) y delega en el modulo de su rol. Los modulos
 * que aun no estan conectados (ver {@link Modulos}) lanzan {@link NoImplementadoException}.
 */
public class SistemaTransporteImpl implements SistemaTransporte {

    private final AuthService auth;
    private final Autorizador autorizador;
    private final Modulos m;

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

    @Override
    public Sesion registrar(String usuario, String contrasena) {
        return auth.registrar(usuario, contrasena);
    }

    @Override
    public Sesion iniciarSesion(String usuario, String contrasena) {
        return auth.iniciarSesion(usuario, contrasena);
    }

    @Override
    public Lista<String> sugerirEstaciones(Sesion sesion, String prefijo) {
        autorizador.exigirSesion(sesion);
        return conectado(m.busqueda(), "Busqueda").sugerir(sesion.usuario(), prefijo);
    }

    @Override
    public Lista<String> busquedasRecientes(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        return conectado(m.busqueda(), "Busqueda").recientes(sesion.usuario());
    }

    @Override
    public Optional<RutaCalculada> calcularRuta(Sesion sesion, String origenCodigo, String destinoCodigo) {
        autorizador.exigirSesion(sesion);
        // RN-7: cuando Historial este conectado, guardar aqui la ruta (solo si existe, RN-6).
        return conectado(m.ruta(), "Ruta").calcular(origenCodigo, destinoCodigo, sesion.usuario());
    }

    @Override
    public Lista<RutaCalculada> miHistorial(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        throw pendiente("Historial");
    }

    @Override
    public RedVial verRed(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        return new RedSoloLectura(conectado(m.red(), "Ruta"));
    }

    @Override
    public void agregarTramo(Sesion sesion, String origenCodigo, String destinoCodigo, int minutos, double km) {
        autorizador.exigirAdmin(sesion);
        conectado(m.red(), "Ruta").agregarTramo(origenCodigo, destinoCodigo, minutos, km);
    }

    @Override
    public EstadisticasGlobales estadisticas(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Historial");
    }

    /** Busca por nombre (sin distinguir mayusculas) y, si no aparece, por codigo. */
    @Override
    public Optional<Estacion> buscarEstacion(Sesion sesion, String texto) {
        autorizador.exigirSesion(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        Estacion e = directorio.buscarPorNombre(texto);
        return Optional.ofNullable(e != null ? e : directorio.buscarPorCodigo(texto));
    }

    /** Da de alta la estacion en el directorio y en la red vial. */
    @Override
    public void crearEstacion(Sesion sesion, Estacion estacion) {
        autorizador.exigirAdmin(sesion);
        var directorio = conectado(m.directorio(), "Paradas");
        if (estacion == null) {
            throw new ValidacionException("La estacion es obligatoria");
        }
        directorio.darDeAlta(estacion.codigo(), estacion.nombre(), estacion.zona(),
                estacion.x(), estacion.y(), estacion.activa());
        if (m.red() != null) {
            m.red().agregarEstacion(directorio.buscarPorCodigo(estacion.codigo().trim()));
        }
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
        terminal.registrarUnidad(unidad);
    }

    @Override
    public UnidadTransporte despacharSiguiente(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        return conectado(m.terminal(), "Terminal").despacharUnidad();
    }

    @Override
    public Lista<UnidadTransporte> verOrdenDespacho(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        return conectado(m.terminal(), "Terminal").verOrdenDespacho();
    }

    @Override
    public void encolarPasajero(Sesion sesion, TipoRuta tipo, Pasajero pasajero) {
        autorizador.exigirAdmin(sesion);
        Cola<Pasajero> fila = fila(tipo);
        if (pasajero == null) {
            throw new ValidacionException("El pasajero es obligatorio");
        }
        fila.encolar(pasajero);
    }

    @Override
    public Pasajero atenderPasajero(Sesion sesion, TipoRuta tipo) {
        autorizador.exigirAdmin(sesion);
        return fila(tipo).desencolar();
    }

    private Cola<Pasajero> fila(TipoRuta tipo) {
        var filas = conectado(m.filas(), "Terminal");
        if (tipo == null) {
            throw new ValidacionException("El tipo de ruta es obligatorio");
        }
        return filas.obtener(tipo);
    }
}
