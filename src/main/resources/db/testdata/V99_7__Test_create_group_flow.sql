insert into "group_flow"
    (id, group_flow_type, group_id, student_id, flow_datetime)
values ('group_flow1_id', 'JOIN', 'group1_id', 'student1_id', '2021-11-08T08:25:24.00Z'),
       ('group_flow2_id', 'LEAVE', 'group1_id', 'student1_id', '2021-11-09T08:25:24.00Z'),
       ('group_flow3_id', 'JOIN', 'group1_id', 'student1_id', '2021-11-10T08:25:24.00Z'),
       ('group_flow4_id', 'JOIN', 'group2_id', 'student1_id', '2021-11-08T08:25:24.00Z'),
       ('group_flow5_id', 'JOIN', 'group2_id', 'student2_id', '2021-11-08T08:25:24.00Z'),
       ('group_flow6_id', 'JOIN', 'group1_id', 'student2_id', '2021-11-08T08:25:24.00Z'),
       ('group_flow7_id', 'LEAVE', 'group1_id', 'student2_id', '2021-11-09T08:25:24.00Z'),
       ('group_flow8_id', 'JOIN', 'group1_id', 'student3_id', '2021-11-08T08:25:24.00Z');
-- that mean student1 is in group1, group2 but student2 is just in group2, and student3_id is in group1