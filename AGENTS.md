# AGENTS.md

## Contexto
Repo Android legacy, Java puro, pre-AndroidX, monolitico en `app`.
`settings.gradle` aun declara `:socketchat`; verificar si debe restaurarse o eliminarse antes de cambios grandes.

## Baseline tecnico
- Gradle wrapper 5.4.1 / AGP 3.5.3 / Java 8 / compileSdk 28 / buildTools 28.0.3.
- No Kotlin.
- Dependencias legacy: Support Library 22.x, Google Play Services 4.2.42, Fabric/Crashlytics, Apache Http, JCenter.

## Reglas de trabajo
- No migrar a AndroidX, AGP moderno o Kotlin salvo pedido explicito.
- No tocar permisos, manifest, sync offline, DB schema ni URLs base sin revisar impacto end-to-end.
- Tratar `Comm.URL`, `Comm.ControlURL`, `AppDatabase` y `AndroidManifest.xml` como zonas criticas.
- No asumir que el build actual es reproducible: confirmar JDK 8, SDK 28 y estado de `:socketchat`.
- Si tocas Fabric/Crashlytics o el arranque de `LoginActivity`/`Main`, leer `.codex/skills/multipartes-crashlytics-guard/SKILL.md` antes de cambiar la inicializacion.
- Si la tarea toca API externa, `CommReq`, `Comm.URL`, login/auth, health, sync, reportes PDF o validacion de endpoints, usar primero `.codex/skills/multipartes-external-api/SKILL.md` y delegar esa exploracion a un subagente especializado separado.
- Ese especialista de API debe trabajar en modo read-only por defecto y ejecutar requests live solo bajo pedido explicito, leyendo `MULTIPARTES_API_*` desde variables de entorno.

## Mapa rapido
- `app/src/main/java/py/multipartesapp/activities`: pantallas y navegacion.
- `app/src/main/java/py/multipartesapp/db`: SQLite manual y esquema.
- `app/src/main/java/py/multipartesapp/comm` y `app/src/main/java/py/multipartesapp/services`: red/API.
- `app/src/main/java/py/multipartesapp/locationServices` y `app/src/main/java/py/multipartesapp/utils/control`: background, ubicacion y control remoto.
- `app/src/debug/res/values/google_maps_api.xml` y `app/src/release/res/values/google_maps_api.xml`: keys por entorno.

## Memorias tecnicas
- Para futuras tareas sobre `Consultas`, `Mis Remitos` o reportes PDF, leer primero `docs/codex/consultas-remitos.md`.

## Validacion minima
- `.\gradlew.bat :app:assembleDebug`
- Si se toca login/sync/db: probar login, menu principal, sincronizacion y un flujo offline/online.
- Si se toca manifest/permisos/background: probar en Android 10+ y anotar incompatibilidades.
- Si el build falla antes de compilar, reportar primero blockers de entorno y de dependencias.

## Riesgos conocidos
- `:socketchat` faltante.
- Repositorios/dependencias obsoletas (`jcenter`, Fabric, raw GitHub).
- El plugin de Fabric no se aplica en debug normal; no reintroducir `Fabric.with(...)` directo en activities o vuelve el crash `The Crashlytics build ID is missing`.
- Secretos versionados (Maps keys, Fabric key, keystore).
- Cleartext HTTP y clientes Apache Http.
- Cobertura de tests practicamente inexistente.


