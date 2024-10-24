alter table "letter" add column if not exists event_participant_id varchar;
alter table "letter" add constraint ep_fk foreign key (event_participant_id) references "event_participant"("id");