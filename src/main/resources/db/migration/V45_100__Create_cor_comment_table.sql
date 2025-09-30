CREATE TABLE cor_comment
(
    id                VARCHAR  NOT NULL        DEFAULT uuid_generate_v4(),
    creation_datetime TIMESTAMP WITH TIME ZONE DEFAULT now(),
    comment           TEXT     NOT NULL,
    cor_id            VARCHAR  REFERENCES cor (id) NOT NULL,
    status            cor_status NOT NULL DEFAULT 'IN_PROGRESS',
    CONSTRAINT cor_comment_pk PRIMARY KEY (id)
);