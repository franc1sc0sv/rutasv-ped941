package edu.udb.rutasv.api;

import edu.udb.rutasv.auth.modelo.Sesion;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.RedVial;
import edu.udb.rutasv.nucleo.modelo.Arista;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.nucleo.modelo.RutaCalculada;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Formas JSON de entrada y salida de la API. Ver {@code docs/api.md}. */
final class Dto {

    private Dto() {
    }

    // ---- Entrada ----

    record Credenciales(String usuario, String contrasena) {
    }

    record NuevoTramo(String origen, String destino, int minutos, double km) {
    }

    record CambioEstado(boolean activa) {
    }

    // ---- Salida ----

    record SesionDto(String token, String usuario, String rol) {
        static SesionDto de(Sesion s) {
            return new SesionDto(s.token(), s.usuario(), s.rol().name());
        }
    }

    record Error(String error, boolean pendiente) {
    }

    record Tramo(String origen, String destino, int minutos, double km) {
    }

    record Red(List<Estacion> estaciones, List<Tramo> tramos) {
        static Red de(RedVial red) {
            List<Estacion> estaciones = lista(red.estaciones());
            List<Tramo> tramos = new ArrayList<>();
            for (Estacion e : estaciones) {
                for (Arista a : red.vecinos(e.codigo())) {
                    tramos.add(new Tramo(e.codigo(), a.destinoCodigo(), a.minutos(), a.km()));
                }
            }
            return new Red(estaciones, tramos);
        }
    }

    /** RutaCalculada con el detalle que pinta la pantalla Resultado de ruta (Fig. 6). */
    record Ruta(String origen, String destino, int minutos, double km, LocalDateTime fecha, long tiempoCalculoNs,
                String usuario, List<Estacion> estaciones, List<Tramo> tramos) {
        static Ruta de(RutaCalculada r, RedVial red) {
            Map<String, Estacion> porCodigo = new HashMap<>();
            for (Estacion e : red.estaciones()) {
                porCodigo.put(e.codigo(), e);
            }
            List<Estacion> estaciones = new ArrayList<>();
            List<Tramo> tramos = new ArrayList<>();
            for (int i = 0; i < r.tramos().size(); i++) {
                String codigo = r.tramos().get(i);
                estaciones.add(porCodigo.get(codigo));
                if (i > 0) {
                    tramos.add(tramo(red, r.tramos().get(i - 1), codigo));
                }
            }
            return new Ruta(r.origen(), r.destino(), r.minutos(), r.km(), r.fecha(), r.tiempoCalculoNs(),
                    r.usuario(), estaciones, tramos);
        }

        private static Tramo tramo(RedVial red, String desde, String hasta) {
            for (Arista a : red.vecinos(desde)) {
                if (a.destinoCodigo().equals(hasta)) {
                    return new Tramo(desde, hasta, a.minutos(), a.km());
                }
            }
            return new Tramo(desde, hasta, 0, 0);
        }
    }

    static <T> List<T> lista(Lista<T> lista) {
        List<T> out = new ArrayList<>();
        for (T t : lista) {
            out.add(t);
        }
        return out;
    }
}
