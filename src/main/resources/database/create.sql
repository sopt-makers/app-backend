-- DDL generated by Postico 2.0 RC 6
-- Not all database features are supported. Do not use for backup.

-- Table Definition ----------------------------------------------

set search_path to app_dev;

CREATE TABLE app_dev.app_users (
                                   user_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                   username character varying(30) NOT NULL,
                                   is_opt_in boolean DEFAULT false,
                                   playground_id bigint UNIQUE ,
                                   playground_token character varying(500),
--                                    os_type character varying(20),
--                                    password character varying(600) NOT NULL,
                                   created_at timestamp without time zone,
                                   updated_at timestamp without time zone
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX "USER_pkey" ON app_dev.app_users(user_id int8_ops);


-- DDL generated by Postico 2.0 RC 6
-- Not all database features are supported. Do not use for backup.

-- Table Definition ----------------------------------------------

CREATE TABLE app_dev.mission (
                                 mission_id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                                 title character varying(300) NOT NULL,
                                 level integer NOT NULL,
                                 display boolean,
                                 profile_image text[]
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX "STAMP_pkey" ON app_dev.mission(mission_id int8_ops);

-- DDL generated by Postico 2.0 RC 6
-- Not all database features are supported. Do not use for backup.

-- Table Definition ----------------------------------------------

CREATE TABLE app_dev.stamp (
                               id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                               contents character varying(500) NOT NULL,
                               images text[],
                               created_at timestamp without time zone,
                               updated_at timestamp without time zone,
                               user_id bigint REFERENCES app_dev.app_users(user_id) ON DELETE CASCADE,
                               mission_id bigint
);

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX "MISSION_pkey" ON app_dev.stamp(id int8_ops);


-- Table Definition ----------------------------------------------

CREATE TABLE app_dev.push_token
(
    playground_id bigint REFERENCES app_dev.app_users(playground_id),
    token         text[],
    created_at    timestamp without time zone,
    updated_at    timestamp without time zone,
    PRIMARY KEY (playground_id, token)
);

alter table app_dev.push_token
    owner to makers;

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX "PUSH_TOKEN_pkey" ON app_dev.app_users(playground_id int8_ops);

-- Table Definition ----------------------------------------------

CREATE TABLE app_dev.notification_option
(
    opt_id bigint  GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id bigint REFERENCES app_dev.app_users(user_id) UNIQUE ,
    all_opt_in boolean DEFAULT false,
    part_opt_in boolean DEFAULT false,
    news_opt_in boolean DEFAULT false
);

alter table app_dev.notification_option
    owner to makers;

-- Indices -------------------------------------------------------

CREATE UNIQUE INDEX "PUSH_OPTION_pkey" ON app_dev.app_users(user_id int8_ops);

create table app_dev.main_description
(
    id                          serial
        constraint main_description_pk
            primary key,
    active_top_description      varchar(100),
    active_bottom_description   varchar(100),
    inactive_top_description    varchar(100),
    inactive_bottom_description varchar(100),
    created_at                  timestamp default now() not null,
    updated_at                  timestamp default now() not null
);

alter table app_dev.main_description
    owner to makers;

create unique index main_description_id_uindex
    on app_dev.main_description (id);

create table app_dev.soptamp_point
(
    id              serial
        constraint soptamp_point_pk
            primary key,
    generation      integer,
    soptamp_user_id integer,
    points          integer   default 0,
    created_at      timestamp default now(),
    updated_at      timestamp default now()
);

alter table app_dev.soptamp_point
    owner to makers;

create unique index soptamp_point_id_uindex
    on app_dev.soptamp_point (id);

create index soptamp_point_soptamp_user_id_index
    on app_dev.soptamp_point (soptamp_user_id);

create table app_dev.soptamp_user
(
    id serial
        constraint soptamp_user_pk
            primary key,
    user_id integer not null,
    profile_message varchar(255),
    total_points bigint,
    nickname varchar(255),
    created_at timestamp default now(),
    updated_at timestamp default now()
);

alter table app_dev.soptamp_user owner to makers;

create unique index soptamp_user_id_uindex
    on app_dev.soptamp_user (id);

create unique index soptamp_user_user_id_uindex
    on app_dev.soptamp_user (user_id);

