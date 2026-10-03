package edu.udb.rutasv.nucleo.estructuras;

import edu.udb.rutasv.nucleo.temporal.MapaTemporal;

class MapaTemporalTest extends MapaContractTest {
    @Override
    protected Mapa<String, Integer> crear() {
        return new MapaTemporal<>();
    }
}
