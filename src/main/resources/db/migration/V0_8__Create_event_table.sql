CREATE TABLE "event"
(
    id                 VARCHAR(255) NOT NULL,
    supervisor_id      VARCHAR(255),
    place_id           VARCHAR(255),
    starting_date_time TIMESTAMP WITHOUT TIME ZONE,
    ending_date_time   TIMESTAMP WITHOUT TIME ZONE,
    event_type         VARCHAR(255),
    title              VARCHAR(255),
    course_id          VARCHAR(255),
    CONSTRAINT pk_event PRIMARY KEY (id)
);

ALTER TABLE "event"
    ADD CONSTRAINT FK_EVENT_ON_COURSE FOREIGN KEY (course_id) REFERENCES course (id);

ALTER TABLE "event"
    ADD CONSTRAINT FK_EVENT_ON_PLACE FOREIGN KEY (place_id) REFERENCES place (id);

ALTER TABLE "event"
    ADD CONSTRAINT FK_EVENT_ON_SUPERVISOR FOREIGN KEY (supervisor_id) REFERENCES "user" (id);