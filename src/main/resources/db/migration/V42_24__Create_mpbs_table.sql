do
$$
begin
        if not exists(select from pg_type where typname = 'mobile_money_type') then
create type "mobile_money_type" as enum ('MVOLA', 'ORANGE_MONEY', 'AIRTEL_MONEY');
end if;
end
$$;

create table if not exists "mpbs" (
    id varchar constraint mpbs_pk primary key default uuid_generate_v4(),
    mobile_money_type mobile_money_type not null,
    psp_id varchar not null,
    amount integer,
    creation_datetime timestamp with time zone default now(),
    successfully_verified_on timestamp with time zone,
    student_id varchar not null constraint mpbs_student_id_fk references "user"(id),
    fee_id varchar not null constraint mpbs_fee_id_fk references "fee"(id)
);