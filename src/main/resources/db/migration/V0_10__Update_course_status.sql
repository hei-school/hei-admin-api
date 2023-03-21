do
$$
    begin
        if not exists(select from pg_type where typname = 'course_status') then
            create type "fee_status" as enum ('UNLINKED', 'LINKED');
        end if;
    end
$$;

alter table "course" add column if not exists status course_status default 'UNLINKED';
create index if not exists course_status_index on "course" (status);
alter table "course" add column if not exists user_id varchar
    constraint fee_user_id_fk references "user"(id);