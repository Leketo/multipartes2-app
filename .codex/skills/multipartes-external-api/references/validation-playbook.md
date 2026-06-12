# Multipartes External API Validation Playbook

## Purpose

Use this playbook when validating API reachability, auth, login, sync reads, or endpoint contracts from a separate API-focused subagent.

Default behavior:

- analyze first
- probe live only under explicit request
- keep live checks read-only unless the user explicitly asked for a mutating request

## Required Environment

Set only what is needed for the check you want to run:

```powershell
$env:MULTIPARTES_API_BASE_URL = "http://app.multipartes.com.py/"
$env:MULTIPARTES_API_USER = "usuario"
$env:MULTIPARTES_API_PASSWORD = "password"
$env:MULTIPARTES_API_TOKEN = "jwt"
```

Use env vars instead of committing secrets or pasting them into skill files.

## Recommended Validation Order

### 1. Rebuild static context when needed

Run:

```powershell
python .codex/skills/multipartes-external-api/scripts/collect_api_context.py .
```

Use this when:

- the code changed
- a new endpoint was added
- the current docs look stale
- the user asks for a contract summary grounded in the repo

### 2. Validate synthetic health first

There is no documented `/health` endpoint in the client code.

Use:

```powershell
python .codex/skills/multipartes-external-api/scripts/api_probe.py health
```

Interpretation:

- `200`: authenticated or anonymously accessible account endpoint
- `401` or `403`: API is reachable but auth is required or denied
- network error / timeout / HTML portal content: treat as unhealthy or false connectivity

Do not claim "API is down" from Android connectivity state alone.

## Login Validation

Use only when login validation was explicitly requested:

```powershell
python .codex/skills/multipartes-external-api/scripts/api_probe.py login
python .codex/skills/multipartes-external-api/scripts/api_probe.py account
```

Expected flow:

1. `login` calls `erp/api/authenticate`
2. Parse `id_token`
3. `account` calls `erp/api/account` with `Authorization: Bearer ...`
4. Expect a JSON document containing at least `id`

Important repo-specific note:

- The app has legacy cookie support in `Comm`, but the active login flow is JWT.
- Do not assume successful `Comm` cookies mean the JWT flow is healthy.

### Real case kept as reference

Known good live result captured on `2026-03-25`:

- Base URL: `http://app.multipartes.com.py/`
- Auth mode: username/password -> bearer token
- `POST erp/api/authenticate`: HTTP `200`, JSON payload, `id_token` present
- `GET erp/api/account`: HTTP `200`, JSON object present, `id` and `login` observed
- Observed `account.id`: `88`

Use this as a sanity-check reference for future probes, but do not reuse the original credentials and do not print the full JWT in reports.

What the client expects from `erp/api/account`:

- A successful authenticated JSON response
- At minimum an `id` field that can be converted to `int`
- In the live case, `login` was also present

That `account.id` is the value `LoginActivity` writes into `Session.userId` after a successful login.

## Read-Only Endpoint Checks

Examples:

```powershell
python .codex/skills/multipartes-external-api/scripts/api_probe.py request GET multip/api/product/family/summary
python .codex/skills/multipartes-external-api/scripts/api_probe.py request GET multip/api/product/price/summary/new/2024-01-01 --decode base64-gzip
python .codex/skills/multipartes-external-api/scripts/api_probe.py request GET multip/api/product/stock-producto/ --query codigo_producto=123
```

For sync endpoints, use `--decode base64-gzip` when the client marks the request with `respuestaBase64=true`.

## Mutating Endpoint Policy

Mutating endpoints in this repo include:

- `location/save`
- `delivery/save`
- `routes/save`
- `routes/update`
- `visit/.../save`
- `order/save`
- `cobro/registrar-cobro`
- `authenticate`

Rules:

- Do not execute them live unless the user explicitly asked for that exact validation.
- Even then, prefer test or disposable credentials and a known-safe environment.
- When using `api_probe.py request` with `POST` or similar methods, you must pass `--allow-mutation`.

## False Connectivity Signals

Treat these as first-class findings:

- `Portal Movil Tigo` HTML in a supposed API response
- DNS or socket failures
- timeouts
- redirects to non-API content

When any of those appear, report them as connectivity-layer problems, not business endpoint regressions.

## Reporting Format

For every live validation, report:

- exact command run
- base URL used
- whether auth was anonymous, username/password, or bearer token
- HTTP status code
- whether the response was plain JSON, PDF, `Base64 + GZIP`, raw text, or captive portal HTML
- whether the endpoint is read-only or mutating

When a result is inferred rather than directly observed, say so explicitly.

