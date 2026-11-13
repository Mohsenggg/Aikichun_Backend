-- ============================================================
-- V5__domain_and_subscription_schema.sql
-- Additive migration for existing and fresh databases
-- ============================================================

-- 1. Update users table constraints and add subscription dates
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check CHECK (role IN ('ADMIN', 'MODERATOR', 'MEMBER'));

ALTER TABLE users DROP CONSTRAINT IF EXISTS chk_account_status;
ALTER TABLE users ADD CONSTRAINT chk_account_status CHECK (account_status IN ('PENDING_VERIFICATION', 'ACTIVE', 'INACTIVE', 'BLOCKED'));

-- Migrate legacy status values if any exist in the database
UPDATE users SET account_status = 'BLOCKED' WHERE account_status IN ('SUSPENDED', 'BANNED');

-- Add subscription date ranges
ALTER TABLE users ADD COLUMN IF NOT EXISTS subscription_start_date DATE;
ALTER TABLE users ADD COLUMN IF NOT EXISTS subscription_end_date DATE;

-- 2. Domain Tables: Roadmaps Hierarchy
CREATE TABLE IF NOT EXISTS roadmaps (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    access_mode VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_roadmaps_access_mode CHECK (access_mode IN ('OPEN', 'PROGRESSIVE'))
);

CREATE TABLE IF NOT EXISTS levels (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    roadmap_id BIGINT NOT NULL REFERENCES roadmaps(id) ON DELETE CASCADE,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    position INTEGER NOT NULL,
    link VARCHAR(500),
    weight NUMERIC(8,2) NOT NULL DEFAULT 10.00,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_levels_roadmap_id ON levels(roadmap_id);

CREATE TABLE IF NOT EXISTS priority_stages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    color VARCHAR(30),
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS roadmap_stages (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    level_id BIGINT NOT NULL REFERENCES levels(id) ON DELETE CASCADE,
    priority_stage_id BIGINT REFERENCES priority_stages(id) ON DELETE SET NULL,
    code VARCHAR(50),
    name VARCHAR(150),
    link VARCHAR(500),
    weight NUMERIC(8,2) NOT NULL DEFAULT 10.00,
    position INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_stages_level_id ON roadmap_stages(level_id);
CREATE INDEX IF NOT EXISTS idx_stages_priority_stage_id ON roadmap_stages(priority_stage_id);

CREATE TABLE IF NOT EXISTS tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    code VARCHAR(50),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    link VARCHAR(500),
    weight NUMERIC(8,2) DEFAULT 10.00,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS roadmap_stage_tasks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    stage_id BIGINT NOT NULL REFERENCES roadmap_stages(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    position INTEGER NOT NULL,
    is_main BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_stage_task UNIQUE (stage_id, task_id)
);
CREATE INDEX IF NOT EXISTS idx_stage_tasks_stage_id ON roadmap_stage_tasks(stage_id);
CREATE INDEX IF NOT EXISTS idx_stage_tasks_task_id ON roadmap_stage_tasks(task_id);

CREATE TABLE IF NOT EXISTS check_definitions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS task_check_assignments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    check_definition_id BIGINT NOT NULL REFERENCES check_definitions(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_task_check_assignment UNIQUE (task_id, check_definition_id)
);
CREATE INDEX IF NOT EXISTS idx_assignment_task_id ON task_check_assignments(task_id);
CREATE INDEX IF NOT EXISTS idx_assignment_check_id ON task_check_assignments(check_definition_id);

CREATE TABLE IF NOT EXISTS trainee_task_progress (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trainee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    knowledge BOOLEAN NOT NULL DEFAULT FALSE,
    practice BOOLEAN NOT NULL DEFAULT FALSE,
    pressure BOOLEAN NOT NULL DEFAULT FALSE,
    application BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL DEFAULT 'NOT_STARTED',
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_trainee_task UNIQUE (trainee_id, task_id),
    CONSTRAINT chk_progress_status CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED'))
);
CREATE INDEX IF NOT EXISTS idx_progress_trainee_id ON trainee_task_progress(trainee_id);
CREATE INDEX IF NOT EXISTS idx_progress_task_id ON trainee_task_progress(task_id);
CREATE INDEX IF NOT EXISTS idx_progress_trainee_status ON trainee_task_progress(trainee_id, status);

CREATE TABLE IF NOT EXISTS trainee_task_check_progress (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    trainee_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    task_id BIGINT NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    check_definition_id BIGINT NOT NULL REFERENCES check_definitions(id) ON DELETE CASCADE,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_trainee_task_check UNIQUE (trainee_id, task_id, check_definition_id)
);
CREATE INDEX IF NOT EXISTS idx_check_prog_trainee_id ON trainee_task_check_progress(trainee_id);
CREATE INDEX IF NOT EXISTS idx_check_prog_task_id ON trainee_task_check_progress(task_id);
CREATE INDEX IF NOT EXISTS idx_check_prog_check_id ON trainee_task_check_progress(check_definition_id);

