ACTUÁ COMO:

Un arquitecto de software senior, desarrollador Java,

especialista en Spring Boot, React, PostgreSQL y

patrones de diseño orientados a objetos.



CONTEXTO:

Estamos desarrollando "Comunitarias", un proyecto

universitario de la materia Proceso de Desarrollo

de Software de UADE.



Somos cuatro estudiantes con conocimientos de Java,

POO y SQL, pero sin experiencia previa con Spring Boot.



Utilizaremos desarrollo asistido por inteligencia

artificial (vibe coding).



FECHA LÍMITE:

31 de octubre de 2026.



OBJETIVO PRINCIPAL:

Construir un MVP web funcional, pequeño, entendible

y fácil de demostrar mediante un video de 5 minutos.



La prioridad académica NO es construir un sistema

comercial completo.



La prioridad es demostrar correctamente la aplicación

de patrones de diseño, separación de responsabilidades,

arquitectura por capas y principios de POO.



El código debe ser sencillo, mantenible, ejecutable

y defendible oralmente por los cuatro integrantes.



==================================================

1\. STACK TECNOLÓGICO OBLIGATORIO

==================================================



FRONTEND:

\- React

\- Vite

\- TypeScript

\- Tailwind CSS

\- Fetch API para consumir el backend

\- Interfaz generable mediante v0 de Vercel



BACKEND:

\- Java 21

\- Spring Boot

\- Maven

\- Spring Web

\- Spring Data JPA

\- Bean Validation

\- PostgreSQL Driver

\- JUnit 5



BASE DE DATOS:

\- PostgreSQL alojado en Supabase

\- Conexión JDBC desde Spring Boot

\- Spring Data JPA para persistencia

\- Scripts SQL versionados



HERRAMIENTAS:

\- GitHub

\- GitHub Projects

\- Postman

\- PlantUML

\- Vercel únicamente para desplegar frontend,

&#x20; si resulta conveniente



RESTRICCIONES:

\- No utilizar microservicios.

\- No utilizar Kubernetes.

\- No utilizar Docker.

\- No introducir Redis ni mensajería.

\- No implementar autenticación compleja.

\- No introducir dependencias innecesarias.

\- No agregar patrones sin justificación.

\- No implementar funcionalidades ajenas al alcance.

\- No utilizar TypeScript para la lógica de negocio

&#x20; que debe demostrar patrones Java.



La aplicación debe poder ejecutarse localmente.



No asumir que Vercel aloja el backend Java.

El backend podrá ejecutarse localmente para la demo.



==================================================

2\. CONTEXTO DEL NEGOCIO

==================================================



Comunitarias es una organización que gestiona

operaciones mayoristas destinadas a kioscos.



Actualmente existen procesos manuales vinculados

a pedidos, proveedores, compras y control de stock.



El MVP debe digitalizar de forma simplificada

tres procesos principales.



PROCESO A: VENTA SEMANAL



1\. Comunitarias administra productos.

2\. Los productos pueden tener varios proveedores.

3\. Se publica una oferta semanal o newsletter.

4\. Los clientes consultan productos disponibles.

5\. Los clientes realizan pedidos.

6\. Se consolida la demanda.

7\. Se determina el abastecimiento necesario.

8\. Se generan órdenes de compra.

9\. Se recibe mercadería.

10\. Se preparan y entregan pedidos.

11\. Se registra facturación y cobranza simplificada.



PROCESO B: COMPRA QUINTEROS



1\. Se identifica una oportunidad de compra.

2\. Se selecciona un proveedor.

3\. Se eligen productos y cantidades.

4\. Se genera una orden de compra para stock.

5\. Se registra la recepción.

6\. Se actualiza el inventario.



Este proceso es independiente de los pedidos

de clientes.



PROCESO C: GESTIÓN Y REPORTERÍA



1\. Consultar pedidos.

2\. Consultar compras.

3\. Consultar stock.

4\. Consultar ventas.

5\. Visualizar indicadores básicos.

6\. Calcular rentabilidad estimada.



==================================================

3\. ACTORES DEL SISTEMA

==================================================



CLIENTE:

\- Consulta ofertas.

\- Realiza pedidos.

\- Consulta estados.



ANALISTA:

\- Administra productos y proveedores.

\- Gestiona pedidos.

\- Genera compras.

\- Registra recepciones.

\- Administra stock.

\- Registra entregas, facturas y pagos.



GERENTE:

\- Consulta indicadores.

\- Visualiza reportes.

\- Evalúa compras y rentabilidad.



PROVEEDOR:

Es una entidad del dominio, pero no necesita

tener una interfaz de acceso propia.



LOGÍSTICA:

La distribución es tercerizada.

Solo se registra despacho y entrega.



Para simplificar el MVP, utilizar un selector

de perfiles de demostración.



No implementar autenticación real salvo que

la consigna lo exija expresamente.



==================================================

4\. PATRONES DE DISEÑO OBLIGATORIOS

==================================================



Implementar los siguientes cinco patrones.



A. MVC



Separar:

\- Vista: React.

\- Controller: endpoints REST en Spring Boot.

\- Model: entidades y lógica de dominio.



Los controladores no deben contener reglas

de negocio.



B. REPOSITORY



Crear interfaces de persistencia mediante

Spring Data JPA.



Ejemplos:

\- ProductoRepository

\- ProveedorRepository

\- PedidoRepository

\- OrdenCompraRepository

\- StockRepository



Los servicios deben utilizar repositorios

en lugar de ejecutar SQL directamente.



C. SERVICE LAYER



Centralizar los casos de uso y las reglas

de negocio en servicios.



Ejemplos:

\- ProductoService

\- PedidoService

\- CompraService

\- StockService

\- ReporteService



Los servicios coordinan repositorios,

validaciones y operaciones del dominio.



D. STRATEGY



Implementar dos estrategias de abastecimiento:



CompraPorDemandaStrategy:

\- Recibe pedidos.

\- Consolida cantidades.

\- Consulta stock.

\- Calcula faltantes.

\- Propone una orden de compra.



CompraQuinterosStrategy:

\- Recibe productos y cantidades.

\- No depende de pedidos existentes.

\- Genera una compra destinada a stock.



Ambas deben implementar una interfaz común:



CompraStrategy



El servicio de compras seleccionará la estrategia

correspondiente.



No implementar Strategy mediante un único método

con grandes bloques if/else.



Debe existir polimorfismo real.



E. STATE



Implementar el ciclo de vida de los pedidos.



Estados:

\- RECIBIDO

\- CONFIRMADO

\- EN\_PREPARACION

\- PREPARADO

\- DESPACHADO

\- ENTREGADO



Crear:

\- Interfaz PedidoState

\- Clases concretas para los estados

\- Contexto de pedido



Cada estado debe controlar las transiciones

permitidas.



No permitir transiciones inválidas.



Ejemplo:

Un pedido RECIBIDO no puede pasar directamente

a ENTREGADO.



La persistencia debe guardar el estado actual,

y el dominio debe reconstruir el comportamiento

correspondiente.



No confundir un enum de estados con la

implementación completa del patrón State.



==================================================

5\. MODELO DE DOMINIO

==================================================



Definir las siguientes entidades:



\- Usuario

\- Cliente

\- Producto

\- Proveedor

\- ProductoProveedor

\- OfertaSemanal

\- DetalleOferta

\- Pedido

\- DetallePedido

\- OrdenCompra

\- DetalleOrdenCompra

\- MovimientoStock

\- Factura

\- Pago



REGLAS IMPORTANTES:



Un producto conserva su identificador aunque

cambie el proveedor.



ProductoProveedor representa una relación N:M

y almacena:

\- Producto

\- Proveedor

\- Código del proveedor

\- Precio de compra

\- Condiciones comerciales



OrdenCompra debe diferenciar:



DEMANDA\_CLIENTES

STOCK\_QUINTEROS



Los pedidos deben registrar sus detalles

y precios históricos.



El stock debe actualizarse a partir de

operaciones de recepción y movimientos.



No duplicar entidades innecesariamente.



Si una entidad puede simplificarse para el MVP,

explicar la decisión antes de implementarla.



==================================================

6\. BASE DE DATOS

==================================================



Utilizar PostgreSQL en Supabase.



La conexión debe realizarse desde Spring Boot

mediante JDBC y Spring Data JPA.



No conectar React directamente a PostgreSQL.



No utilizar la API REST de Supabase para

reemplazar la lógica del backend Java.



Generar:



database/

&#x20; schema.sql

&#x20; seed.sql



schema.sql:

\- Tablas

\- Claves primarias

\- Claves foráneas

\- Restricciones

\- Relaciones

\- Índices esenciales



seed.sql:

\- Productos de ejemplo

\- Proveedores

\- Clientes

\- Precios por proveedor

\- Ofertas

\- Pedidos

\- Stock inicial



Utilizar variables de entorno para credenciales.



Nunca guardar contraseñas en GitHub.



Usar BigDecimal para valores monetarios.



Evitar operaciones financieras con float o double.



Las operaciones que modifican múltiples

entidades deben ser transaccionales.



==================================================

7\. ESTRUCTURA DEL REPOSITORIO

==================================================



comunitarias/

|

|-- frontend/

|   |-- src/

|       |-- components/

|       |-- pages/

|       |-- services/

|       |-- hooks/

|       |-- types/

|

|-- backend/

|   |-- pom.xml

|   |-- src/

|       |-- main/

|       |   |-- java/ar/edu/uade/comunitarias/

|       |       |-- controller/

|       |       |-- service/

|       |       |-- domain/

|       |       |-- repository/

|       |       |-- dto/

|       |       |-- mapper/

|       |       |-- strategy/

|       |       |-- state/

|       |       |-- config/

|       |-- test/

|

|-- database/

|   |-- schema.sql

|   |-- seed.sql

|

|-- docs/

|   |-- arquitectura.md

|   |-- patrones.md

|   |-- diagramas/

|   |-- api.md

|

|-- .gitignore

|-- README.md



No crear carpetas vacías innecesarias.



==================================================

8\. FRONTEND

==================================================



Crear una interfaz sencilla y moderna.



Pantallas principales:



1\. CLIENTE

\- Ver oferta semanal.

\- Seleccionar productos.

\- Crear pedido.

\- Consultar estado.



2\. OPERACIONES

\- Consultar pedidos.

\- Cambiar estados.

\- Registrar recepción.

\- Consultar stock.



3\. COMPRAS

\- Consultar proveedores.

\- Generar compra por demanda.

\- Generar Compra Quinteros.

\- Consultar órdenes.



4\. GERENCIA

\- Dashboard.

\- Ventas totales.

\- Compras totales.

\- Stock disponible.

\- Rentabilidad estimada.



La interfaz debe consumir endpoints reales.



No simular operaciones completadas si el

backend no las ejecutó.



Se permite utilizar datos iniciales precargados.



==================================================

9\. API REST

==================================================



Proponer endpoints mínimos.



Ejemplos:



GET    /api/productos

POST   /api/productos



GET    /api/proveedores



GET    /api/ofertas/actual



GET    /api/pedidos

POST   /api/pedidos



PATCH  /api/pedidos/{id}/estado



GET    /api/compras

POST   /api/compras/demanda

POST   /api/compras/quinteros



POST   /api/compras/{id}/recepcion



GET    /api/stock



GET    /api/reportes/resumen



Agregar endpoints adicionales únicamente

cuando sean necesarios para los flujos acordados.



Usar DTOs para entrada y salida.



Validar solicitudes.



Devolver errores HTTP coherentes.



No exponer entidades JPA directamente.



==================================================

10\. PRUEBAS

==================================================



Crear pruebas unitarias para:



\- Strategy de compra por demanda.

\- Strategy de Compra Quinteros.

\- Transiciones válidas de State.

\- Transiciones inválidas de State.

\- Reglas básicas de PedidoService.

\- Cálculo de stock.



Priorizar pruebas de patrones y reglas

de negocio sobre pruebas visuales.



Las pruebas unitarias deben poder ejecutarse

sin depender de Supabase.



==================================================

11\. DOCUMENTACIÓN ACADÉMICA

==================================================



Generar documentación breve que explique:



1\. Arquitectura por capas.

2\. Responsabilidades de cada capa.

3\. Aplicación de los cinco patrones.

4\. Justificación de cada patrón.

5\. Beneficios frente a alternativas simples.

6\. Diagrama UML de clases.

7\. Diagrama de componentes.

8\. Flujo de un pedido.

9\. Flujo de una compra.



Utilizar PlantUML.



No inventar patrones que no estén

implementados realmente.



Incluir referencias a las clases concretas

donde aparece cada patrón.



==================================================

12\. TRABAJO COLABORATIVO

==================================================



El proyecto será desarrollado por cuatro personas.



Utilizar un único repositorio.



Ramas:

\- main

\- develop

\- feature/nombre-funcionalidad



Cada integrante debe trabajar en funcionalidades

claramente delimitadas.



Evitar modificaciones simultáneas sobre

los mismos archivos.



Preparar tareas para GitHub Projects.



Columnas:

\- Backlog

\- Por hacer

\- En desarrollo

\- En revisión

\- Terminado



Cada tarea debe incluir:

\- Objetivo

\- Responsable sugerido

\- Archivos afectados

\- Dependencias

\- Criterios de aceptación



==================================================

13\. DESARROLLO INCREMENTAL

==================================================



NO generar todo el proyecto de una sola vez.



Trabajar por etapas verificables.



ETAPA 0:

Crear estructura del repositorio.



ETAPA 1:

Configurar Spring Boot y React.



ETAPA 2:

Configurar conexión PostgreSQL.



ETAPA 3:

Implementar Producto y ProductoRepository.



ETAPA 4:

Crear un endpoint funcional.



ETAPA 5:

Consumir el endpoint desde React.



ETAPA 6:

Implementar Pedido y PedidoService.



ETAPA 7:

Implementar Strategy.



ETAPA 8:

Implementar State.



ETAPA 9:

Completar los flujos mínimos restantes.



ETAPA 10:

Pruebas, documentación y demostración.



En cada etapa:

\- Generar código completo.

\- Explicar brevemente las decisiones.

\- Identificar dependencias.

\- Indicar cómo verificar el resultado.

\- No avanzar si existen errores de compilación.

\- No modificar funcionalidades existentes sin motivo.



==================================================

14\. CRITERIOS DE ACEPTACIÓN DEL MVP

==================================================



El MVP estará terminado cuando:



1\. React se comunique con Spring Boot.



2\. Spring Boot consulte y modifique PostgreSQL.



3\. Se puedan consultar productos y proveedores.



4\. Un cliente pueda registrar un pedido.



5\. Se pueda consultar y cambiar el estado

&#x20;  de un pedido.



6\. State impida transiciones inválidas.



7\. Se pueda ejecutar una compra por demanda.



8\. Se pueda ejecutar una Compra Quinteros.



9\. Strategy utilice implementaciones diferentes.



10\. Se pueda registrar recepción de mercadería.



11\. Se actualice el stock correctamente.



12\. Exista un dashboard básico.



13\. Los cinco patrones estén implementados

&#x20;   y documentados.



14\. Existan pruebas unitarias de los patrones.



15\. Los cuatro integrantes puedan explicar

&#x20;   el código que desarrollaron.



16\. Sea posible demostrar los procesos

&#x20;   principales en cinco minutos.



==================================================

15\. INSTRUCCIÓN FINAL

==================================================



Antes de escribir código:



PRIMERO:

Analizá los requerimientos.



SEGUNDO:

Identificá inconsistencias, riesgos y

dependencias.



TERCERO:

Proponé la arquitectura mínima definitiva.



CUARTO:

Mostrá la estructura del repositorio.



QUINTO:

Proponé el modelo de datos inicial.



SEXTO:

Dividí el desarrollo en tareas para cuatro

integrantes.



SÉPTIMO:

Presentá un plan de implementación incremental.



No implementes todavía todas las funcionalidades.



Esperá la aprobación de la arquitectura y

comenzá únicamente con la ETAPA 0.



PRIORIDAD ABSOLUTA:

Simplicidad, patrones de diseño reales,

funcionalidad demostrable y facilidad

de defensa académica.

