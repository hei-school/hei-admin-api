alter table fee add column if not exists "archive_status" varchar(20);
update fee set archive_status = 'ARCHIVED' where is_archived = true and archive_status is null;
