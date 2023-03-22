CREATE TABLE IF NOT EXISTS "course" (
                                        id SERIAL PRIMARY KEY,
                                        code TEXT NOT NULL,
                                        name TEXT NOT NULL,
                                        credits INTEGER NOT NULL,
                                        total_hours INTEGER NOT NULL,
                                        teacher_id INTEGER REFERENCES user(id)
    );