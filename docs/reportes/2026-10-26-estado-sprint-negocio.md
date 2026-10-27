# Reporte de ejecución: estado del sprint e inicio de negocio

Estado: CERRADO (ventana de trabajo; Sprint 1 continúa abierto).
Fecha: 26 de octubre de 2026, America/Buenos_Aires.
Rama: docs/guias-equipo-kanban. Commit base: 11ed8a7.

## Alcance y estado inicial

Usuario pidió revisar último reporte, ubicar sprint y comenzar lógica de negocio.
Se leyeron AGENTS, superprompt, ambas entregas PDF, decisiones, planificación
operativa Sprint 1, pendientes MVP y reporte 17/10. El último reporte solo
configura agentes. Código base: infraestructura HTTP, sin negocio implementado.
S1-01 en revisión final; restantes siete P0 pendientes según registro.
No se puede declarar Sprint 3 por calendario sin cerrar incrementos anteriores.
Había numerosas copias no versionadas -Andres-PC, además de configuración y docs
preexistentes no versionados. No se sobrescribieron esos documentos.

## Cambios

- Modelo mínimo v1 en docs/MODELO_MAESTROS_PROPUESTA.md, pendiente de aprobación:
  productos, proveedores, vínculo estable e historial inmutable separado.
  Incluye reglas, contrato, archivos, reparto de trabajo y discrepancias.
- SupabaseJdbcIT: DataSource administrado por Spring y SELECT 1, sin DDL.
  Ejecución explícita, fuera de tests unitarios por convención de nombre IT.
- Nueve copias Java preexistentes trasladadas desde backend/src a
  .cache/conflictos-onedrive/backend/src, conservando estructura y contenido.
  No se alteró código funcional de infraestructura ni dependencias.

## Verificaciones observadas

Java 21: C:/Users/Noxie-PC/.jdks/temurin-21.0.12.1.
Maven incluido en IntelliJ IDEA 2026.2.0.1, plugins/maven-plugin/lib/maven3/bin.

1. mvn -o test desde backend: ocho errores de compilación por copias Java.
2. Tras preservar copias: compilación principal/tests correcta; seis errores
   de infraestructura al crear HttpClient: Unable to establish loopback connection.
3. mvn -o -f backend/pom.xml test fuera del sandbox, con escalación aprobada:
   BUILD SUCCESS; seis tests, cero fallos, cero errores, cero omitidos.
   SupabaseJdbcIT compila, no se ejecuta en este comando.
4. git diff --check: sin errores. No commit/push ni despliegue.
5. Variables JDBC presentes en proceso: 0/3. Integración no ejecutada; S1-02
   no está terminada. No se buscaron ni mostraron secretos.

Extracción PDF inicial tuvo error cp1252 en un símbolo; se releyó el tramo
restante con Python -X utf8. La lectura fue textual, sin validar diagramas
embebidos visualmente; el modelo propuesto no se atribuye al diagrama original.

## Seguridad y límites

Revisión principal del cambio: sin endpoints públicos nuevos, secretos ni DDL.
No hubo auditoría independiente ni revisión cruzada del equipo.
No se implementaron entidades o reglas pendientes de aprobación. El registro
de decisiones exige aprobación del modelo/historial antes de entidades/SQL.
Login real prevalece sobre selector del superprompt; Docker aprobado;
recorte de rentabilidad/reportes sigue sin validación docente acreditada.

## Próximo prompt

Continuar Sprint 1, no asumir completadas P0. Revisar/aprobar propuesta v1
de maestros e historial y luego implementar ese incremento con tests de negocio.
Completar contrato S1-04 antes de exponer operaciones. Proveer variables por
entorno local para ejecutar mvn -Dtest=SupabaseJdbcIT test, luego repetir desde
contenedor. Faltan modelo Usuario/roles/oferta y destino/permisos reales antes
de completar/aplicar esquema. Conservar respaldos de copias fuera de src.
