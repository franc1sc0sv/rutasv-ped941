// Mapa esquematico de la red vial en SVG (sin librerias), con estilo de diagrama de transito.
// - El viewBox se ajusta a las estaciones (y a sus nombres) + margen.
// - El eje y se invierte: y mayor queda ARRIBA (como en un plano).
// - Cada par de tramos ida/vuelta se dibuja una sola vez.
// - `ruta` (codigos en orden) se dibuja como linea de transito: gruesa, con halo, y se "traza" al aparecer.
// - Textos y trazos tienen tamano fijo en pixeles: se mide el ancho real del mapa
//   (ResizeObserver) y los nombres se ubican evitando choques con otros nombres y tramos.
// - En pantallas angostas los nombres pasan a codigos y aparece la lista de codigos + leyenda.
import { useEffect, useId, useMemo, useRef, useState } from 'react';
import type { Estacion, Tramo } from '../api/tipos';
import './mapa.css';

interface Props {
  estaciones: Estacion[];
  tramos: Tramo[];
  /** Codigos de estacion en orden de origen a destino. */
  ruta?: string[];
  /** Muestra los minutos de cada tramo en su punto medio. */
  mostrarMinutos?: boolean;
  /** Atenua toda la red que no es parte de la ruta. */
  atenuarRed?: boolean;
  /** Etiqueta accesible del SVG. */
  titulo?: string;
  className?: string;
}

// Tamanos en pixeles de pantalla.
const PX = { nombre: 12, minutos: 11, radio: 5.5, radioExtremo: 8, tramo: 3, ruta: 6, halo: 4, margen: 12, reticula: 24 };

interface Linea {
  a: Estacion;
  b: Estacion;
  minutos: number;
  /** Si ida y vuelta tienen minutos distintos, se muestran ambos ("8/9"). */
  minutosVuelta?: number;
  enRuta: boolean;
}

interface Caja {
  x: number;
  y: number;
  w: number;
  h: number;
}

interface Etiqueta {
  x: number;
  y: number;
  anchor: 'start' | 'middle' | 'end';
}

export function MapaRed({ estaciones, tramos, ruta = [], mostrarMinutos = true, atenuarRed = false, titulo, className }: Props) {
  const contenedor = useRef<HTMLDivElement>(null);
  const [anchoPx, setAnchoPx] = useState(640);
  const idPatron = useId().replace(/:/g, '');

  useEffect(() => {
    const el = contenedor.current;
    if (!el || typeof ResizeObserver === 'undefined') return;
    const obs = new ResizeObserver(([e]) => {
      const w = Math.round(e.contentRect.width);
      if (w > 0) setAnchoPx(w);
    });
    obs.observe(el);
    return () => obs.disconnect();
  }, []);

  const modelo = useMemo(
    () => construir(estaciones, tramos, ruta, anchoPx, mostrarMinutos),
    [estaciones, tramos, ruta, anchoPx, mostrarMinutos],
  );

  if (estaciones.length === 0) {
    return <p className="texto-suave">No hay estaciones para dibujar.</p>;
  }

  const { vb, k, lineas, porCodigo, etiquetas, compacto } = modelo;
  const enRuta = new Set(ruta);
  const hayRuta = ruta.length > 1;
  const origen = ruta[0];
  const destino = ruta[ruta.length - 1];
  const puntosRuta = ruta
    .map((c) => porCodigo.get(c))
    .filter((e): e is Estacion => !!e)
    .map((e) => `${e.x},${-e.y}`)
    .join(' ');
  const etiquetaTitulo = titulo ?? 'Mapa de la red vial';
  const hayInactivas = estaciones.some((e) => !e.activa);
  const visibles = estaciones.filter((e) => !atenuarRed || !hayRuta || enRuta.has(e.codigo));
  // La lista de codigos sigue el orden de la ruta cuando la hay.
  const listaCodigos = atenuarRed && hayRuta ? ruta.map((c) => porCodigo.get(c)).filter((e): e is Estacion => !!e) : visibles;

  return (
    <figure ref={contenedor} className={`mapa-contenedor ${compacto ? 'es-compacto' : ''} ${className ?? ''}`}>
      <svg
        className={`mapa ${atenuarRed && hayRuta ? 'mapa--atenuado' : ''}`}
        viewBox={`${vb.x} ${vb.y} ${vb.w} ${vb.h}`}
        role="img"
        aria-label={etiquetaTitulo}
        preserveAspectRatio="xMidYMid meet"
      >
        <title>{etiquetaTitulo}</title>
        <defs>
          <pattern id={idPatron} width={PX.reticula * k} height={PX.reticula * k} patternUnits="userSpaceOnUse" x={vb.x} y={vb.y}>
            <path className="mapa__reticula" d={`M ${PX.reticula * k} 0 L 0 0 0 ${PX.reticula * k}`} fill="none" strokeWidth={k} />
          </pattern>
        </defs>
        <rect className="mapa__fondo" x={vb.x} y={vb.y} width={vb.w} height={vb.h} />
        <rect x={vb.x} y={vb.y} width={vb.w} height={vb.h} fill={`url(#${idPatron})`} />

        <g className="mapa__tramos">
          {lineas.map((l) => {
            const inactivo = !l.a.activa || !l.b.activa;
            return (
              <line
                key={`${l.a.codigo}-${l.b.codigo}`}
                className={`mapa__tramo ${l.enRuta ? 'es-ruta' : ''} ${inactivo ? 'es-inactivo' : ''}`}
                x1={l.a.x}
                y1={-l.a.y}
                x2={l.b.x}
                y2={-l.b.y}
                strokeWidth={PX.tramo * k}
                strokeDasharray={inactivo ? `${5 * k} ${4 * k}` : undefined}
              />
            );
          })}
        </g>

        {hayRuta && (
          <g className="mapa__linea-ruta">
            <polyline className="mapa__ruta-halo" points={puntosRuta} strokeWidth={(PX.ruta + PX.halo * 2) * k} fill="none" />
            <polyline className="mapa__ruta" points={puntosRuta} strokeWidth={PX.ruta * k} fill="none" pathLength={1} />
          </g>
        )}

        {mostrarMinutos && (
          <g className="mapa__minutos">
            {lineas.map((l) => (
              <text
                key={`m-${l.a.codigo}-${l.b.codigo}`}
                className={`mapa__min ${l.enRuta ? 'es-ruta' : ''}`}
                x={(l.a.x + l.b.x) / 2}
                y={-(l.a.y + l.b.y) / 2}
                fontSize={PX.minutos * k}
                strokeWidth={3.5 * k}
                textAnchor="middle"
                dominantBaseline="central"
              >
                {textoMinutos(l)}
              </text>
            ))}
          </g>
        )}

        <g className="mapa__estaciones">
          {estaciones.map((e) => {
            const extremo = hayRuta && (e.codigo === origen || e.codigo === destino);
            const et = etiquetas.get(e.codigo)!;
            const clases = ['mapa__estacion', enRuta.has(e.codigo) && 'es-ruta', extremo && 'es-extremo', !e.activa && 'es-inactiva']
              .filter(Boolean)
              .join(' ');
            return (
              <g key={e.codigo} className={clases}>
                <title>
                  {e.codigo} · {e.nombre} ({e.zona}){e.activa ? '' : ' · inactiva'}
                </title>
                <circle
                  cx={e.x}
                  cy={-e.y}
                  r={(extremo ? PX.radioExtremo : PX.radio) * k}
                  strokeWidth={(extremo ? 3.5 : 2.5) * k}
                  strokeDasharray={e.activa ? undefined : `${2.5 * k} ${2 * k}`}
                />
                {extremo && <circle className="mapa__centro" cx={e.x} cy={-e.y} r={2.5 * k} />}
                <text
                  className="mapa__nombre"
                  x={et.x}
                  y={et.y}
                  textAnchor={et.anchor}
                  dominantBaseline="central"
                  fontSize={PX.nombre * k}
                  strokeWidth={3.5 * k}
                >
                  {compacto ? e.codigo : e.nombre}
                </text>
              </g>
            );
          })}
        </g>
      </svg>

      <figcaption className="mapa__pie">
        <ul className="mapa__leyenda" aria-label="Leyenda del mapa">
          {hayRuta && (
            <li>
              <span className="leyenda__ruta" aria-hidden="true" /> Tu ruta
            </li>
          )}
          <li>
            <span className="leyenda__tramo" aria-hidden="true" /> Tramo{mostrarMinutos ? ' (minutos)' : ''}
          </li>
          <li>
            <span className="leyenda__estacion" aria-hidden="true" /> Estación
          </li>
          {hayInactivas && (
            <li>
              <span className="leyenda__estacion leyenda__estacion--inactiva" aria-hidden="true" /> Inactiva
            </li>
          )}
        </ul>
        {compacto && (
          <ul className="mapa__codigos" aria-label="Códigos de estación">
            {listaCodigos.map((e) => (
              <li key={e.codigo} className={e.activa ? '' : 'es-inactiva'}>
                <b>{e.codigo}</b> {e.nombre}
              </li>
            ))}
          </ul>
        )}
      </figcaption>
    </figure>
  );
}

function textoMinutos(l: Linea): string {
  return l.minutosVuelta !== undefined ? `${l.minutos}/${l.minutosVuelta}′` : `${l.minutos}′`;
}

/** Ancho aproximado de un texto (sans-serif) en pixeles. */
function anchoTexto(t: string, fontPx: number): number {
  return t.length * fontPx * 0.56;
}

function construir(estaciones: Estacion[], tramos: Tramo[], ruta: string[], anchoPx: number, conMinutos: boolean) {
  const porCodigo = new Map(estaciones.map((e) => [e.codigo, e]));

  // Pares de la ruta (sin direccion) para marcar tramos resaltados.
  const paresRuta = new Set<string>();
  for (let i = 1; i < ruta.length; i++) paresRuta.add(clave(ruta[i - 1], ruta[i]));

  // Ida y vuelta se dibujan una sola vez.
  const mapaLineas = new Map<string, Linea>();
  for (const t of tramos) {
    const a = porCodigo.get(t.origen);
    const b = porCodigo.get(t.destino);
    if (!a || !b) continue;
    const kk = clave(t.origen, t.destino);
    const existente = mapaLineas.get(kk);
    if (!existente) mapaLineas.set(kk, { a, b, minutos: t.minutos, enRuta: paresRuta.has(kk) });
    else if (existente.a.codigo !== t.origen && existente.minutos !== t.minutos) existente.minutosVuelta = t.minutos;
  }
  const lineas = [...mapaLineas.values()];

  if (estaciones.length === 0) {
    return { vb: { x: 0, y: 0, w: 100, h: 100 }, k: 1, lineas, porCodigo, etiquetas: new Map<string, Etiqueta>(), compacto: false };
  }

  // Limites de las estaciones (en coordenadas SVG: y invertida).
  const xs = estaciones.map((e) => e.x);
  const ys = estaciones.map((e) => -e.y);
  const datos: Caja = { x: Math.min(...xs), y: Math.min(...ys), w: Math.max(...xs) - Math.min(...xs) || 1, h: Math.max(...ys) - Math.min(...ys) || 1 };
  const ancho = Math.max(anchoPx, 200);
  // Modo compacto: si la red queda a menos de 6 px por unidad, los nombres no caben y se usan codigos.
  const compacto = ancho / datos.w < 6;
  const texto = (e: Estacion) => (compacto ? e.codigo : e.nombre);

  // Se busca la escala que deja espacio (en px) para lo que los nombres sobresalen de la red.
  // El desborde en px casi no depende de la escala, asi que converge en pocas vueltas.
  let desbordeIzq = PX.margen + PX.radioExtremo;
  let desbordeDer = PX.margen + PX.radioExtremo;
  let etiquetas = new Map<string, Etiqueta>();
  let cajas: Caja[] = [];
  let k = 1;
  for (let vuelta = 0; vuelta < 4; vuelta++) {
    const util = Math.max(ancho - desbordeIzq - desbordeDer, ancho * 0.45);
    k = datos.w / util; // unidades del mapa por pixel
    const res = ubicarEtiquetas(estaciones, lineas, k, conMinutos, texto);
    etiquetas = res.etiquetas;
    cajas = res.cajas;
    const minX = Math.min(datos.x - PX.radioExtremo * k, ...cajas.map((c) => c.x));
    const maxX = Math.max(datos.x + datos.w + PX.radioExtremo * k, ...cajas.map((c) => c.x + c.w));
    desbordeIzq = (datos.x - minX) / k + PX.margen;
    desbordeDer = (maxX - datos.x - datos.w) / k + PX.margen;
  }
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity;
  const incluir = (c: Caja) => {
    minX = Math.min(minX, c.x);
    minY = Math.min(minY, c.y);
    maxX = Math.max(maxX, c.x + c.w);
    maxY = Math.max(maxY, c.y + c.h);
  };
  for (const e of estaciones) incluir({ x: e.x - PX.radioExtremo * k, y: -e.y - PX.radioExtremo * k, w: 2 * PX.radioExtremo * k, h: 2 * PX.radioExtremo * k });
  for (const c of cajas) incluir(c);
  const m = PX.margen * k;
  const vb: Caja = { x: minX - m, y: minY - m, w: maxX - minX + 2 * m, h: maxY - minY + 2 * m };
  k = vb.w / ancho;

  return { vb, k, lineas, porCodigo, etiquetas, compacto };
}

/** Ubica cada nombre en la posicion candidata con menos choques (nombres, tramos, estaciones, minutos). */
function ubicarEtiquetas(estaciones: Estacion[], lineas: Linea[], k: number, conMinutos: boolean, texto: (e: Estacion) => string) {
  const fh = PX.nombre * k;
  const sep = (PX.radio + 4) * k;
  const minutos: Caja[] = conMinutos
    ? lineas.map((l) => {
        const w = anchoTexto(textoMinutos(l), PX.minutos) * k;
        const h = PX.minutos * k;
        return { x: (l.a.x + l.b.x) / 2 - w / 2, y: -(l.a.y + l.b.y) / 2 - h / 2, w, h };
      })
    : [];
  const colocadas: Caja[] = [];
  const etiquetas = new Map<string, Etiqueta>();

  // Primero las estaciones con mas vecinos: son las de zonas densas.
  const grado = new Map<string, number>();
  for (const l of lineas) {
    grado.set(l.a.codigo, (grado.get(l.a.codigo) ?? 0) + 1);
    grado.set(l.b.codigo, (grado.get(l.b.codigo) ?? 0) + 1);
  }
  const orden = [...estaciones].sort((a, b) => (grado.get(b.codigo) ?? 0) - (grado.get(a.codigo) ?? 0));

  for (const e of orden) {
    const cx = e.x;
    const cy = -e.y;
    const w = anchoTexto(texto(e), PX.nombre) * k;
    const candidatos: { et: Etiqueta; caja: Caja; costoBase: number }[] = [
      { et: { x: cx, y: cy + sep + fh / 2, anchor: 'middle' }, caja: { x: cx - w / 2, y: cy + sep, w, h: fh }, costoBase: 0 },
      { et: { x: cx, y: cy - sep - fh / 2, anchor: 'middle' }, caja: { x: cx - w / 2, y: cy - sep - fh, w, h: fh }, costoBase: 0.1 },
      { et: { x: cx + sep, y: cy, anchor: 'start' }, caja: { x: cx + sep, y: cy - fh / 2, w, h: fh }, costoBase: 0.2 },
      { et: { x: cx - sep, y: cy, anchor: 'end' }, caja: { x: cx - sep - w, y: cy - fh / 2, w, h: fh }, costoBase: 0.2 },
      { et: { x: cx + sep * 0.7, y: cy + sep * 0.7 + fh / 2, anchor: 'start' }, caja: { x: cx + sep * 0.7, y: cy + sep * 0.7, w, h: fh }, costoBase: 0.3 },
      { et: { x: cx - sep * 0.7, y: cy + sep * 0.7 + fh / 2, anchor: 'end' }, caja: { x: cx - sep * 0.7 - w, y: cy + sep * 0.7, w, h: fh }, costoBase: 0.3 },
      { et: { x: cx + sep * 0.7, y: cy - sep * 0.7 - fh / 2, anchor: 'start' }, caja: { x: cx + sep * 0.7, y: cy - sep * 0.7 - fh, w, h: fh }, costoBase: 0.3 },
      { et: { x: cx - sep * 0.7, y: cy - sep * 0.7 - fh / 2, anchor: 'end' }, caja: { x: cx - sep * 0.7 - w, y: cy - sep * 0.7 - fh, w, h: fh }, costoBase: 0.3 },
    ];

    let mejor = candidatos[0];
    let mejorCosto = Infinity;
    for (const c of candidatos) {
      let costo = c.costoBase;
      for (const o of colocadas) costo += 100 * solape(c.caja, o);
      for (const o of estaciones) if (o !== e && puntoEnCaja(o.x, -o.y, c.caja, PX.radio * k)) costo += 60;
      for (const l of lineas) if (segmentoCruzaCaja(l.a.x, -l.a.y, l.b.x, -l.b.y, c.caja)) costo += 8;
      for (const m of minutos) costo += 30 * solape(c.caja, m);
      if (costo < mejorCosto) {
        mejorCosto = costo;
        mejor = c;
      }
    }
    colocadas.push(mejor.caja);
    etiquetas.set(e.codigo, mejor.et);
  }
  return { etiquetas, cajas: colocadas };
}

/** Fraccion (0..1) del area de `a` que se solapa con `b`. */
function solape(a: Caja, b: Caja): number {
  const w = Math.min(a.x + a.w, b.x + b.w) - Math.max(a.x, b.x);
  const h = Math.min(a.y + a.h, b.y + b.h) - Math.max(a.y, b.y);
  return w > 0 && h > 0 ? (w * h) / (a.w * a.h) : 0;
}

function puntoEnCaja(x: number, y: number, c: Caja, holgura: number): boolean {
  return x >= c.x - holgura && x <= c.x + c.w + holgura && y >= c.y - holgura && y <= c.y + c.h + holgura;
}

function segmentoCruzaCaja(x1: number, y1: number, x2: number, y2: number, c: Caja): boolean {
  // Muestreo simple a lo largo del segmento (suficiente para decidir donde poner un nombre).
  for (let i = 1; i < 20; i++) {
    const t = i / 20;
    if (puntoEnCaja(x1 + (x2 - x1) * t, y1 + (y2 - y1) * t, c, 0)) return true;
  }
  return false;
}

function clave(a: string, b: string): string {
  return a < b ? `${a}|${b}` : `${b}|${a}`;
}
