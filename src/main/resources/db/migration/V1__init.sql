CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA public;

CREATE TABLE player (
  id       SERIAL PRIMARY KEY,
  username CITEXT UNIQUE NOT NULL,
  email    CITEXT UNIQUE NOT NULL,
  password TEXT          NOT NULL,
  scores   BIGINT DEFAULT 0,
  level    INT    DEFAULT 0
);