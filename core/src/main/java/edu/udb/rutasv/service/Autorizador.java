package edu.udb.rutasv.service;

import edu.udb.rutasv.auth.AuthService;
import edu.udb.rutasv.auth.Rol;
import edu.udb.rutasv.auth.Sesion;
import java.util.Objects;

/** Reglas de autorizacion (RN-1). Valida cada sesion contra {@link AuthService}, no confia en la sesion. */
public class Autorizador {

    private final AuthService auth;

    public Autorizador(AuthService auth) {
        this.auth = Objects.requireNonNull(auth, "auth");
    }

    /** Exige una sesion emitida por AuthService y aun abierta. Devuelve el rol actual. */
    public Rol exigirSesion(Sesion sesion) {
        return auth.validar(sesion).orElseThrow(() -> new SesionInvalidaException("Sesion invalida: inicie sesion"));
    }

    /** Exige una sesion valida con rol ADMIN. */
    public void exigirAdmin(Sesion sesion) {
        if (exigirSesion(sesion) != Rol.ADMIN) {
            throw new AccesoDenegadoException("Se requiere rol ADMIN");
        }
    }
}
