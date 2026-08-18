-- The column named the person a document is about "student", which will be false as soon as a
-- teacher contract is issued. "subject" also tells it apart from generated_by, the person who asked
-- for the document, and from the recipients who sign it.
ALTER TABLE documenso_document
    RENAME COLUMN student_id TO subject_id;
