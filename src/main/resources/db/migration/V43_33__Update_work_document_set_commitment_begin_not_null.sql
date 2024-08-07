-- Step 1: Update the `commitment_begin` column with `creation_datetime` where `commitment_begin` is NULL
update work_document
set commitment_begin = creation_datetime
where commitment_begin is null;

-- Step 2: Alter the `commitment_begin` column to set the NOT NULL constraint
alter table work_document
    alter column commitment_begin set not null;