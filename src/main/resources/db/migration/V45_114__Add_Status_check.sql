CREATE TYPE status_check_result AS ENUM ('PENDING', 'RESOLVED', 'WITHDRAWN');

CREATE TABLE status_check (
          id                   VARCHAR PRIMARY KEY DEFAULT uuid_generate_v4(),
          concerned_student_id VARCHAR      NOT NULL REFERENCES "user" (id),
          requesting_user_id   VARCHAR      NOT NULL REFERENCES "user" (id),
          description          TEXT         NOT NULL,
          result               status_check_result NOT NULL DEFAULT 'PENDING',
          creation_datetime    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
          update_datetime      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
