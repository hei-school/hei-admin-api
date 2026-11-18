alter table "user"
    add column if not exists lms_tmp_password varchar;

do
$$
begin
    if not exists (select from pg_constraint where conname = 'user_lms_tmp_password_uq') then
        alter table "user"
            add constraint user_lms_tmp_password_uq unique (lms_tmp_password);
    end if;
end
$$;

-- Temporary helper: 8 chars = 2 uppercase + 1 special + 5 lowercase/digits, shuffled.
-- Ambiguous characters (O/0, I/l/1) are excluded.
create or replace function pg_temp.generate_lms_tmp_password() returns varchar as
$$
declare
    upper_chars   constant text := 'ABCDEFGHJKLMNPQRSTUVWXYZ';
    special_chars constant text := '!@#$%*?';
    other_chars   constant text := 'abcdefghijkmnopqrstuvwxyz23456789';
    picked text[] := '{}';
begin
    for i in 1..2 loop
        picked := picked || substr(upper_chars, 1 + floor(random() * length(upper_chars))::int, 1);
    end loop;
    picked := picked || substr(special_chars, 1 + floor(random() * length(special_chars))::int, 1);
    for i in 1..5 loop
        picked := picked || substr(other_chars, 1 + floor(random() * length(other_chars))::int, 1);
    end loop;
    return (select string_agg(c, '' order by random()) from unnest(picked) as c);
end;
$$ language plpgsql;

do
$$
declare
    student record;
    candidate varchar;
begin
    for student in
        select u.id
        from "user" u
        where u.role = 'STUDENT'
          and u.is_deleted = false
          and u.lms_tmp_password is null
          and exists (select 1
                      from group_flow gf
                      where gf.student_id = u.id
                        and gf.group_id in ('91d0310f-3b43-4e38-af7b-837152d9122b',
                                            '54511170-af1a-4935-8683-1957599598d6',
                                            'a8bbec09-98b9-4c3a-80e4-523deeb7669f')
                      group by gf.group_id
                      having sum(case when gf.group_flow_type = 'JOIN' then 1 else 0 end) >
                             sum(case when gf.group_flow_type = 'LEAVE' then 1 else 0 end))
    loop
        loop
            candidate := pg_temp.generate_lms_tmp_password();
            exit when not exists (select 1 from "user" where lms_tmp_password = candidate);
        end loop;
        update "user" set lms_tmp_password = candidate where id = student.id;
    end loop;
end
$$;
