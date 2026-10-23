// Pantalla Login / Registro. Rol: Auth (ya conectado).
// Endpoints: POST /auth/login, POST /auth/registro (crea un PASAJERO).
import { useState, type FormEvent } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { ApiError, mensajeDe } from '../api/cliente';
import { MensajeError } from '../comun/MensajeError';
import { inicioPara, useSesion } from './SesionContext';
import './login.css';

type Modo = 'login' | 'registro';

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
    <main className="login">
      <section className="login__tarjeta tarjeta" aria-labelledby="login-titulo">
        <div className="login__marca">
          <span className="logo" aria-hidden="true">
            <svg viewBox="0 0 32 32" width="40" height="40">
              <rect width="32" height="32" rx="8" fill="currentColor" />
              <path d="M8 22 L14 12 L20 18 L25 9" fill="none" stroke="#fff" strokeWidth="3" strokeLinecap="round" strokeLinejoin="round" />
              <circle cx="8" cy="22" r="2.6" fill="#fff" />
              <circle cx="25" cy="9" r="2.6" fill="#fff" />
            </svg>
          </span>
          <div>
            <h1 id="login-titulo">RutaSV</h1>
            <p className="texto-suave">Planificador de rutas de transporte público</p>
          </div>
        </div>

        <div className="segmentado" role="tablist" aria-label="Modo de acceso">
          <button type="button" role="tab" aria-selected={modo === 'login'} onClick={() => cambiar('login')}>
            Iniciar sesión
          </button>
          <button type="button" role="tab" aria-selected={modo === 'registro'} onClick={() => cambiar('registro')}>
            Crear cuenta
          </button>
        </div>

        <form onSubmit={enviar} className="formulario" noValidate>
          <label className="campo">
            <span>Usuario</span>
            <input
              name="usuario"
              autoComplete="username"
              value={usuario}
              onChange={(e) => setUsuario(e.target.value)}
              required
              autoFocus
            />
            {modo === 'registro' && <small className="texto-suave">3 a 32 caracteres: letras, números, _ . -</small>}
          </label>
          <label className="campo">
            <span>Contraseña</span>
            <input
              name="contrasena"
              type="password"
              autoComplete={modo === 'login' ? 'current-password' : 'new-password'}
              value={contrasena}
              onChange={(e) => setContrasena(e.target.value)}
              required
            />
            {modo === 'registro' && <small className="texto-suave">Mínimo 8 caracteres.</small>}
          </label>

          {error && <MensajeError mensaje={error} />}

          <button className="boton boton--primario boton--ancho" type="submit" disabled={enviando}>
            {enviando ? 'Enviando…' : modo === 'login' ? 'Entrar' : 'Crear cuenta de pasajero'}
          </button>
        </form>

        <p className="login__pie texto-suave">
          {modo === 'login'
            ? '¿No tienes cuenta? Elige “Crear cuenta”. Las cuentas nuevas son de pasajero.'
            : 'Las cuentas de administrador las crea el sistema (RN-1).'}
        </p>
      </section>
      <p className="login__credito texto-suave">PED941 · Universidad Don Bosco</p>
    </main>
  );
}
