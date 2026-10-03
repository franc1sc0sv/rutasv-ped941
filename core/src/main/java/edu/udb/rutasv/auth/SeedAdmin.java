package edu.udb.rutasv.auth;

import edu.udb.rutasv.persistence.UsuarioRepositorio;
import edu.udb.rutasv.structures.Lista;
import edu.udb.rutasv.structures.Mapa;
import edu.udb.rutasv.structures.temporal.ListaTemporal;
import java.util.Map;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Crea el administrador en el primer arranque. Usuario: RUTASV_ADMIN_USER (por defecto "admin").
 * Contrasena: solo RUTASV_ADMIN_PASSWORD (minimo 8). Sin contrasena valida no se crea nada.
 */
public class SeedAdmin {

    public static final String VAR_USUARIO = "RUTASV_ADMIN_USER";
    public static final String VAR_CONTRASENA = "RUTASV_ADMIN_PASSWORD";
    private static final Logger LOG = Logger.getLogger(SeedAdmin.class.getName());

    private final Mapa<String, Usuario> usuarios;
    private final UsuarioRepositorio repo;
    private final PasswordHasher hasher;
    private final Map<String, String> entorno;
    private final Supplier<Lista<Usuario>> fabricaListas = ListaTemporal::new;

    /** El entorno se inyecta para poder probar sin leer variables reales. */
    public SeedAdmin(Mapa<String, Usuario> usuarios, UsuarioRepositorio repo, PasswordHasher hasher,
                     Map<String, String> entorno) {
        this.usuarios = usuarios;
        this.repo = repo;
        this.hasher = hasher;
        this.entorno = entorno;
    }

    /**
     * Devuelve true si creo el administrador. Se llama al arrancar, antes de publicar el
     * servicio, y toma el candado del Mapa para no cruzarse con otros hilos.
     */
    public boolean ejecutar() {
        synchronized (usuarios) {
            return sembrar();
        }
    }

    private boolean sembrar() {
        String nombre = entorno.get(VAR_USUARIO);
        if (nombre == null || nombre.isBlank()) {
            nombre = "admin";
        }
        if (!AuthServiceImpl.PATRON_USUARIO.matcher(nombre).matches()) {
            LOG.warning("Administrador NO creado: " + VAR_USUARIO + " no cumple [A-Za-z0-9_.-]{3,32}.");
            return false;
        }
        if (usuarios.contiene(nombre)) {
            return false;
        }
        String contrasena = entorno.get(VAR_CONTRASENA);
        if (contrasena == null || contrasena.length() < AuthServiceImpl.MIN_CONTRASENA) {
            LOG.warning("Administrador NO creado: define " + VAR_CONTRASENA + " con al menos "
                    + AuthServiceImpl.MIN_CONTRASENA + " caracteres.");
            return false;
        }
        String sal = hasher.nuevaSalBase64();
        Usuario admin = new Usuario(nombre, sal, hasher.hashear(contrasena, sal), Rol.ADMIN);
        repo.guardar(AuthServiceImpl.conNuevo(usuarios, fabricaListas, admin));
        usuarios.poner(nombre, admin);
        LOG.info("Administrador '" + nombre + "' creado.");
        return true;
    }
}
