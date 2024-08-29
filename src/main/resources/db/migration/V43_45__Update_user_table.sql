alter table "user" add column monitor_id varchar;

alter table "user"
add constraint fk_monitor
foreign key (monitor_id)
references "user"(id);
