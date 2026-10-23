# RutaSV · Sistema de diseño "Andén"

## Dirección

RutaSV se ve como la señalética de una parada de buses: placas azules (azul señal RAL 5017), tipografía de carretera (Overpass, derivada de Highway Gothic), líneas gruesas con puntos de estación y amarillo de señal (RAL 1023) solo para lo exprés y para lo que sigue "en obra". La información manda: el tiempo de viaje es la cifra más grande de la app, y el itinerario se lee como el diagrama de línea que va arriba de la puerta del bus. El lado pasajero es angosto, tranquilo y pensado para el teléfono. El lado admin es una herramienta de operación densa: tablas, formularios y un menú lateral oscuro dibujado como una línea con sus paradas.

Lo memorable está en un solo lugar (tablero de tiempo + diagrama de línea). Todo lo demás es callado: bordes de 1 px, escalones de tono y nada de sombras en reposo.

## Tokens (`src/styles/tokens.css`)

Es la única fuente de valores. Ninguna pantalla escribe colores, tamaños de letra ni espacios a mano.

### Color

| Rol | Token | Claro | Oscuro | Uso |
|---|---|---|---|---|
| Lienzo | `--fondo` | #f1f4f6 | #0b1016 | fondo de la app |
| Superficie | `--superficie` | #ffffff | #121922 | paneles, campos |
| Escalón | `--superficie-2` | #e7ecf0 | #1a232e | cabeceras de tabla, filas |
| Borde | `--borde` / `--borde-fuerte` | #d2d9e0 / #75828f | #283441 / #6b7a89 | divisiones / borde de campo (3:1) |
| Tinta | `--texto` / `--texto-suave` | #0d1824 / #4a5664 | #e6ebf0 / #a0adba | texto / notas |
| Panel de señal | `--tinta`, `--sobre-tinta` | #0d1824 | #070b10 | menú admin, boleto de salida |
| Línea principal | `--primario` | #0a5296 | #62a3ec | acción primaria, ruta, foco |
| Exprés | `--expres` + `--sobre-expres` | #f5b700 | #f2be3a | insignia EXPRES (rombo), banda "en obra" |
| Normal | `--normal` + `--sobre-normal` | #42505e | #3a4856 | insignia NORMAL (círculo) |
| Éxito / peligro / aviso / info | `--exito`, `--peligro`, `--aviso`, `--info` (+ `-suave`) | | | avisos e insignias de estado |
| Mapa | `--mapa-fondo`, `--mapa-reticula`, `--mapa-tramo`, `--mapa-estacion`, `--mapa-ruta` | | | MapaRed |

Todos los pares texto/fondo pasan AA (4.5:1) y los bordes de controles pasan 3:1 en ambos temas.

### Tipografía

- **Overpass** (`--fuente-senal`): títulos, botones, etiquetas, cifras, códigos de estación.
- **Atkinson Hyperlegible** (`--fuente-texto`): texto corrido, ayudas y tablas. Su cero lleva barra; es intencional, porque así no se confunde con la O.
- Escala de tercera mayor (1.25): `--fs-1` 12 · `--fs-2` 14 · `--fs-3` 16 · `--fs-4` 20 · `--fs-5` 25 · `--fs-6` 32 · `--fs-7` 48 · `--fs-8` 72.
- Las cifras usan `font-variant-numeric: tabular-nums` (clase `.cifra` o `.num`).
- Todo en minúscula de oración. No se usan mayúsculas sostenidas ni monoespaciadas.

### Espacio, radio y elevación

- Espacio: `--esp-1`…`--esp-8` = 4 · 8 · 12 · 16 · 24 · 32 · 48 · 64 px.
- Radio: `--radio-1` 2 px (insignias) · `--radio-2` 4 px (botones, campos) · `--radio-3` 8 px (paneles) · `--radio-redondo` solo para puntos y avatares.
- Elevación: `--elev-flotante` solo para lo que flota (lista del combobox, cajón del menú). En reposo no hay sombras.
- Controles: `--control-alto` mide 40 px y sube a 44 px en pantallas táctiles.

### Movimiento

| Token | Valor | Uso |
|---|---|---|
| `--dur-presion` | 120 ms | `scale(0.97)` al presionar |
| `--dur-corta` | 160 ms | hover, lista de sugerencias, tramos del itinerario |
| `--dur-media` | 220 ms | avisos, fila que sale del despacho, pasajero atendido |
| `--dur-larga` | 280 ms | trazo de la ruta en el mapa, cajón del menú |
| `--curva-salida` | cubic-bezier(0.23, 1, 0.32, 1) | entradas y respuestas |
| `--curva-movimiento` | cubic-bezier(0.77, 0, 0.175, 1) | algo que se mueve en pantalla |
| `--curva-cajon` | cubic-bezier(0.32, 0.72, 0, 1) | cajón lateral |

Solo se animan `transform`, `opacity` y el trazo SVG. Con `prefers-reduced-motion` todo queda en 1 ms.

## Componentes (`src/comun/ui`)

| Componente | Variantes y estados |
|---|---|
| `Boton`, `BotonEnlace` | primario · secundario · fantasma · peligro; sm · md · lg; `icono`, `iconoFinal`, `soloIcono`, `ancho`; hover, presionado, foco, deshabilitado, `cargando` |
| `Campo` + `Entrada` / `Selector` | etiqueta, `ayuda`, `error` (conecta `aria-describedby` y `aria-invalid`), deshabilitado |
| `Casilla`, `Segmentado` | checkbox propio; grupo de radios con aspecto de botones (flechas nativas) |
| `Combobox` | sugerencias asíncronas, teclado completo, estado sin coincidencias y error de red |
| `Tabla`, `FilaVacia` | región desplazable propia, caption oculto, `.num`, filas `es-resaltada`, `es-tenue`, `es-saliendo` |
| `Tarjeta` | título h2/h3, `detalle`, `acciones`, `sinRelleno`, `acento` (primario, exprés, normal) |
| `Insignia`, `InsigniaTipo`, `CodigoEstacion` | tonos semánticos; EXPRES rombo amarillo y NORMAL círculo pizarra; placa del código |
| `Aviso` | error (`role="alert"`) · aviso · éxito · info; `titulo`, `accion` |
| `EstadoVacio` | qué falta y cuál es el siguiente paso; `compacto` |
| `Cargando`, `Esqueleto` | indicador con texto; bloques con la forma del contenido |
| `Pendiente` | tarjeta "en obra" para respuestas 501 y la variante `enLinea` |
| `Pagina` | h1 único, `figura`, `descripcion`, `acciones`, `angosta` |
| `Icono` | set SVG local (trazo 2 px, 24×24) |

Fuera de `ui/`: `MapaRed` (diagrama SVG con etiquetas que no chocan, ruta trazada, leyenda y lista de códigos en pantallas angostas) y `layout/Marca`.

## Layout

- **Admin:** menú lateral `--tinta` de 248 px. Debajo de 960 px se vuelve un cajón con foco atrapado y Escape para cerrar. El contenido llega hasta 1200 px.
- **Pasajero:** contenido de 760 px como máximo. En teléfono hay barra inferior de 3 pestañas; desde 960 px, las pestañas pasan al encabezado.
- Las rejillas (`.rejilla-2`, `--invertida`, `--pareja`) responden al ancho del **contenido** (`@container`), no al de la ventana.
- Hay un margen exterior por pantalla y los paneles se separan con 24 o 32 px.

## Hacer / no hacer

| Hacer | No hacer |
|---|---|
| Usar tokens y componentes de `comun/ui` | Escribir colores hex, `rgb()` o px de espacio en una pantalla |
| Una sola acción primaria por zona | Dos botones primarios juntos |
| Mostrar el error junto a lo que lo causó (`Campo error` o `Aviso`) | Mensajes vagos ("Error") o `alert()` |
| Poner cifras en `.cifra` (Overpass, tabulares) | Cifras en la fuente de texto dentro de tablas |
| Usar amarillo solo para EXPRES, "en obra" y la línea de muestra del panel oscuro del login (ahí el azul no contrasta) | Usar amarillo como acento decorativo |
| Usar iconos de `Icono` | Usar emoji como iconos |
| Animar transform y opacity en menos de 300 ms | Animar alto, ancho o márgenes; usar `scale(0)` |
| Separar superficies con escalón de tono o bordes de 1 px | Sombras en reposo, gradientes, vidrio esmerilado |
