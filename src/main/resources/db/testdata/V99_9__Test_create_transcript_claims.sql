insert into "student_transcript_claim"
    (id, transcript_id, student_transcript_version_id, status, creation_datetime, closed_datetime, reason)
values
    ('transcript1_version1_claim1_id', 'transcript1_id', 'transcript1_version1_id', 'OPEN', '2021-11-08T08:25:24.00Z', '2021-11-08T08:25:24.00Z', 'Not okay'),
    ('transcript1_version1_claim2_id', 'transcript1_id', 'transcript1_version1_id', 'CLOSE', '2021-12-10T08:25:25.00Z', '2021-12-10T08:25:25.00Z', 'Okay'),
    ('transcript2_version1_claim1_id', 'transcript2_id', 'transcript2_version1_id', 'CLOSE', '2022-11-11T08:25:26.00Z', '2022-11-11T08:25:26.00Z', 'Okay'),
    ('transcript2_version1_claim2_id', 'transcript2_id', 'transcript2_version1_id', 'CLOSE', '2022-12-12T08:25:26.00Z', '2022-12-12T08:25:26.00Z', 'Okay');