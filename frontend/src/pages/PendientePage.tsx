export function PendientePage({ title }: { title: string }) {
  return <section className="surface empty-state"><span className="state-badge status-checking">Próximamente</span><h2 className="mt-5">{title}</h2><p className="muted mt-3">Este módulo todavía no está implementado. Estará disponible en un próximo incremento.</p></section>
}
