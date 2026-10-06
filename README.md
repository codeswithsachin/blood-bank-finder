# Blood Bank Finder

A production-style REST backend for finding eligible blood donors and managing blood-bank stock.

## Stack

- Java 21
- Spring Boot 3.5.16
- Spring Data JPA / Hibernate
- Spring Security (form login, BCrypt password hashing)
- Thymeleaf (server-rendered web dashboard)
- MySQL
- Springdoc OpenAPI / Swagger UI
- Railway-ready configuration
- Twilio notification adapter point

## Web login & dashboards

In addition to the REST API, the app now has a browser-based login system:

- `GET /register` — donor self-registration (creates both a `Donor` profile and a login account)
- `GET /login` — shared login form for donors and admin
- `GET /dashboard` — donor's own dashboard: profile + emergency requests matching their blood group/city
- `GET /admin/dashboard` — admin dashboard: manage blood bank stock, view/close open requests

An admin login account is seeded automatically on first startup from `ADMIN_USERNAME` / `ADMIN_PASSWORD`
(defaults: `admin` / `admin123` — **change these before deploying anywhere public**). This is separate
from `ADMIN_TOKEN`, which still protects the raw `/api/admin/**` REST endpoints via the `X-ADMIN-TOKEN` header.

## Core features

1. Donor registration
2. Blood group + city search
3. Automatic 90-day donation eligibility filter
4. Emergency blood-request creation
5. Automatic matching of eligible donors
6. Blood-bank stock management
7. Admin-token protected endpoints
8. Health endpoint + Actuator
9. OpenAPI documentation

## Run locally

Create a MySQL database named `blood_bank_finder`.

Set environment variables if your local credentials differ:

```powershell
$env:MYSQLUSER="root"
$env:MYSQLPASSWORD="root"
$env:MYSQL_URL="jdbc:mysql://localhost:3306/blood_bank_finder?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
$env:ADMIN_TOKEN="my-secret-admin-token"
```

Run:

```powershell
mvn spring-boot:run
```

Swagger:
`http://localhost:8080/swagger-ui/index.html`

Health:
`http://localhost:8080/actuator/health`

## API examples

### Register donor

POST `/api/donors`

```json
{
  "name": "Rahul Patel",
  "bloodGroup": "O_POSITIVE",
  "city": "Vadodara",
  "phone": "+919876543210",
  "lastDonationDate": "2026-01-10",
  "active": true
}
```

### Search eligible donors

GET `/api/donors/search?bloodGroup=O_POSITIVE&city=Vadodara`

A donor whose last donation is inside the configured 90-day gap is excluded.

### Create emergency request

POST `/api/requests`

```json
{
  "bloodGroup": "O_POSITIVE",
  "city": "Vadodara",
  "unitsRequired": 2,
  "requesterName": "Amit Shah",
  "requesterPhone": "+919812345678",
  "hospital": "Example Hospital",
  "message": "Urgent requirement"
}
```

### Admin

Use header:

`X-ADMIN-TOKEN: your-token`

POST `/api/admin/blood-banks`

```json
{
  "name": "City Blood Centre",
  "city": "Vadodara",
  "address": "Main Road",
  "phone": "+912656000000",
  "bloodGroup": "O_POSITIVE",
  "unitsAvailable": 8
}
```

## Railway

1. Push this project to GitHub.
2. Create a Railway project.
3. Add a MySQL service.
4. Deploy the GitHub repository as a Java/Spring Boot service.
5. Set `MYSQL_URL` to Railway's MySQL JDBC URL, plus `MYSQLUSER`, `MYSQLPASSWORD`.
6. Set `ADMIN_TOKEN` to a long random value.
7. Railway provides the public service URL.

The application listens on Railway's `PORT` environment variable.

## Twilio

The notification service is deliberately isolated in `NotificationService`. To enable real SMS, add the Twilio Java SDK and configure credentials through environment variables. Never commit Twilio credentials to Git.

## Important

This project is a technical prototype, not a medical authority. Donation eligibility rules should be reviewed against the applicable blood-bank/medical guidelines before real-world use.
