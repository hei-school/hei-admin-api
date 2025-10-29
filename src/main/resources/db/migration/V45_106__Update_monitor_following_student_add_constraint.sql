ALTER TABLE "monitor_following_student"
    ADD CONSTRAINT mfs_student_monitor_unique
        UNIQUE (student_id, monitor_id);
