package edu.udb.rutasv.auth.servicios;

import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.auth.excepciones.SesionInvalidaException;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.modelo.Usuario;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorio;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void sesionInventadaNoPasaAunqueElConstructorSeaPublico() {
        Sesion real = crearAdmin();
        // Mismo usuario y rol que una sesion real, pero con un token inventado.
        Sesion conTokenFalso = new Sesion(real.usuario(), Rol.ADMIN, "token-inventado");
        Sesion sinToken = new Sesion(real.usuario(), Rol.ADMIN, "");
        Sesion usuarioQueNoExiste = new Sesion("fantasma", Rol.ADMIN, real.token() + "x");
        for (Sesion falsa : new Sesion[] {conTokenFalso, sinToken, usuarioQueNoExiste}) {
            assertThrows(SesionInvalidaException.class, () -> a.exigirSesion(falsa));
            assertThrows(SesionInvalidaException.class, () -> a.exigirAdmin(falsa));
            assertTrue(auth.validar(falsa).isEmpty());
        }
    }

    @Test
    void pasajeroNoPuedeSubirseDeRolCambiandoElCampo() {
        Sesion pasajero = auth.registrar("ana_01", CLAVE);
        // Reusa su token legitimo pero declara rol ADMIN: el rol se vuelve a leer del servicio.
        Sesion maquillada = new Sesion(pasajero.usuario(), Rol.ADMIN, pasajero.token());
        assertDoesNotThrow(() -> a.exigirSesion(maquillada));
        assertThrows(AccesoDenegadoException.class, () -> a.exigirAdmin(maquillada));
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
