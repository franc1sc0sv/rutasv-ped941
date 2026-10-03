# RutaSV

Planificador de rutas de transporte publico para el proyecto PED941 (equipo de 5).
Esta hecho en Java 21 con Maven. Por ahora solo existe el modulo `core`: aun no hay interfaz ni capa HTTP.

Lo unico que funciona completo es el login: registro, inicio de sesion y roles (PASAJERO y ADMIN).
El resto del sistema tiene interfaces y adaptadores temporales basados en `java.util`.
Cada companero reemplaza uno de ellos con su propia estructura.

## Primera vez en el repo

Hazlo en este orden. Son unos 15 minutos y no necesitas esperar a nadie.

**1. Acceso.** Acepta la invitacion al repo (llega a tu correo de GitHub y en [github.com/notifications](https://github.com/notifications)). Sin invitacion puedes leer el codigo, pero no subir ramas.

**2. Instala JDK 21 y Maven** (ver abajo). Comprueba:

```bash
java -version    # debe decir 21
mvn -version     # Maven 3.9 o superior
```

**3. Clona y comprueba que todo compila en tu maquina:**

```bash
git clone https://github.com/franc1sc0sv/rutasv-ped941.git
cd rutasv-ped941
mvn -B verify
```

Debe terminar en `BUILD SUCCESS`. Veras pruebas `Skipped`: es normal, son los contratos de cada rol, desactivados hasta que alguien los implemente. Si falla, copia el error completo al chat del grupo; casi siempre es la version de Java.

**4. Elige tu rol y asignate su issue:** [Ruta #1](../../issues/1), [Busqueda #2](../../issues/2), [Terminal #3](../../issues/3), [Historial #4](../../issues/4), [Paradas #5](../../issues/5). Abre la guia de tu modulo: `core/src/main/java/edu/udb/rutasv/<modulo>/LEEME.md`. Dice que archivos ya existen, que te toca crear y en que orden.

**5. Crea tu rama desde `main` actualizada.** Nunca trabajes sobre `main`: esta protegida.

```bash
git checkout main && git pull
git checkout -b feat/<rol>-<tarea>      # ejemplo: feat/paradas-tabla-hash
```

**6. Trabaja en cambios chicos.** Un commit por paso, con mensaje en ingles y prefijo (`feat:`, `fix:`, `test:`, `docs:`). Antes de subir, corre `mvn -B verify`.

**7. Sube y abre un PR chico (una tarea por PR):**

```bash
git push -u origin feat/<rol>-<tarea>
gh pr create --fill     # o abrelo desde la pagina de GitHub
```

Completa la plantilla del PR. Pide la revision a una persona de otro rol: `main` exige **1 aprobacion** y el check `build` en verde. No mezcles tu propio PR sin que alguien lo revise.

**8. Mantente al dia.** Antes de empezar cada tarea nueva: `git checkout main && git pull`.

### Que no debes hacer
- No hagas commit directo a `main`.
- No subas contrasenas, datos reales ni `data/usuarios*`.
- No cambies una interfaz de `nucleo` ni `app` sin avisar al grupo: afecta a los cinco.
- No edites el modulo de otro rol. Si algo tuyo depende de el, usa las versiones de `nucleo/temporal` mientras tanto.

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
# Ubuntu / Debian / WSL
sudo apt install openjdk-21-jdk maven

# macOS (Homebrew)
brew install openjdk@21 maven
```

En Windows lo mas simple es usar **WSL con Ubuntu** y seguir los pasos de Ubuntu dentro de WSL. Si no, instala Temurin 21 y Maven 3.9 y revisa que `java -version` diga 21.

Sin permisos de administrador: descarga el JDK 21 (Temurin) y Maven, descomprimelos en tu carpeta de usuario y agrega sus `bin/` al `PATH`.

## Compilar y probar

```bash
mvn -B verify          # compila y corre todas las pruebas
mvn -B test            # solo pruebas
```

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
