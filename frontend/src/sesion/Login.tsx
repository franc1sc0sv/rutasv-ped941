// Pantalla Login / Registro. Rol: Auth (ya conectado).
// Endpoints: POST /auth/login, POST /auth/registro (crea un PASAJERO).
import { useState, type FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ApiError, mensajeDe } from '../api/cliente';
import { Aviso, Boton, Campo, Entrada, Segmentado } from '../comun/ui';
import { Marca } from '../layout/Marca';
import { inicioPara, useSesion } from './SesionContext';
import './login.css';

type Modo = 'login' | 'registro';

/** Tira de linea decorativa: muestra en una mirada lo que hace la app. */
const MUESTRA = [
  { nombre: 'Terminal Occidente', min: 8 },
  { nombre: 'Redondel Masferrer', min: 8 },
  { nombre: 'Plaza Merliot', min: 2 },
  { nombre: 'Metrocentro', min: 0 },
];

export function Login() {
  const { iniciarSesion, registrar } = useSesion();
  const navegar = useNavigate();
  const ubicacion = useLocation();
  const [modo, setModo] = useState<Modo>('login');
  const [usuario, setUsuario] = useState('');
  const [contrasena, setContrasena] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [enviando, setEnviando] = useState(false);

  async function enviar(ev: FormEvent) {
    ev.preventDefault();
    setError(null);
    setEnviando(true);
    try {
      const c = { usuario: usuario.trim(), contrasena };
      const s = modo === 'login' ? await iniciarSesion(c) : await registrar(c);
      const desde = (ubicacion.state as { desde?: string } | null)?.desde;
      const puedeVolver = desde && (s.rol === 'ADMIN' || !desde.startsWith('/admin'));
      navegar(puedeVolver ? desde : inicioPara(s), { replace: true });
    } catch (e) {
      if (e instanceof ApiError && e.status === 409) setError(`${e.message}. Prueba con otro nombre de usuario.`);
      else setError(mensajeDe(e));
    } finally {
      setEnviando(false);
    }
  }

  function cambiar(m: Modo) {
    setModo(m);
    setError(null);
  }

  return (
    <div className="login">
      <aside className="login__senal" aria-hidden="true">
        <Marca tamano={36} className="marca--invertida" />
        <div className="login__muestra">
          <p className="login__lema">Del punto A al punto B, por el camino más corto.</p>
          <div className="login__tira">
            <p className="login__tiempo">
              <span className="cifra">18</span> min
            </p>
            <ol className="login__estaciones">
              {MUESTRA.map((e, i) => (
                <li key={e.nombre} className={i === 0 || i === MUESTRA.length - 1 ? 'es-extremo' : ''}>
                  <span className="login__punto" />
                  <span>{e.nombre}</span>
                  {e.min > 0 && <span className="login__seg">{e.min} min</span>}
                </li>
              ))}
            </ol>
          </div>
        </div>
        <p className="login__credito">PED941 · Universidad Don Bosco</p>
      </aside>

      <main className="login__panel">
        <div className="login__formulario">
          <Marca tamano={32} className="login__marca-movil" />
          <div className="login__titulos">
            <h1>{modo === 'login' ? 'Inicia sesión' : 'Crea tu cuenta'}</h1>
            <p className="texto-suave">
              {modo === 'login' ? 'Planifica tus viajes en transporte público por San Salvador.' : 'Las cuentas nuevas son de pasajero.'}
            </p>
          </div>

          <Segmentado
            nombre="modo-acceso"
            etiqueta="Modo de acceso"
            etiquetaOculta
            valor={modo}
            alCambiar={cambiar}
            opciones={[
              { valor: 'login', texto: 'Iniciar sesión' },
              { valor: 'registro', texto: 'Crear cuenta' },
            ]}
          />

          <form onSubmit={enviar} className="pila" noValidate>
            <Campo etiqueta="Usuario" ayuda={modo === 'registro' ? 'De 3 a 32 caracteres: letras, números, _ . -' : undefined}>
              <Entrada name="usuario" autoComplete="username" value={usuario} onChange={(e) => setUsuario(e.target.value)} required autoFocus />
            </Campo>
            <Campo etiqueta="Contraseña" ayuda={modo === 'registro' ? 'Mínimo 8 caracteres.' : undefined}>
              <Entrada
                name="contrasena"
                type="password"
                autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
                value={contrasena}
                onChange={(e) => setContrasena(e.target.value)}
                required
              />
            </Campo>

            {error && <Aviso tono="error">{error}</Aviso>}

            <Boton variante="primario" tamano="lg" ancho type="submit" cargando={enviando}>
              {modo === 'login' ? 'Entrar' : 'Crear cuenta de pasajero'}
            </Boton>
          </form>

          <p className="login__pie texto-suave">
            {modo === 'login'
              ? '¿Primera vez? Elige “Crear cuenta”.'
              : 'Las cuentas de administración las crea el sistema (RN-1).'}
          </p>
        </div>
      </main>
    </div>
  );
}
