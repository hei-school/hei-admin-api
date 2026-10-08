delete from "retake_exam" re
using (
    select id,
           row_number() over (
               partition by course_id, session_id, student_id
               order by registration_date asc nulls last, id
           ) as rn
    from "retake_exam"
) dup
where re.id = dup.id
  and dup.rn > 1;

alter table "retake_exam"
    add constraint retake_exam_course_id_session_id_student_id_unique
        unique (course_id, session_id, student_id);
