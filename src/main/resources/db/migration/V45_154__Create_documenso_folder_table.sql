CREATE TABLE IF NOT EXISTS documenso_folder
(
    id                  VARCHAR CONSTRAINT pk_documenso_folder PRIMARY KEY DEFAULT uuid_generate_v4(),
    documenso_folder_id VARCHAR NOT NULL UNIQUE,
    path                VARCHAR NOT NULL UNIQUE,
    creation_datetime   TIMESTAMP WITH TIME ZONE DEFAULT now()
);
