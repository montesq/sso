# --- First database schema

# --- !Ups

create table user (
  username                     varchar(255) not null primary key,
  password                  varchar(255) not null
);

# --- !Downs

drop table if exists user;
