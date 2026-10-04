# Clarigence Contact API and Admin Dashboard

Java 21 / Spring Boot service for the public contact form and a separate admin workspace. The backend serves the admin UI at `/admin/`; the public static website remains in the repository root.

## Requirements

- Java 21
- Maven 3.6.3 or later
- MySQL 8 or later

## Deploy the backend for the live website

### Vercel backend with hosted MySQL

The backend folder is linked to the separate Vercel project `clarigence-api`.
The public website remains linked to `clarigence.in` from the parent folder.
`backend/vercel.json` builds the existing Dockerfile as a Vercel container
service and routes requests to Spring Boot without changing their paths.
Vercel container services are currently in beta.

1. Create a hosted MySQL service and a database named `clarigence`. If existing
   local enquiries or admin accounts need to be carried over, export and import
   them privately before starting the hosted backend. Preserve admin password
   hashes; no passwords need to be reset or revealed.
   `clarigence-hosted.sql` reuses the database definition from the existing
   `clarigence.sql` without its local user/password settings. The original file
   is preserved for local use and excluded from deployment uploads. It is a
   setup script, not an export of the local database's records.
2. Follow the MySQL provider's verified TLS connection instructions, including
   its CA certificate when required. Do not disable certificate verification to
   work around a connection error. The public CA certificate and hostname can
   be shared for setup; the database password must remain private.
3. Run `mysql-session-schema.sql` once on that hosted database through a locally
   authenticated database client. This adds session tables without altering
   `contacts` or `admin_users`.
4. In the **clarigence-api** Vercel project's private production environment
   settings, supply `DB_URL` (a JDBC URL without credentials), `DB_USERNAME`, and
   `DB_PASSWORD`. Use `SPRING_PROFILES_ACTIVE=production,vercel` and `PORT=8080`.
   Those two non-credential settings have already been configured on the project.
   The Vercel profile shares authenticated sessions through MySQL so login works
   across backend instances; the normal local session behavior remains intact.
5. Deploy from the **backend** directory with `vercel --prod`. Check the build
   and startup logs without exposing environment values. Verify the actual URL
   using empty JSON validation and CORS preflight requests, which do not create
   enquiries. Also confirm unauthenticated admin API requests remain denied.
6. In the **clarigence.in** Vercel project, set production `CLARIGENCE_API_URL`
   to the verified backend HTTPS URL plus `/api/contact`. Redeploy from the
   **parent website directory**. Obtain approved real enquiry details before
   submitting the live form, then verify its success response and saved record.

The JDBC session schema and authentication flow are tested with an isolated H2
database. Hosted MySQL connectivity and Vercel container startup must still be
verified after actual private environment settings are supplied.

### Other container hosts

The `backend` folder is source code on your computer. Public visitors need a
running backend at a public HTTPS URL and a persistent MySQL database reachable
from that backend. The static Vercel deployment excludes this folder.

Use `backend` as the Docker build context on your backend host. The included
`Dockerfile` builds Java 21, runs the integration tests, and runs the resulting
service as a non-root user. It supports the host's `PORT` environment variable;
`SERVER_PORT` takes precedence when explicitly configured.
The container enables the `production` profile, which requires explicit database
settings, allows the two Clarigence HTTPS origins by default, and enables secure
session and CSRF cookies. For a non-container deployment, set
`SPRING_PROFILES_ACTIVE=production` in the host's environment settings.

Set the database and CORS variables listed in `.env.production.example` in the
host's environment settings, using the database provider's actual credentials
and TLS configuration. The example file is documentation; Spring Boot does not
load it automatically. Create the `clarigence` database before starting the
service. Keep `SESSION_COOKIE_SECURE=true` on the HTTPS host. Bootstrap the first
admin using the existing instructions below, then disable bootstrap.

Once the service is running, verify an empty JSON POST to its `/api/contact`
returns HTTP 400 with field validation errors, and verify an OPTIONS request
with `Origin: https://www.clarigence.in` allows that origin. Then set the Vercel
production environment variable `CLARIGENCE_API_URL` to the actual HTTPS URL
ending in `/api/contact`, and redeploy the public site from the project root.
Finally, obtain the owner's approved enquiry details before submitting through
the live form, then confirm the enquiry appears in the backend admin dashboard.
Do not create fabricated production enquiries. A successful frontend deployment
alone does not mean enquiries are being stored.

## Prepare MySQL

Open MySQL Command Line Client (or run `mysql -u root -p` in CMD). Create a database and application account, choosing your own strong password. Do not commit that password to this project.

```sql
CREATE DATABASE clarigence CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'clarigence_app'@'localhost' IDENTIFIED BY 'CHOOSE_YOUR_OWN_STRONG_PASSWORD';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX ON clarigence.* TO 'clarigence_app'@'localhost';
```

The application creates or updates the `contacts` and `admin_users` tables on startup. The `contacts` table keeps existing form submissions and adds a `status` field defaulting to `NEW`.

## Create the first admin and start the backend (Windows CMD)

For an existing local MySQL user/database, start from the backend folder with:

```cmd
powershell -NoProfile -ExecutionPolicy Bypass -File .\start-local.ps1
```

Enter the database user's password at the masked local prompt. The script uses
port 8081 because Oracle occupies port 8080 on this computer. It supplies the
database settings only to the running process and restores them after stopping.
It does not change database credentials or bootstrap an admin. Keep the window
open; the dashboard is at `http://localhost:8081/admin/`. For a local frontend,
its form must also point to `http://localhost:8081/api/contact`. This local
startup does not make the backend reachable from the live Vercel website.

The manual environment-variable instructions below remain available for a
different local configuration or first-time admin creation.

Open a Command Prompt and configure the database connection. The password is prompted for rather than placed in a command:

```cmd
cd /d C:\Users\prash\Desktop\clarigence.in\backend
set "DB_URL=jdbc:mysql://localhost:3306/clarigence?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
set DB_USERNAME=clarigence_app
set /p DB_PASSWORD=Enter the MySQL password: 
set "CORS_ALLOWED_ORIGINS=http://localhost:8000,http://127.0.0.1:8000"
```

Set an admin email and a new password of 12–72 UTF-8 bytes. Turn on the one-time bootstrap and start Spring Boot:

```cmd
set ADMIN_BOOTSTRAP_ENABLED=true
set ADMIN_EMAIL=you@your-company.com
set /p ADMIN_PASSWORD=Create an admin password: 
mvn spring-boot:run
```

On the first start, this creates one admin account with a BCrypt password hash if no admin exists yet. It does not print or store the plaintext password. After the account is created, stop the app with **Ctrl+C**, clear the bootstrap variables, then start the backend normally:

```cmd
set ADMIN_BOOTSTRAP_ENABLED=
set ADMIN_EMAIL=
set ADMIN_PASSWORD=
mvn spring-boot:run
```

Keep the backend Command Prompt open. The API and admin workspace are available at:

- Admin dashboard: http://localhost:8080/admin/
- Contact API: http://localhost:8080/api/contact

The admin dashboard uses an HttpOnly session cookie, BCrypt password hashes, session ID rotation at login, CSRF tokens for state-changing requests, and server-side authorization. Admin credentials are not seeded into the repository. Admin bootstrap is disabled by default. Store secrets in a secret manager or environment variables for deployed use; on HTTPS deployments set `SESSION_COOKIE_SECURE=true`.

## Admin features

- Dashboard totals for all enquiries and each status.
- Search by name, email, company, service, or message; filter by status and paginate results.
- View full enquiry details, including phone and message.
- Set status to `NEW`, `CONTACTED`, `CONVERTED`, or `CLOSED`.
- Delete enquiries after an explicit confirmation.
- Sign out to invalidate the current admin session.

Admin REST routes require an authenticated admin session:

- `GET /api/admin/dashboard`
- `GET /api/admin/enquiries?query=&status=&page=0&size=20`
- `GET /api/admin/enquiries/{id}`
- `PUT /api/admin/enquiries/{id}/status` with `{"status":"CONTACTED"}`
- `DELETE /api/admin/enquiries/{id}`

The admin UI and API are served from the same backend origin. Configure `CORS_ALLOWED_ORIGINS` with exact frontend origins for the public contact form. Do not use `*` for production origins.

## Run the public frontend

In a second Command Prompt:

```cmd
cd /d C:\Users\prash\Desktop\clarigence.in
py -m http.server 8000
```

Visit http://localhost:8000 for the public site. The contact form posts to the backend and shows an inline result. If the API is hosted elsewhere, change `data-api-url` on the contact form in `contact.html` and add that frontend's exact origin to `CORS_ALLOWED_ORIGINS`.

## Build and test

From the `backend` directory:

```cmd
mvn clean verify
```

Integration tests use a temporary H2 database and exercise contact submission, validation and CORS, plus admin login, authorization, CSRF, dashboard listing/filtering, details, status changes, deletion, and logout. They do not use MySQL credentials or add test records to your MySQL database.
