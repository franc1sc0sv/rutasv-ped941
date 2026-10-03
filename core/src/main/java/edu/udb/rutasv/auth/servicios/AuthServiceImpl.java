package edu.udb.rutasv.auth.servicios;

import edu.udb.rutasv.auth.excepciones.CredencialesInvalidasException;
import edu.udb.rutasv.auth.excepciones.UsuarioExistenteException;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.modelo.Usuario;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorio;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.Mapa;
import edu.udb.rutasv.nucleo.excepciones.ValidacionException;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;

/** Registro e inicio de sesion. Busca usuarios en el {@link Mapa} y guarda con el repositorio. */
public class AuthServiceImpl implements AuthService {

    /** Patron valido de nombre de usuario. */
    public static final Pattern PATRON_USUARIO = Pattern.compile("[A-Za-z0-9_.-]{3,32}");
    /** Largo minimo de contrasena. */
    public static final int MIN_CONTRASENA = 8;
    private static final String MENSAJE_CREDENCIALES = "Usuario o contrasena incorrectos";
    private static final String SAL_FALSA = "AAAAAAAAAAAAAAAAAAAAAA==";
    private static final String HASH_FALSO = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=";

    private final Mapa<String, Usuario> usuarios;
    private final UsuarioRepositorio repo;
    private final PasswordHasher hasher;
    private final Supplier<Lista<Usuario>> fabricaListas;
    /** Todo acceso al Mapa pasa por este candado: la estructura propia no es segura entre hilos. */
    private final ReentrantReadWriteLock candado = new ReentrantReadWriteLock();
    /** token -> usuario de las sesiones abiertas. */
    private final Map<String, String> sesiones = new ConcurrentHashMap<>();
    private final SecureRandom azar = new SecureRandom();

    public AuthServiceImpl(Mapa<String, Usuario> usuarios, UsuarioRepositorio repo, PasswordHasher hasher) {
        this(usuarios, repo, hasher, ListaTemporal::new);
    }

    /** Permite elegir la lista usada al guardar (por ejemplo la propia del equipo). */
    public AuthServiceImpl(Mapa<String, Usuario> usuarios, UsuarioRepositorio repo, PasswordHasher hasher,
                           Supplier<Lista<Usuario>> fabricaListas) {
        this.usuarios = usuarios;
        this.repo = repo;
        this.hasher = hasher;
        this.fabricaListas = fabricaListas;
    }

    /** Lista con todos los usuarios del Mapa mas el nuevo. El Mapa es la unica fuente de verdad. */
    static Lista<Usuario> conNuevo(Mapa<String, Usuario> usuarios, Supplier<Lista<Usuario>> fabrica, Usuario nuevo) {
        Lista<Usuario> todos = fabrica.get();
        for (String clave : usuarios.claves()) {
            todos.agregar(usuarios.obtener(clave));
        }
        todos.agregar(nuevo);
        return todos;
    }

    @Override
    public Sesion registrar(String usuario, String contrasena) {
        if (usuario == null || !PATRON_USUARIO.matcher(usuario).matches()) {
            throw new ValidacionException("Usuario invalido: 3 a 32 caracteres entre letras, numeros, _ . -");
        }
        if (contrasena == null || contrasena.length() < MIN_CONTRASENA) {
            throw new ValidacionException("La contrasena debe tener al menos " + MIN_CONTRASENA + " caracteres");
        }
        String sal = hasher.nuevaSalBase64();
        Usuario nuevo = new Usuario(usuario, sal, hasher.hashear(contrasena, sal), Rol.PASAJERO);
        candado.writeLock().lock();
        try {
            if (usuarios.contiene(usuario)) {
                throw new UsuarioExistenteException("El usuario ya existe");
            }
            repo.guardar(conNuevo(usuarios, fabricaListas, nuevo));
            usuarios.poner(usuario, nuevo);
        } finally {
            candado.writeLock().unlock();
        }
        return emitir(usuario, Rol.PASAJERO);
    }

    @Override
    public Sesion iniciarSesion(String usuario, String contrasena) {
        String pass = contrasena == null ? "" : contrasena;
        Usuario u = null;
        if (usuario != null) {
            candado.readLock().lock();
            try {
                u = usuarios.obtener(usuario);
            } finally {
                candado.readLock().unlock();
            }
        }
        if (u == null) {
            hasher.verificar(pass, SAL_FALSA, HASH_FALSO); // igualar tiempos
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
        }
        if (!hasher.verificar(pass, u.salBase64(), u.hashBase64())) {
            throw new CredencialesInvalidasException(MENSAJE_CREDENCIALES);
        }
        return emitir(u.nombre(), u.rol());
    }

    @Override
    public void cerrarSesion(Sesion sesion) {
        if (sesion != null) {
            sesiones.remove(sesion.token());
        }
    }

    @Override
    public Optional<Rol> validar(Sesion sesion) {
        if (sesion == null || sesion.token() == null) {
            return Optional.empty();
        }
        String dueno = sesiones.get(sesion.token());
        if (dueno == null || !dueno.equals(sesion.usuario())) {
            return Optional.empty();
        }
        candado.readLock().lock();
        try {
            Usuario u = usuarios.obtener(dueno);
            return u == null ? Optional.empty() : Optional.of(u.rol());
        } finally {
            candado.readLock().unlock();
        }
    }

    private Sesion emitir(String usuario, Rol rol) {
        byte[] bytes = new byte[16];
        azar.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        sesiones.put(token, usuario);
        return new Sesion(usuario, rol, token);
    }
}
