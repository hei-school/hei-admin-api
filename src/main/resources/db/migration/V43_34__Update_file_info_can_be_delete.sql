-- alter table file info
alter table "file_info" add column if not exists is_deleted boolean default false;

-- alter table work document
alter table "work_document" add column if not exists is_deleted boolean default false;