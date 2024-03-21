create table if not exists "event_group_participate" (
    id varchar constraint event_group_pariticipant_pk primary key default uuid_generate_v4(),
    event_id varchar constraint event_participant_id_fk references "event"(id),
    group_id varchar constraint group_id_fk references "group"(id)
);