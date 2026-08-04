alter table fee add column if not exists "is_archived" boolean default false;
update fee set is_archived = false where is_archived is null;
