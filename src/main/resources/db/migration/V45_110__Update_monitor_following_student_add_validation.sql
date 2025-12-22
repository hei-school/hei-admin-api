DO
$$
    BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'mfs_status') THEN
            CREATE TYPE "mfs_status" AS ENUM ('LINKED', 'DENIED', 'PENDING');
        END IF;
    END
$$;

alter table monitor_following_student add column status mfs_status default 'LINKED';
