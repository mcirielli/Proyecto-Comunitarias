-- Ejecución única, solo con autorización específica. Sin arranque automático.
-- Preserva pagos legados: clave NULL para registros anteriores; la API nueva la exige.
BEGIN;
ALTER TABLE public.pagos ADD COLUMN clave_solicitud uuid;
ALTER TABLE public.pagos ADD CONSTRAINT pagos_clave_solicitud_key UNIQUE (clave_solicitud);
COMMIT;
