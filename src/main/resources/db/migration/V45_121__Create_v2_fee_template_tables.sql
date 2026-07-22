create table if not exists "v2_fee_template"(
    id varchar
        constraint v2_fee_template_pkey primary key default uuid_generate_v4(),
    label varchar,
    type fee_type not null,
    category fee_category not null,
    creation_datetime timestamp with time zone not null default now()
);

create table if not exists "v2_fee_template_content"(
    id varchar
        constraint v2_fee_template_content_pkey primary key default uuid_generate_v4(),
    id_fee_template varchar
        constraint v2_fee_template_content_template_fkey references "v2_fee_template"(id),
    label varchar,
    amount numeric,
    due_date date
);
