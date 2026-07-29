create table credit (
   id varchar constraint credit_pkey primary key default uuid_generate_v4(),
   student_id varchar not null constraint student_fkey references "user"(id),
   amount integer not null,
   creation_datetime timestamp with time zone not null default now()
);

