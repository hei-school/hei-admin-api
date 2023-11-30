create table if not exists "create_fee_type" (
    id                    varchar
    constraint create_fee_type_pk primary key default uuid_generate_v4(),
    name             varchar                     not null
);

create table if not exists "fee_type_component" (
    id                    varchar
    constraint fee_type_component_pk primary key default uuid_generate_v4(),
    type                    fee_type                    not null,
    monthly_amount             int                     not null,
    name             varchar                     not null,
    months_number             int                     not null,
    fee_type_id           varchar                  not null
    constraint create_fee_type_fee_type_component_id_fk references "create_fee_type"(id)
);


