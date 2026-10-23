package edu.udb.rutasv.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.udb.rutasv.app.configuracion.Aplicacion;
import io.javalin.Javalin;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Recorre la API por HTTP real, con los CSV de ejemplo de data/. Datos sinteticos. */
class ServidorTest {

    private static final ObjectMapper JSON = new ObjectMapper();
    private final HttpClient http = HttpClient.newHttpClient();

    @TempDir
    Path dir;

    private Javalin app;
    private String base;

    private record Respuesta(int estado, JsonNode cuerpo) {
    }

    @BeforeEach
    void arrancar() throws IOException {
        for (String archivo : new String[] {"estaciones.csv", "tramos.csv"}) {
            Files.copy(Path.of("..", "data", archivo), dir.resolve(archivo));
        }
        app = Servidor.crear(Aplicacion.crear(dir, Map.of("RUTASV_ADMIN_PASSWORD", "clave-sintetica-9"))).start(0);
        base = "http://localhost:" + app.port();
    }

    @AfterEach
    void detener() {
        app.stop();
    }

    private Respuesta pedir(String metodo, String ruta, String token, String cuerpo) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta))
                .method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                        : HttpRequest.BodyPublishers.ofString(cuerpo))
                .header("Content-Type", "application/json");
        if (token != null) {
            b.header("Authorization", "Bearer " + token);
        }
        HttpResponse<String> r = http.send(b.build(), HttpResponse.BodyHandlers.ofString());
        return new Respuesta(r.statusCode(), r.body().isEmpty() ? null : JSON.readTree(r.body()));
    }

    private String token(String ruta, String usuario, String clave) throws Exception {
        String cuerpo = "{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + clave + "\"}";
        return pedir("POST", ruta, null, cuerpo).cuerpo().get("token").asText();
    }

    @Test
    void pasajeroBuscaYCalculaLaRutaDelEjemplo() throws Exception {
        String t = token("/api/auth/registro", "ana_01", "clave-sintetica-1");

        Respuesta sugerencias = pedir("GET", "/api/estaciones/sugerencias?prefijo=ter", t, null);
        assertEquals(200, sugerencias.estado());
        assertEquals("Terminal Occidente", sugerencias.cuerpo().get(0).asText());

        Respuesta ruta = pedir("GET", "/api/rutas?origen=E01&destino=E04", t, null);
        assertEquals(200, ruta.estado());
        assertEquals(18, ruta.cuerpo().get("minutos").asInt());
        assertEquals(4, ruta.cuerpo().get("estaciones").size());
        assertEquals("Terminal Occidente", ruta.cuerpo().get("estaciones").get(0).get("nombre").asText());
        assertEquals(3, ruta.cuerpo().get("tramos").size());
        assertEquals(2, ruta.cuerpo().get("tramos").get(2).get("minutos").asInt());
    }

    @Test
    void erroresSeTraducenACodigosHttp() throws Exception {
        assertEquals(401, pedir("GET", "/api/red", null, null).estado());
        assertEquals(401, pedir("POST", "/api/auth/login", null,
                "{\"usuario\":\"nadie\",\"contrasena\":\"clave-sintetica-1\"}").estado());

        String t = token("/api/auth/registro", "ana_01", "clave-sintetica-1");
        assertEquals(409, pedir("POST", "/api/auth/registro", null,
                "{\"usuario\":\"ana_01\",\"contrasena\":\"clave-sintetica-1\"}").estado());
        assertEquals(400, pedir("GET", "/api/rutas?origen=E01&destino=ZZ", t, null).estado());
        assertEquals(403, pedir("POST", "/api/red/tramos", t,
                "{\"origen\":\"E01\",\"destino\":\"E05\",\"minutos\":3,\"km\":1}").estado());

        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");
        Respuesta pendiente = pedir("GET", "/api/estadisticas", admin, null);
        assertEquals(501, pendiente.estado());
        assertTrue(pendiente.cuerpo().get("pendiente").asBoolean());
    }

    @Test
    void adminAgregaTramoYDespachaUnidades() throws Exception {
        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");

        assertEquals(201, pedir("POST", "/api/red/tramos", admin,
                "{\"origen\":\"E07\",\"destino\":\"E05\",\"minutos\":3,\"km\":1.5}").estado());
        assertEquals(400, pedir("POST", "/api/red/tramos", admin,
                "{\"origen\":\"E07\",\"destino\":\"E05\",\"minutos\":0,\"km\":1.5}").estado());
        assertEquals(15, pedir("GET", "/api/red", admin, null).cuerpo().get("tramos").size());

        assertEquals(201, pedir("POST", "/api/terminal/unidades", admin,
                "{\"id\":\"U-005\",\"tipoRuta\":\"NORMAL\",\"horaProgramada\":\"06:00\"}").estado());
        assertEquals(201, pedir("POST", "/api/terminal/unidades", admin,
                "{\"id\":\"U-014\",\"tipoRuta\":\"EXPRES\",\"horaProgramada\":\"07:00\"}").estado());
        assertEquals("U-014", pedir("GET", "/api/terminal/orden", admin, null).cuerpo().get(0).get("id").asText());
        assertEquals("U-014", pedir("POST", "/api/terminal/despachar", admin, null).cuerpo().get("id").asText());
        assertEquals("U-005", pedir("POST", "/api/terminal/despachar", admin, null).cuerpo().get("id").asText());
        assertEquals(409, pedir("POST", "/api/terminal/despachar", admin, null).estado());
    }

    @Test
    void sinRutaDevuelve404Rn6() throws Exception {
        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");
        assertEquals(201, pedir("POST", "/api/estaciones", admin,
                "{\"codigo\":\"E30\",\"nombre\":\"Aislada\",\"zona\":\"Sur\",\"x\":1,\"y\":1,\"activa\":true}")
                .estado());
        Respuesta r = pedir("GET", "/api/rutas?origen=E01&destino=E30", admin, null);
        assertEquals(404, r.estado());
        assertTrue(r.cuerpo().get("error").asText().contains("No existe ruta"));
    }

    @Test
    void jsonInvalidoYMinutosFraccionariosDan400() throws Exception {
        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");
        assertEquals(400, pedir("POST", "/api/red/tramos", admin, "{no es json").estado());
        assertEquals(400, pedir("POST", "/api/red/tramos", admin,
                "{\"origen\":\"E07\",\"destino\":\"E05\",\"minutos\":2.7,\"km\":1}").estado());
        assertEquals(400, pedir("POST", "/api/terminal/unidades", admin,
                "{\"id\":\"U-1\",\"tipoRuta\":\"RAPIDA\",\"horaProgramada\":\"06:00\"}").estado());
        assertEquals(400, pedir("POST", "/api/terminal/unidades", admin,
                "{\"id\":\"U-1\",\"tipoRuta\":\"NORMAL\",\"horaProgramada\":\"6 am\"}").estado());
    }

    @Test
    void logoutInvalidaElToken() throws Exception {
        String t = token("/api/auth/registro", "ana_01", "clave-sintetica-1");
        assertEquals(200, pedir("GET", "/api/auth/yo", t, null).estado());
        assertEquals(204, pedir("POST", "/api/auth/logout", t, null).estado());
        assertEquals(401, pedir("GET", "/api/auth/yo", t, null).estado());
    }

    @Test
    void paradasBuscarYPendientes() throws Exception {
        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");
        assertEquals("E04", pedir("GET", "/api/estaciones/buscar?texto=metrocentro", admin, null)
                .cuerpo().get("codigo").asText());
        assertEquals(404, pedir("GET", "/api/estaciones/buscar?texto=nada", admin, null).estado());
        assertEquals(8, pedir("GET", "/api/estaciones", admin, null).cuerpo().size());
        Respuesta pendiente = pedir("PATCH", "/api/estaciones/E01/estado", admin, "{\"activa\":false}");
        assertEquals(501, pendiente.estado());
        assertTrue(pendiente.cuerpo().get("pendiente").asBoolean());
    }

    @Test
    void filasDePasajerosSonFifo() throws Exception {
        String admin = token("/api/auth/login", "admin", "clave-sintetica-9");
        assertEquals(201, pedir("POST", "/api/terminal/filas/normal/pasajeros", admin,
                "{\"id\":\"P1\",\"nombre\":\"Ana\"}").estado());
        assertEquals(201, pedir("POST", "/api/terminal/filas/NORMAL/pasajeros", admin,
                "{\"id\":\"P2\",\"nombre\":\"Luis\"}").estado());
        assertEquals("P1", pedir("POST", "/api/terminal/filas/NORMAL/atender", admin, null).cuerpo().get("id").asText());
        assertEquals(409, pedir("POST", "/api/terminal/filas/EXPRES/atender", admin, null).estado());
        assertEquals(400, pedir("POST", "/api/terminal/filas/OTRA/atender", admin, null).estado());
    }
}
