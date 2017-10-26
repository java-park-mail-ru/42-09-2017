CREATE TABLE map_meta (
  id           SERIAL PRIMARY KEY,
  name         TEXT UNIQUE,
  level        INT,
  timer        INT,
  rating       INT DEFAULT 0,
  created      DATE DEFAULT current_date,
  preview      TEXT,
  played_times INT DEFAULT 0,
  players      INT DEFAULT 1
)