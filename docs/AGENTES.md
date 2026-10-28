# Agentes de Comunitarias

Para empezar y copiar prompts, consultar la [guía de uso](GUIA_USO_AGENTES.md).

Configuración limitada a este repositorio. No modifica la aplicación ni la
configuración global del usuario. Los modelos y el esfuerzo se heredan del
agente principal para evitar fijar modelos no disponibles en otra cuenta.

| Rol | Trabajo | Escritura asignada |
|---|---|---|
| Tito, principal | Delimita, coordina, integra y transmite evidencia | Instrucciones e índice de reportes |
| desarrollo | Implementa y verifica la etapa autorizada | Archivos de implementación asignados |
| seguridad | Revisa el estado inicial y el cambio final | Ninguna; devuelve hallazgos |
| reportes | Documenta la ventana de ejecución para otro prompt | Un reporte asignado en docs/reportes/ |

## Coordinación

1. Tito lee el último reporte, registra estado inicial y delimita objetivo,
   archivos y autorizaciones. Asigna un identificador único a la sesión.
2. Delega a los tres roles con esas instrucciones y sus archivos TOML.
   Desarrollo implementa; Seguridad revisa la base; Reportes prepara el borrador.
3. Tito transmite los hitos y evidencia de Desarrollo a los demás. Los agentes
   no tienen acceso automático al historial de las otras conversaciones.
4. Al estabilizar el cambio, Seguridad revisa el resultado final. Desarrollo
   corrige lo autorizado y ejecuta las verificaciones pertinentes. Si hay nuevas
   correcciones, Seguridad confirma el cambio final antes de cerrar el reporte.
5. Reportes recibe los resultados finales y escribe
   `docs/reportes/<fecha>-<identificador>.md`, según PLANTILLA.md.
6. Tito revisa el reporte y actualiza `docs/reportes/ULTIMO.md` con un enlace
   relativo al reporte cerrado. No se sobrescriben reportes anteriores.

No se ejecutan builds o pruebas sobre archivos que Desarrollo esté modificando.
Una revisión inicial no sustituye la revisión final. El reporte definitivo
depende de los resultados: el trabajo puede solaparse, su cierre es secuencial.
Los roles no sustituyen las revisiones ni la comprensión de los cuatro alumnos.

## Activación y comprobación

La documentación oficial actual define archivos independientes en
`.codex/agents/` con `name`, `description` y `developer_instructions`.
`.codex/config.toml` habilita agentes y solicita tres hilos secundarios
simultáneos, además del principal. Los límites del entorno pueden prevalecer.

Abrir una nueva sesión del cliente local en este repositorio para comprobar
que carga la configuración. Si no descubre los roles, revisar versión del
cliente, confianza del proyecto y restricciones administradas; no asumir que
escribir los archivos registra agentes en una conversación ya abierta.

Prompt sugerido:

> Tito, usá los agentes desarrollo, seguridad y reportes en paralelo para
> [tarea autorizada]. Respetá docs/AGENTES.md. Desarrollo implementa solamente
> [alcance]; Seguridad revisa sin editar; Reportes genera el reporte de esta
> ejecución y contexto para el próximo prompt. Esperá los resultados finales,
> integrá las correcciones autorizadas y actualizá docs/reportes/ULTIMO.md.

Si la herramienta de delegación no permite seleccionar roles personalizados,
Tito debe informar esa limitación y pasar explícitamente las instrucciones
del TOML al subagente, sin afirmar que el cliente lo cargó automáticamente.
No se garantiza ejecución permanente entre prompts ni memoria automática:
el traspaso se realiza leyendo los reportes guardados.

Los permisos se heredan de la sesión principal. Las restricciones de escritura
por rol son instrucciones de trabajo, no barreras de seguridad independientes.

## Fuente

Documentación oficial consultada el 17 de octubre de 2026:
[Subagents](https://learn.chatgpt.com/docs/agent-configuration/subagents).
