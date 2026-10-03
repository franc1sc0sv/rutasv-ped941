package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.servicios.Autorizador;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import java.util.Optional;


/**
 * Fachada. Solo el login funciona; el resto valida la autorizacion y luego
 * lanza {@link NoImplementadoException} hasta que cada responsable lo implemente.
 */
public class SistemaTransporteImpl implements SistemaTransporte {

    private final AuthService auth;
    private final Autorizador autorizador;

    public SistemaTransporteImpl(AuthService auth, Autorizador autorizador) {
        this.auth = auth;
        this.autorizador = autorizador;
    }

    private static NoImplementadoException pendiente(String rol) {
        return new NoImplementadoException("Pendiente: modulo " + rol);
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
        throw pendiente("Busqueda");
    }

    @Override
    public Lista<String> busquedasRecientes(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        throw pendiente("Busqueda");
    }

    @Override
    public Optional<RutaCalculada> calcularRuta(Sesion sesion, String origenCodigo, String destinoCodigo) {
        autorizador.exigirSesion(sesion);
        throw pendiente("Ruta");
    }

    @Override
    public Lista<RutaCalculada> miHistorial(Sesion sesion) {
        autorizador.exigirSesion(sesion);
        throw pendiente("Historial");
    }

    @Override
    public RedVial verRed(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Historial");
    }

    @Override
    public void agregarTramo(Sesion sesion, String origenCodigo, String destinoCodigo, int minutos, double km) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Historial");
    }

    @Override
    public EstadisticasGlobales estadisticas(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Historial");
    }

    @Override
    public Optional<Estacion> buscarEstacion(Sesion sesion, String texto) {
        autorizador.exigirSesion(sesion);
        throw pendiente("Paradas");
    }

    @Override
    public void crearEstacion(Sesion sesion, Estacion estacion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Paradas");
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
        throw pendiente("Terminal");
    }

    @Override
    public UnidadTransporte despacharSiguiente(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Terminal");
    }

    @Override
    public Lista<UnidadTransporte> verOrdenDespacho(Sesion sesion) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Terminal");
    }

    @Override
    public void encolarPasajero(Sesion sesion, TipoRuta tipo, Pasajero pasajero) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Terminal");
    }

    @Override
    public Pasajero atenderPasajero(Sesion sesion, TipoRuta tipo) {
        autorizador.exigirAdmin(sesion);
        throw pendiente("Terminal");
    }
}
