ALTER TABLE documenso_document
    ADD COLUMN IF NOT EXISTS archived_datetime TIMESTAMP WITH TIME ZONE;
