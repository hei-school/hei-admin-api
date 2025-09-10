CREATE OR REPLACE FUNCTION public.get_event_student_attendance(reference character varying,
                                                               event_participant_status attendance_status,
                                                               from_datetime timestamp without time zone,
                                                               to_datetime timestamp without time zone,
                                                               title character varying)
    RETURNS TABLE
            (
                user_reference    character varying,
                attendance_status attendance_status,
                begin_datetime    timestamp with time zone,
                end_datetime      timestamp with time zone,
                event_type        event_type,
                event_title       character varying,
                event_description character varying
            )
    LANGUAGE plpgsql
AS
$function$
BEGIN
RETURN QUERY SELECT "user".ref                 as "user_reference",
                        "event_participant".status as "attendance_status",
                        "event".begin_datetime,
                        "event".end_datetime,
                        "event".type               as "event_type",
                        "event".title              as "event_title",
                        "event".description        as "event_description"
                 FROM "event_participant"
                          JOIN "user" ON "event_participant".participant_id = "user".id
                          JOIN "event" ON "event_participant".event_id = "event".id
                 WHERE "user".ref = reference
                   AND "event_participant".status = event_participant_status
                   AND "event".begin_datetime >= from_datetime
                   AND "event".end_datetime <= to_datetime
                   AND "title" ILIKE title;
END;
$function$