# PreparEx — Feature Implementation Plan

> **Stack:** Spring Boot 3 / Java 21 · PostgreSQL + Flyway · Redis · Kafka  
> **Package:** `com.preparex.preparex_backend`  
> Step-by-step prompts for Cursor / Windsurf — test one phase before starting the next.

---

## Table of Contents

1. [How To Use This Document](#1-how-to-use-this-document)
2. [Architecture & Tech Decisions](#2-architecture--tech-decisions)
3. [Phase 1 — Problem Listing, Filters, Search & Problem Sets](#3-phase-1--problem-listing-filters-search--problem-sets)
4. [Phase 2 — Submissions, Daily Challenge & Streak](#4-phase-2--submissions-daily-challenge--streak)
5. [Phase 3 — Contest Engine](#5-phase-3--contest-engine)
6. [Phase 4 — Sprint Mode](#6-phase-4--sprint-mode)
7. [Phase 5 — User Profile & Analytics](#7-phase-5--user-profile--analytics)
8. [Summary](#8-summary)

---

## 1. How To Use This Document

Each phase contains:
- A plain-English summary of what gets built
- A complete, copy-paste Cursor/Windsurf prompt with all context
- A test checklist to verify before moving to the next phase

> **Golden Rules**
> - Complete and verify Phase N fully before starting Phase N+1
> - Auth (already built) is Phase 0 — all phases below depend on it
> - Every prompt references your existing package: `com.preparex.preparex_backend`
> - Your stack: Spring Boot 3 / Java 21 · PostgreSQL + Flyway · Redis · Kafka
> - Run `./gradlew test` after each phase — fix failures before proceeding
> - Cursor context: always open the relevant module files alongside the prompt

---

## 2. Architecture & Tech Decisions

### 2.1 Database Responsibilities

| Store | What lives here | Why |
|---|---|---|
| **PostgreSQL** | users, problems, topics, subjects, submissions, contest metadata, daily_problems, daily_completions, user_streaks, contest_results, user_profiles, user_badges, user_solved_stats, user_subject_stats | Source of truth for ALL data — relational + profile |
| **Redis** | Sessions, OTPs, problem cache, leaderboard ZSets, rate limits, RLocks, profile stats cache | Speed layer — sub-millisecond reads for hot data |
| **Kafka** | contest-submissions, contest-ended, submission-saved, badge-events, notification-dispatch | Decouple HTTP response from expensive async work |
| **S3** | Problem figures, diagrams, user avatars | Binary assets — never store in DB |

### 2.2 Premium / Paid Access Model

- **FREE** — access to standard problems, non-premium sets, free contests, sprint
- **PREMIUM** — unlocks premium problem sets, premium contests, advanced analytics
- Problems and sets have an `isPremium` boolean; contests have `accessType` enum: `FREE | PREMIUM | PAID`
- Middleware `PremiumAccessGuard` checks `user.subscriptionTier` before returning gated content

### 2.3 Design Patterns in Use

| Pattern | Where used |
|---|---|
| Strategy | Auth (already built) + Contest scoring (JEE Mains vs Advanced marking schemes) |
| Factory | Question type deserialisation — MCQ / Numerical / Paragraph / MultiCorrect |
| Observer (Spring Events) | Post-submission: streak update, badge check, analytics update, leaderboard update |
| Template Method | ContestResultCalculator — shared pipeline, per-exam scoring override |
| Decorator | CachedProblemService wraps ProblemService with Redis |
| Chain of Responsibility | Contest submission validation — timing, registration, duplicate, format |

---

## 3. Phase 1 — Problem Listing, Filters, Search & Problem Sets

**Timeline: Weeks 1–3**

### What gets built

- Flyway migrations: subjects, topics, problems tables
- Problem entity with all question types (MCQ_SINGLE, MCQ_MULTIPLE, NUMERICAL, PARAGRAPH, ASSERTION)
- Full REST API: list problems with filters, get single problem, get passage+children
- Redis caching on individual problem fetches (1hr TTL, evict on admin update)
- Problem Sets (curated lists like Mains 300, BITSAT 250) — with completion percentage per user
- Premium gating on problems and sets via `PremiumAccessGuard`
- Admin APIs: create/update/delete problem, create/update problem set

### Entities / Tables

| Table | Key columns | Notes |
|---|---|---|
| `subjects` | id, name, exam_id, display_order | e.g. Physics / JEE |
| `topics` | id, subject_id, name, display_order | e.g. Kinematics |
| `problems` | id, slug, title, body_text, figure_url, question_type, difficulty, options(jsonb), answer_key(jsonb), solution_text, hints(jsonb), subject_id, topic_id, exam_id, pyq_year, parent_id, is_active, is_premium | Core table |
| `problem_sets` | id, slug, title, description, exam_id, is_premium, display_order | e.g. Mains 300 |
| `problem_set_items` | id, set_id, problem_id, position | Join table |

### APIs

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| GET | `/api/v1/problems` | U | `?subject= &topic= &difficulty= &type= &pyq=true &page= &size=` |
| GET | `/api/v1/problems/{id}` | U | Redis cached 1hr. Strips answer_key from response |
| GET | `/api/v1/problems/{id}/solution` | U | Returns solution only if user has a submission for this problem |
| GET | `/api/v1/problems/{id}/hints` | U | Returns hints array; client reveals one at a time |
| GET | `/api/v1/problems/passage/{parentId}` | U | Returns parent + all children in one call |
| GET | `/api/v1/subjects` | U | List subjects filtered by `?examId=` |
| GET | `/api/v1/topics` | U | List topics filtered by `?subjectId=` |
| GET | `/api/v1/problem-sets` | U | List all sets. Premium sets show `locked:true` for free users |
| GET | `/api/v1/problem-sets/{slug}` | U | Set detail + problems. completedCount included for authenticated user |
| GET | `/api/v1/problem-sets/{slug}/progress` | U | `{total, completed, percentage}` for current user |
| POST | `/api/v1/admin/problems` | A | Create problem — accepts jsonb options + answer_key |
| PUT | `/api/v1/admin/problems/{id}` | A | Update — evicts Redis cache on success |
| DELETE | `/api/v1/admin/problems/{id}` | A | Soft delete: sets is_active=false |
| POST | `/api/v1/admin/problem-sets` | A | Create curated set |
| POST | `/api/v1/admin/problem-sets/{id}/problems` | A | Add problem to set |

> **Auth:** U = JWT required, A = Admin role required

### Cursor Prompt — Phase 1

```
// Context: PreparEx backend · Spring Boot 3 · Java 21 · PostgreSQL + Flyway · Redis
// Package: com.preparex.preparex_backend
// Auth is already built (Phase 0). Do NOT touch auth code.
// Base entity: com.preparex.preparex_backend.common.BaseEntity (id UUID, createdAt, updatedAt)

Implement the Problem Listing feature for PreparEx. Build everything below in order.

=== STEP 1: Flyway Migrations ===
Create these migration files in src/main/resources/db/migration/

V4__create_subjects_table.sql
  - id SERIAL PK, name VARCHAR(100) NOT NULL, exam_id VARCHAR(50) NOT NULL,
    display_order INT, UNIQUE(name, exam_id)

V5__create_topics_table.sql
  - id SERIAL PK, subject_id INT FK → subjects, name VARCHAR(150) NOT NULL,
    display_order INT

V6__create_problems_table.sql
  - id UUID PK DEFAULT gen_random_uuid()
  - slug VARCHAR(200) UNIQUE NOT NULL
  - title VARCHAR(500) NOT NULL
  - body_text TEXT NOT NULL (stores LaTeX markdown)
  - figure_url VARCHAR(500)
  - question_type VARCHAR(30) NOT NULL  -- MCQ_SINGLE | MCQ_MULTIPLE | NUMERICAL | PARAGRAPH | ASSERTION
  - difficulty VARCHAR(10) NOT NULL     -- EASY | MEDIUM | HARD
  - options JSONB         -- null for NUMERICAL type
  - answer_key JSONB NOT NULL
  - solution_text TEXT
  - hints JSONB           -- array of strings
  - subject_id INT FK → subjects
  - topic_id INT FK → topics
  - exam_id VARCHAR(50) NOT NULL
  - pyq_year INT          -- null if not PYQ
  - parent_id UUID FK → problems (self-join for PARAGRAPH type)
  - is_active BOOLEAN DEFAULT true
  - is_premium BOOLEAN DEFAULT false
  - attempt_count INT DEFAULT 0
  - correct_count INT DEFAULT 0
  - created_at TIMESTAMP, updated_at TIMESTAMP
  - Index: (topic_id, difficulty), (subject_id, difficulty), (is_active), (exam_id)

V7__create_problem_sets_table.sql
  - id UUID PK, slug VARCHAR(200) UNIQUE, title VARCHAR(255), description TEXT,
    exam_id VARCHAR(50), is_premium BOOLEAN DEFAULT false,
    display_order INT, is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP, updated_at TIMESTAMP

V8__create_problem_set_items_table.sql
  - id SERIAL PK, set_id UUID FK → problem_sets, problem_id UUID FK → problems,
    position INT, UNIQUE(set_id, problem_id)

=== STEP 2: Entities ===
Create JPA entities with Lombok @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
  - entity/Subject.java  (fields from V4 above)
  - entity/Topic.java    (ManyToOne Subject)
  - entity/Problem.java  (all fields; self-join @ManyToOne parent; @OneToMany children)
    - QuestionType enum: MCQ_SINGLE, MCQ_MULTIPLE, NUMERICAL, PARAGRAPH, ASSERTION
    - Difficulty enum: EASY, MEDIUM, HARD
    - Use @JdbcTypeCode(SqlTypes.JSON) for options, answer_key, hints columns
  - entity/ProblemSet.java
  - entity/ProblemSetItem.java  (ManyToOne ProblemSet, ManyToOne Problem)

=== STEP 3: Repositories ===
  - ProblemRepository extends JpaRepository<Problem, UUID>
    - Page<Problem> findByFilters(@Param subject, topic, difficulty, type, pyqOnly, pageable) with @Query
    - Optional<Problem> findBySlug(String slug)
    - List<Problem> findByParentId(UUID parentId)
  - SubjectRepository, TopicRepository, ProblemSetRepository, ProblemSetItemRepository

=== STEP 4: DTOs ===
Request:
  - ProblemFilterDto: subjectId, topicId, difficulty, questionType, pyqOnly(boolean), page, size
  - CreateProblemDto: all problem fields (admin use)
  - CreateProblemSetDto: title, slug, description, examId, isPremium

Response:
  - ProblemListItemDto: id, slug, title, difficulty, questionType, topicName, subjectName,
      isPremium, isSolved(boolean — populated from user submissions), attemptCount
  - ProblemDetailDto: full problem fields EXCEPT answer_key (never expose)
  - ProblemSetDto: id, slug, title, description, isPremium, problemCount,
      completedCount, percentage (last two populated per-user)
  - PassageDto: parent ProblemDetailDto + List<ProblemDetailDto> children

=== STEP 5: Services ===
ProblemService interface + ProblemServiceImpl:
  - Page<ProblemListItemDto> getProblems(ProblemFilterDto filter, UUID userId)
  - ProblemDetailDto getProblemById(UUID id, UUID userId)
  - PassageDto getPassage(UUID parentId)
  - List<String> getHints(UUID problemId, UUID userId)
  - ProblemDetailDto getSolution(UUID problemId, UUID userId)  -- only if submission exists

CachedProblemService (Decorator pattern):
  - Wraps ProblemService with @Primary
  - Caches getProblemById result in Redis under key "problem:{id}" with 1hr TTL
  - @CacheEvict on admin update/delete

ProblemSetService interface + ProblemSetServiceImpl:
  - List<ProblemSetDto> getAllSets(UUID userId, boolean isPremiumUser)
  - ProblemSetDto getSetBySlug(String slug, UUID userId)
  - ProblemSetProgressDto getProgress(String slug, UUID userId)

PremiumAccessGuard component:
  - boolean canAccess(UUID userId, boolean resourceIsPremium)
  - Reads user subscription tier from Redis cache or DB
  - Throws PremiumRequiredException (403) if free user tries premium resource

=== STEP 6: Controllers ===
ProblemController  (/api/v1/problems) — all GET endpoints listed above
ProblemSetController (/api/v1/problem-sets) — all GET endpoints
AdminProblemController (/api/v1/admin/problems) — POST/PUT/DELETE, @PreAuthorize("hasRole('ADMIN')")

=== STEP 7: Redis Config ===
Add to RedisKeyConstants.java:
  PROBLEM_CACHE = "problem:"
  PROBLEM_CACHE_TTL_HOURS = 1

=== STEP 8: Exception ===
Add PremiumRequiredException extends BaseException (HTTP 403)

=== CONSTRAINTS ===
- answer_key MUST NEVER appear in any response DTO
- All endpoints require JWT auth except Swagger
- Page size max 50, default 20
- Soft delete only (is_active = false)
- Follow existing coding standards from CODING_STANDARDS.md
```

### ✅ Phase 1 Test Checklist

Before moving to Phase 2 — verify all of these:

- [ ] Flyway migrations V4–V8 apply cleanly on fresh DB
- [ ] `GET /api/v1/problems` returns paginated list with filter params working
- [ ] `answer_key` field is absent from ALL API responses
- [ ] Premium problem returns 403 for free user, 200 for premium user
- [ ] `GET /api/v1/problems/{id}` second call hits Redis (check logs — no DB query)
- [ ] Admin `PUT /api/v1/admin/problems/{id}` evicts cache (third call hits DB again)
- [ ] `GET /api/v1/problem-sets/{slug}/progress` returns correct percentage
- [ ] Passage endpoint returns parent + children in one response
- [ ] `./gradlew test` passes with no failures

---

## 4. Phase 2 — Submissions, Daily Challenge & Streak

**Timeline: Weeks 4–5**

### What gets built

- Submission API — attempt a problem, get scored, get history
- Scoring engine using Strategy pattern (one strategy per question type)
- Daily Challenge — 3 questions per day (Physics + Chemistry + Maths)
- Daily completion tracking with calendar view (last 90 days)
- Streak tracking — current streak, longest streak, auto-reset logic
- Spring Events: `SubmissionSavedEvent` triggers streak + analytics update

### APIs

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/api/v1/submissions` | U | Body: `{problemId, answer, timeTakenSecs, source}`. Returns `{correct, marksAwarded, explanation}` |
| GET | `/api/v1/submissions/history` | U | `?problemId= &status= &source= &page=` |
| GET | `/api/v1/submissions/stats` | U | Returns subject-wise easy/med/hard solved counts |
| GET | `/api/v1/daily/today` | U | Returns 3 problems (one per subject). Redis cached until midnight |
| POST | `/api/v1/daily/{dailyProblemId}/complete` | U | Marks daily complete, triggers streak update |
| GET | `/api/v1/daily/calendar` | U | Returns `{date: 'SOLVED'|'MISSED'|'FUTURE'}` map for last 90 days |
| GET | `/api/v1/daily/streak` | U | Returns `{currentStreak, longestStreak, lastActiveDate}` |

### Cursor Prompt — Phase 2

```
// Context: PreparEx backend · Phase 1 (problems, sets) already complete and tested
// Package: com.preparex.preparex_backend
// Auth: complete. Problems/Sets: complete. Do NOT modify Phase 1 code.

Implement Submissions, Daily Challenge, and Streak for PreparEx.

=== STEP 1: Flyway Migrations ===
V9__create_submissions_table.sql
  - id UUID PK, user_id UUID FK → users, problem_id UUID FK → problems
  - status VARCHAR(20) NOT NULL  -- CORRECT | WRONG | PARTIAL
  - submitted_answer JSONB NOT NULL
  - marks_awarded INT
  - time_taken_secs INT
  - source VARCHAR(20) NOT NULL   -- PRACTICE | DAILY | CONTEST
  - submitted_at TIMESTAMP DEFAULT now()
  - Index: (user_id, status), (user_id, problem_id), (problem_id, source)

V10__create_daily_problems_table.sql
  - id SERIAL PK, problem_id UUID FK → problems
  - subject_id INT FK → subjects
  - scheduled_date DATE NOT NULL UNIQUE
  - is_active BOOLEAN DEFAULT true, created_at TIMESTAMP

V11__create_daily_completions_table.sql
  - id BIGSERIAL PK, user_id UUID FK → users
  - daily_problem_id INT FK → daily_problems
  - submission_id UUID FK → submissions
  - completed_date DATE NOT NULL
  - completed_at TIMESTAMP
  - UNIQUE(user_id, daily_problem_id)
  - Index: (user_id, completed_date)

V12__create_user_streaks_table.sql
  - id UUID PK, user_id UUID UNIQUE FK → users
  - current_streak INT DEFAULT 0, longest_streak INT DEFAULT 0
  - last_active_date DATE, updated_at TIMESTAMP

=== STEP 2: Entities ===
  - entity/Submission.java (Status enum: CORRECT, WRONG, PARTIAL; Source enum: PRACTICE, DAILY, CONTEST)
  - entity/DailyProblem.java (ManyToOne Problem, ManyToOne Subject)
  - entity/DailyCompletion.java (ManyToOne User, ManyToOne DailyProblem, OneToOne Submission)
  - entity/UserStreak.java (OneToOne User)

=== STEP 3: Scoring Engine — Strategy Pattern ===
Interface: service/strategy/ScoringStrategy.java
  QuestionType getSupportedType();
  ScoringResult score(Object submittedAnswer, Map<String,Object> answerKey);

ScoringResult value object: { boolean correct; int marksAwarded; String explanation; }

Implementations (each @Component):
  McqSingleScoringStrategy  — correct if selected key matches answer_key.correct
  McqMultipleScoringStrategy — full marks only if EXACT set match; partial if partial scoring enabled
  NumericalScoringStrategy  — correct if value within answer_key.tolerance range
  AssertionScoringStrategy  — evaluates assertion+reason combination

ScoringService: resolves strategy by QuestionType from injected Map<QuestionType,ScoringStrategy>

=== STEP 4: Submission Service ===
SubmissionService interface + SubmissionServiceImpl:
  SubmissionResponseDto submit(UUID userId, SubmitRequestDto req)
    1. Load problem (via CachedProblemService)
    2. Resolve ScoringStrategy, call score()
    3. Persist Submission entity
    4. Publish SubmissionSavedEvent via ApplicationEventPublisher
    5. Return result (never expose answer_key)

  Page<SubmissionHistoryDto> getHistory(UUID userId, SubmissionFilterDto filter)
  SubjectStatsDto getStats(UUID userId)

=== STEP 5: Spring Event Listeners ===
SubmissionSavedEvent: carries Submission object

Listeners (each @Component, @EventListener):
  StreakEventListener  — if source==DAILY: update UserStreak (see streak logic below)
  AnalyticsEventListener — increment denorm counters on problem entity (attempt_count, correct_count)

Streak logic in StreakEventListener:
  - Load UserStreak for user (create if absent)
  - If lastActiveDate == today: do nothing (already counted)
  - If lastActiveDate == yesterday: currentStreak++
  - If lastActiveDate < yesterday OR null: currentStreak = 1
  - Update longestStreak if currentStreak > longestStreak
  - Save + evict Redis cache "streak:{userId}"

=== STEP 6: Daily Problem Service ===
DailyProblemService:
  List<DailyProblemDto> getToday(UUID userId)
    - Cache key: "daily:today" TTL = seconds until midnight
    - Returns 3 DailyProblemDto (one per subject: Physics, Chemistry, Maths)
    - Each includes isCompletedByUser boolean

  DailyCompletionResponseDto complete(UUID userId, Integer dailyProblemId, SubmitRequestDto req)
    - Creates Submission with source=DAILY
    - Creates DailyCompletion record
    - Fires SubmissionSavedEvent
    - Returns result

  Map<LocalDate, String> getCalendar(UUID userId)
    - Query daily_completions for last 90 days
    - Map each date to SOLVED / MISSED / FUTURE

  UserStreakDto getStreak(UUID userId)
    - Check Redis "streak:{userId}" first
    - Fall through to DB, cache result 30min

=== STEP 7: Scheduler ===
DailyProblemScheduler (@Scheduled cron="0 0 0 * * *"):
  - Pick one unsolved (by majority) problem per subject for next day
  - Insert into daily_problems table
  - Evict "daily:today" Redis key

=== STEP 8: Controllers ===
SubmissionController (/api/v1/submissions)
DailyController (/api/v1/daily)

=== CONSTRAINTS ===
- answer_key never in any response
- Daily completion is idempotent — duplicate POST returns existing completion
- Streak is updated asynchronously via event — HTTP response never waits for it
- All Redis keys added to RedisKeyConstants.java
```

### ✅ Phase 2 Test Checklist

- [ ] `POST /api/v1/submissions` with correct MCQ answer returns `correct:true` and `marksAwarded`
- [ ] POST same submission twice — second returns duplicate (idempotent for daily)
- [ ] Numerical answer within tolerance returns correct, outside returns wrong
- [ ] `GET /api/v1/daily/today` returns exactly 3 problems, one per subject
- [ ] Complete daily problem → streak increments → `GET /api/v1/daily/streak` reflects it
- [ ] Skip a day → next completion resets streak to 1
- [ ] `GET /api/v1/daily/calendar` returns 90-day map with correct statuses
- [ ] `GET /api/v1/submissions/stats` returns per-subject, per-difficulty counts
- [ ] Scheduler runs at midnight and populates next day
- [ ] `./gradlew test` passes

---

## 5. Phase 3 — Contest Engine

**Timeline: Weeks 6–9**

### What gets built

- Contest entity with state machine: `DRAFT → SCHEDULED → LIVE → ENDED → RESULTS_PUBLISHED`
- Admin creates contests, attaches problems of any type, sets marking scheme
- Student registration with reminder scheduling
- Contest attempt interface — server-enforced timing, per-question submission
- Kafka pipeline: submissions → async scoring → Redis leaderboard → WebSocket broadcast
- Post-contest result calculation using Template Method pattern
- Percentile scoring (JEE Mains style) for applicable contest types
- Paid / premium-only contest gating
- Rewards and leaderboard

### Contest State Machine

| Transition | Trigger | Side Effects |
|---|---|---|
| DRAFT → SCHEDULED | Admin publishes via `PATCH /admin/contests/{id}/publish` | Validates ≥1 problem attached, start time in future |
| SCHEDULED → LIVE | `@Scheduled` job at start time, acquires RLock `contest:state:{id}` | Opens Redis leaderboard ZSet, allows submissions |
| LIVE → ENDED | `@Scheduled` job at end time OR admin force-end | Publishes `contest-ended` Kafka event, locks all submissions |
| ENDED → RESULTS_PUBLISHED | `ContestResultFinalizer` Kafka consumer finishes calculation | Persists ContestResult rows, sends notifications |
| ANY → CANCELLED | Admin API call | Notifies registered users, refunds if paid |

### Kafka Topics

| Topic | Partitions | Producer | Consumer | What happens |
|---|---|---|---|---|
| `contest-submissions` | 3 | ContestSubmissionService | ContestScoreConsumer | Score answer → update Redis ZSet → broadcast via WebSocket |
| `contest-ended` | 1 | ContestScheduler | ContestResultFinalizer | Calculate all results → persist → publish badge-events |
| `contest-reminders` | 1 | ContestScheduler | ReminderDeliveryConsumer | Send push/email reminder 30min before start |
| `badge-events` | 1 | BadgeService | NotificationConsumer | Push badge award notification to user |

### APIs

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| GET | `/api/v1/contests` | U | `?status=upcoming\|live\|ended &page=` |
| GET | `/api/v1/contests/{id}` | U | Detail. Problems visible only after registration + LIVE |
| POST | `/api/v1/contests/{id}/register` | U | Idempotent. RLock on (contestId, userId) |
| GET | `/api/v1/contests/{id}/questions` | U | Only if LIVE + registered. No answer_key |
| POST | `/api/v1/contests/{id}/submit` | U | Async — validate chain → persist → Kafka → 202 Accepted |
| PATCH | `/api/v1/contests/{id}/finalsubmit` | U | Marks user done, locks further submissions |
| GET | `/api/v1/contests/{id}/leaderboard` | U | Reads Redis ZSet. Returns top 50 + caller's rank |
| GET | `/api/v1/contests/{id}/results/me` | U | Personal result after RESULTS_PUBLISHED |
| GET | `/api/v1/contests/{id}/results` | U | Full leaderboard paginated — only after RESULTS_PUBLISHED |
| POST | `/api/v1/admin/contests` | A | Create contest (status=DRAFT) |
| PATCH | `/api/v1/admin/contests/{id}/publish` | A | DRAFT → SCHEDULED |
| POST | `/api/v1/admin/contests/{id}/problems` | A | Attach problems to contest |
| PATCH | `/api/v1/admin/contests/{id}/end` | A | Force end LIVE contest |

### Cursor Prompt — Phase 3

```
// Context: PreparEx · Phases 1+2 complete · Auth complete
// Package: com.preparex.preparex_backend
// New infrastructure: Kafka (already in build.gradle from auth plan), WebSocket (add dependency)

Implement the full Contest Engine for PreparEx. This is the most complex phase. Build in order.

=== STEP 1: Flyway Migrations ===
V13__create_contests_table.sql
  - id UUID PK, title VARCHAR(255), description TEXT
  - type VARCHAR(30) NOT NULL  -- JEE_MAINS_MOCK | JEE_ADVANCED_MOCK | NEET_MOCK | SUBJECT_WISE
  - status VARCHAR(25) DEFAULT 'DRAFT' -- DRAFT|SCHEDULED|LIVE|ENDED|RESULTS_PUBLISHED|CANCELLED
  - exam_id VARCHAR(50), starts_at TIMESTAMP, ends_at TIMESTAMP, duration_mins INT
  - marking_scheme JSONB NOT NULL  -- {"correct":4,"wrong":-1,"unattempted":0}
  - access_type VARCHAR(20) DEFAULT 'FREE'  -- FREE | PREMIUM | PAID
  - paid_amount_inr NUMERIC(8,2)
  - max_participants INT
  - created_at TIMESTAMP, updated_at TIMESTAMP
  - Index: (status), (starts_at)

V14__create_contest_problems_table.sql
  - id SERIAL PK, contest_id UUID FK → contests, problem_id UUID FK → problems
  - position INT, marks INT DEFAULT 4, negative_marks INT DEFAULT 1, section VARCHAR(50)
  - UNIQUE(contest_id, problem_id)

V15__create_contest_registrations_table.sql
  - id BIGSERIAL PK, contest_id UUID FK, user_id UUID FK
  - started BOOLEAN DEFAULT false, started_at TIMESTAMP, final_submitted_at TIMESTAMP
  - registered_at TIMESTAMP DEFAULT now()
  - UNIQUE(contest_id, user_id)
  - Index: (contest_id), (user_id)

V16__create_contest_submissions_table.sql
  - id UUID PK, contest_id UUID FK, user_id UUID FK, problem_id UUID FK
  - status VARCHAR(20), submitted_answer JSONB, marks_awarded INT DEFAULT 0
  - time_taken_secs INT, submitted_at TIMESTAMP DEFAULT now()
  - UNIQUE(contest_id, user_id, problem_id)
  - Index: (contest_id, user_id), (contest_id)

V17__create_contest_results_table.sql
  - id UUID PK, contest_id UUID FK, user_id UUID FK
  - total_score INT, rank INT, percentile FLOAT
  - correct_count INT, wrong_count INT, unattempted_count INT, time_taken_secs INT
  - subject_breakdown JSONB
  - finalized_at TIMESTAMP DEFAULT now()
  - UNIQUE(contest_id, user_id)
  - Index: (contest_id, rank)

=== STEP 2: Entities ===
Contest.java — with ContestType, ContestStatus, AccessType enums
ContestProblem.java — ManyToOne Contest, ManyToOne Problem
ContestRegistration.java — ManyToOne Contest, ManyToOne User
ContestSubmission.java — ManyToOne Contest, User, Problem; reuse Submission.Status enum
ContestResult.java — ManyToOne Contest, User; subject_breakdown as Map<String,Object>

=== STEP 3: Contest Validation Chain (Chain of Responsibility) ===
Interface: ContestSubmissionHandler
  void handle(ContestSubmissionRequest req) throws ContestSubmissionException;
  void setNext(ContestSubmissionHandler next);

Implementations:
  TimeWindowHandler  — checks contest status == LIVE and now() is between starts_at/ends_at
  RegistrationHandler — checks ContestRegistration exists for (contestId, userId)
  DuplicateHandler   — checks no existing ContestSubmission for (contestId, userId, problemId)
  FormatHandler      — validates answer format matches problem questionType

Wire chain in ContestSubmissionService constructor:
  new TimeWindowHandler(new RegistrationHandler(new DuplicateHandler(new FormatHandler(null))))

=== STEP 4: Scoring Strategies for Contests ===
Reuse ScoringStrategy implementations from Phase 2.
ContestScoringService applies negative marking from ContestProblem.negativeMarks.

=== STEP 5: Kafka Configuration ===
Add to application.yml:
  Topics: contest-submissions (3 partitions), contest-ended (1), contest-reminders (1), badge-events (1)

ContestKafkaConfig.java — @Bean NewTopic for each topic

Producers:
  ContestSubmissionProducer — publishes ContestSubmissionEvent to contest-submissions
  ContestEndedProducer — publishes ContestEndedEvent to contest-ended

Consumers:
  ContestScoreConsumer (@KafkaListener, groupId=scoring-group):
    1. Score submission using ContestScoringService
    2. Update ContestSubmission.status and marks_awarded in DB
    3. Update Redis ZSet: ZADD contest:leaderboard:{contestId} {score} {userId}
    4. Broadcast via WebSocket to /topic/contest/{contestId}/leaderboard
    5. Evict user's cached result

  ContestResultFinalizer (@KafkaListener, groupId=result-group):
    Triggered by contest-ended event:
    1. Load all ContestSubmissions for contest
    2. Use ContestResultCalculator (Template Method — see Step 6)
    3. Persist ContestResult rows for all participants
    4. Update Contest.status = RESULTS_PUBLISHED
    5. Publish badge-events for rank 1, top 10%, etc.

=== STEP 6: Template Method — Result Calculators ===
Abstract class ContestResultCalculator:
  final ContestResultSummary calculate(UUID contestId)  // template method
    collectSubmissions() → applyMarkingScheme() → computeRanks() → computePercentiles() → buildResults()
  abstract List<ParticipantScore> applyMarkingScheme(List<ContestSubmission> subs)

JeeMainsCalculator extends ContestResultCalculator — +4/-1
JeeAdvancedCalculator extends ContestResultCalculator — multi-correct with partial negative
ContestResultCalculatorFactory — returns correct calculator based on ContestType

=== STEP 7: LeaderboardService ===
Redis ZSet operations:
  void updateScore(UUID contestId, UUID userId, int score)
    → ZADD contest:leaderboard:{contestId} {score} {userId}
  Long getRank(UUID contestId, UUID userId)
    → ZREVRANK (0-indexed, return +1 for display)
  List<LeaderboardEntryDto> getTop50(UUID contestId)
    → ZREVRANGEWITHSCORES contest:leaderboard:{contestId} 0 49
    → Enrich each entry with username+avatar from Redis or DB

=== STEP 8: WebSocket Config ===
Add spring-boot-starter-websocket to build.gradle

WebSocketConfig.java:
  @EnableWebSocketMessageBroker
  configureMessageBroker: enableSimpleBroker("/topic"), setApplicationDestinationPrefixes("/app")
  registerStompEndpoints: "/ws" with SockJS fallback

LeaderboardBroadcaster:
  void broadcastUpdate(UUID contestId, List<LeaderboardEntryDto> top50)
    → messagingTemplate.convertAndSend("/topic/contest/{contestId}/leaderboard", top50)

=== STEP 9: Contest Scheduler ===
ContestScheduler (@Scheduled fixedDelay=60000 — check every minute):
  - Query contests WHERE status=SCHEDULED AND starts_at <= now()
  - For each: acquire RLock "contest:state:{id}", transition to LIVE
  - Query contests WHERE status=LIVE AND ends_at <= now()
  - For each: acquire RLock, transition to ENDED, publish contest-ended Kafka event

ReminderScheduler (@Scheduled fixedDelay=60000):
  - Query SCHEDULED contests where starts_at BETWEEN now() AND now()+31min
  - Publish to contest-reminders Kafka topic for registered users

=== STEP 10: Redis RLocks (Redisson) ===
Add redisson-spring-boot-starter to build.gradle

Use RLock for:
  1. Contest registration: "contest:register:{contestId}:{userId}" — prevent double-registration
  2. Contest state transition: "contest:state:{contestId}" — prevent race between scheduler and admin
  3. Result finalization: "contest:finalize:{contestId}" — prevent duplicate finalization

=== STEP 11: Contest Controllers ===
ContestController (/api/v1/contests) — all student endpoints
AdminContestController (/api/v1/admin/contests) — admin endpoints with @PreAuthorize("hasRole('ADMIN')")

=== CONSTRAINTS ===
- Contest submission HTTP response: 202 Accepted immediately — never wait for Kafka consumer
- answer_key NEVER in any response
- Server enforces timing — client timer is display only
- Premium/Paid check in register endpoint before creating registration
- All RLock usages must have tryLock with timeout + finally unlock
```

### ✅ Phase 3 Test Checklist

- [ ] Admin creates contest → status=DRAFT. Publish → status=SCHEDULED
- [ ] Scheduler transitions SCHEDULED→LIVE at correct time
- [ ] `POST /api/v1/contests/{id}/register` returns 200. Second call returns same (idempotent)
- [ ] Premium contest registration fails for free user (403)
- [ ] Contest submission returns 202 immediately — Kafka consumer processes asynchronously
- [ ] `GET /api/v1/contests/{id}/leaderboard` returns correct ranking from Redis ZSet
- [ ] WebSocket client receives leaderboard update within 2s of submission
- [ ] Scheduler ends contest → `contest-ended` Kafka event fires → `ContestResultFinalizer` runs
- [ ] ContestResult rows created for all participants with correct rank + percentile
- [ ] Concurrent registrations — no duplicate registration rows (RLock working)
- [ ] `./gradlew test` passes

---

## 6. Phase 4 — Sprint Mode

**Timeline: Week 10**

### What gets built

- 30-minute timed blitz session — questions pop one at a time
- Server-side session management — start, answer, skip, end
- Intelligent question selection — ELO-bounded difficulty randomisation
- Weekly/monthly leaderboard with points and Sprint Champion badge
- Redis ZSet for leaderboard + RLock on session creation

### Sprint Flow

| Step | Action | Implementation |
|---|---|---|
| 1 | `POST /sprint/start` | Creates SprintSession in Redis. RLock prevents duplicate sessions. Returns sessionId + first question |
| 2 | `POST /sprint/{sessionId}/answer` | Scores answer, awards points, returns next question. Updates Redis session state |
| 3 | `POST /sprint/{sessionId}/skip` | Decrements skip counter (max 5), queues question to return later |
| 4 | Session ends (30min or user quits) | `POST /sprint/{sessionId}/end` — returns summary, updates weekly ZSet |
| 5 | Weekly reset | `@Scheduled` every Monday midnight — snapshot winner, award badge, reset ZSet |

### Cursor Prompt — Phase 4

```
// Context: PreparEx · Phases 1–3 complete
// Package: com.preparex.preparex_backend

Implement Sprint mode for PreparEx.

=== STEP 1: Flyway Migration ===
V18__create_sprint_sessions_table.sql
  - id UUID PK, user_id UUID FK → users
  - status VARCHAR(20) DEFAULT 'ACTIVE'  -- ACTIVE | COMPLETED | ABANDONED
  - subject_filter VARCHAR(50)  -- null = all subjects
  - difficulty_filter VARCHAR(10)  -- null = mixed
  - total_questions_attempted INT DEFAULT 0
  - total_correct INT DEFAULT 0
  - total_wrong INT DEFAULT 0
  - total_skipped INT DEFAULT 0
  - sprint_points INT DEFAULT 0
  - started_at TIMESTAMP DEFAULT now(), ended_at TIMESTAMP
  - exam_id VARCHAR(50)

V19__create_sprint_answers_table.sql
  - id UUID PK, session_id UUID FK → sprint_sessions
  - problem_id UUID FK → problems
  - status VARCHAR(20)  -- CORRECT | WRONG | SKIPPED
  - marks_awarded INT DEFAULT 0, time_taken_secs INT
  - answered_at TIMESTAMP DEFAULT now()

=== STEP 2: Redis Data Model ===
SprintSessionState stored under key "sprint:session:{sessionId}" (TTL 35min):
  - userId, status, subjectFilter, difficultyFilter
  - questionQueue: List<UUID> (pre-generated list of 60 problem IDs)
  - currentIndex: int
  - skipsRemaining: int (default 5)
  - skippedQueue: List<UUID> (problems to recycle)
  - startedAt: Instant
  - sprintPoints: int

=== STEP 3: Point Scoring ===
Point values (add to SprintConstants.java):
  EASY_CORRECT = 5, MEDIUM_CORRECT = 10, HARD_CORRECT = 15
  WRONG = 0, SKIPPED = 0
  FIRST_ATTEMPT_BONUS = 2 (extra points if answered without skipping)
  TIME_BONUS: if answered in < 30s → +2, < 60s → +1

=== STEP 4: Sprint Service ===
SprintService interface + SprintServiceImpl:

  SprintStartResponseDto startSprint(UUID userId, SprintStartRequestDto req):
    1. Check no existing ACTIVE sprint for user (RLock "sprint:user:{userId}")
    2. Generate question queue: select 60 problems from DB filtered by subject/difficulty
       ordered by RANDOM() — bounded to user's ELO tier (simple: map EASY/MEDIUM/HARD to
       user's historical accuracy: >70% = MEDIUM+HARD, <40% = EASY+MEDIUM, else mixed)
    3. Save SprintSessionState to Redis (TTL 35min)
    4. Persist SprintSession row (status=ACTIVE)
    5. Return sessionId + first question (no answer_key)

  SprintAnswerResponseDto answerQuestion(UUID userId, UUID sessionId, SprintAnswerRequestDto req):
    1. Load SprintSessionState from Redis (throw if expired/not found)
    2. Verify userId matches session
    3. Score answer using ScoringService (reuse from Phase 2)
    4. Calculate points with TIME_BONUS and FIRST_ATTEMPT_BONUS
    5. Persist SprintAnswer row
    6. Update SprintSessionState in Redis (increment points, advance index)
    7. Check if 30min elapsed → auto-end if so
    8. Return {result, pointsAwarded, nextQuestion, timeRemainingSecs, sessionStats}

  SprintAnswerResponseDto skipQuestion(UUID userId, UUID sessionId):
    1. Decrement skipsRemaining (throw SprintNoSkipsException if 0)
    2. Move current question to skippedQueue
    3. Advance to next question (from main queue, then recycle skipped)
    4. Return next question

  SprintSummaryDto endSprint(UUID userId, UUID sessionId):
    1. Mark SprintSession status=COMPLETED in DB
    2. Calculate final sprint_points
    3. Update weekly leaderboard: ZADD sprint:leaderboard:weekly:{weekKey} {points} {userId}
    4. Delete session from Redis
    5. Return full summary

=== STEP 5: Weekly Leaderboard ===
Redis keys:
  sprint:leaderboard:weekly:{YYYY-WW}  — ZSet, score=sprint points accumulated this week
  sprint:leaderboard:monthly:{YYYY-MM} — ZSet, score=sprint points accumulated this month

SprintLeaderboardService:
  void addPoints(UUID userId, int points):
    - ZADD with INCR on both weekly and monthly keys
    - Set TTL: weekly key expires in 8 days, monthly in 35 days

  List<SprintLeaderboardEntryDto> getWeeklyTop(int limit):
    - ZREVRANGEWITHSCORES on current week key, enrich with user details

  SprintRankDto getUserRank(UUID userId):
    - ZREVRANK + ZSCORE on current week key

SprintWeeklyReset (@Scheduled cron="0 0 0 * * MON"):
  - Get rank 1 from previous week ZSet
  - Award SPRINT_CHAMPION_WEEKLY badge via BadgeService
  - Log winner (future: send trophy notification)
  - Previous week key auto-expires (TTL already set)

=== STEP 6: Controllers ===
SprintController (/api/v1/sprint):
  POST /sprint/start
  POST /sprint/{sessionId}/answer
  POST /sprint/{sessionId}/skip
  POST /sprint/{sessionId}/end
  GET  /sprint/{sessionId}/status  (returns current state + time remaining)
  GET  /sprint/leaderboard/weekly
  GET  /sprint/leaderboard/monthly

=== STEP 7: Exceptions ===
SprintAlreadyActiveException (409) — user already has active sprint
SprintSessionExpiredException (410) — 30min elapsed
SprintNoSkipsException (422) — no skips remaining
SprintSessionNotFoundException (404)

=== CONSTRAINTS ===
- 30min limit is SERVER-enforced via startedAt in Redis — client timer is display only
- Question queue is generated ONCE at start — not fetched live per question (fast)
- On any sprint answer, check elapsed time first — auto-end if >30min
- Leaderboard points accumulate across multiple sprints in the same week
```

### ✅ Phase 4 Test Checklist

- [ ] `POST /sprint/start` returns sessionId + first question (no answer_key)
- [ ] Answer correctly → points awarded with correct multiplier for difficulty
- [ ] Answer after 30min → auto-end triggered, summary returned
- [ ] `POST /sprint/start` when session already active → 409
- [ ] Skip 5 times → 6th skip returns 422 SprintNoSkipsException
- [ ] Skipped questions recycle back into the queue
- [ ] Weekly leaderboard updates after sprint end — ZREVRANK correct
- [ ] Monday midnight scheduler: previous week champion gets SPRINT_CHAMPION_WEEKLY badge
- [ ] Two concurrent sprint starts for same user → only one succeeds (test RLock)

---

## 7. Phase 5 — User Profile & Analytics

**Timeline: Weeks 11–13**

### What gets built

- PostgreSQL tables for user profile (bio, social links, preferences), solved stats, subject stats, badges
- Pre-computed stats rows updated by Kafka consumers — no dual source of truth
- Profile API: heatmap, streak calendar, solved stats, contest history, subject graph, badges
- Contest ranking graph, score graphs, subject-wise performance — all from PostgreSQL
- Redis caching on hot profile reads with event-driven invalidation
- All data in one system — simpler ops, no cross-DB consistency issues

### PostgreSQL Table Design for Profile

| Table | Key columns | Purpose | Updated by |
|---|---|---|---|
| `user_profiles` | id UUID PK (= users.id), bio, gender, location, date_of_birth, twitter_url, linkedin_url, github_url, instagram_url, theme, email_notifications, push_notifications, daily_reminder_time | User-provided profile data | Direct write via PUT /profile |
| `user_solved_stats` | id UUID PK, user_id UUID UNIQUE FK, total INT, easy INT, medium INT, hard INT | Denormalised solved counts per user | Kafka: SubmissionSavedEvent |
| `user_subject_stats` | id UUID PK, user_id UUID FK, subject_id INT FK, solved INT, attempted INT, accuracy FLOAT — UNIQUE(user_id, subject_id) | Per-subject accuracy — powers radar chart | Kafka: SubmissionSavedEvent |
| `user_badges` | id UUID PK, user_id UUID FK, badge_type VARCHAR(50), context VARCHAR(255) — UNIQUE(user_id, badge_type) | Earned badges | Kafka: BadgeAwardedEvent |
| `user_sprint_stats` | id UUID PK, user_id UUID UNIQUE FK, total_sprints INT, total_points INT, best_weekly_rank INT | Sprint aggregate stats | Kafka: SprintEndedEvent |

### APIs

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| GET | `/api/v1/profile/me` | U | Full profile. Redis cached 15min |
| GET | `/api/v1/profile/{userId}` | U | Public subset — no email/phone |
| PUT | `/api/v1/profile/me` | U | Update user_profiles row only |
| GET | `/api/v1/profile/me/heatmap` | U | Query submissions + daily_completions last 365 days. Redis cached 1hr |
| GET | `/api/v1/profile/me/contest-history` | U | Paginated from contest_results JOIN contests. Redis cached 30min |
| GET | `/api/v1/profile/me/contest-graph` | U | `[{title, score, rank, percentile, date}]` sorted by date for line chart |
| GET | `/api/v1/profile/me/subject-graph` | U | `[{subject, solved, accuracy}]` from user_subject_stats for radar chart |
| GET | `/api/v1/profile/me/badges` | U | Earned from user_badges. Locked = BadgeType.values() minus earned |
| POST | `/api/v1/admin/profile/{userId}/recalculate` | A | Rebuild solved_stats + subject_stats from raw submissions |

### Cursor Prompt — Phase 5

```
// Context: PreparEx · Phases 1–4 complete · Auth complete
// Package: com.preparex.preparex_backend
// Stack: PostgreSQL + Flyway + Redis + Kafka ONLY. No MongoDB. No new DB dependencies.

Implement User Profile and Analytics for PreparEx using PostgreSQL everywhere.

=== STEP 1: Flyway Migrations ===

V20__create_user_profiles_table.sql
  - id UUID PK (same UUID as users.id — one-to-one)
  - bio TEXT
  - gender VARCHAR(20)
  - location VARCHAR(100)
  - date_of_birth DATE
  - twitter_url VARCHAR(255)
  - linkedin_url VARCHAR(255)
  - github_url VARCHAR(255)
  - instagram_url VARCHAR(255)
  - theme VARCHAR(10) DEFAULT 'LIGHT'
  - email_notifications BOOLEAN DEFAULT true
  - push_notifications BOOLEAN DEFAULT true
  - daily_reminder_time VARCHAR(5) DEFAULT '08:00'
  - created_at TIMESTAMP DEFAULT now()
  - updated_at TIMESTAMP DEFAULT now()
  - CONSTRAINT fk_profile_user FOREIGN KEY (id) REFERENCES users(id) ON DELETE CASCADE

V21__create_user_solved_stats_table.sql
  - id UUID PK DEFAULT gen_random_uuid()
  - user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
  - total INT DEFAULT 0, easy INT DEFAULT 0, medium INT DEFAULT 0, hard INT DEFAULT 0
  - updated_at TIMESTAMP DEFAULT now()

V22__create_user_subject_stats_table.sql
  - id UUID PK DEFAULT gen_random_uuid()
  - user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
  - subject_id INT NOT NULL REFERENCES subjects(id)
  - solved INT DEFAULT 0, attempted INT DEFAULT 0, accuracy FLOAT DEFAULT 0.0
  - updated_at TIMESTAMP DEFAULT now()
  - UNIQUE(user_id, subject_id)
  - Index: (user_id)

V23__create_user_badges_table.sql
  - id UUID PK DEFAULT gen_random_uuid()
  - user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE
  - badge_type VARCHAR(50) NOT NULL
  - context VARCHAR(255)
  - awarded_at TIMESTAMP DEFAULT now()
  - UNIQUE(user_id, badge_type)
  - Index: (user_id)

V24__create_user_sprint_stats_table.sql
  - id UUID PK DEFAULT gen_random_uuid()
  - user_id UUID UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE
  - total_sprints INT DEFAULT 0, total_points INT DEFAULT 0
  - best_weekly_rank INT, updated_at TIMESTAMP DEFAULT now()

=== STEP 2: Entities ===

UserProfile.java — @Entity @Table("user_profiles")
  @Id UUID id  -- same as User.id, no @GeneratedValue
  @OneToOne(fetch=LAZY) @MapsId @JoinColumn(name="id") User user
  String bio, gender, location
  LocalDate dateOfBirth
  String twitterUrl, linkedinUrl, githubUrl, instagramUrl
  String theme  (default "LIGHT")
  Boolean emailNotifications, pushNotifications
  String dailyReminderTime

UserSolvedStats.java — @Entity @Table("user_solved_stats")
  @Id UUID id
  @OneToOne(fetch=LAZY) @JoinColumn(name="user_id") User user
  int total, easy, medium, hard

UserSubjectStat.java — @Entity @Table("user_subject_stats")
  @Id UUID id
  @ManyToOne(fetch=LAZY) User user
  @ManyToOne(fetch=LAZY) Subject subject
  int solved, attempted
  double accuracy

UserBadge.java — @Entity @Table("user_badges")
  @Id UUID id
  @ManyToOne(fetch=LAZY) User user
  @Enumerated(STRING) BadgeType badgeType
  String context
  LocalDateTime awardedAt

  BadgeType enum: STREAK_7, STREAK_30, STREAK_100, SOLVED_50, SOLVED_100, SOLVED_500,
    CONTEST_PARTICIPANT, CONTEST_TOP_10_PERCENT, CONTEST_WINNER,
    SPRINT_CHAMPION_WEEKLY, SPRINT_CHAMPION_MONTHLY,
    PHYSICS_MASTER, CHEMISTRY_MASTER, MATHS_MASTER, PERFECT_CONTEST_SCORE

UserSprintStats.java — @Entity @Table("user_sprint_stats")
  @Id UUID id
  @OneToOne(fetch=LAZY) User user
  int totalSprints, totalPoints
  Integer bestWeeklyRank

=== STEP 3: Repositories ===
UserProfileRepository extends JpaRepository<UserProfile, UUID>
UserSolvedStatsRepository — findByUserId(UUID userId)
UserSubjectStatRepository — findAllByUserId, findByUserIdAndSubjectId
UserBadgeRepository — findAllByUserId, existsByUserIdAndBadgeType
UserSprintStatsRepository — findByUserId

=== STEP 4: Kafka Consumers — Update Stats Asynchronously ===

ProfileStatsConsumer (@KafkaListener topics=submission-saved, groupId=profile-stats-pg):
  On SubmissionSavedEvent (source=PRACTICE or DAILY, status=CORRECT):
  1. Load UserSolvedStats (INSERT ... ON CONFLICT DO NOTHING if absent)
  2. Increment total + difficulty bucket (easy/medium/hard)
  3. Save UserSolvedStats
  4. Load UserSubjectStat for (userId, problem.subjectId) — create if absent
  5. Increment solved + attempted, recalculate accuracy = (solved/attempted) * 100.0
  6. Save — evict Redis "profile:stats:{userId}", "profile:subject:{userId}"

  On SubmissionSavedEvent (status=WRONG):
  1. Increment attempted only, recalculate accuracy
  2. Save — evict cache

BadgeConsumer (@KafkaListener topics=badge-events, groupId=badge-pg):
  INSERT INTO user_badges ON CONFLICT (user_id, badge_type) DO NOTHING
  Evict Redis "profile:badges:{userId}"

SprintStatsConsumer (@KafkaListener topics=sprint-ended, groupId=sprint-stats-pg):
  1. Load UserSprintStats (create if absent)
  2. Increment totalSprints, add sprint_points to totalPoints
  3. Update bestWeeklyRank if improved
  4. Save — evict Redis "profile:sprint:{userId}"

=== STEP 5: BadgeService ===
BadgeService:
  void award(UUID userId, BadgeType type, String context):
    1. existsByUserIdAndBadgeType → return silently if already awarded
    2. Persist UserBadge row (UNIQUE constraint as safety net)
    3. Publish BadgeAwardedEvent to Kafka badge-events topic

  void checkAndAwardStreakBadges(UUID userId, int currentStreak)
  void checkAndAwardSolvedBadges(UUID userId, int totalSolved)
  void checkAndAwardContestBadges(UUID userId, UUID contestId, int rank, double percentile)

=== STEP 6: ProfileService ===
UserProfileDto getFullProfile(UUID userId):
  // Check Redis "profile:full:{userId}" first (TTL 15min)
  1. Load User, UserProfile (getOrCreate), UserSolvedStats, UserStreak
  2. Load List<UserSubjectStat>, List<UserBadge>, UserSprintStats
  3. Assemble UserProfileDto, cache in Redis, return

Map<LocalDate, Integer> getHeatmap(UUID userId):
  // Check Redis "profile:heatmap:{userId}" TTL 1hr
  - Query daily_completions last 365 days — group by completed_date
  - Query submissions last 365 days (PRACTICE + CONTEST) — group by DATE(submitted_at)
  - Merge maps, take MAX per date

List<ContestHistoryDto> getContestHistory(UUID userId, Pageable pageable):
  // Check Redis "profile:contest:{userId}" TTL 30min
  SELECT cr.*, c.title FROM contest_results cr JOIN contests c ON c.id=cr.contest_id
  WHERE cr.user_id=? ORDER BY cr.finalized_at DESC

BadgesDto getBadges(UUID userId):
  earned = findAllByUserId
  locked = BadgeType.values() stream minus earned types
  Return {earned, locked}

=== STEP 7: Admin Recalculate Endpoint ===
POST /api/v1/admin/profile/{userId}/recalculate
  1. Delete UserSolvedStats + UserSubjectStat rows for user
  2. Query ALL correct submissions for user
  3. Recompute totals from scratch
  4. Insert fresh rows — evict all Redis profile keys

=== STEP 8: ProfileController ===
GET  /api/v1/profile/me
GET  /api/v1/profile/{userId}
PUT  /api/v1/profile/me
GET  /api/v1/profile/me/heatmap
GET  /api/v1/profile/me/contest-history
GET  /api/v1/profile/me/contest-graph
GET  /api/v1/profile/me/subject-graph
GET  /api/v1/profile/me/badges

=== STEP 9: Redis Keys ===
Add to RedisKeyConstants.java:
  PROFILE_FULL     = "profile:full:"      TTL 15min
  PROFILE_HEATMAP  = "profile:heatmap:"   TTL 1hr
  PROFILE_CONTEST  = "profile:contest:"   TTL 30min
  PROFILE_BADGES   = "profile:badges:"    TTL 30min
  PROFILE_STATS    = "profile:stats:"     TTL 1hr
  PROFILE_SUBJECT  = "profile:subject:"   TTL 1hr
  PROFILE_SPRINT   = "profile:sprint:"    TTL 30min

=== CONSTRAINTS ===
- No MongoDB, no new database dependency — PostgreSQL only
- user_profiles auto-created on first profile read (INSERT ON CONFLICT DO NOTHING)
- PostgreSQL is BOTH source of truth AND read store for stats
- Admin /recalculate endpoint is the recovery mechanism if Kafka consumers miss events
- accuracy stored as 0.0–100.0 float, rounded to 1 decimal in DTO
- No MongoDB imports anywhere in codebase
```

### ✅ Phase 5 Test Checklist

- [ ] `GET /api/v1/profile/me` returns unified response — user + profile + stats in one call
- [ ] First-time profile fetch auto-creates `user_profiles` row with defaults
- [ ] `PUT /api/v1/profile/me` updates bio/social links — next GET reflects changes
- [ ] Solve a problem → Kafka `ProfileStatsConsumer` fires → `solved_stats` row incremented
- [ ] Wrong answer → `attempted` increments, `solved` does not, accuracy recalculates
- [ ] Complete contest → contest_history in `GET /api/v1/profile/me/contest-history`
- [ ] Heatmap returns correct daily counts — verify with known submission dates
- [ ] Badge awarded after 7-day streak → appears in `badges.earned`
- [ ] Locked badges list shrinks as user earns more badges
- [ ] Second `GET /api/v1/profile/me` hits Redis (no DB log) — caching confirmed
- [ ] Admin `POST /recalculate` rebuilds stats and matches Kafka-maintained totals
- [ ] `./gradlew test` passes — no MongoDB imports anywhere in codebase

---

## 8. Summary

### All Phases at a Glance

| Phase | Feature | New Tables | New Kafka Topics | Est. Weeks |
|---|---|---|---|---|
| 0 (done) | Auth — password, OTP, Google SSO | users, user_identities, session_history | — | done |
| 1 | Problem listing, sets, premium gating | subjects, topics, problems, problem_sets, problem_set_items | — | 2 |
| 2 | Submissions, daily challenge, streak | submissions, daily_problems, daily_completions, user_streaks | submission-saved | 2 |
| 3 | Contest engine, Kafka, WebSocket, leaderboard | contests, contest_problems, contest_registrations, contest_submissions, contest_results | contest-submissions, contest-ended, contest-reminders, badge-events | 4 |
| 4 | Sprint mode, weekly leaderboard, badge | sprint_sessions, sprint_answers | sprint-ended | 1 |
| 5 | Profile, analytics, PostgreSQL only | user_profiles, user_solved_stats, user_subject_stats, user_badges, user_sprint_stats | badge-events (consumer), sprint-ended (consumer) | 3 |

**Total: ~12 weeks** at full-time pace, ~22 months at 2hrs/day

### Dependencies to Add to build.gradle

| Dependency | Phase needed | Purpose |
|---|---|---|
| `spring-boot-starter-websocket` | Phase 3 | Contest real-time leaderboard via STOMP/SockJS |
| `redisson-spring-boot-starter` | Phase 3 | Distributed RLocks for contest registration and state transitions |
| `spring-kafka` | Phase 3 | Contest submissions async pipeline |

### Final Production Checklist

- [ ] All Flyway migrations V4–V24 apply cleanly on a fresh database
- [ ] `answer_key` is absent from 100% of API responses (`grep` codebase to verify)
- [ ] All RLock usages have `tryLock` with timeout AND `finally unlock` — no lock leaks
- [ ] Kafka consumers have dead-letter queue config for failed message handling
- [ ] Redis cache TTLs set on every cached key — no unbounded cache growth
- [ ] Admin `/recalculate` endpoint tested — correctly rebuilds solved_stats from raw submissions
- [ ] Rate limiting on OTP, sprint start, and submission endpoints via Redis
- [ ] Admin endpoints all protected with `@PreAuthorize("hasRole('ADMIN')")`
- [ ] All secrets (`JWT_SECRET`, `GOOGLE_CLIENT_ID`, `DB_PASSWORD`, etc.) in env vars only
- [ ] `./gradlew test` passes with >80% coverage on service layer
- [ ] No MongoDB dependency anywhere in `build.gradle` or codebase

---

*PreparEx Implementation Plan — PostgreSQL + Redis + Kafka everywhere*
