CREATE TYPE cycle_level_enum AS ENUM ('BACHELOR', 'MASTER');

ALTER TABLE promotion
    ADD COLUMN cycle_level cycle_level_enum NOT NULL DEFAULT 'BACHELOR';
