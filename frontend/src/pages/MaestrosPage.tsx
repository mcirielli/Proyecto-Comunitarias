import { useEffect, useState } from 'react'
import type { FormEvent } from 'react'
import type { Maestro } from '../types/session'
import { api, write } from '../services/http'

export function MaestrosPage({ tab }: { tab: 'productos' | 'proveedores' }) {
  const [rows, setRows] = useState<Maestro[]>([])
  const [loading, setLoading] = useState(true)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [attempt, setAttempt] = useState(0)
  const [creating, setCreating] = useState(false)
  const [search, setSearch] = useState('')
  const field = 'field'
  useEffect(() => {
    let active = true; setLoading(true); setError('')
    api('/' + tab).then(data => { if (active) setRows(data) })
      .catch((reason: Error) => { if (active) setError(reason.message) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [tab, attempt])
  async function save(event: FormEvent<HTMLFormElement>) {
    event.preventDefault(); setBusy(true); setError(''); setSuccess('')
    const form = event.currentTarget
    try {
      await write('/' + tab, JSON.stringify(Object.fromEntries(new FormData(form).entries())), true)
      setCreating(false); setSuccess('Registro guardado correctamente.'); setAttempt(value => value + 1)
    } catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  async function toggle(row: Maestro) {
    setBusy(true); setError(''); setSuccess('')
    try {
      await write('/' + tab + '/' + row.id + '/activo', JSON.stringify({ activo: !row.activo }), true, 'PATCH')
      setAttempt(value => value + 1)
    } catch (reason) { setError((reason as Error).message) }
    finally { setBusy(false) }
  }
  const filtered = rows.filter(row => (row.nombre ?? row.razonSocial ?? '').toLocaleLowerCase().includes(search.toLocaleLowerCase()))
  return <>
    <div className="view-toolbar"><p className="muted">Administrá tu catálogo de {tab}.</p><button className="action" disabled={busy} onClick={() => {setCreating(!creating); setSuccess('')}}>{creating ? 'Cerrar formulario' : tab === 'productos' ? '+ Nuevo producto' : '+ Nuevo proveedor'}</button></div>
    {error && <p role="alert" className="notice error-text">{error}</p>}
    {success && <p role="status" className="notice accent">{success}</p>}
    {creating && <section className="surface form-panel"><h2>{tab === 'productos' ? 'Nuevo producto' : 'Nuevo proveedor'}</h2><form onSubmit={save} className="form-grid">
{tab === 'productos' ? <>
            <label>Nombre<input className={field} name="nombre" maxLength={150} required /></label>
            <label>Unidad de medida<input className={field} name="unidadMedida" maxLength={30} required /></label>
            <label>Categoría<input className={field} name="categoria" maxLength={100} /></label>
            <label>Descripción<textarea className={field} name="descripcion" /></label>
          </> : <>
            <label>Razón social<input className={field} name="razonSocial" maxLength={150} required /></label>
            <label>CUIT<input className={field} name="cuit" maxLength={11} /></label>
            <label>Email<input className={field} name="email" type="email" maxLength={150} /></label>
            <label>Teléfono<input className={field} name="telefono" maxLength={30} /></label>
          </>}
          <div className="form-actions"><button className="action" disabled={busy}>{busy ? 'Guardando…' : 'Guardar registro'}</button><button type="button" className="theme-switch" disabled={busy} onClick={() => setCreating(false)}>Cancelar</button></div></form></section>}
    <section className="surface table-panel" aria-label={'Listado de ' + tab}>
      <div className="panel-heading"><div><h2>{tab === 'productos' ? 'Catálogo de productos' : 'Directorio de proveedores'}</h2><p className="muted">{loading ? 'Consultando registros…' : `${rows.length} registros en total`}</p></div><button className="theme-switch" disabled={loading || busy} onClick={() => setAttempt(value => value + 1)}>Actualizar</button></div>
      <div className="search-bar"><label className="muted" htmlFor="search-maestros">Buscar por nombre</label><input id="search-maestros" className="field" value={search} onChange={event => setSearch(event.target.value)} placeholder={tab === 'productos' ? 'Buscar un producto…' : 'Buscar un proveedor…'} /></div>
      {loading ? <p className="empty-state" role="status">Cargando registros…</p> : error ? <p className="empty-state muted">No se pudo actualizar el listado. Volvé a intentar.</p> : filtered.length === 0 ? <p className="empty-state muted">{search ? 'No hay coincidencias para tu búsqueda.' : 'Todavía no hay registros. Creá el primero para comenzar.'}</p> : <div className="table-scroll"><table><thead><tr><th>ID</th><th>{tab === 'productos' ? 'Producto' : 'Razón social'}</th><th>{tab === 'productos' ? 'Unidad' : 'Email'}</th><th>Estado</th><th className="text-right">Acciones</th></tr></thead><tbody>{filtered.map(row => <tr key={row.id}><td className="muted">#{row.id}</td><td className="font-semibold">{row.nombre ?? row.razonSocial}</td><td className="muted">{tab === 'productos' ? row.unidadMedida : row.email || '—'}</td><td><span className={'state-badge ' + (row.activo ? 'status-available' : 'status-checking')}>{row.activo ? 'Activo' : 'Inactivo'}</span></td><td className="text-right"><button className="row-action" disabled={busy} onClick={() => toggle(row)}>{row.activo ? 'Desactivar' : 'Activar'}</button></td></tr>)}</tbody></table></div>}
    </section>
  </>
}
