import { useState } from 'react'

type Theme = 'dark' | 'light'
const preferenceKey = 'comunitarias.theme'

export function initializeTheme(): Theme {
  let theme: Theme = 'dark'
  try {
    if (localStorage.getItem(preferenceKey) === 'light') theme = 'light'
  } catch { /* El tema funciona aunque el navegador bloquee almacenamiento. */ }
  document.documentElement.dataset.theme = theme
  return theme
}

export function ThemeToggle() {
  const [theme, setTheme] = useState<Theme>(() => document.documentElement.dataset.theme === 'light' ? 'light' : 'dark')
  function toggle() {
    const next: Theme = theme === 'dark' ? 'light' : 'dark'
    document.documentElement.dataset.theme = next
    setTheme(next)
    try { localStorage.setItem(preferenceKey, next) } catch { /* Preferencia solo para esta pestaña. */ }
  }
  return <button type="button" className="theme-switch" onClick={toggle}
    aria-label={theme === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}>
    <span aria-hidden="true">{theme === 'dark' ? '☀' : '☾'}</span> {theme === 'dark' ? 'Modo claro' : 'Modo oscuro'}
  </button>
}
