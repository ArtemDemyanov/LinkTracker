-- liquibase formatted sql

-- changeset ArtemDemyanov:init_chat_table
CREATE TABLE "chat"
(
    "id"         bigint                   NOT NULL,
    --"username"   text                     NOT NULL,
    --"created_at" timestamp with time zone NOT NULL,
    PRIMARY KEY ("id")
);

-- changeset ArtemDemyanov:init_link_table
CREATE TABLE IF NOT EXISTS "link"
(
    "id"         bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "url"        text                     NOT NULL UNIQUE,
    "filter"     text                     NULL,
    "created_at" timestamp with time zone NOT NULL,
    "checked_at" timestamp with time zone NOT NULL,
    "updated_at" timestamp with time zone NOT NULL
);

-- changeset ArtemDemyanov:init_tag_table
CREATE TABLE IF NOT EXISTS "tag"
(
    "id" bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "name" text NOT NULL UNIQUE
);

-- changeset ArtemDemyanov:init_link_tag_table
CREATE TABLE IF NOT EXISTS "link_tag"
(
    "id"      bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    "link_id" bigint NOT NULL REFERENCES "link" ("id") ON DELETE CASCADE,
    "tag_id"  bigint NOT NULL REFERENCES "tag" ("id") ON DELETE CASCADE,
    "chat_id" bigint NOT NULL REFERENCES "chat" ("id") ON DELETE CASCADE
    );

-- changeset ArtemDemyanov:init_tracking_table
CREATE TABLE IF NOT EXISTS "chat_link"
(
    "chat_id" bigint NOT NULL REFERENCES "chat" ("id") ON DELETE CASCADE,
    "link_id" bigint NOT NULL REFERENCES "link" ("id") ON DELETE CASCADE,
    PRIMARY KEY ("chat_id", "link_id")
    );

-- changeset ArtemDemyanov:init_indexes
CREATE INDEX IF NOT EXISTS idx_link_url ON "link" ("url");
CREATE INDEX IF NOT EXISTS idx_tag_name ON "tag" ("name");
