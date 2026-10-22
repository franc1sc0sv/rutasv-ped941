package edu.udb.rutasv.busqueda.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class NormalizadorTest {

    @Test
    void quitaAcentosYPasaAMinusculas() {
        assertEquals("estacion peru", Normalizador.normalizar("Estación Perú"));
        assertEquals("agata", Normalizador.normalizar("ÁGATA"));
    }

    @Test
    void enieSeVuelveN() {
        assertEquals("nunoa", Normalizador.normalizar("Ñuñoa"));
    }

    @Test
    void conservaEspacios() {
        assertEquals("san ", Normalizador.normalizar("San "));
    }

    @Test
    void nuloOVacioDaTextoVacio() {
        assertEquals("", Normalizador.normalizar(null));
        assertEquals("", Normalizador.normalizar(""));
    }
}
