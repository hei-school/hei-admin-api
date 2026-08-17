ALTER TABLE documenso_document
    ADD COLUMN IF NOT EXISTS generated_by VARCHAR REFERENCES "user" (id);
