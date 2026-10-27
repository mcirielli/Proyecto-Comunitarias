import type { FormEvent } from 'react'
import { ThemeToggle } from '../components/ThemeToggle'
export function LoginPage({ busy, error, onLogin }: { busy: boolean; error: string; onLogin: (event: FormEvent<HTMLFormElement>) => void }) {
  return <div className="login-shell"><div className="login-theme"><ThemeToggle /></div>
    <section className="login-intro"><span className="brand-mark">C</span><p className="eyebrow">GESTIÓN MAYORISTA</p><h1>Todo en un<br />mismo lugar.</h1><p className="muted">Productos, proveedores y el trabajo de tu comunidad.<br />Una operación más simple, paso a paso.</p></section>
    <main className="login-card surface"><p className="eyebrow">COMUNITARIAS</p><h2>Bienvenido de nuevo</h2><p className="muted">Ingresá para acceder a tu espacio de trabajo.</p>
      {error && <p role="alert" className="notice error-text">{error}</p>}
      <form onSubmit={onLogin} className="mt-8"><label>Email<input className="field" name="email" type="email" autoComplete="username" required /></label><label>Contraseña<input className="field" name="password" type="password" autoComplete="current-password" required /></label><button className="action w-full" disabled={busy}>{busy ? 'Ingresando…' : 'Ingresar'}</button></form>
      <p className="muted login-footnote">Proyecto Comunitarias · UADE</p>
    </main></div>
}
