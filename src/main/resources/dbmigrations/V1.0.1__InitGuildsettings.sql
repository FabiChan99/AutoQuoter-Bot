set schema 'public';


create table guildsettings (
    guild_id bigint not null primary key,
    crossguildposting boolean not null default false
);