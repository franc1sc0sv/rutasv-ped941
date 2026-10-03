package edu.udb.rutasv.auth;

import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import org.junit.jupiter.api.Test;

class AutorizadorTest {
    private static final String CLAVE = "clave-sintetica-1";
    private final PasswordHasher hasher = new PasswordHasher(1000);
    private final MapaTemporal<String, Usuario> mapa = new MapaTemporal<>();
    private final AuthService auth = new AuthServiceImpl(mapa, new UsuarioRepositorio() {
        @Override
        public Lista<Usuario> cargar() {
            return new ListaTemporal<>();
        }

        @Override
        public void guardar(Lista<Usuario> usuarios) {
        }
    }, hasher);
    private final Autorizador a = new Autorizador(auth);

    private Sesion crearAdmin() {
        String sal = hasher.nuevaSalBase64();
        mapa.poner("root", new Usuario("root", sal, hasher.hashear(CLAVE, sal), Rol.ADMIN));
        return auth.iniciarSesion("root", CLAVE);
    }

    @Test
    void sesionNulaRechazada() {
        assertThrows(SesionInvalidaException.class, () -> a.exigirSesion(null));
        assertThrows(SesionInvalidaException.class, () -> a.exigirAdmin(null));
    }

    @Test
    void pasajeroNoEsAdmin() {
        Sesion s = auth.registrar("ana_01", CLAVE);
        assertDoesNotThrow(() -> a.exigirSesion(s));
        assertThrows(AccesoDenegadoException.class, () -> a.exigirAdmin(s));
    }

    @Test
    void adminPasa() {
        Sesion s = crearAdmin();
        assertDoesNotThrow(() -> a.exigirAdmin(s));
    }

    @Test
    void sesionCerradaRechazada() {
        Sesion s = crearAdmin();
        auth.cerrarSesion(s);
        assertThrows(SesionInvalidaException.class, () -> a.exigirAdmin(s));
    }

    @Test
    void sesionNoSePuedeFalsificarDesdeFuera() {
        for (Constructor<?> c : Sesion.class.getDeclaredConstructors()) {
            assertDoesNotThrow(() -> {
                if (Modifier.isPublic(c.getModifiers())) {
                    throw new AssertionError("Sesion no debe tener constructor publico");
                }
            });
        }
    }

    @Test
    void sesionDeOtroServicioRechazada() {
        Sesion ajena = crearAdmin();
        Autorizador otro = new Autorizador(new AuthServiceImpl(mapa, new UsuarioRepositorio() {
            @Override
            public Lista<Usuario> cargar() {
                return new ListaTemporal<>();
            }

            @Override
            public void guardar(Lista<Usuario> usuarios) {
            }
        }, hasher));
        assertThrows(SesionInvalidaException.class, () -> otro.exigirAdmin(ajena));
    }
}
