BEGIN;

ALTER TABLE event
    ADD COLUMN room room_name;

ALTER TABLE event
    ADD COLUMN place place_name;

UPDATE event
SET room = COALESCE(
        (SELECT rn::room_name
         FROM unnest(enum_range(NULL::room_name)) rn
         WHERE event.title ILIKE rn ::text
        LIMIT 1 ),
    'UNKNOWN'::room_name
);

COMMIT;
