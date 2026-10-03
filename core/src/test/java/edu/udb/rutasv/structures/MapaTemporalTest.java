package edu.udb.rutasv.structures;

import edu.udb.rutasv.structures.temporal.MapaTemporal;

class MapaTemporalTest extends MapaContractTest {
    @Override
    protected Mapa<String, Integer> crear() {
        return new MapaTemporal<>();
    }
}
