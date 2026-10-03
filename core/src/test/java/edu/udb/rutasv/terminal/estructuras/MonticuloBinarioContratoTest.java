package edu.udb.rutasv.terminal.estructuras;

import org.junit.jupiter.api.Disabled;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridad;
import java.util.Comparator;
import edu.udb.rutasv.nucleo.estructuras.ColaPrioridadContractTest;

@Disabled("Quita esta linea cuando MonticuloBinario este implementada")
class MonticuloBinarioContratoTest extends ColaPrioridadContractTest {

    @Override
    protected ColaPrioridad<Integer> crear(Comparator<Integer> comparador) {
        return new MonticuloBinario<>(comparador);
    }
}
