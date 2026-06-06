-- Sprint answers table: records each question attempt within a sprint session
CREATE TABLE IF NOT EXISTS sprint_answers (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    session_id      UUID NOT NULL REFERENCES sprint_sessions(id) ON DELETE CASCADE,
    problem_id      UUID NOT NULL REFERENCES problems(id),
    status          VARCHAR(20) NOT NULL,
    marks_awarded   INT NOT NULL DEFAULT 0,
    time_taken_secs INT,
    answered_at     TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_sprint_answers_session ON sprint_answers(session_id);
CREATE INDEX idx_sprint_answers_session_problem ON sprint_answers(session_id, problem_id);
