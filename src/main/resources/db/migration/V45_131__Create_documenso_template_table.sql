CREATE TABLE IF NOT EXISTS documenso_template
(
    id                    VARCHAR
    CONSTRAINT pk_documenso_template
    PRIMARY KEY DEFAULT uuid_generate_v4(),

    documenso_template_id BIGINT NOT NULL UNIQUE,

    title                 VARCHAR NOT NULL,

    type                  VARCHAR,

    admin_id              VARCHAR
    REFERENCES "user" (id),

    creation_datetime     TIMESTAMP WITH TIME ZONE DEFAULT now()
    );