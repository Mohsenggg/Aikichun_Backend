-- Drop old branch check constraint
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_branch_check;

-- Add new branch check constraint with correct enum value
ALTER TABLE users ADD CONSTRAINT users_branch_check CHECK (branch IN ('SIX_OF_OCTOBER', 'N90_TAGAMO3', 'TAHRIR_DOWNTOWN'));
