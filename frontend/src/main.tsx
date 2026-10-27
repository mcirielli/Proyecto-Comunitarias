import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { OperacionesPage } from './pages/OperacionesPage'
import { initializeTheme } from './components/ThemeToggle'
import './index.css'

initializeTheme()

createRoot(document.getElementById('root')!).render(
  <StrictMode><OperacionesPage /></StrictMode>,
)
