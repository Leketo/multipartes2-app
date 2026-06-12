---
name: multipartes-external-api
description: Inspect, document, summarize, and validate Multipartes external REST APIs used by `multipartes2-app`. Use when work touches `Comm.URL`, `CommReq`, login/auth, sync endpoints, report/PDF URLs, API health checks, external API regressions, or any task that should be delegated to a separate API-focused subagent before changing app code.
---

# Multipartes External API

## Overview

Use this skill as the first specialist for any API-level task in this repo.
Ground every answer in the Android client code, not in backend assumptions.

## Quick Start

- Read `references/endpoint-catalog.md` first for the current endpoint map.
- Read `references/validation-playbook.md` before running live probes.
- Run `python .codex/skills/multipartes-external-api/scripts/collect_api_context.py .` when the catalog may be stale or when new endpoints were added.
- Run `python .codex/skills/multipartes-external-api/scripts/api_probe.py --help` for the supported live validation commands.

## Workflow

### 1. Build context from the client code

- Treat `Comm.URL` as runtime-configured from `ConfiguracionActivity` and persisted in the local DB.
- Inspect both `CommReq` constants and hardcoded `HttpPost` / `HttpGet` URLs in activities.
- Cover `multip/api`, `erp/api`, and bare `api/...` namespaces; do not normalize them away in summaries.
- Distinguish the active JWT login flow from legacy cookie-oriented helpers.

### 2. Decide static analysis vs live validation

- Default to static analysis and code-grounded summaries.
- Only run live HTTP validation when the user explicitly asks to validate, execute, probe, or test the API.
- Use env vars for live validation:
  - `MULTIPARTES_API_BASE_URL`
  - `MULTIPARTES_API_USER`
  - `MULTIPARTES_API_PASSWORD`
  - `MULTIPARTES_API_TOKEN`
- Never store credentials or tokens inside the repo or the skill files.

### 3. Handle this repo's API quirks explicitly

- Expect mixed transport styles:
  - `Comm.requestGet` / `Comm.requestPost`
  - manual `HttpPost` / `HttpGet`
  - an unused Retrofit stub
- Expect `Base64 + GZIP` responses for several sync endpoints; decode them before judging the endpoint broken.
- Expect `Portal Movil Tigo` HTML as a captive-portal or false-connectivity signal.
- Do not invent a real `/health` endpoint. For this repo, health is a synthetic reachability check built around auth endpoints.

### 4. Keep live validation safe by default

- Treat `GET`, `HEAD`, and `OPTIONS` as read-only by default.
- Treat `POST`, `PUT`, `PATCH`, and `DELETE` as mutating. Do not execute them live unless the user explicitly asked for that exact validation.
- Prefer these read-only live checks first:
  - `erp/api/account` reachability with expected `401`, `403`, or `200`
  - `erp/api/authenticate` only when login validation is explicitly requested
  - read-only sync or summary endpoints
- When a task touches login, auth, health, sync, or endpoint contracts, keep API work isolated in a separate subagent that uses this skill.

## Reporting

- Separate static facts from runtime observations.
- Cite the concrete endpoint path, caller, request style, and response shape.
- Call out inconsistencies directly:
  - duplicated save endpoints
  - namespace mismatches
  - double slashes in paths
  - legacy endpoints still present in code
- For live probes, report:
  - exact command used
  - base URL used
  - auth mode used
  - response code
  - whether the result came from a real endpoint response, captive portal, or local failure

## Resources

- `references/endpoint-catalog.md`
  - Canonical map of login, sync, save, report, and legacy endpoints seen in the Android client.
- `references/validation-playbook.md`
  - Read this before any live probe or health validation.
- `scripts/collect_api_context.py`
  - Refresh endpoint discovery from the repo and surface callsites, params, body keys, and flags.
- `scripts/api_probe.py`
  - Controlled live probe helper for `health`, `login`, `account`, and generic `request`.
