package edu.udb.rutasv.rendimiento;

import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.udb.rutasv.busqueda.estructuras.ArbolAvl;
import edu.udb.rutasv.nucleo.estructuras.Lista;
import edu.udb.rutasv.nucleo.estructuras.impl.ListaDinamica;
import edu.udb.rutasv.nucleo.estructuras.impl.MonticuloBinario;
import edu.udb.rutasv.nucleo.estructuras.impl.TablaHash;
import edu.udb.rutasv.nucleo.modelo.Estacion;
import edu.udb.rutasv.ruta.algoritmos.Dijkstra;
import edu.udb.rutasv.ruta.estructuras.GrafoListaAdyacencia;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;
import java.util.function.LongSupplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * Mide el tiempo real de las operaciones clave y lo compara con su Big-O. No corre en el CI:
 *
 * <pre>mvn -B -pl core test -Dtest=MedicionesRendimientoTest -Drendimiento=true -Dsurefire.failIfNoSpecifiedTests=false</pre>
 *
 * Escribe {@code docs/rendimiento.md}. Cada medida es la mediana de 7 corridas despues de calentar
 * la JVM (el compilador JIT acelera el codigo las primeras veces). Datos sinteticos y semilla fija.
 */
@EnabledIfSystemProperty(named = "rendimiento", matches = "true")
class MedicionesRendimientoTest {

    private static final int CORRIDAS = 7;
    private final StringBuilder md = new StringBuilder();

    /** Mediana de varias corridas, en nanosegundos. La primera mitad de calentamiento se descarta. */
    private static long mediana(LongSupplier corrida) {
        for (int i = 0; i < 3; i++) {
            corrida.getAsLong();
        }
        long[] t = new long[CORRIDAS];
        for (int i = 0; i < CORRIDAS; i++) {
            t[i] = corrida.getAsLong();
        }
        Arrays.sort(t);
        return t[CORRIDAS / 2];
    }

    private static String f(double v) {
        return String.format(Locale.ROOT, "%,.1f", v);
    }

    @Test
    void medirYEscribirElReporte() throws IOException {
        md.append("# Mediciones de rendimiento\n\n")
                .append("Generado por `MedicionesRendimientoTest` (").append(System.getProperty("java.version"))
                .append(", ").append(Runtime.getRuntime().availableProcessors()).append(" núcleos). ")
                .append("Cada valor es la mediana de ").append(CORRIDAS)
                .append(" corridas tras calentar la JVM. Datos sintéticos, semilla fija.\n\n")
                .append("La columna \"normalizado\" divide el tiempo entre la función del Big-O: si se mantiene ")
                .append("más o menos constante al crecer n, la medida confirma la teoría.\n");
        tablaHashContraBusquedaLineal();
        arbolAvl();
        monticulo();
        dijkstra();
        topCinco();
        Path salida = Path.of("..", "docs", "rendimiento.md");
        Files.writeString(salida, md.toString(), StandardCharsets.UTF_8);
        assertTrue(Files.size(salida) > 0);
    }

    // ---- 1. Tabla hash: O(1) promedio contra busqueda lineal O(n) ----

    private void tablaHashContraBusquedaLineal() {
        md.append("\n## 1. Tabla hash contra búsqueda lineal\n\n")
                .append("Buscar 1 000 claves existentes. La tabla hash es O(1) promedio; recorrer una lista es O(n).\n\n")
                .append("| n | TablaHash (ns por búsqueda) | Lista lineal (ns por búsqueda) | Lista / Tabla |\n")
                .append("|---:|---:|---:|---:|\n");
        Random azar = new Random(941);
        for (int n : new int[] {1_000, 10_000, 100_000, 1_000_000}) {
            TablaHash<String, Integer> tabla = new TablaHash<>();
            ListaDinamica<String> lista = new ListaDinamica<>();
            for (int i = 0; i < n; i++) {
                tabla.poner("E" + i, i);
                if (n <= 100_000) {
                    lista.agregar("E" + i);
                }
            }
            String[] buscadas = new String[1_000];
            for (int i = 0; i < buscadas.length; i++) {
                buscadas[i] = "E" + azar.nextInt(n);
            }
            long hash = mediana(() -> {
                long t0 = System.nanoTime();
                int suma = 0;
                for (String k : buscadas) {
                    suma += tabla.obtener(k);
                }
                return System.nanoTime() - t0 + (suma == -1 ? 1 : 0);
            });
            String linealTxt = "—";
            String razon = "—";
            if (n <= 100_000) {
                long lineal = mediana(() -> {
                    long t0 = System.nanoTime();
                    int halladas = 0;
                    for (int j = 0; j < 100; j++) {
                        for (String e : lista) {
                            if (e.equals(buscadas[j])) {
                                halladas++;
                                break;
                            }
                        }
                    }
                    return (System.nanoTime() - t0) * 10 + (halladas == -1 ? 1 : 0);
                });
                linealTxt = f(lineal / 1_000.0);
                razon = f((double) lineal / hash) + "×";
            }
            md.append("| ").append(String.format(Locale.ROOT, "%,d", n)).append(" | ").append(f(hash / 1_000.0))
                    .append(" | ").append(linealTxt).append(" | ").append(razon).append(" |\n");
        }
        md.append("\nCon un millón de claves la tabla tarda casi lo mismo que con mil: eso es O(1) promedio. ")
                .append("La lista crece en proporción a n. Por eso el directorio de estaciones, los usuarios, ")
                .append("las sesiones y el grafo usan `TablaHash`.\n");
    }

    // ---- 2. Arbol AVL: O(log n) ----

    private void arbolAvl() {
        md.append("\n## 2. Árbol AVL (autocompletado)\n\n")
                .append("Buscar 1 000 nombres en un árbol con n nombres. Teoría: O(L log n), con L el largo del nombre.\n\n")
                .append("| n | ns por búsqueda | log₂ n | ns / log₂ n |\n|---:|---:|---:|---:|\n");
        for (int n : new int[] {1_000, 10_000, 100_000, 500_000}) {
            ArbolAvl arbol = new ArbolAvl();
            // Insertar en orden: el peor caso de un ABB sin balancear (se volveria una lista).
            for (int i = 0; i < n; i++) {
                String nombre = String.format(Locale.ROOT, "Estacion %07d", i);
                arbol.insertar(nombre, new Estacion("C" + i, nombre, "Z", 0, 0, true));
            }
            Random azar = new Random(7);
            String[] buscadas = new String[1_000];
            for (int i = 0; i < buscadas.length; i++) {
                buscadas[i] = String.format(Locale.ROOT, "Estacion %07d", azar.nextInt(n));
            }
            long t = mediana(() -> {
                long t0 = System.nanoTime();
                int halladas = 0;
                for (String b : buscadas) {
                    if (arbol.buscar(b) != null) {
                        halladas++;
                    }
                }
                return System.nanoTime() - t0 + (halladas == -1 ? 1 : 0);
            });
            double porBusqueda = t / 1_000.0;
            double log = Math.log(n) / Math.log(2);
            md.append("| ").append(String.format(Locale.ROOT, "%,d", n)).append(" | ").append(f(porBusqueda))
                    .append(" | ").append(f(log)).append(" | ").append(f(porBusqueda / log)).append(" |\n");
        }
        md.append("\nLos nombres se insertaron **en orden**, el peor caso de un árbol binario sin balancear: ")
                .append("sin rotaciones se volvería una lista de altura n y cada búsqueda sería O(n). Con las ")
                .append("rotaciones del AVL la altura queda en O(log n) y el tiempo crece muy lento. Que la ")
                .append("columna normalizada baje con n se debe a la caché y al JIT, no al algoritmo: lo que importa ")
                .append("es que el tiempo no se dispara al pasar de mil a medio millón de nombres.\n");
    }

    // ---- 3. Monticulo binario: n operaciones O(n log n) ----

    private void monticulo() {
        md.append("\n## 3. Montículo binario (despacho y Dijkstra)\n\n")
                .append("Insertar n elementos y extraerlos todos. Teoría: O(log n) cada operación, O(n log n) en total.\n\n")
                .append("| n | ms total | ns / (n log₂ n) |\n|---:|---:|---:|\n");
        for (int n : new int[] {10_000, 100_000, 1_000_000}) {
            Random azar = new Random(3);
            int[] datos = new int[n];
            for (int i = 0; i < n; i++) {
                datos[i] = azar.nextInt();
            }
            long t = mediana(() -> {
                long t0 = System.nanoTime();
                MonticuloBinario<Integer> m = new MonticuloBinario<>(Comparator.<Integer>naturalOrder());
                for (int d : datos) {
                    m.insertar(d);
                }
                long suma = 0;
                while (!m.estaVacia()) {
                    suma += m.extraer();
                }
                return System.nanoTime() - t0 + (suma == 1 ? 1 : 0);
            });
            double nlogn = n * (Math.log(n) / Math.log(2));
            md.append("| ").append(String.format(Locale.ROOT, "%,d", n)).append(" | ").append(f(t / 1e6))
                    .append(" | ").append(f(t / nlogn)).append(" |\n");
        }
    }

    // ---- 4. Dijkstra: O((V + E) log V) ----

    private static GrafoListaAdyacencia cuadricula(int lado, Random azar) {
        GrafoListaAdyacencia red = new GrafoListaAdyacencia();
        for (int f = 0; f < lado; f++) {
            for (int c = 0; c < lado; c++) {
                red.agregarEstacion(new Estacion("N" + (f * lado + c), "N" + (f * lado + c), "Z", c, f, true));
            }
        }
        for (int f = 0; f < lado; f++) {
            for (int c = 0; c < lado; c++) {
                int id = f * lado + c;
                if (c + 1 < lado) {
                    int m = 1 + azar.nextInt(10);
                    red.agregarTramo("N" + id, "N" + (id + 1), m, 1);
                    red.agregarTramo("N" + (id + 1), "N" + id, m, 1);
                }
                if (f + 1 < lado) {
                    int m = 1 + azar.nextInt(10);
                    red.agregarTramo("N" + id, "N" + (id + lado), m, 1);
                    red.agregarTramo("N" + (id + lado), "N" + id, m, 1);
                }
            }
        }
        return red;
    }

    private void dijkstra() {
        md.append("\n## 4. Dijkstra (ruta más rápida)\n\n")
                .append("Red en cuadrícula de V estaciones con tramos de 1 a 10 minutos en ambos sentidos ")
                .append("(E ≈ 4V tramos dirigidos). Ruta de una esquina a la opuesta: el peor caso, porque ")
                .append("Dijkstra visita casi toda la red antes de cerrar el destino. Teoría: O((V + E) log V).\n\n")
                .append("| V | E | ms por ruta | ns / ((V+E) log₂ V) |\n|---:|---:|---:|---:|\n");
        Dijkstra dijkstra = new Dijkstra(MonticuloBinario::new, TablaHash::new);
        for (int lado : new int[] {10, 32, 100, 224}) {
            GrafoListaAdyacencia red = cuadricula(lado, new Random(941));
            int v = lado * lado;
            int e = 4 * lado * (lado - 1);
            String destino = "N" + (v - 1);
            long t = mediana(() -> {
                long t0 = System.nanoTime();
                int minutos = dijkstra.calcular(red, "N0", destino).orElseThrow().minutos();
                return System.nanoTime() - t0 + (minutos < 0 ? 1 : 0);
            });
            double teoria = (v + e) * (Math.log(v) / Math.log(2));
            md.append("| ").append(String.format(Locale.ROOT, "%,d", v)).append(" | ")
                    .append(String.format(Locale.ROOT, "%,d", e)).append(" | ").append(f(t / 1e6))
                    .append(" | ").append(f(t / teoria)).append(" |\n");
        }
        md.append("\nLa red de la demo tiene 30 estaciones y 83 tramos: una ruta tarda microsegundos. ")
                .append("El tiempo que muestra la app (\"tiempo de cálculo\") incluye el calentamiento de la JVM, ")
                .append("por eso las primeras rutas tardan más que las siguientes.\n");
    }

    // ---- 5. Top 5: monticulo acotado contra ordenar todo ----

    private void topCinco() {
        md.append("\n## 5. Top 5 de rutas: montículo acotado contra montículo completo\n\n")
                .append("P pares distintos con su conteo. El montículo acotado guarda solo 5: O(P log 5) = O(P). ")
                .append("Meter todos los pares y sacar 5 cuesta O(P log P).\n\n")
                .append("| P | acotado (ms) | completo (ms) | completo / acotado |\n|---:|---:|---:|---:|\n");
        for (int p : new int[] {10_000, 100_000, 1_000_000}) {
            Random azar = new Random(5);
            int[] conteos = new int[p];
            for (int i = 0; i < p; i++) {
                conteos[i] = azar.nextInt(1_000);
            }
            Comparator<Integer> mejorPrimero = (a, b) -> {
                int c = Integer.compare(conteos[b], conteos[a]);
                return c != 0 ? c : Integer.compare(a, b);
            };
            long acotado = mediana(() -> {
                long t0 = System.nanoTime();
                MonticuloBinario<Integer> peores = new MonticuloBinario<>(mejorPrimero.reversed());
                for (int i = 0; i < p; i++) {
                    peores.insertar(i);
                    if (peores.tamano() > 5) {
                        peores.extraer();
                    }
                }
                return System.nanoTime() - t0 + (peores.tamano() == 99 ? 1 : 0);
            });
            long completo = mediana(() -> {
                long t0 = System.nanoTime();
                MonticuloBinario<Integer> todos = new MonticuloBinario<>(mejorPrimero);
                for (int i = 0; i < p; i++) {
                    todos.insertar(i);
                }
                Lista<Integer> top = new ListaDinamica<>();
                for (int i = 0; i < 5; i++) {
                    top.agregar(todos.extraer());
                }
                return System.nanoTime() - t0 + (top.tamano() == 99 ? 1 : 0);
            });
            md.append("| ").append(String.format(Locale.ROOT, "%,d", p)).append(" | ").append(f(acotado / 1e6))
                    .append(" | ").append(f(completo / 1e6)).append(" | ").append(f((double) completo / acotado))
                    .append("× |\n");
        }
        md.append("\nCon pocos pares el acotado puede salir **más lento**: hace una extracción en casi cada ")
                .append("inserción. La ventaja en tiempo aparece con muchos pares, porque el montículo completo ")
                .append("hace hasta log P comparaciones por inserción y el acotado como mucho log 5 ≈ 2,3. En ")
                .append("RutaSV hay pocos pares, así que la ganancia real es de **memoria**: guarda 5 elementos ")
                .append("en vez de P. Es una limitación honesta de la optimización.\n");
    }
}
