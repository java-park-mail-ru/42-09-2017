CREATE TABLE player (
  id       BIGSERIAL PRIMARY KEY,
  username TEXT UNIQUE NOT NULL,
  email    TEXT UNIQUE NOT NULL,
  password TEXT          NOT NULL,
  scores   BIGINT DEFAULT 0,
  level    INT    DEFAULT 0
);