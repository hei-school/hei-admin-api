CREATE TABLE IF NOT EXISTS cor_interviewers
(
    id      VARCHAR CONSTRAINT cor_interviewers_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id VARCHAR REFERENCES "user" (id)         NOT NULL,
    cor_id  VARCHAR REFERENCES "cor" (id)          NOT NULL
)