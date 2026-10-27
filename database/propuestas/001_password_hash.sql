-- PROPUESTA NO APLICADA. El usuario autorizó solo preparar login (27/10/2026).
-- Requiere aprobación explícita, verificación del esquema y provisión de cuentas.
-- No conectar este archivo al arranque ni ejecutar automáticamente.
ALTER TABLE public.usuarios ADD COLUMN password_hash varchar(60) NULL;
