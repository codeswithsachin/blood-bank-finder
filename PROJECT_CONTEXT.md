# Blood Bank Finder — Project Context & Handoff Document

> Read this whole file before making changes. It explains what this project is,
> how it's built, why key decisions were made, and where things live — written
> so another AI assistant (or developer) can pick this up cold and be productive
> immediately, without re-deriving the architecture from the code alone.

---

## 1. What this project is

A web application that connects **blood donors** with **people who urgently
need blood**, plus an **admin panel** for managing blood bank stock. It started
as a Spring Boot REST API (built for a student/portfolio project by **Sachin
Kumar**) and has since grown a full server-rendered web UI with login,
registration, and role-based dashboards on top of that API.

Two audiences use the same running app:
1. **Donors** — register, view their own profile, log donations, check
   eligibility, raise emergency requests, and see requests matching their
   blood group + city.
2. **Admin** — logs into a separate dashboard to manage blood bank stock
   levels and close resolved emergency requests. Sees the full donor list.

The original REST API (consumed via the included Postman collection) still
works untouched and independently of the web UI — see Section 5.

---

## 2. Tech stack

| Layer | Choice | Notes |
|---|---|---|
| Language | Java 21 | Pinned in `pom.xml` (`<java.version>21</java.version>`) and Dockerfile |
| Framework | Spring Boot 3.5.16 | |
| Persistence | Spring Data JPA + Hibernate | `ddl-auto: update` — schema is auto-generated, no migration tool (Flyway/Liquibase) is used |
| Database | MySQL 8.x (prod/dev), H2 in-memory (tests only) | |
| Web UI | Thymeleaf (server-rendered HTML) | No SPA framework — deliberately simple, matches a student project's scope |
| Auth | Spring Security (form login, session-based) | BCrypt password hashing |
| API docs | springdoc-openapi (Swagger UI) | |
| Build | Maven | **Note:** the `mvnw` wrapper shipped in earlier zips was a broken 24-byte stub — a real, system-installed Maven is required unless someone regenerates the wrapper properly |
| Deploy target | Railway | `railway.toml` present |

---

## 3. Data model

```
Donor (donors)                     User (app_users)
├─ id                              ├─ id
├─ name                            ├─ username (unique)
├─ bloodGroup (enum)               ├─ password (BCrypt hash)
├─ city                            ├─ role (ADMIN | DONOR)
├─ phone                           └─ donor_id → Donor (OneToOne, nullable —
├─ lastDonationDate (LocalDate)         only set for DONOR-role users)
└─ active (boolean)

BloodRequest (blood_requests)      BloodBank (blood_banks)
├─ id                              ├─ id
├─ bloodGroup (enum)               ├─ name, city, address, phone
├─ city                            ├─ bloodGroup (enum)
├─ unitsRequired                   └─ unitsAvailable (int)
├─ requesterName / requesterPhone
├─ hospital, message
├─ status (OPEN | CLOSED)
└─ createdAt
```

**Key relationship to understand:** `User` and `Donor` are separate entities
linked by a nullable one-to-one. This was a deliberate choice so the original
REST API's `Donor` entity (used by `/api/donors/**`) didn't have to change
shape when login was bolted on later — `User` is purely a web-login concern.
The **admin** account has `role=ADMIN` and `donor=null` (admin isn't a donor).

**Eligibility rule** (used in donor search, emergency-request matching, and
the dashboard eligibility badge): a donor is eligible if
`lastDonationDate == null` (never donated) OR `lastDonationDate` is more than
`app.donation-gap-days` (default 90) days ago. This logic is currently
**duplicated** in three places — `DonorService.search()`,
`BloodRequestService.create()`, and `DashboardController.donorDashboard()` —
see Section 8 (tech debt) for why this should be consolidated.

---

## 4. Security model — two separate auth systems, on purpose

This app has **two independent authentication mechanisms** that coexist:

1. **REST API auth** (`/api/admin/**` only): a custom `AdminTokenFilter`
   (in `security/`) checks for an `X-ADMIN-TOKEN` header matching
   `app.admin-token` (env var `ADMIN_TOKEN`). This predates the web UI and is
   what the Postman collection uses. All other `/api/**` routes are
   unauthenticated by design (public search/browse endpoints).

2. **Web UI auth** (everything else): standard Spring Security form login
   (`SecurityConfig`), session-cookie based, backed by the `User` table via
   `AppUserDetailsService`. Login redirects by role via
   `RoleBasedLoginSuccessHandler` — ADMIN → `/admin/dashboard`, DONOR →
   `/dashboard`.

**Why both exist:** `/api/**` is `permitAll()` in `SecurityConfig` specifically
so Spring Security's session/CSRF machinery doesn't interfere with the
stateless REST clients (Postman, potential future mobile app) that use the
token header instead. Don't collapse these into one system without checking
both consumers still work.

**CSRF note (a real bug that was fixed once — don't reintroduce it):**
Spring Security's `/logout` endpoint only accepts POST by default. The web
templates must use a `<form method="post">` for logout, not an `<a href>`
link — Thymeleaf auto-injects the CSRF hidden field into `th:action` forms
via its Spring integration, so no manual token handling is needed. `/api/**`
is CSRF-exempt (see `SecurityConfig`'s `.csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))`).

**Admin account bootstrapping:** `config/AdminSeeder.java` (a
`CommandLineRunner`) creates the one admin login account on first startup
from `ADMIN_USERNAME`/`ADMIN_PASSWORD` env vars (defaults `admin`/`admin123`)
— but only if that username doesn't already exist. Changing the env vars
after first run has **no effect** until the `app_users` row is deleted —
this trips people up, flag it if asked "why didn't my new admin password
work."

---

## 5. Route map

| Route | Method | Auth | Purpose |
|---|---|---|---|
| `/` | GET | public | Landing page (hero + Login/Register CTAs) |
| `/login` | GET/POST | public | Shared login form (donor + admin) |
| `/register` | GET/POST | public | Donor self-registration (creates `Donor` + `User`) |
| `/logout` | POST | any logged-in | Ends session, redirects to `/login?logout` |
| `/dashboard` | GET | DONOR, ADMIN | Donor's own dashboard |
| `/dashboard/log-donation` | POST | DONOR, ADMIN | Records a donation date |
| `/dashboard/profile` | POST | DONOR, ADMIN | Updates donor's city/phone/active status |
| `/dashboard/request-blood` | POST | DONOR, ADMIN | Donor raises their own emergency request |
| `/admin/dashboard` | GET | ADMIN only | Stock management, donor list, open requests |
| `/admin/blood-banks` | POST | ADMIN only | Add a blood bank stock entry |
| `/admin/blood-banks/{id}/stock` | POST | ADMIN only | Update stock units |
| `/admin/requests/{id}/close` | POST | ADMIN only | Mark a request closed |
| `/api/donors` | POST | public (X-ADMIN-TOKEN not required) | Register a donor via API |
| `/api/donors/search` | GET | public | Search donors by group+city |
| `/api/requests` | POST | public | Create an emergency request via API |
| `/api/admin/blood-banks` | * | requires `X-ADMIN-TOKEN` header | Admin stock API |
| `/api/status` | GET | public | JSON health/status check (was at `/` originally — moved to free that path for the landing page) |
| `/hospitals` | GET | public | Hospital directory + interactive India map |
| `/find-donors` | GET | public | AI compatibility-based donor matching (aggregate counts only, see Section 11) |
| `/request-blood` | GET/POST | public | Anyone can raise an emergency request without an account |
| `/contact` | GET | public | Verified national blood-service helplines |
| `/swagger-ui/**` | GET | public | API docs |

---

## 6. Design system (frontend)

The UI deliberately avoids a generic Bootstrap look. Design language:
**dark glassmorphism** (frosted-glass cards over animated backgrounds), red/
maroon brand palette (`#ff3b4e` primary), Poppins font.

**Shared base** — `static/css/style.css`: glass card styles (`.glass`,
`.glass-card`), buttons, forms, tables, topbar, landing hero styles. This file
has **no background animation** in it — that's deliberate (see next point).

**Per-page animated backgrounds — each page has its own, not a shared one:**

| Page | CSS file | Effect | Why this concept |
|---|---|---|---|
| `/` (landing) | `bg-landing.css` | Floating blood-cell discs rotating in pseudo-3D (`rotateY`/`rotateX` + `perspective`), cursor parallax | Literal "blood cells" motif for the entry point |
| `/login` | `bg-login.css` | Expanding radar/sonar pulse rings + a moving ECG scan line | "Pulse" = login as a heartbeat/access point |
| `/register` | `bg-register.css` | Bubbles rising from the bottom with a slight 3D tilt | Rising bubbles = new donors "joining" |
| `/dashboard` | `bg-dashboard.css` | Calm heartbeat monitor line (green accent) + soft drifting blobs | Personal health/life monitoring feel |
| `/admin/dashboard` | `bg-admin.css` | Real 3D perspective grid floor (`perspective` + `rotateX(75deg)` on a moving grid) + glowing pulsing nodes | "Control panel" / data-management feel, deliberately cooler and more technical than the donor-facing pages |

Each page's `<head>` links `style.css` **and** its own `bg-*.css`. The
matching background markup lives in `templates/fragments.html` as named
Thymeleaf fragments (`bg-landing`, `bg-login`, `bg-register`, `bg-dashboard`,
`bg-admin`), pulled in via `th:replace="~{fragments :: bg-XXX}"`.

**Two JS-driven interactive effects (both in `static/js/`):**
- `glass-effect.js` — tracks the cursor over any `.glass-card` and renders a
  soft radial-gradient spotlight that follows the mouse (the "magnify" glass
  highlight). Applied automatically to every `.glass-card` on the page.
- `parallax.js` — generic: any element with `class="parallax" data-depth="N"`
  gets its `transform: translate()` updated on `mousemove`, proportional to
  `N`, to fake depth. Used on the landing page's blood cells and the admin
  page's glow nodes.

**If asked to add a new page:** give it its own `bg-*.css` + fragment rather
than reusing another page's — that's the whole point of this system. Keep the
shared glass/form/table styles in `style.css` untouched.

---

## 7. Donor-facing features (what exists today)

Beyond basic register/login/search, the donor dashboard (`/dashboard`)
currently offers:

1. **Eligibility tracker** — shows "Eligible now" or "Eligible from
   `<date>`" computed from `lastDonationDate + app.donation-gap-days`.
2. **Log a Donation** — donor records today's (or a chosen) donation date,
   which updates their eligibility window immediately.
3. **Edit Profile** — donor can update their own city, phone, and toggle
   whether they're currently available to donate (`active` flag).
4. **Raise an Emergency Request** — a donor can submit their own emergency
   blood request (pre-filled with their blood group/city) without needing
   the admin or the raw API — reuses the existing `BloodRequestService`, so
   it still triggers the same eligible-donor matching/notification logic.
5. **Matching requests + one-tap call** — the dashboard lists open requests
   matching the donor's blood group and city, each with a `tel:` link so a
   donor can call the requester directly from their phone.

**Ideas not yet built** (good next steps if asked to keep expanding donor
features):
- Persistent **donation history** (currently only the single most-recent
  `lastDonationDate` is stored — no `DonationRecord` table exists yet, so a
  full history/streak/badge system would need a new entity).
- Donor "I can help" response button on a matching request (currently the
  donor can only call; there's no in-app way to signal intent back to the
  requester or admin).
- Email/SMS notifications (there's a `NotificationService` stub already —
  check it before assuming this needs to be built from scratch).
- Nearby blood bank stock visibility on the donor dashboard (currently stock
  is admin-only).
- Password reset / forgot-password flow (none exists — resetting currently
  requires direct DB access).

---

## 8. Known gaps / tech debt (be upfront about these if asked)

- **No database migration tool.** Schema changes rely on Hibernate's
  `ddl-auto: update`, which is fine for a student project but not how you'd
  do this in a team/production setting. If this evolves further, introducing
  Flyway would be a natural next step.
- **Eligibility logic is duplicated** in `DonorService`, `BloodRequestService`,
  and `DashboardController`. Should be extracted into one shared method
  (e.g., `Donor.isEligible(long gapDays)` or a small utility) if touched again.
- **Test coverage is thin** — only `DonorServiceTest` (unit) and a basic
  Spring context-load test exist. No controller/integration tests, no
  security-flow tests (login, role redirects).
- **No password-reset flow.**
- **`mvnw` wrapper caveat** — earlier zips of this project had a broken
  wrapper stub; confirm the current copy has a proper `.mvn/wrapper` folder
  before assuming `./mvnw` works.
- **Single-server assumption** — no caching layer, no rate limiting on the
  public `/api/**` endpoints (e.g., `/api/donors` registration is wide open).

---

## 9. How to run it locally

1. JDK 21 + Maven installed and on PATH.
2. MySQL 8.x running locally; create an empty database: `CREATE DATABASE blood_bank_finder;`
3. Defaults in `application.yml` assume `root`/`root` on `localhost:3306` —
   override via env vars (`MYSQL_URL`, `MYSQLUSER`, `MYSQLPASSWORD`,
   `ADMIN_TOKEN`, `ADMIN_USERNAME`, `ADMIN_PASSWORD`) if different — see
   `.env.example`.
4. `mvn spring-boot:run` from the project root (the folder containing
   `pom.xml`).
5. Visit `http://localhost:8080/` for the web UI, or import
   `Blood-Bank-Finder.postman_collection.json` for the raw API.

---

## 11. Version 2 additions (AI matching, India map, public request flow)

A second pass expanded this from a login-gated demo into something closer to
a real usable product. Key additions, and the reasoning behind each:

**AI-powered donor matching (`DonorMatchingService`)** — Be precise about
what "AI" means here if asked: this is a **rule-based expert system**, not a
call to an external LLM. It encodes the real clinical blood-compatibility
matrix (`BloodGroup.canDonateTo()` / `compatibleRecipients()`) — e.g. O- is
the universal donor, AB+ the universal recipient — and applies a weighted
score (100 for exact match, 90 for O- universal-donor compatibility, 70 for
other compatible groups). This is a legitimate, explainable technique
(symbolic/expert-system AI) and should be described that way rather than
oversold as generative AI — the honest framing is what makes it defensible
in a viva or code review.

**Privacy design decision — read this before changing `/find-donors`:**
That page is public and unauthenticated by design (no login wall — anyone
needing blood should be able to use it). It deliberately returns only
**aggregate counts** per compatible blood group ("12 eligible O+ donors in
Vadodara"), never individual donor names or phone numbers. Exposing donor
identities to anonymous visitors would be a real privacy problem. The
actual way to reach a donor is still the existing request/notification flow
(`BloodRequestService` + the donor dashboard's matching-requests view) —
don't "fix" `/find-donors` by making it list real donors without
re-considering this tradeoff first.

**Public emergency request flow (`PublicRequestController`, `/request-blood`)**
— previously, raising a request required either the raw API or a logged-in
donor account. Real emergencies don't wait for account creation, so this is
now a public, unauthenticated form that feeds the same `BloodRequestService`
used everywhere else.

**Hospital directory + interactive map (`Hospital`, `HospitalSeeder`,
`DirectoryController`, `/hospitals`, `/find-donors`)** — uses Leaflet.js +
OpenStreetMap tiles (both free, no API key required). `HospitalSeeder` seeds
~15 real, well-known Indian hospitals (names/cities are public knowledge) on
first startup — **coordinates are city-level approximations, not verified
exact addresses**, and no phone numbers were invented for individual
hospitals (that data wasn't independently verifiable, so it was left out
rather than guessed). `CityCoordinates` is a small hardcoded lookup (~30
major Indian cities) used to place blood-bank markers on the map too, since
`BloodBank` only stores a city name, not coordinates.

**Verified national helpline numbers (`/contact`)** — these were looked up,
not invented, because getting emergency contact info wrong would be
actively harmful: 108 (national ambulance/emergency, free), 104 (state
health helpline, availability varies by state), Indian Red Cross Society
National Blood Services (011-23359379, 24×7), National Blood Transfusion
Council (011-43509999), and eRaktKosh — the real Government of India blood
bank portal at `https://eraktkosh.mohfw.gov.in/`. The page explicitly notes
India has **no single unified toll-free blood helpline** — that's accurate,
not a gap to "fix" by fabricating one.

**AI Assistant widget (`ai-assistant.js`, floating button on every page)** —
also rule-based (keyword matching against a small hardcoded Q&A knowledge
base), not a live LLM call. Chosen deliberately over wiring up a real LLM
API so the feature works reliably offline/during a demo without needing an
API key or internet dependency at grading time. If asked to upgrade this to
a real LLM-backed assistant later, that's a reasonable next step — just be
transparent that today's version is pattern-matching, not generative.

**Blood group display fix** — `BloodGroup` enum constants (`O_POSITIVE`,
`A_NEGATIVE`, etc.) were being shown raw in the UI, underscores and all.
Added `BloodGroup.getSymbol()` (`"O+"`, `"A-"`, etc.) and every template now
displays that instead of the raw enum name.

**Live stats on the landing page (`HomeController`)** — the donor/hospital/
open-request counts shown on `/` are real live database counts, not
placeholder marketing numbers. Keep it that way — don't replace them with
hardcoded figures to make the page look more impressive than the data
actually supports.

**Per-page network/map background (`bg-map.css`)** — used by `/hospitals`
and `/find-donors`, following the same "each page gets its own background"
pattern from Section 6. `/contact` and `/request-blood` reuse `bg-login.css`
(the pulse/heartbeat theme) since both are "urgent contact" pages
thematically.


## 12. If you're an LLM picking this up fresh

Good places to start depending on what's being asked:
- **New donor feature** → look at `DashboardController` +
  `templates/dashboard.html` + `DonorService` as the pattern to follow.
- **New admin feature** → `AdminDashboardController` +
  `templates/admin-dashboard.html`.
- **New page** → follow the pattern in Section 6: own `bg-*.css`, own
  fragment in `fragments.html`, link both `style.css` and the new file.
- **Touching auth** → read Section 4 fully first; there are two systems and
  it's easy to accidentally break one while fixing the other.
- **Anything involving eligibility** → check Section 3's note about
  duplicated logic before adding a fourth copy of the same rule.
- **Anything involving `/find-donors` or donor privacy** → read Section 11's
  privacy note before changing what that page returns.
- **Adding real hospital phone numbers or more cities** → verify them first
  (web search or an authoritative source) rather than inventing plausible-
  looking ones — this project deliberately avoided fabricating contact
  details for exactly this reason.

## 13. Version 3 additions (hardening, admin tools, calmer UI, auto-launch)

A third pass focused on making this feel like a real, usable, submittable
product rather than a flashy demo — the person building this explicitly
asked for calmer/readable visuals ("normal people can see clearly"),
security basics, and admin usability.

**Auto-open browser on startup (`BrowserLauncher`)** — `mvn spring-boot:run`
now opens the default browser to `http://localhost:8080/` automatically via
`java.awt.Desktop`, listening for `ApplicationReadyEvent`. Controlled by
`app.auto-open-browser` (default `true`). Safe on headless
servers/Railway/Docker — `Desktop.isDesktopSupported()` returns false there,
so it just logs the URL instead of throwing.

**Rate limiting (`RateLimitFilter`)** — a simple in-memory, dependency-free
per-IP fixed-window limiter (5 requests / 10 minutes) on the spam-prone
public POST endpoints: `/register`, `/request-blood`, `/api/donors`,
`/api/requests`. Deliberately basic — good enough for a single-server
deployment, but note in the code comment that a multi-instance deployment
would need a shared store (Redis) instead of the in-memory map.

**Math CAPTCHA (`CaptchaSupport`)** — session-based arithmetic challenge on
`/register` and `/request-blood`. Chosen over Google reCAPTCHA specifically
because reCAPTCHA needs a site key/secret registered against the actual
deployed domain — not something that could be hard-coded for someone else's
deployment. One-time use (answer is removed from session after checking).

**Server-side phone validation (`PhoneValidator`)** — validates Indian
10-digit mobile numbers server-side (not just the HTML `required`
attribute, which is trivially bypassed by posting directly to the
endpoint). Applied on donor registration and public request submission.

**Eligibility logic consolidated** — the "90-day gap" rule that used to be
duplicated in `DonorService`, `BloodRequestService`, and
`DashboardController` now lives in exactly one place:
`Donor.isEligible(long gapDays)` / `Donor.nextEligibleDate(long gapDays)`.
If you add a fourth copy of this rule anywhere, you've missed this method.

**Donation history (`DonationRecord`)** — previously only the single most
recent `lastDonationDate` was stored on `Donor`. Logging a donation now also
inserts a `DonationRecord` (date + optional location), and the donor
dashboard shows the full history table. `Donor.lastDonationDate` is still
kept in sync (used by the eligibility calculation) — `DonationRecord` is
additive, not a replacement.

**AI Insights on the admin dashboard (`AiInsightService`)** — another
rule-based (not LLM-backed) feature: flags blood groups with zero or
critically low aggregate stock, and open requests with zero currently-
eligible matching donors. Same honesty note as the matching engine in
Section 11 — explainable threshold rules, not a model making judgment
calls.

**Admin donor filter + hospital management** — the donor table on
`/admin/dashboard` can now be filtered by city and/or blood group via query
params (`filterCity`, `filterGroup`). Admin can also add/remove hospitals
directly from the dashboard now (previously hospitals only existed via
`HospitalSeeder`, with no UI path to add more) — new hospitals get
coordinates via `CityCoordinates.lookup()` if the city is recognised,
otherwise fall back to a default India-center point (so they don't crash
the map, but may appear in an inexact spot).

**Charts (Chart.js via CDN)** — admin dashboard now shows a bar chart of
stock by blood group and a doughnut chart of open vs. closed requests.
**Important gotcha already fixed once, don't reintroduce it:** the stock
chart data is built as a `Map<String, Integer>` keyed by
`BloodGroup.getSymbol()` ("O+"), not by the raw `BloodGroup` enum — if you
key it by the enum directly, Jackson serializes the enum name for
Thymeleaf's JS inlining and the chart labels show `O_POSITIVE` again,
undoing the earlier underscore fix.

**Calmer, more readable visual design** — the animated backgrounds
(`bg-*.css`) had their opacity/glow intensity reduced by roughly 40–60%
across the board, and `.glass` card background opacity was raised (0.07 →
0.10) for better text contrast. The goal was to keep the distinct
per-page-background concept (Section 6) while making it feel like a
calm, everyday utility site rather than a flashy gaming aesthetic. If asked
to make it "pop" more again, that's a legitimate direction too — just be
aware this was a deliberate readability-driven toning-down, not an
oversight.

**Mobile responsiveness pass** — added a proper `@media (max-width: 720px)`
block in `style.css` covering the topbar (wraps instead of overflowing),
hero text sizing, tables (horizontal scroll instead of breaking layout),
the AI assistant panel (full-width instead of a fixed 320px overflowing the
viewport), and stat/feature grids collapsing to one column.

## 14. Getting a shareable public link (for a teacher/demo)

This app is not deployed anywhere by default — running it locally only
gives you `http://localhost:8080/`, which only works on the same machine.
To get a real `https://something.up.railway.app` link others can open:

1. Push this project to a GitHub repo (Railway deploys from GitHub).
2. Create a free Railway account, "New Project" → "Deploy from GitHub repo"
   → select the repo. Railway will detect the `Dockerfile` and build it.
3. Add a MySQL database from Railway's plugin marketplace ("New" → 
   "Database" → "MySQL") within the same project.
4. Set environment variables on the web service (Railway's "Variables" tab):
   `MYSQL_URL`, `MYSQLUSER`, `MYSQLPASSWORD` (Railway's MySQL plugin exposes
   these as reference variables you can link in, or read off its own
   "Connect" tab), plus `ADMIN_TOKEN`, `ADMIN_USERNAME`, `ADMIN_PASSWORD` —
   don't leave the code defaults (`admin`/`admin123`) in a public deployment.
5. Once deployed, Railway gives you a public URL under Settings →
   Networking → "Generate Domain". That's the link to share.

This wasn't done automatically because it requires an account and
credentials only the project owner has — but `railway.toml` and the
`Dockerfile` are already set up for exactly this flow.

## 15. Version 4: theme pivot to a clean, light design

Explicit feedback from the person building this: the dark, glowing,
animated "blood cell / radar pulse / 3D grid" design from Sections 6 and 13
read as scary/gothic rather than professional, and they wanted something a
normal person would find calm and easy to use daily.

**What changed:** every dark animated background (`bg-landing.css`,
`bg-login.css`, `bg-register.css`, `bg-dashboard.css`, `bg-admin.css`,
`bg-map.css`) was deleted, along with the JS that powered their effects
(`parallax.js`, `glass-effect.js` cursor spotlight). `style.css` was
rewritten from scratch as a plain light theme: white background, a single
red accent color used sparingly (buttons, badges, links), Inter font,
simple flat cards with a subtle shadow instead of frosted-glass blur over
neon gradients.

**If you're asked to touch visual design again:** the "one distinct
animated background per page" concept from Section 6 no longer applies —
don't resurrect it without being asked. The current design is deliberately
plain. Any new page should just link `style.css` and use `.glass-card` for
panels; there's no per-page background file to create anymore.

**Also removed everywhere:** em-dash punctuation (—) in user-facing text.
Every string a visitor can see uses periods, commas, or colons instead —
this was an explicit, repeated request, not a one-off fix. If you add new
copy anywhere in a template or a user-facing message string in Java, don't
introduce an em dash.

**Map marker colors are the one exception** to the light-theme palette —
they're plotted directly onto OpenStreetMap tiles on `/hospitals` and
`/find-donors`, not onto page background, so they still use bright,
high-contrast colors (red/green/amber) chosen for visibility against map
tiles rather than matching the site's card palette.

## 16. Version 5: research-grounded features, live map fix, Hindi/English

This round was triggered by reviewing two real reference platforms
(Ezovion, a commercial blood bank management product, and eBloodBank.gov.in,
a real NIC/government platform) and explicit feedback that the map wasn't
displaying well and the site needed to support Hindi speakers.

**Map fix (`live-map.js`, `/api/map-data`)** — the old map used a fixed
`setView(center, zoom)` that often left markers scattered oddly. Replaced
with: (1) `map.fitBounds()` computed from whatever markers actually exist,
so the view always frames the real data; (2) a live JSON endpoint
(`GET /api/map-data`, public) that the map polls every 60 seconds, so stock
changes show up without a page reload; (3) a "Locate me" button using
`navigator.geolocation`. `/hospitals` and `/find-donors` both use the same
shared `live-map.js` — don't duplicate this logic if adding a third map page,
extend the shared file instead.

**Hindi/English toggle (`WebConfig`, `messages.properties` /
`messages_hi.properties`)** — a real, working `CookieLocaleResolver`-based
toggle (persists 365 days, not tied to login session). **Coverage is
partial by design, not accidental:** nav, landing page, login, register,
contact, and request-blood are fully translated. The donor dashboard and
admin dashboard remain English-only — translating those next would mean
extending the same `messages*.properties` files with `dashboard.*` /
`admin.*` keys following the existing pattern (see any translated template
for the `th:text="#{key}"` convention). Switch language via `?lang=en` or
`?lang=hi` on any URL — the nav bar link does exactly that.

**Features added after reviewing real platforms, each tied to what was
actually seen there (not guessed):**
- **Blood expiry tracking** (`BloodBank.expiryDate`) — Ezovion markets
  expiry-aware stock management as a core feature. `AiInsightService` now
  flags stock expiring within 7 days or already expired. Per-batch/per-unit
  tracking would be more realistic than one expiry date per stock line, but
  that's a bigger schema change than was justified for the current scope.
- **Donor deferment** (`Donor.deferred` / `deferredUntil` / `deferReason`) —
  also an Ezovion feature. A deferred donor is automatically excluded from
  `Donor.isEligible()` (temporary if `deferredUntil` is set, permanent if
  null), so deferment flows through matching, requests, and dashboards
  without any extra plumbing. Admin can defer/clear from the donor table.
- **Blood donation camps** (`BloodCamp`, `/camps`) — both reference
  platforms treat "upcoming camps" as a standard feature. **Be clear about
  what this currently is:** `BloodCampSeeder` seeds a handful of *sample*
  camps with dates computed relative to app startup (so they always look
  "upcoming" instead of going stale) — this is demo/placeholder data, not a
  real, admin-managed camp schedule. There's no admin UI to add/edit real
  camps yet; that would be the natural next step if this feature needs to
  become real rather than illustrative.

**What was explicitly NOT done, and why:** a live scrape/sync against
eRaktKosh or eBloodBank.gov.in's actual data was considered and rejected —
neither exposes a public API for this, and screen-scraping a government
health portal is fragile and likely against its terms of use. The camps/
hospital data here is deliberately original seed data, not attributed to
or pulled from those sites.
