create table if not exists "mpbs_verification" (
                                                   id varchar constraint mpbs_verification_pk primary key default uuid_generate_v4(),
    mobile_money_type mobile_money_type not null,
    psp_id varchar not null,
    amount_of_fee_remaining_payment integer,
    amount_in_psp integer,
    creation_datetime_of_payment_in_psp timestamp with time zone,
    creation_datetime_of_mpbs timestamp with time zone not null,
    creation_datetime timestamp with time zone default now(),
    comment varchar,
    student_id varchar not null constraint mpbs_student_id_fk references "user"(id),
    fee_id varchar not null constraint mpbs_fee_id_fk references "fee"(id)
<<<<<<< HEAD
    );
=======
);
>>>>>>> 12d9e30 (feat: crupdate and get mbps)
