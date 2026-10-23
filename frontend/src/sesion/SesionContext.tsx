// Sesion del usuario (token + usuario + rol). Rol: Auth (ya conectado).
// Endpoints: POST /auth/login, POST /auth/registro, POST /auth/logout, GET /auth/yo.
import { createContext, useCallback, useContext, useEffect, useMemo, useState, type ReactNode } from 'react';
import { alPerderSesion, api, configurarToken } from '../api/cliente';
import type { Credenciales, Sesion } from '../api/tipos';

const CLAVE = 'rutasv.sesion';

function leerGuardada(): Sesion | null {
  try {
    const crudo = localStorage.getItem(CLAVE);
    if (!crudo) return null;
    const s = JSON.parse(crudo) as Partial<Sesion>;
    if (typeof s.token === 'string' && typeof s.usuario === 'string' && (s.rol === 'ADMIN' || s.rol === 'PASAJERO')) {
      return s as Sesion;
    }
  } catch {
    // localStorage bloqueado o dato corrupto: se trata como sin sesion.
  }
  return null;
}

function guardar(s: Sesion | null): void {
  try {
    if (s) localStorage.setItem(CLAVE, JSON.stringify(s));
    else localStorage.removeItem(CLAVE);
  } catch {
    // Sin almacenamiento la sesion dura solo mientras la pestana este abierta.
  }
}

interface ValorSesion {
  sesion: Sesion | null;
  esAdmin: boolean;
  /** true mientras se valida con GET /auth/yo una sesion guardada. */
  verificando: boolean;
  iniciarSesion: (c: Credenciales) => Promise<Sesion>;
  registrar: (c: Credenciales) => Promise<Sesion>;
  cerrarSesion: () => Promise<void>;
}

const Contexto = createContext<ValorSesion | null>(null);

export function SesionProvider({ children }: { children: ReactNode }) {
  const [sesion, setSesion] = useState<Sesion | null>(() => {
    const s = leerGuardada();
    configurarToken(s?.token ?? null);
    return s;
  });
  const [verificando, setVerificando] = useState<boolean>(() => sesion !== null);

  const aplicar = useCallback((s: Sesion | null) => {
    configurarToken(s?.token ?? null);
    guardar(s);
    setSesion(s);
  }, []);

  // Un 401 con sesion abierta (p. ej. el backend se reinicio) devuelve al login.
  useEffect(() => {
    alPerderSesion(() => aplicar(null));
    return () => alPerderSesion(null);
  }, [aplicar]);

  // Al recargar la pagina, confirma que el token guardado sigue vivo en el backend.
  useEffect(() => {
    if (!verificando) return;
    let vivo = true;
    api
      .get<Sesion>('/auth/yo')
      .then((s) => vivo && aplicar(s))
      .catch(() => vivo && aplicar(null))
      .finally(() => vivo && setVerificando(false));
    return () => {
      vivo = false;
    };
    // Solo al montar.
  }, []);

  const iniciarSesion = useCallback(
    async (c: Credenciales) => {
      const s = await api.post<Sesion>('/auth/login', c);
      aplicar(s);
      return s;
    },
    [aplicar],
  );

  const registrar = useCallback(
    async (c: Credenciales) => {
      const s = await api.post<Sesion>('/auth/registro', c);
      aplicar(s);
      return s;
    },
    [aplicar],
  );

  const cerrarSesion = useCallback(async () => {
    try {
      await api.post('/auth/logout');
    } catch {
      // Aunque falle en el servidor, la sesion local se cierra igual.
    }
    aplicar(null);
  }, [aplicar]);

  const valor = useMemo<ValorSesion>(
    () => ({ sesion, esAdmin: sesion?.rol === 'ADMIN', verificando, iniciarSesion, registrar, cerrarSesion }),
    [sesion, verificando, iniciarSesion, registrar, cerrarSesion],
  );

  return <Contexto.Provider value={valor}>{children}</Contexto.Provider>;
}

export function useSesion(): ValorSesion {
  const v = useContext(Contexto);
  if (!v) throw new Error('useSesion debe usarse dentro de <SesionProvider>');
  return v;
}

/** Pantalla de inicio segun el rol (pasajero -> Buscar ruta, admin -> Red vial). */
export function inicioPara(s: Sesion | null): string {
  if (!s) return '/login';
  return s.rol === 'ADMIN' ? '/admin/red' : '/buscar';
}
