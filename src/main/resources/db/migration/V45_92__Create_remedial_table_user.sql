create table if not exists "student_remedials" (
                                                 remedial_id varchar not null,
                                                 user_id varchar not null,
                                                 primary key (remedial_id, user_id),
    constraint fk_remedial foreign key (remedial_id)
    references "remedial"(id),
    constraint fk_user foreign key (user_id)
    references "user"(id)
    );
