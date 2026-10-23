package edu.udb.rutasv.api;

import edu.udb.rutasv.auth.excepciones.SesionInvalidaException;
import edu.udb.rutasv.auth.modelo.Sesion;
import io.javalin.http.Context;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Sesiones abiertas por token. El navegador manda {@code Authorization: Bearer <token>} y aqui se
 * recupera la {@link Sesion} que emitio el core; el core la vuelve a validar en cada llamada.
 */
final class Sesiones {

    private final Map<String, Sesion> porToken = new ConcurrentHashMap<>();

    void guardar(Sesion sesion) {
        porToken.put(sesion.token(), sesion);
    }

    /** Olvida el token del pedido y devuelve su sesion, o {@code null} si no habia. */
    Sesion cerrar(Context ctx) {
        String token = token(ctx);
        return token == null ? null : porToken.remove(token);
    }

    /** Sesion del pedido. Lanza {@link SesionInvalidaException} (401) si no hay token valido. */
    Sesion de(Context ctx) {
        String token = token(ctx);
        Sesion sesion = token == null ? null : porToken.get(token);
        if (sesion == null) {
            throw new SesionInvalidaException("Sesion invalida: inicie sesion");
        }
        return sesion;
    }

    private static String token(Context ctx) {
        String encabezado = ctx.header("Authorization");
        if (encabezado == null || !encabezado.startsWith("Bearer ")) {
            return null;
        }
        return encabezado.substring("Bearer ".length()).trim();
    }
}
