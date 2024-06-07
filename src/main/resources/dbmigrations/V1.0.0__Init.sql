set schema 'public';


create table qoutestats
(
    id         serial primary key,
    user_id    bigint not null,
    channel_id bigint not null,
    guild_id   bigint not null,
    timestamp  bigint not null
);
