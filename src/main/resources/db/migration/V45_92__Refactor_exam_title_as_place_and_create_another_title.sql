BEGIN;

ALTER TABLE event
    ADD COLUMN room room_name;

ALTER TABLE event
    ADD COLUMN place place_name;

UPDATE event
SET room = CASE
               WHEN room_str = ANY (enum_range(NULL::room_name)::text[])
                   THEN room_str::status_enum
               ELSE NULL
    END;

COMMIT;
