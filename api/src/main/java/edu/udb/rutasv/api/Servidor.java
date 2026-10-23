package edu.udb.rutasv.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import edu.udb.rutasv.app.configuracion.Aplicacion;
import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.excepciones.SesionInvalidaException;
import edu.udb.rutasv.auth.excepciones.UsuarioExistenteException;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.excepciones.ColeccionVaciaException;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.Pasajero;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.HttpStatus;
import io.javalin.json.JavalinJackson;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Capa HTTP delgada para el frontend React. No tiene logica de negocio: traduce cada pedido a una
 * llamada de {@link SistemaTransporte} y cada excepcion del core a un codigo HTTP.
 * Contrato completo en {@code docs/api.md}.
 */
public final class Servidor {

    private static final Logger LOG = Logger.getLogger(Servidor.class.getName());

    private Servidor() {
    }

    /** Arranca en {@code RUTASV_PORT} (7070) con los datos de {@code RUTASV_DATA_DIR} (./data). */
    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        Path datos = Path.of(env.getOrDefault("RUTASV_DATA_DIR", "data"));
        int puerto = Integer.parseInt(env.getOrDefault("RUTASV_PORT", "7070"));
        crear(Aplicacion.crear(datos)).start(puerto);
        LOG.info("RutaSV API en http://localhost:" + puerto + "/api (datos: " + datos.toAbsolutePath() + ")");
    }

    static ObjectMapper json() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    /** Arma las rutas sobre el sistema dado, sin arrancar el servidor. */
    public static Javalin crear(SistemaTransporte sistema) {
        Sesiones sesiones = new Sesiones();
        Javalin app = Javalin.create(cfg -> {
            cfg.showJavalinBanner = false;
            cfg.jsonMapper(new JavalinJackson(json(), false));
        });
        errores(app);

        // ---- Autenticacion ----
        app.post("/api/auth/registro", ctx -> {
            Dto.Credenciales c = ctx.bodyAsClass(Dto.Credenciales.class);
            Sesion s = sistema.registrar(c.usuario(), c.contrasena());
            sesiones.guardar(s);
            ctx.status(HttpStatus.CREATED).json(Dto.SesionDto.de(s));
        });
        app.post("/api/auth/login", ctx -> {
            Dto.Credenciales c = ctx.bodyAsClass(Dto.Credenciales.class);
            Sesion s = sistema.iniciarSesion(c.usuario(), c.contrasena());
            sesiones.guardar(s);
            ctx.json(Dto.SesionDto.de(s));
        });
        app.post("/api/auth/logout", ctx -> {
            sesiones.cerrar(ctx);
            ctx.status(HttpStatus.NO_CONTENT);
        });
        app.get("/api/auth/yo", ctx -> ctx.json(Dto.SesionDto.de(sesiones.de(ctx))));

        // ---- Busqueda ----
        app.get("/api/estaciones/sugerencias", ctx -> ctx.json(Dto.lista(
                sistema.sugerirEstaciones(sesiones.de(ctx), ctx.queryParamAsClass("prefijo", String.class)
                        .getOrDefault("")))));
        app.get("/api/busquedas/recientes", ctx -> ctx.json(Dto.lista(sistema.busquedasRecientes(sesiones.de(ctx)))));

        // ---- Ruta ----
        app.get("/api/rutas", ctx -> {
            Sesion s = sesiones.de(ctx);
            var ruta = sistema.calcularRuta(s, ctx.queryParam("origen"), ctx.queryParam("destino"));
            if (ruta.isEmpty()) {
                ctx.status(HttpStatus.NOT_FOUND).json(new Dto.Error("No existe ruta entre esas estaciones", false));
                return;
            }
            ctx.json(Dto.Ruta.de(ruta.get(), sistema.verRed(s)));
        });

        // ---- Red vial ----
        app.get("/api/red", ctx -> ctx.json(Dto.Red.de(sistema.verRed(sesiones.de(ctx)))));
        app.post("/api/red/tramos", ctx -> {
            Dto.NuevoTramo t = ctx.bodyAsClass(Dto.NuevoTramo.class);
            sistema.agregarTramo(sesiones.de(ctx), t.origen(), t.destino(), t.minutos(), t.km());
            ctx.status(HttpStatus.CREATED);
        });

        // ---- Historial ----
        app.get("/api/historial", ctx -> ctx.json(Dto.lista(sistema.miHistorial(sesiones.de(ctx)))));
        app.get("/api/estadisticas", ctx -> ctx.json(sistema.estadisticas(sesiones.de(ctx))));

        // ---- Paradas ----
        app.get("/api/estaciones", ctx -> ctx.json(Dto.lista(sistema.verRed(sesiones.de(ctx)).estaciones())));
        app.get("/api/estaciones/buscar", ctx -> {
            var e = sistema.buscarEstacion(sesiones.de(ctx), ctx.queryParam("texto"));
            if (e.isEmpty()) {
                ctx.status(HttpStatus.NOT_FOUND).json(new Dto.Error("No existe esa estacion", false));
                return;
            }
            ctx.json(e.get());
        });
        app.post("/api/estaciones", ctx -> {
            sistema.crearEstacion(sesiones.de(ctx), ctx.bodyAsClass(Estacion.class));
            ctx.status(HttpStatus.CREATED);
        });
        app.put("/api/estaciones/{codigo}", ctx -> {
            Estacion e = ctx.bodyAsClass(Estacion.class);
            sistema.editarEstacion(sesiones.de(ctx), new Estacion(ctx.pathParam("codigo"), e.nombre(), e.zona(),
                    e.x(), e.y(), e.activa()));
            ctx.status(HttpStatus.NO_CONTENT);
        });
        app.patch("/api/estaciones/{codigo}/estado", ctx -> {
            Dto.CambioEstado c = ctx.bodyAsClass(Dto.CambioEstado.class);
            sistema.activarDesactivarEstacion(sesiones.de(ctx), ctx.pathParam("codigo"), c.activa());
            ctx.status(HttpStatus.NO_CONTENT);
        });

        // ---- Terminal ----
        app.post("/api/terminal/unidades", ctx -> {
            sistema.registrarUnidad(sesiones.de(ctx), ctx.bodyAsClass(UnidadTransporte.class));
            ctx.status(HttpStatus.CREATED);
        });
        app.get("/api/terminal/orden", ctx -> ctx.json(Dto.lista(sistema.verOrdenDespacho(sesiones.de(ctx)))));
        app.post("/api/terminal/despachar", ctx -> ctx.json(sistema.despacharSiguiente(sesiones.de(ctx))));
        app.post("/api/terminal/filas/{tipo}/pasajeros", ctx -> {
            sistema.encolarPasajero(sesiones.de(ctx), tipo(ctx), ctx.bodyAsClass(Pasajero.class));
            ctx.status(HttpStatus.CREATED);
        });
        app.post("/api/terminal/filas/{tipo}/atender",
                ctx -> ctx.json(sistema.atenderPasajero(sesiones.de(ctx), tipo(ctx))));

        return app;
    }

    private static TipoRuta tipo(Context ctx) {
        try {
            return TipoRuta.valueOf(ctx.pathParam("tipo").toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ValidacionException("Tipo de ruta invalido: usa NORMAL o EXPRES");
        }
    }

    /** Cada excepcion del core se traduce a un codigo HTTP con {@code {"error": "..."}}. */
    private static void errores(Javalin app) {
        app.exception(ValidacionException.class, (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, e, false));
        app.exception(IllegalArgumentException.class, (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, e, false));
        app.exception(io.javalin.http.BadRequestResponse.class,
                (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, e, false));
        app.exception(io.javalin.validation.ValidationException.class,
                (e, ctx) -> error(ctx, HttpStatus.BAD_REQUEST, e, false));
        app.exception(CredencialesInvalidasException.class, (e, ctx) -> error(ctx, HttpStatus.UNAUTHORIZED, e, false));
        app.exception(SesionInvalidaException.class, (e, ctx) -> error(ctx, HttpStatus.UNAUTHORIZED, e, false));
        app.exception(AccesoDenegadoException.class, (e, ctx) -> error(ctx, HttpStatus.FORBIDDEN, e, false));
        app.exception(UsuarioExistenteException.class, (e, ctx) -> error(ctx, HttpStatus.CONFLICT, e, false));
        app.exception(ColeccionVaciaException.class, (e, ctx) -> error(ctx, HttpStatus.CONFLICT, e, false));
        app.exception(NoImplementadoException.class, (e, ctx) -> error(ctx, HttpStatus.NOT_IMPLEMENTED, e, true));
        app.exception(Exception.class, (e, ctx) -> {
            LOG.severe("Error no controlado en " + ctx.path() + ": " + e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(new Dto.Error("Error interno del servidor", false));
        });
    }

    private static void error(Context ctx, HttpStatus estado, Exception e, boolean pendiente) {
        String mensaje = e.getMessage() == null ? "Pedido invalido" : e.getMessage();
        ctx.status(estado).json(new Dto.Error(mensaje, pendiente));
    }
}
