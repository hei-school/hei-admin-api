with ranked as (
    select id,
           event_id,
           participant_id,
           row_number() over (
               partition by event_id, participant_id
               order by
                   case status
                       when 'PRESENT' then 0
                       when 'LATE' then 1
                       when 'MISSING' then 2
                       when 'UNCHECKED' then 3
                       else 4
                   end,
                   id
           ) as rn
    from "event_participant"
),
keepers as (
    select event_id, participant_id, id as keeper_id
    from ranked
    where rn = 1
),
duplicates as (
    select r.id as duplicate_id, k.keeper_id
    from ranked r
    join keepers k on k.event_id = r.event_id and k.participant_id = r.participant_id
    where r.rn > 1
)
update "letter" l
set event_participant_id = d.keeper_id
from duplicates d
where l.event_participant_id = d.duplicate_id;

delete from "event_participant" ep
using (
    select id,
           row_number() over (
               partition by event_id, participant_id
               order by
                   case status
                       when 'PRESENT' then 0
                       when 'LATE' then 1
                       when 'MISSING' then 2
                       when 'UNCHECKED' then 3
                       else 4
                   end,
                   id
           ) as rn
    from "event_participant"
) dup
where ep.id = dup.id
  and dup.rn > 1;

alter table "event_participant"
    add constraint event_participant_event_id_participant_id_unique
        unique (event_id, participant_id);
