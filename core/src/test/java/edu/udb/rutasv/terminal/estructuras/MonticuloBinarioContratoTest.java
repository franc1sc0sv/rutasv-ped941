package edu.udb.rutasv.terminal.estructuras;

import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import java.util.Comparator;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridadContractTest;

class MonticuloBinarioContratoTest extends ColaPrioridadContractTest {

    @Override
    protected ColaPrioridad<Integer> crear(Comparator<Integer> comparador) {
        return new MonticuloBinario<>(comparador);
    }
}
