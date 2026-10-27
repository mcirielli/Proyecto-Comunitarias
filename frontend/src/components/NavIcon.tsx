type IconName = 'resumen' | 'productos' | 'proveedores' | 'oferta' | 'pedidos' | 'compras' | 'reportes'
const paths: Record<IconName, string> = {
  resumen: 'M3 3h7v7H3z M14 3h7v7h-7z M3 14h7v7H3z M14 14h7v7h-7z',
  productos: 'M12 3 3 7.5v9L12 21l9-4.5v-9L12 3z M3 7.5l9 4.5 9-4.5 M12 12v9 M7.5 5.25l9 4.5',
  proveedores: 'M3 21V7l9-4v18 M12 9h9v12 M7 8v2 M7 13v2 M16 13h2 M16 17h2 M1 21h22',
  oferta: 'M3 4h18v16H3z M3 9h18 M7 2v4 M17 2v4 M7 13h4 M7 16h8',
  pedidos: 'M6 3h12v18H6z M9 7h6 M9 11h6 M9 15h4',
  compras: 'M3 3h2l3 12h11l3-9H6 M9 20h.01 M18 20h.01',
  reportes: 'M4 3v18h17 M8 16v-5 M13 16V6 M18 16V9',
}
export function NavIcon({ name }: { name: IconName }) {
  return <svg width="19" height="19" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.6" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true"><path d={paths[name]} /></svg>
}
