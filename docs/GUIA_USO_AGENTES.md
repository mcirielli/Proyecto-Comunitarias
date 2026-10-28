# Guía de uso de los agentes de Comunitarias

Esta guía explica cómo solicitar trabajo a los agentes y conservar el contexto
entre prompts. La coordinación técnica se define en [Agentes](AGENTES.md).

## Roles disponibles

| Rol | Responsabilidad | Resultado esperado |
|---|---|---|
| Tito, agente principal | Orquestar, delimitar alcance e integrar resultados | Resultado consolidado y próximo paso |
| desarrollo | Implementar la etapa autorizada y verificarla | Código, pruebas, comandos y resultados |
| seguridad | Revisar código y configuración sin editarlos | Hallazgos con evidencia y recomendaciones |
| reportes | Documentar lo ocurrido durante la ejecución | Reporte de sesión y contexto para otro prompt |

Reportes registra la ejecución del trabajo; no desarrolla los reportes de ventas
o compras de la aplicación. Los roles no sustituyen las tareas de los alumnos.

## Primer uso

1. Abrí una nueva sesión de Codex en la raíz de Comunitarias.
2. Pedá comprobar los roles antes de comenzar una implementación:

   > Tito, comprobá si esta sesión permite seleccionar los roles desarrollo,
   > seguridad y reportes definidos en .codex/agents/. Informá si los carga
   > automáticamente o si tenés que transmitir sus instrucciones al delegar.
   > No modifiques la aplicación.

3. Elegá una tarea autorizada del proyecto e indicá objetivo, alcance y límites.

Los archivos configurados no prueban por sí solos que el cliente haya cargado
los roles. Si no puede seleccionarlos, Tito debe explicar la limitación y pasar
sus instrucciones explícitamente a los subagentes. Los permisos y límites del
entorno siguen aplicando. No hace falta crear agentes dentro del backend Java.

## Usar Desarrollo

Copiá y completá este prompt:

> Tito, delegá a desarrollo la tarea [identificador y objetivo]. El alcance
> autorizado es [comportamiento esperado] y los archivos previstos son [rutas].
> Respetá las decisiones vigentes y las entregas académicas. Implementá las
> pruebas de negocio pertinentes y devolvé cambios, comandos ejecutados,
> resultados, fallos y verificaciones pendientes. Si hace falta una decisión
> arquitectónica relevante, presentala antes de implementarla.

Desarrollo debe distinguir sus cambios de los que ya estaban en el repositorio.
Una recomendación o un comando sugerido no cuenta como una prueba ejecutada.

## Usar Seguridad

> Tito, delegá a seguridad una revisión de [archivos o cambios concretos].
> Trabajá sin editar código. Informá alcance revisado, hallazgos con archivo y
> línea cuando corresponda, gravedad justificada, evidencia, impacto y
> corrección propuesta. Separá riesgos confirmados de hipótesis y controles
> no verificados. No muestres secretos ni hagas pruebas intrusivas.

Esta solicitud autoriza revisar, no aplicar correcciones. Para corregir un
hallazgo, delimitá la tarea para Desarrollo. Seguridad debe confirmar el cambio
final después de las correcciones antes de cerrar la revisión.

## Usar Reportes

> Tito, delegá a reportes el cierre de esta ejecución. Transmitile la consigna,
> el estado inicial, los cambios y la evidencia de Desarrollo y Seguridad.
> Usí docs/reportes/PLANTILLA.md. Registrá qué se hizo, qué se ejecutó, qué falló,
> qué quedá resuelto y qué sigue pendiente. Prepará contexto autocontenido para
> el próximo prompt. Revisí el resultado y actualizá docs/reportes/ULTIMO.md.

Si no hay evidencia suficiente, el reporte debe indicar “no verificado”. No
reconstruye pruebas a partir del código ni inventa resultados de otra sesión.
Si hubo interrupción, se conserva un borrador y se indican las tareas inconclusas.

## Usar Tito como orquestador

Tito es el agente principal que distribuye el trabajo; no requiere un cuarto
subagente. Para una ejecución coordinada:

> Tito, actuá como orquestador para [tarea autorizada]. Leé el último reporte
> y respetá docs/AGENTES.md. Asigná la implementación a desarrollo, la revisión
> a seguridad y el reporte de ejecución a reportes. Usí paralelo para trabajo
> independiente. Limitá los cambios a [alcance]. Esperá las verificaciones,
> hacá revisar las correcciones finales y cerrá el reporte con los pendientes.
> Actualizá docs/reportes/ULTIMO.md y entregame el resultado consolidado.

La secuencia es:

1. Tito delimita objetivo, archivos, restricciones y estado inicial.
2. Desarrollo implementa; Seguridad puede revisar la base; Reportes prepara
   un borrador con el contexto disponible.
3. Con los archivos estables, Desarrollo verifica y Seguridad revisa el cambio.
4. Si se corrige código, se verifica y revisa nuevamente lo afectado.
5. Reportes cierra con evidencia final; Tito revisa y actualiza el índice.

Paralelo no significa que los tres deban modificar los mismos archivos o que
el reporte pueda cerrarse antes de que terminen las verificaciones. Tito
transmite los resultados: no se presupone acceso automático entre historiales.

## Retomar desde otro prompt

> Tito, leé docs/reportes/ULTIMO.md y el reporte enlazado. Contrastá ese contexto
> con el estado actual del repositorio. Retomá [pendiente concreto] dentro del
> alcance [límite autorizado]. Usí los agentes pertinentes y dejá un nuevo
> reporte al finalizar. No presentes verificaciones anteriores como nuevas.

También podés copiar la sección “Contexto para el próximo prompt” del reporte.
Los agentes no trabajan permanentemente entre conversaciones ni garantizan
memoria automática. El contexto persistente está en los archivos del proyecto.

## Archivos de referencia

| Archivo | Para qué sirve |
|---|---|
| [Instrucciones del proyecto](../AGENTS.md) | Reglas académicas y de trabajo |
| [Coordinación](AGENTES.md) | Responsabilidades, permisos y flujo |
| [Configuración](../.codex/config.toml) | Habilitación y concurrencia solicitada |
| [Desarrollo](../.codex/agents/desarrollo.toml) | Instrucciones del rol |
| [Seguridad](../.codex/agents/seguridad.toml) | Instrucciones del rol |
| [Reportes](../.codex/agents/reportes.toml) | Instrucciones del rol |
| [Plantilla de reporte](reportes/PLANTILLA.md) | Estructura de cada ejecución |
| [último reporte](reportes/ULTIMO.md) | Punto de entrada para continuar |

Cada ejecución guarda un archivo propio en docs/reportes/. ULTIMO.md enlaza el
último reporte cerrado; los anteriores se conservan.
