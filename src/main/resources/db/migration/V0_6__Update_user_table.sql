alter table "user" add column group_id varchar;
alter table "user" add constraint fk_user_group foreign key(group_id) references "group"(id);
