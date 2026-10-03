package edu.udb.rutasv.app;

import edu.udb.rutasv.auth.AuthService;
import edu.udb.rutasv.auth.AuthServiceImpl;
import edu.udb.rutasv.auth.Autorizador;
import edu.udb.rutasv.auth.PasswordHasher;
import edu.udb.rutasv.auth.SeedAdmin;
import edu.udb.rutasv.auth.Usuario;
import edu.udb.rutasv.auth.UsuarioRepositorio;
import edu.udb.rutasv.auth.UsuarioRepositorioArchivo;
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
