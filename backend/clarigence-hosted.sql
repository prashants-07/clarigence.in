-- Hosted database setup based on clarigence.sql, without local credentials.
-- Run using your provider's privately authenticated MySQL client, or create
-- this database in the provider console if SQL database creation is restricted.
CREATE DATABASE IF NOT EXISTS clarigence
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

-- Create/configure the application user privately through the hosting provider.
-- The original 'clarigence_app'@'localhost' account is for local use only.
-- This file does not migrate existing enquiries or admin accounts.
-- Spring Boot creates/updates contacts and admin_users on startup.
-- Run mysql-session-schema.sql on this database before the Vercel deployment.
