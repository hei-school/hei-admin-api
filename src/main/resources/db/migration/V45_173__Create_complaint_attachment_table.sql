CREATE TABLE IF NOT EXISTS complaint_attachment
(
    id                 VARCHAR CONSTRAINT complaint_attachment_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    complaint_id       VARCHAR REFERENCES complaint (id)                      NOT NULL,
    file_name          VARCHAR                                                NOT NULL,
    file_path          VARCHAR                                                NOT NULL,
    uploaded_by_id     VARCHAR REFERENCES "user" (id),
    creation_datetime  TIMESTAMP WITH TIME ZONE DEFAULT now()
);
