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
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import java.nio.file.Path;
import java.util.Map;


/** Raiz de composicion: arma las piezas y devuelve el sistema. */
public final class Aplicacion {

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
        return new SistemaTransporteImpl(auth, new Autorizador(auth));
    }
}
