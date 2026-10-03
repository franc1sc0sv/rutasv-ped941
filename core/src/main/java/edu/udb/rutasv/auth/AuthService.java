package edu.udb.rutasv.auth;

import edu.udb.rutasv.model.ValidacionException;
import java.util.Optional;

/** Registro, inicio y cierre de sesion. Es la unica fuente de sesiones validas. */
public interface AuthService {

    /**
     * Registra un usuario nuevo con rol PASAJERO. Usuario: [A-Za-z0-9_.-]{3,32}; contrasena: minimo 8.
     * Lanza {@link ValidacionException} si no cumple y {@link UsuarioExistenteException} si ya existe.
     */
    Sesion registrar(String usuario, String contrasena);

    /** Inicia sesion. Lanza {@link CredencialesInvalidasException} (mismo mensaje) si el usuario o la contrasena fallan. */
    Sesion iniciarSesion(String usuario, String contrasena);

    /** Cierra la sesion. Despues {@link #validar} devuelve vacio. Ignora nulas o desconocidas. */
    void cerrarSesion(Sesion sesion);

    /**
     * Rol actual de la sesion, leido del registro de usuarios. Vacio si la sesion es nula,
     * no fue emitida por este servicio, fue cerrada o su usuario ya no existe.
     */
    Optional<Rol> validar(Sesion sesion);
}
