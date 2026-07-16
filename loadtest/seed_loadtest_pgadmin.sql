-- ============================================================================
--  SEED PARA PRUEBAS DE CARGA — versión para PEGAR EN pgAdmin (Query Tool)
--  (Sin meta-comandos de psql. SQL puro.)
--
--  Crea: N estudiantes + 1 cuestionario PUBLICADO con 10 preguntas y matriz.
--  Estudiantes: estudiante1@test.com .. estudianteN@test.com  | contraseña: Admin123!
--
--  👉 CAMBIA el número de estudiantes en la línea "generate_series(1, 100)".
--  Requiere que la app haya arrancado antes en esta BD (roles + admin existen).
--  Ejecútalo contra tu Postgres LOCAL, NUNCA contra Neon (producción).
-- ============================================================================

-- ── Comprobación previa: deben existir roles y admin ────────────────────────
DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM roles WHERE nombre = 'ROLE_ESTUDIANTE') THEN
    RAISE EXCEPTION 'No existe ROLE_ESTUDIANTE. Arranca la app una vez en esta BD (DataInitializer) antes de sembrar.';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM usuarios WHERE email = 'admin@skillbridge.edu.co') THEN
    RAISE EXCEPTION 'No existe el admin (de donde se copia el hash). Arranca la app una vez antes de sembrar.';
  END IF;
END $$;

-- ── 1) Estudiantes (CAMBIA el 100 por el N que quieras) ─────────────────────
INSERT INTO usuarios
  (tipo_identificacion, numero_identificacion, nombre, apellido, email,
   password_hash, auth_provider, activado, email_verificado, profile_completed, created_at)
SELECT
  'CC',
  'LT' || g,
  'Estudiante',
  'Carga' || g,
  'estudiante' || g || '@test.com',
  (SELECT password_hash FROM usuarios WHERE email = 'admin@skillbridge.edu.co' LIMIT 1),
  'LOCAL', true, true, true, now()
FROM generate_series(1, 100) AS g       -- <── AQUÍ el número de estudiantes
ON CONFLICT (email) DO NOTHING;

-- ── 2) Asignar ROLE_ESTUDIANTE ──────────────────────────────────────────────
INSERT INTO usuario_roles (id_usuario, id_rol)
SELECT u.id, (SELECT id FROM roles WHERE nombre = 'ROLE_ESTUDIANTE' LIMIT 1)
FROM usuarios u
WHERE u.email LIKE 'estudiante%@test.com'
ON CONFLICT DO NOTHING;

-- ── 3) Cuestionario PUBLICADO (general, siempre disponible) ─────────────────
INSERT INTO cuestionario
  (nombre, objetivo, instrucciones, estado_cuestionario, orden_aleatorio, created_at, creado_por)
VALUES
  ('LOADTEST - Examen de carga', 'Cuestionario para pruebas de estrés (JMeter)',
   'Responde todas las preguntas.', 'PUBLICADO', false, now(), 'Seed LoadTest')
ON CONFLICT (nombre) DO NOTHING;

-- ── 4) Preguntas + opciones + vínculo + matriz (solo si aún no tiene) ───────
DO $$
DECLARE
  v_cuest BIGINT;
  v_preg  BIGINT;
  i       INT;
BEGIN
  SELECT id_cuestionario INTO v_cuest
    FROM cuestionario WHERE nombre = 'LOADTEST - Examen de carga';

  IF NOT EXISTS (SELECT 1 FROM pregunta_cuestionario WHERE id_cuestionario = v_cuest) THEN
    FOR i IN 1..10 LOOP
      INSERT INTO pregunta (tipo_pregunta, texto, created_at)
      VALUES ('OPCION_UNICA', 'LOADTEST Pregunta #' || i, now())
      RETURNING id_pregunta INTO v_preg;

      INSERT INTO opcion_pregunta (texto, peso, mostrar_orden, id_pregunta) VALUES
        ('Opción A (peso 1)', 1, 1, v_preg),
        ('Opción B (peso 2)', 2, 2, v_preg),
        ('Opción C (peso 3)', 3, 3, v_preg);

      INSERT INTO pregunta_cuestionario (id_cuestionario, id_pregunta, obligatoria, peso, is_condicional)
      VALUES (v_cuest, v_preg, true, 1, false);
    END LOOP;

    INSERT INTO puntuacion_matrices
      (id_cuestionario, skill, nivel, min_puntaje, max_puntaje, descripcion, created_at) VALUES
      (v_cuest, 'PENSAMIENTO_CRITICO', 'BAJO',       0, 10, 'Nivel bajo (carga)',       now()),
      (v_cuest, 'PENSAMIENTO_CRITICO', 'INTERMEDIO', 11, 20, 'Nivel intermedio (carga)', now()),
      (v_cuest, 'PENSAMIENTO_CRITICO', 'AVANZADO',   21, 30, 'Nivel avanzado (carga)',   now());

    RAISE NOTICE 'Preguntas, opciones y matriz creadas para el cuestionario %', v_cuest;
  ELSE
    RAISE NOTICE 'El cuestionario de carga ya tenía preguntas; no se re-sembró.';
  END IF;
END $$;

-- ── Resumen ─────────────────────────────────────────────────────────────────
SELECT
  (SELECT count(*) FROM usuarios WHERE email LIKE 'estudiante%@test.com') AS estudiantes_carga,
  (SELECT id_cuestionario FROM cuestionario WHERE nombre = 'LOADTEST - Examen de carga') AS id_cuestionario,
  (SELECT count(*) FROM pregunta_cuestionario pc
     JOIN cuestionario c ON c.id_cuestionario = pc.id_cuestionario
     WHERE c.nombre = 'LOADTEST - Examen de carga') AS preguntas_del_cuestionario;
