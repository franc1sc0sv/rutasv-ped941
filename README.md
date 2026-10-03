# RutaSV

Planificador de rutas de transporte publico para el proyecto PED941 (equipo de 5).
Esta hecho en Java 21 con Maven. Por ahora solo existe el modulo `core`: aun no hay interfaz ni capa HTTP.

Lo unico que funciona completo es el login: registro, inicio de sesion y roles (PASAJERO y ADMIN).
El resto del sistema tiene interfaces y adaptadores temporales basados en `java.util`.
Cada companero reemplaza uno de ellos con su propia estructura.

## Las 7 estructuras

| Estructura | Interfaz | Rol | Uso |
|---|---|---|---|
| Lista dinamica | `Lista<T>` | Historial | Usuarios, historial, vecinos |
| Pila | `Pila<T>` | Ruta | Reconstruir la ruta |
| Cola FIFO | `Cola<T>` | Terminal | Fila de pasajeros |
| Cola de prioridad | `ColaPrioridad<T>` | Terminal | Despacho, Dijkstra, top 5 |
| Tabla hash | `Mapa<K,V>` | Paradas | Estaciones, usuarios, conteos |
| Arbol AVL | `IndiceNombres` | Busqueda | Busqueda por prefijo |
| Grafo | `RedVial` | Ruta | Red de estaciones y tramos |

## Instalar JDK 21 y Maven

```bash
sudo apt install openjdk-21-jdk maven   # Ubuntu / Debian
java -version                           # debe decir 21
mvn -version                            # Maven 3.9 o superior
```

## Compilar y probar

```bash
mvn -B verify          # compila y corre todas las pruebas
mvn -B test            # solo pruebas
```

## Empieza aqui (sin reuniones)

1. Elige tu rol y asignate su issue: [Ruta #1](../../issues/1), [Busqueda #2](../../issues/2), [Terminal #3](../../issues/3), [Historial #4](../../issues/4), [Paradas #5](../../issues/5).
2. Abre la guia de tu modulo: `core/src/main/java/edu/udb/rutasv/<modulo>/LEEME.md`. Dice que archivos ya existen, que te toca crear y en que orden.
3. Clona, corre `mvn verify` y crea tu rama `feat/<rol>-<tarea>`.
4. Implementa tu estructura, quita el `@Disabled` de su prueba de contrato y abre un PR chico por tarea.

No necesitas esperar a nadie: `nucleo/temporal` trae versiones con `java.util` de todo lo que dependa de otro rol.

## Estructura del repositorio

```
core/                    modulo Java (edu.udb.rutasv)
  nucleo/                contratos compartidos
    estructuras/         interfaces (Lista, Pila, Cola, ColaPrioridad, Mapa, IndiceNombres, RedVial)
    modelo/              registros del dominio
    excepciones/         excepciones compartidas
    archivo/             escritura atomica de archivos
    temporal/            adaptadores temporales (java.util), se reemplazan por los propios
  <modulo>/              auth, ruta, paradas, busqueda, terminal, historial. Cada uno con:
    interfaces/          contratos del servicio
    servicios/           logica de negocio
    repositorios/        guardar y cargar datos
    modelo/              clases propias del modulo
    excepciones/         excepciones propias
    estructuras/         estructuras de datos propias del rol
    (ruta tambien tiene algoritmos/)
  app/                   unica puerta de entrada
    interfaces/          SistemaTransporte
    servicios/           SistemaTransporteImpl
    configuracion/       Aplicacion (cableado)
data/                    CSV de ejemplo (datos sinteticos)
docs/                    documentacion del equipo
api/  frontend/          vacios hasta decidir el framework
.github/                 CI y plantilla de PR
```

## Variables de entorno

- `RUTASV_ADMIN_USER`: usuario admin inicial (por defecto `admin`).
- `RUTASV_ADMIN_PASSWORD`: contrasena del admin inicial, minimo 8 caracteres. Sin ella no se crea el admin.

## Documentacion

- [CONTRIBUTING.md](CONTRIBUTING.md): flujo de ramas y PR
- [docs/arquitectura.md](docs/arquitectura.md)
- [docs/decisiones.md](docs/decisiones.md)
- [docs/contratos.md](docs/contratos.md)
- [docs/tareas-por-rol.md](docs/tareas-por-rol.md)
