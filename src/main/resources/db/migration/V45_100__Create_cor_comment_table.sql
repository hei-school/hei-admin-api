CREATE TABLE public.cor_comment
(
    id                varchar  NOT NULL        default uuid_generate_v4(),
    creation_datetime timestamp with time zone default now(),
    comment           text     NOT NULL,
    cor_id            varchar  REFERENCES cor (id) NOT NULL,
    status            cor_status NOT NULL default 'IN_PROGRESS'::cor_status,
    CONSTRAINT cor_comment_pk PRIMARY KEY (id)
);