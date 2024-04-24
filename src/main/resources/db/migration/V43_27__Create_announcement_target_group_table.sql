create table if not exists announcement_target(
    id varchar constraint announcement_target_pk primary key default uuid_generate_v4(),
    announcement_id varchar constraint announcement_id_fk references "announcement"(id),
    group_id varchar constraint group_target_id_fk references "group"(id)
)