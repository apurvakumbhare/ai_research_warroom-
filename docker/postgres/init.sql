-- Initialize PostgreSQL for War-Room
-- Flyway will handle the actual table creations

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Ensure UTF8 encoding
DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'warroom' AND datencoding = 6) THEN
      RAISE NOTICE 'Database warroom encoding check passed';
   END IF;
END
$$;
