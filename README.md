# Aikichun Progress App — Backend (MVP v1)

## Quick Start

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Setup configuration
cp src/main/resources/application.yaml.example src/main/resources/application-dev.yaml
# Edit application-dev.yaml with your credentials

# 3. Build and run
mvn clean spring-boot:run -Dspring.profiles.active=dev

# 4. Run tests
mvn test
```

## Configuration

The project uses environment-specific configuration files:

- **`application.yaml.example`** - Template for git (no sensitive data)
- **`application-dev.yaml`** - Local development config (ignored by git)
- **`application-prod.yaml`** - Production config (ignored by git)

### Setup Steps

1. Copy the example file:
```bash
cp src/main/resources/application.yaml.example src/main/resources/application-dev.yaml
```

2. Edit `application-dev.yaml` with your credentials or use environment variables

### Environment Variables

Override any config value with environment variables:

| Variable | Description |
|---|---|
| `DB_URL` | PostgreSQL connection URL |
| `DB_USERNAME` | Database username |
| `DB_PASSWORD` | Database password |
| `SMTP_HOST` | SMTP server host |
| `SMTP_PORT` | SMTP server port |
| `SMTP_USERNAME` | SMTP username (email) |
| `SMTP_PASSWORD` | SMTP app password |
| `AIKICHUN_JWT_SECRET` | JWT signing secret (min 32 chars) |
| `SMTP_FROM_EMAIL` | From email address |
| `SMTP_FROM_NAME` | From name |

## Authentication Flow

1. **Register** → User created with `PENDING_VERIFICATION` status
2. **Send OTP** → 6-digit code sent to email (expires in 5 minutes)
3. **Verify OTP** → Email verified, status changed to `ACTIVE`, JWT token issued
4. **Login** → Only works for verified active users

## API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register new user (multipart) | Public |
| POST | `/api/v1/auth/send-otp` | Send OTP to email | Public |
| POST | `/api/v1/auth/verify-otp` | Verify OTP and activate account | Public |
| POST | `/api/v1/auth/login` | Login with email/password | Public |
| POST | `/api/v1/auth/logout` | Logout | Public |

## Test Registration (curl)

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -F "fullName=Kareem Khater" \
  -F "email=karee@example.com" \
  -F "password=Secret123" \
  -F "dateOfBirth=1999-01-15" \
  -F "profession=Engineer" \
  -F "branch=SIX_OF_OCTOBER" \
  -F "phoneNumber=+201234567890" \
  -F "phoneNumberEmergency=+201234567891" \
  -F "hasChronicConditionOrInjury=false" \
  -F "subscriptionStartMonth=1" \
  -F "subscriptionStartYear=2026" \
  -F "profilePhoto=@/path/to/profile.jpg" \
  -F "idPhoto=@/path/to/id.jpg"
```

## Test OTP Flow (curl)

```bash
# Send OTP
curl -X POST http://localhost:8080/api/v1/auth/send-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"kareem@example.com"}'

# Verify OTP (use code from email)
curl -X POST http://localhost:8080/api/v1/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{"email":"kareem@example.com","otp":"123456"}'
```

## Test Login (curl)

```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"kareem@example.com","password":"Secret123"}'
```

## Branches

- `SIX_OF_OCTOBER`
- `N90_TAGAMO3`
- `TAHRIR_DOWNTOWN`

## Security

- **JWT TTL**: 12 hours
- **OTP expiry**: 5 minutes
- **Password**: min 8 chars, 1 uppercase, 1 digit
- **Phone**: E.164 format (`+201234567890`)
- **Files**: max 5 MB each
- **Email verification**: Required before login
- **Account status**: PENDING_VERIFICATION → ACTIVE/SUSPENDED/BANNED

## Project Structure

```
src/main/java/com/aikichun/
├── auth/              # Authentication services
│   ├── AuthService.java
│   ├── EmailService.java
│   ├── JwtService.java
│   ├── OtpService.java
│   ├── AuthController.java
│   └── dto/           # Request/Response DTOs
├── config/            # Security configuration
├── exception/         # Custom exceptions
├── otp/               # OTP entity & repository
├── Security/          # JWT filter
├── storage/           # File storage service
└── user/              # User entity, repository, DTOs
```

## Database Migrations

Flyway migrations are located in `src/main/resources/db/migration/`:
- `V1__create_users_table.sql` - Users table
- `V2__update_branch_constraint.sql` - Branch constraint fix
- `V3__add_otp_tables.sql` - OTP tables and account status
