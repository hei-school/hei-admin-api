do
$$
begin
    if not exists(select from pg_type where typname = 'attendance_status') then
create type "attendance_status" as enum ('MISSING', 'LATE', 'PRESENT');
end if;
end
$$;

create table if not exists "event_participant"(
    id varchar constraint event_participant_pk primary key default uuid_generate _v4(),
    event_id varchar constraint event_id_fk references "event"(id),
    participant_id varchar constraint participant_id_fk references "user"(id),
    status attendance_status not null
)