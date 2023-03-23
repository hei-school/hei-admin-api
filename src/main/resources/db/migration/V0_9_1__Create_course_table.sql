CREATE TABLE IF NOT EXISTS "course" (
                                        id VARCHAR PRIMARY KEY,
                                        code TEXT NOT NULL,
                                        name TEXT NOT NULL,
                                        credits INTEGER NOT NULL,
                                        total_hours INTEGER NOT NULL,
                                        teacher_id VARCHAR  REFERENCES "user"(id)
    );