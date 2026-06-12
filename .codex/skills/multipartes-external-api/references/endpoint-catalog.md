# Multipartes External API Catalog

Repository baseline used for this catalog:

- App repo: `multipartes2-app`
- Base URL source: `Comm.URL`, configured at runtime from `ConfiguracionActivity`
- Default suggested host in UI: `http://app.multipartes.com.py/`
- Primary namespaces seen in code:
  - `multip/api/`
  - `erp/api/`
  - `api/`

## Auth and Base URL

### Base URL behavior

- `Comm.URL` starts empty in `Comm.java`.
- `ConfiguracionActivity` persists the chosen URL and loads it back into `Comm.URL`.
- Most endpoints are relative to that root URL and append either `multip/api/...`, `erp/api/...`, or `api/...`.

### Active auth flow

| Method | Path | Callers | Request | Response | Notes |
| --- | --- | --- | --- | --- | --- |
| `POST` | `erp/api/authenticate` | `LoginActivity` | JSON body: `username`, `password`, `remember`; headers: `Accept`, `Content-type`, `api_version` | JSON with `id_token` | Current login flow used by the app; `LoginActivity` parses `id_token` and immediately calls `getDatosUsuario(token)` |
| `GET` | `erp/api/account` | `LoginActivity` | Header `Authorization: Bearer {token}` plus `Accept`, `Content-type`, `api_version` | JSON object with at least `id`; live validation also observed `login` | Used immediately after login to derive `Session.userId` and persist the active bearer session |

### Verified live auth case

Live validation captured on `2026-03-25` against the default suggested host:

- Base URL: `http://app.multipartes.com.py/`
- Flow executed: `POST erp/api/authenticate` -> `GET erp/api/account`
- `authenticate` returned HTTP `200` and a JSON payload containing `id_token`
- `account` returned HTTP `200` and a JSON object containing at least `id` and `login`
- Observed `account.id` for the tested user: `88`
- JWT intentionally omitted from this repo and from the documentation output

How the Android client uses this flow:

- `LoginActivity` parses `id_token` from `erp/api/authenticate`
- It then calls `getDatosUsuario(token)` against `erp/api/account`
- From the `account` response it reads `id`, converts it to `int`, and stores it as `Session.userId`
- It also persists the bearer token itself as the active session token

### Legacy auth helpers still present

| Method | Path | Callers | Request | Response | Notes |
| --- | --- | --- | --- | --- | --- |
| `POST` | `j_spring_security_check` | `LoginActivity` commented flow | Form fields: `j_username`, `j_password` | Legacy login bean path | Cookie-oriented and not the active production login path |
| `GET` | `multip/api/user/current` | `LoginActivity` helper methods | Query: `username` | Expected `Session`, but caller passes `Usuario.class.getName()` | Legacy / inconsistent with current JWT flow |

## Read and Sync Endpoints

These are the primary read-oriented endpoints used by `Comm.requestGet`.

| Method | Path | Callers | Query or Path Params | Response | Auth / Transport Notes |
| --- | --- | --- | --- | --- | --- |
| `GET` | `erp/api/multipartes/stock/sincronizar/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `StockList` | `respuestaBase64=true`; decode `Base64 + GZIP` before JSON parsing |
| `GET` | `multip/api/client/sincronizar/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `ClienteList` | `respuestaBase64=true` |
| `GET` | `erp/api/users` | `SincronizarActivity` | None | `UsuarioList` | Plain JSON array |
| `GET` | `multip/api/order/summary/{ultimaActualizacion}` | `SincronizarActivity` | Query: `userId` | `PedidoList` | Plain JSON |
| `GET` | `multip/api/product/sincronizar/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `ProductoList` | `respuestaBase64=true` |
| `GET` | `multip/api/product/price/version/list` | `SincronizarActivity` | None | `PrecioCategoriaList` | Name is misleading: it returns price categories |
| `GET` | `multip/api/product/price/summary/new/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `PrecioVersionList` | `respuestaBase64=true` |
| `GET` | `multip/api/routes/sincronizar/hoja-ruta/TODOS?userId={id}` | `SincronizarActivity` | `userId` is concatenated directly into the request string | `RutaLocationList` | Query is embedded in the path expression instead of `params` |
| `GET` | `multip/api/charge/summary/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `CobranzaList` | Plain JSON |
| `GET` | `multip/api/invoice/sincronizar/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `FacturaList` | `respuestaBase64=true` |
| `GET` | `multip/api/product/family/summary` | `SincronizarActivity` | None | `ProductoFamiliaList` | Only called when catalog sync is enabled |
| `GET` | `multip/api/product/subfamily/summary` | `SincronizarActivity` | None | `ProductoSubFamiliaList` | Only called when catalog sync is enabled |
| `GET` | `multip/api/product/img/summary/{ultimaActualizacion}` | `SincronizarActivity` | Path var: last sync timestamp | `ProductoImagenList` | Followed by per-file image download |
| `GET` | `multip/api/product/img/file/{m_product_id}` | `SincronizarActivity` | Path var: product id | Binary image file | Downloaded outside `Comm`, via direct URL stream |
| `GET` | `multip/api/visit/user/` | `SincronizarActivity` | `userId`, `fechaDesde`, `fechaHasta` | `RegistroVisitaList` | Plain JSON |
| `GET` | `multip/api/product/stock-producto/` | `ConsultaStockActivity`, `PedidoDetalleNuevoActivity` | `codigo_producto` | `StockList` | Point lookup endpoint |

## Operational Save Endpoints

These endpoints mutate backend state. Treat them as dangerous for live validation unless the user asked for that exact execution.

| Method | Path | Callers | Request Shape | Response | Notes |
| --- | --- | --- | --- | --- | --- |
| `GET` | `multip/api/location/save` | `LocationService`, `LocationReceiver`, `Main` | Query: `latitude`, `longitude`, `user_id`, `time`, `date` | No stable bean mapping | Called through `Comm.requestGet`; caller passes empty class name |
| `GET` | `multip/api/delivery/save` | `EntregaActivity` | Query: `user_id`, `client_id`, `order_id`, `date_delivered`, `time_delivered`, `observation` | `Entrega` | Used both for direct send and resend |
| `POST` | `multip/api/routes/save` | `RutaLocationNewActivity` | JSON body includes `date`, `user_id`, `client_id`, `zone`, `priority`, `status`, `observation`, `type`; one variant also sends `entrada` and `salida` timestamps | Raw string or JSON | Two payload variants exist |
| `POST` | `multip/api/routes/update` | `ListRutasActivity` | Query string built manually: `routeid`, `tipo`, `observation`, `fechahora`, optional `estado`; empty JSON body | Raw string | Hardcoded URL despite existing `CommReqPostUpdateRoute` constant |
| `POST` | `multip/api/visit/visit/save` | `RegistroVisitasActivity` | JSON body: `tipo_visita`, `ruc`, `cliente`, `latitude`, `longitude`, `observation`, `status`, `usuario`, `fechavisita`, `horavisita`, `fecha_prox_visita`, `ent_sal` | Raw string | Main visit save path |
| `POST` | `/multip/api/visit/save` | `RegistroVisitasActivity` retry flow | JSON body similar to main visit save, but forces `status = "A"` and omits `ent_sal` | Raw string | Retry variant has a leading slash and different path |
| `POST` | `multip/api//order/save` | `PedidoActivity` | JSON header fields: `ad_client_id`, `ad_org_id`, `date_order`, `isactive`, `client_id`, `user_id`, `total`, `observation`, `createdby`, `updatedby`; nested `orderline[]` with `isactive`, `product_id`, `quantity`, `price`, `total`, `observation` | Raw string | Double slash comes from `CommReqEnviarPedido` definition |
| `POST` | `multip/api/cobro/registrar-cobro` | `CobranzaActivity` main send | JSON body: `date_received`, `time_received`, `client_id`, `user_id`, `amount`, `receipt_number`, `observation`, `status`, `ad_org_id`, `facturasPagadas[]`, `formaPago[]` | Raw string | Main charge registration path |
| `POST` | `api/cobro/registrar-cobro` | `CobranzaActivity` retry flow | Payload equivalent to main charge save | Raw string | Inconsistent namespace: retry drops `multip/` |

### Nested payload details

#### `orderline[]` in order save

- `isactive`
- `product_id`
- `quantity`
- `price`
- `total`
- `observation`

#### `facturasPagadas[]` in charge save

- `invoice_id`
- `amount`
- `cashed`

#### `formaPago[]` in charge save

- `payment_type`
- `amount`
- `bank`
- `check_number`
- `expired_date`
- `check_name`
- `iscrossed`

## Reports and PDF Downloads

These are read-only but return PDF files instead of JSON.

| Method | Path | Callers | Query | Response | Notes |
| --- | --- | --- | --- | --- | --- |
| `GET` | `multip/api/report/extracto` | `ConsultaClienteActivity` | `clientId`, `userId=0`, `format=pdf` | PDF | Downloaded with `DownloaderPdf` |
| `GET` | `multip/api/report/pedidos/detalle` | `ConsultaPedidosActivity` | `from`, `to`, `clientId=0`, `userId`, `format=pdf` | PDF | Built from `CommReqConsultaPedidos + "/detalle"` |
| `GET` | `api/report/cobros` | `ConsultaCobrosActivity` | `from`, `to`, `clientId=0`, `userId`, `format=pdf` | PDF | Bare `api/` namespace |
| `GET` | `api/report/entregas` | `ConsultaEntregasActivity` | `from`, `to`, `clientId=0`, `userId`, `format=pdf` | PDF | Bare `api/` namespace |
| `GET` | `api/report/app/visitas` | `ConsultaVisitasActivity` | `from`, `to`, `clientId=0`, `userId`, `format=pdf` | PDF | Bare `api/` namespace |

## Common Headers Seen in the Client

### For `Comm` requests

- `api_version: BuildConfig.VERSION_NAME`
- Cookies via `Globals.cookieStore` when using `Http.java`

### For manual JSON POSTs

- `Accept: application/json`
- `Content-type: application/json`
- `api_version: BuildConfig.VERSION_NAME`

### For JWT account lookup

- `Authorization: Bearer {token}`

## Health and Reachability

The app does not expose a dedicated REST health endpoint in the client code.

What exists instead:

- `AppUtils.isOnline()` performs `ping -c 1 app.multipartes.com.py`.
- `Http.java` and several manual request flows treat HTML containing `Portal Movil Tigo` as false connectivity or captive portal.
- `NetworkChangesReceiver` references `Ping.pingHost()` conceptually, but the real ping call is commented out and no `Ping.java` implementation is present.

For this repo, a practical health probe is synthetic:

- Reach the configured host.
- Probe `erp/api/account`.
- Treat `200`, `401`, or `403` as evidence that the API stack is reachable.

## Known Inconsistencies

- Mixed auth models:
  - Active login is JWT-based.
  - `Comm` still carries cookie-oriented behavior through `Globals.cookieStore`.
- Mixed namespaces:
  - `multip/api/...`
  - `erp/api/...`
  - `api/...`
- Duplicated endpoint intentions:
  - visits: `visit/visit/save` vs `/visit/save`
  - charges: `multip/api/cobro/registrar-cobro` vs `api/cobro/registrar-cobro`
  - route update has a constant but the caller hardcodes the URL
- `CommReqEnviarPedido` resolves to `multip/api//order/save`
- `Comm.get()` does not URL-encode query params globally; some callers patch spaces manually
- Several sync endpoints require `Base64 + GZIP` decoding before JSON parsing

## Client-Side Source of Truth

When this catalog needs to be refreshed, inspect these first:

- `app/src/main/java/py/multipartesapp/comm/CommReq.java`
- `app/src/main/java/py/multipartesapp/comm/Comm.java`
- `app/src/main/java/py/multipartesapp/comm/Http.java`
- `app/src/main/java/py/multipartesapp/activities/LoginActivity.java`
- `app/src/main/java/py/multipartesapp/activities/SincronizarActivity.java`
- `app/src/main/java/py/multipartesapp/activities/PedidoActivity.java`
- `app/src/main/java/py/multipartesapp/activities/CobranzaActivity.java`
- `app/src/main/java/py/multipartesapp/activities/RegistroVisitasActivity.java`
- `app/src/main/java/py/multipartesapp/activities/RutaLocationNewActivity.java`
- `app/src/main/java/py/multipartesapp/activities/ListRutasActivity.java`

