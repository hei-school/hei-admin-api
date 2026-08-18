CREATE TABLE IF NOT EXISTS documenso_document_recipient
(
    id                     VARCHAR
    CONSTRAINT pk_documenso_document_recipient
    PRIMARY KEY DEFAULT uuid_generate_v4(),

    documenso_document_id  VARCHAR REFERENCES documenso_document (id) NOT NULL,
    user_id                VARCHAR REFERENCES "user" (id) NOT NULL,
    documenso_recipient_id BIGINT NOT NULL,
    signing_token          VARCHAR NOT NULL,
    signed_datetime        TIMESTAMP WITH TIME ZONE,

    CONSTRAINT uq_documenso_document_recipient_document_user
    UNIQUE (documenso_document_id, user_id)
);