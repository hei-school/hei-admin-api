BEGIN;

ALTER TABLE event
    ADD COLUMN room room_name;

ALTER TABLE event
    ADD COLUMN place place_name;

UPDATE event
SET room = CASE
               WHEN title = ANY (enum_range(NULL::room_name)::text[])
                   THEN title::room_name
               ELSE NULL
    END;

COMMIT;
