DO
$$
    BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'complaint_type') THEN
            CREATE TYPE "complaint_type" AS ENUM ('ACADEMIC', 'FINANCIAL');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS complaint_routing
(
    id                 VARCHAR CONSTRAINT complaint_routing_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    type               complaint_type                                     NOT NULL,
    manager_id         VARCHAR REFERENCES "user" (id)                     NOT NULL,
    creation_datetime  TIMESTAMP WITH TIME ZONE DEFAULT now()
);
