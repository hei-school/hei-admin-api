create table if not exists "student_promotion"
(
    id varchar
    constraint has_promotion_pk primary key default uuid_generate_v4
(
),
    user_id varchar
    constraint user_fk references "user"
(
    "id"
),
    promotion_id varchar
    constraint promotion_fk references "promotion"
(
    "id"
)
    );