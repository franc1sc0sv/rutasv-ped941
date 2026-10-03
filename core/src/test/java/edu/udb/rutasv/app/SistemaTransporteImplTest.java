package edu.udb.rutasv.app;

import edu.udb.rutasv.auth.AccesoDenegadoException;
import edu.udb.rutasv.auth.AuthService;
import edu.udb.rutasv.auth.AuthServiceImpl;
import edu.udb.rutasv.auth.Autorizador;
import edu.udb.rutasv.auth.PasswordHasher;
import edu.udb.rutasv.auth.Rol;
import edu.udb.rutasv.auth.Sesion;
import edu.udb.rutasv.auth.SesionInvalidaException;
import edu.udb.rutasv.auth.Usuario;
import edu.udb.rutasv.auth.UsuarioRepositorio;
import edu.udb.rutasv.nucleo.error.NoImplementadoException;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;


class SistemaTransporteImplTest {

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
    private final SistemaTransporte sistema = new SistemaTransporteImpl(auth, new Autorizador(auth));
    private final Sesion pasajero;
    private final Sesion admin;

    SistemaTransporteImplTest() {
        String sal = hasher.nuevaSalBase64();
        mapa.poner("root", new Usuario("root", sal, hasher.hashear(CLAVE, sal), Rol.ADMIN));
        admin = auth.iniciarSesion("root", CLAVE);
        pasajero = auth.registrar("ana_01", CLAVE);
    }

    private Executable[] soloAdmin(Sesion s) {
        Estacion e = new Estacion("E1", "Centro", "Z1", 0, 0, true);
        return new Executable[] {
            () -> sistema.verRed(s),
            () -> sistema.agregarTramo(s, "A", "B", 5, 1.0),
            () -> sistema.estadisticas(s),
            () -> sistema.crearEstacion(s, e),
            () -> sistema.editarEstacion(s, e),
            () -> sistema.activarDesactivarEstacion(s, "E1", false),
            () -> sistema.registrarUnidad(s, null),
            () -> sistema.despacharSiguiente(s),
            () -> sistema.verOrdenDespacho(s),
            () -> sistema.encolarPasajero(s, TipoRuta.NORMAL, null),
            () -> sistema.atenderPasajero(s, TipoRuta.NORMAL)
        };
    }

    private Executable[] paraCualquierSesion(Sesion s) {
        return new Executable[] {
            () -> sistema.sugerirEstaciones(s, "ce"),
            () -> sistema.busquedasRecientes(s),
            () -> sistema.calcularRuta(s, "A", "B"),
            () -> sistema.miHistorial(s),
            () -> sistema.buscarEstacion(s, "Centro")
        };
    }

    @Test
    void sesionNulaRechazadaEnTodo() {
        for (Executable x : soloAdmin(null)) {
            assertThrows(SesionInvalidaException.class, x);
        }
        for (Executable x : paraCualquierSesion(null)) {
            assertThrows(SesionInvalidaException.class, x);
        }
    }

    @Test
    void pasajeroDenegadoEnOperacionesAdmin() {
        for (Executable x : soloAdmin(pasajero)) {
            assertThrows(AccesoDenegadoException.class, x);
        }
    }

    @Test
    void adminPasaYRecibeNoImplementado() {
        for (Executable x : soloAdmin(admin)) {
            NoImplementadoException ex = assertThrows(NoImplementadoException.class, x);
            assertTrue(ex.getMessage().startsWith("Pendiente: modulo "));
        }
    }

    @Test
    void pasajeroPasaAutorizacionYRecibeNoImplementado() {
        for (Executable x : paraCualquierSesion(pasajero)) {
            assertThrows(NoImplementadoException.class, x);
        }
    }

    @Test
    void mensajeNombraElModulo() {
        assertEquals("Pendiente: modulo Terminal",
                assertThrows(NoImplementadoException.class, () -> sistema.despacharSiguiente(admin)).getMessage());
        assertEquals("Pendiente: modulo Busqueda",
                assertThrows(NoImplementadoException.class, () -> sistema.busquedasRecientes(pasajero)).getMessage());
    }

    @Test
    void loginDelegaEnAuth() {
        assertSame(Rol.PASAJERO, sistema.registrar("bob_01", CLAVE).rol());
        Sesion s = sistema.iniciarSesion("bob_01", CLAVE);
        assertEquals("bob_01", s.usuario());
        
    }
}
