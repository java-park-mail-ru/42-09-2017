CREATE TABLE board_meta (
  id           SERIAL PRIMARY KEY,
  board_id     INT,
  name         TEXT UNIQUE,
  level        INT,
  timer        INT,
  rating       INT  DEFAULT 0,
  created      DATE DEFAULT current_date,
  preview      TEXT,
  played_times INT  DEFAULT 0,
  players      INT  DEFAULT 1,
  FOREIGN KEY (board_id) REFERENCES board (id)
)