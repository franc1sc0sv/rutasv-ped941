package edu.udb.rutasv.service;

import java.util.Optional;

import edu.udb.rutasv.auth.Sesion;
import edu.udb.rutasv.model.Estacion;
import edu.udb.rutasv.model.EstadisticasGlobales;
import edu.udb.rutasv.model.Pasajero;
import edu.udb.rutasv.model.RutaCalculada;
import edu.udb.rutasv.model.TipoRuta;
import edu.udb.rutasv.model.UnidadTransporte;
import edu.udb.rutasv.structures.Lista;
import edu.udb.rutasv.structures.RedVial;

/**
 * Fachada del sistema. Cada metodo (salvo registrar e iniciarSesion) valida la sesion primero:
 * {@link SesionInvalidaException} si es nula, {@link AccesoDenegadoException} si falta rol ADMIN.
 */
public interface SistemaTransporte {

    // ---- Autenticacion (login) ----

    /** Registra un pasajero y devuelve su sesion. */
    Sesion registrar(String usuario, String contrasena);

    /** Inicia sesion con usuario y contrasena. */
    Sesion iniciarSesion(String usuario, String contrasena);

    // ---- Busqueda ----

    /** Nombres de estaciones que empiezan con el prefijo, en orden alfabetico. */
    Lista<String> sugerirEstaciones(Sesion sesion, String prefijo);

    /** Ultimas busquedas del usuario, de la mas reciente a la mas antigua. */
    Lista<String> busquedasRecientes(Sesion sesion);

    // ---- Ruta ----

    /** Ruta mas corta en minutos; vacio si no hay ruta (RN-6). */
    Optional<RutaCalculada> calcularRuta(Sesion sesion, String origenCodigo, String destinoCodigo);

    // ---- Historial ----

    /** Rutas calculadas por el usuario de la sesion. */
    Lista<RutaCalculada> miHistorial(Sesion sesion);

    /** Devuelve la red completa. Solo ADMIN. */
    RedVial verRed(Sesion sesion);

    /** Agrega un tramo a la red (minutos > 0, RN-2). Solo ADMIN. */
    void agregarTramo(Sesion sesion, String origenCodigo, String destinoCodigo, int minutos, double km);

    /** Estadisticas globales de busquedas. Solo ADMIN. */
    EstadisticasGlobales estadisticas(Sesion sesion);

    // ---- Paradas ----

    /** Busca una estacion por nombre exacto; vacio si no existe. */
    Optional<Estacion> buscarEstacion(Sesion sesion, String texto);

    /** Crea una estacion. Solo ADMIN. */
    void crearEstacion(Sesion sesion, Estacion estacion);

    /** Reemplaza los datos de una estacion existente (mismo codigo). Solo ADMIN. */
    void editarEstacion(Sesion sesion, Estacion estacion);

    /** Activa o desactiva una estacion. Solo ADMIN. */
    void activarDesactivarEstacion(Sesion sesion, String codigo, boolean activa);

    // ---- Terminal ----

    /** Registra una unidad en la agenda de despacho. Solo ADMIN. */
    void registrarUnidad(Sesion sesion, UnidadTransporte unidad);

    /** Despacha y devuelve la siguiente unidad segun prioridad. Solo ADMIN. */
    UnidadTransporte despacharSiguiente(Sesion sesion);

    /** Unidades pendientes en el orden en que se despacharian. Solo ADMIN. */
    Lista<UnidadTransporte> verOrdenDespacho(Sesion sesion);

    /** Pone un pasajero en la cola de espera del tipo de ruta. Solo ADMIN. */
    void encolarPasajero(Sesion sesion, TipoRuta tipo, Pasajero pasajero);

    /** Atiende al primer pasajero de la cola del tipo de ruta. Solo ADMIN. */
    Pasajero atenderPasajero(Sesion sesion, TipoRuta tipo);
}
