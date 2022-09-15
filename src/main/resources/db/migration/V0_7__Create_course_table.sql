CREATE TABLE "course"
(
    id          VARCHAR(255) NOT NULL,
    ref         VARCHAR(255),
    name        VARCHAR(255),
    credits     INTEGER,
    total_hours INTEGER,
    CONSTRAINT pk_course PRIMARY KEY (id)
);