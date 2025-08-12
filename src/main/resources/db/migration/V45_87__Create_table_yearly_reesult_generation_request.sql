CREATE TABLE yearly_result_generation_request
(
  id           VARCHAR NOT NULL DEFAULT uuid_generate_v4() ,
  status       yearly_result_transcript_status,
  datetime     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  file_name    VARCHAR NOT NULL,
  file_info_id VARCHAR,
  CONSTRAINT pk_yearlyresultgenerationrequest PRIMARY KEY (id)
);

ALTER TABLE yearly_result_generation_request
  ADD CONSTRAINT fk_yearlyresultgenerationrequest_on_fileinfo FOREIGN KEY (file_info_id) REFERENCES file_info (id);
