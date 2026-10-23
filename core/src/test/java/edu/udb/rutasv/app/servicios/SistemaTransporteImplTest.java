package edu.udb.rutasv.app.servicios;

import edu.udb.rutasv.app.interfaces.SistemaTransporte;
import edu.udb.rutasv.auth.excepciones.AccesoDenegadoException;
import edu.udb.rutasv.auth.excepciones.SesionInvalidaException;
import edu.udb.rutasv.auth.interfaces.AuthService;
import edu.udb.rutasv.auth.modelo.Rol;
import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.auth.modelo.Usuario;
import edu.udb.rutasv.auth.repositorios.UsuarioRepositorio;
import edu.udb.rutasv.auth.servicios.AuthServiceImpl;
import edu.udb.rutasv.auth.servicios.Autorizador;
import edu.udb.rutasv.auth.servicios.PasswordHasher;
import edu.udb.rutasv.busqueda.estructuras.ArbolAvl;
import edu.udb.rutasv.busqueda.servicios.BusquedaService;
import edu.udb.rutasv.historial.repositorios.HistorialRepositorio;
import edu.udb.rutasv.historial.servicios.EstadisticasService;
import edu.udb.rutasv.historial.servicios.HistorialService;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.excepciones.NoImplementadoException;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.EstadisticasGlobales;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import edu.udb.rutasv.nucleo.temporal.ListaTemporal;
import edu.udb.rutasv.nucleo.temporal.MapaTemporal;
import edu.udb.rutasv.ruta.interfaces.RutaService;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final ArbolAvl indice = new ArbolAvl();
    private final SistemaTransporte sistema =
            new SistemaTransporteImpl(auth, new Autorizador(auth), soloBusqueda(new BusquedaService(indice, new MapaTemporal<>())));
    private final HistorialService historial = new HistorialService(new HistorialRepositorio() {
        @Override
        public Lista<RutaCalculada> cargar() {
            return new ListaTemporal<>();
        }

        @Override
        public void agregar(RutaCalculada ruta) {
        }
    });
    private final SistemaTransporte conHistorial = new SistemaTransporteImpl(auth, new Autorizador(auth),
            new Modulos(null, null, null, null, null, null, null, historial,
                    new EstadisticasService(historial, MapaTemporal::new, ColaPrioridadTemporal::new)));
    private final Sesion pasajero;
    private final Sesion admin;

    SistemaTransporteImplTest() {
        String sal = hasher.nuevaSalBase64();
        mapa.poner("root", new Usuario("root", sal, hasher.hashear(CLAVE, sal), Rol.ADMIN));
        admin = auth.iniciarSesion("root", CLAVE);
        pasajero = auth.registrar("ana_01", CLAVE);
        indice.insertar("Centro", new Estacion("E1", "Centro", "Z1", 0, 0, true));
    }

    /** Solo Busqueda conectada: el resto de modulos debe responder "Pendiente". */
    private static Modulos soloBusqueda(BusquedaService busqueda) {
        return new Modulos(busqueda, null, null, null, null, null, null, null, null);
    }

    private Executable[] soloAdmin(Sesion s) {
        Estacion e = new Estacion("E1", "Centro", "Z1", 0, 0, true);
        return new Executable[] {
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
            () -> sistema.calcularRuta(s, "A", "B"),
            () -> sistema.verRed(s),
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
        assertThrows(SesionInvalidaException.class, () -> sistema.sugerirEstaciones(null, "ce"));
        assertThrows(SesionInvalidaException.class, () -> sistema.busquedasRecientes(null));
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
        assertEquals("Pendiente: modulo Ruta",
                assertThrows(NoImplementadoException.class, () -> sistema.calcularRuta(pasajero, "A", "B")).getMessage());
    }

    @Test
    void loginDelegaEnAuth() {
        assertSame(Rol.PASAJERO, sistema.registrar("bob_01", CLAVE).rol());
        Sesion s = sistema.iniciarSesion("bob_01", CLAVE);
        assertEquals("bob_01", s.usuario());

    }

    @Test
    void busquedaDelegaEnElServicioConElUsuarioDeLaSesion() {
        List<String> sugeridas = new ArrayList<>();
        for (String nombre : sistema.sugerirEstaciones(pasajero, "ce")) {
            sugeridas.add(nombre);
        }
        assertEquals(List.of("Centro"), sugeridas);
        assertEquals("ce", sistema.busquedasRecientes(pasajero).obtener(0));
        assertTrue(sistema.busquedasRecientes(admin).estaVacia());
    }

    @Test
    void miHistorialDevuelveSoloLasRutasDelUsuarioDeLaSesion() {
        RutaCalculada deAna = new RutaCalculada("E1", "E2", List.of("E1", "E2"), 5, 1.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), 1000, "ana_01");
        historial.registrar(deAna);
        historial.registrar(new RutaCalculada("E2", "E1", List.of("E2", "E1"), 5, 1.0,
                LocalDateTime.of(2026, 10, 23, 9, 5), 1000, "root"));

        Lista<RutaCalculada> mias = conHistorial.miHistorial(pasajero);
        assertEquals(1, mias.tamano());
        assertEquals(deAna, mias.obtener(0));
    }

    @Test
    void adminVeLasEstadisticasDelHistorial() {
        historial.registrar(new RutaCalculada("E1", "E2", List.of("E1", "E2"), 5, 1.0,
                LocalDateTime.of(2026, 10, 23, 9, 0), 1000, "ana_01"));

        EstadisticasGlobales e = conHistorial.estadisticas(admin);
        assertEquals(1, e.totalBusquedas());
        assertEquals("E1 -> E2", e.rutaMasConsultada());
    }

    @Test
    void rutaEncontradaQuedaEnElHistorialYSinRutaNoSeGuarda() {
        RutaService soloE1aE2 = (origen, destino, usuario) -> origen.equals("E1") && destino.equals("E2")
                ? Optional.of(new RutaCalculada(origen, destino, List.of(origen, destino), 5, 1.0,
                        LocalDateTime.of(2026, 10, 23, 9, 0), 1000, usuario))
                : Optional.empty();
        SistemaTransporte conRuta = new SistemaTransporteImpl(auth, new Autorizador(auth),
                new Modulos(null, null, null, soloE1aE2, null, null, null, historial, null));

        assertTrue(conRuta.calcularRuta(pasajero, "E1", "E9").isEmpty());
        assertTrue(conRuta.miHistorial(pasajero).estaVacia());

        RutaCalculada ruta = conRuta.calcularRuta(pasajero, "E1", "E2").orElseThrow();
        assertEquals(1, conRuta.miHistorial(pasajero).tamano());
        assertEquals(ruta, conRuta.miHistorial(pasajero).obtener(0));
    }

    @Test
    void sinHistorialConectadoLaRutaSeCalculaIgual() {
        RutaService siempre = (origen, destino, usuario) -> Optional.of(new RutaCalculada(origen, destino,
                List.of(origen, destino), 5, 1.0, LocalDateTime.of(2026, 10, 23, 9, 0), 1000, usuario));
        SistemaTransporte soloRuta = new SistemaTransporteImpl(auth, new Autorizador(auth),
                new Modulos(null, null, null, siempre, null, null, null, null, null));

        assertTrue(soloRuta.calcularRuta(pasajero, "E1", "E2").isPresent());
    }
}
