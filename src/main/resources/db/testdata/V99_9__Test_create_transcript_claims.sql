insert into "transcript_claim"
(id,reason,claim_status,creation_datetime,closed_datetime,transcript_version_id)
values ('transcript_claim1_id','reason1','OPEN','2022-10-02T08:25:24.00Z',null,'transcript_version1_id'),
       ('transcript_claim2_id','reason2','CLOSE','2022-10-02T08:25:24.00Z','2023-10-02T08:25:24.00Z','transcript_version1_id'),
       ('transcript_claim3_id','reason3','CLOSE','2022-10-03T08:25:24.00Z','2023-10-03T08:25:24.00Z','transcript_version5_id');
