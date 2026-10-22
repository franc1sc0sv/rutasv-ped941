package edu.udb.rutasv.terminal.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.nucleo.modelo.TipoRuta;
import edu.udb.rutasv.nucleo.modelo.UnidadTransporte;
import edu.udb.rutasv.nucleo.temporal.ColaPrioridadTemporal;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

class ReglaDespachoTest {

    private final ReglaDespacho regla = new ReglaDespacho();

    private static UnidadTransporte unidad(String id, TipoRuta tipo, String hora) {
        return new UnidadTransporte(id, tipo, LocalTime.parse(hora));
    }

    @Test
    void expresSaleAntesQueNormalAunqueSeaMasTarde() {
        UnidadTransporte normal = unidad("U-001", TipoRuta.NORMAL, "06:00");
        UnidadTransporte expres = unidad("U-002", TipoRuta.EXPRES, "09:00");
        assertTrue(regla.compare(expres, normal) < 0);
        assertTrue(regla.compare(normal, expres) > 0);
    }

    @Test
    void mismoTipoSaleLaDeHoraMasProxima() {
        UnidadTransporte temprano = unidad("U-001", TipoRuta.NORMAL, "07:00");
        UnidadTransporte tarde = unidad("U-002", TipoRuta.NORMAL, "08:30");
        assertTrue(regla.compare(temprano, tarde) < 0);
    }

    @Test
    void empateTotalSeDecidePorId() {
        UnidadTransporte a = unidad("U-001", TipoRuta.EXPRES, "07:00");
        UnidadTransporte b = unidad("U-002", TipoRuta.EXPRES, "07:00");
        assertTrue(regla.compare(a, b) < 0);
        assertEquals(0, regla.compare(a, a));
    }

    @Test
    void laColaDespachaEnElOrdenDeLaRegla() {
        ColaPrioridadTemporal<UnidadTransporte> cola = new ColaPrioridadTemporal<>(regla);
        cola.insertar(unidad("N-2", TipoRuta.NORMAL, "07:30"));
        cola.insertar(unidad("X-2", TipoRuta.EXPRES, "08:00"));
        cola.insertar(unidad("N-1", TipoRuta.NORMAL, "06:45"));
        cola.insertar(unidad("X-1", TipoRuta.EXPRES, "07:15"));
        String[] esperado = {"X-1", "X-2", "N-1", "N-2"};
        for (String id : esperado) {
            assertEquals(id, cola.extraer().id());
        }
        assertTrue(cola.estaVacia());
    }
}
