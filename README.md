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

## Estructura del repositorio

```
core/                    modulo Java (edu.udb.rutasv)
  model/                 registros del dominio
  structures/            interfaces de las estructuras
  structures/temporal/   adaptadores temporales (java.util)
  algorithms/            algoritmos (Dijkstra, etc.)
  auth/                  registro, login, roles
  persistence/           repositorios de archivo
  service/               SistemaTransporte y autorizacion
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
- [docs/decisiones.md](docs/decisiones.md)
- [docs/contratos.md](docs/contratos.md)
- [docs/tareas-por-rol.md](docs/tareas-por-rol.md)
