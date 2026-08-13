DO
$$
BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'documenso_document_status'
        ) THEN
CREATE TYPE "documenso_document_status"
    AS ENUM ('PENDING', 'COMPLETED', 'REJECTED');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS documenso_document
(
    id                    VARCHAR CONSTRAINT pk_documenso_document
    PRIMARY KEY DEFAULT uuid_generate_v4(),
    documenso_document_id BIGINT NOT NULL UNIQUE,
    documenso_template_id VARCHAR REFERENCES documenso_template (id) NOT NULL,
    student_id            VARCHAR REFERENCES "user" (id) NOT NULL,
    level                 VARCHAR,
    status documenso_document_status NOT NULL DEFAULT 'PENDING',
    file_info_id            VARCHAR REFERENCES "file_info" (id),
    creation_datetime     TIMESTAMP WITH TIME ZONE DEFAULT now(),
    completed_datetime    TIMESTAMP WITH TIME ZONE
);