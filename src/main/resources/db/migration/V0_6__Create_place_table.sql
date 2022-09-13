CREATE TABLE IF NOT EXISTS "place"
(
    id       VARCHAR(255) NOT NULL,
    name     VARCHAR(255) NOT NULL,
    room_ref VARCHAR(255),
    CONSTRAINT pk_place PRIMARY KEY (id)
);