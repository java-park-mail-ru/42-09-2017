create extension if not exists citext with schema public;

create table player(
  id serial primary key,
  username citext unique not null,
  email citext unique not null,
  password text not null,
  scores bigint default 0,
  level int default 0
);