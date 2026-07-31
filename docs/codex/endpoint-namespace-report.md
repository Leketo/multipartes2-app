# Informe de endpoints por namespace

Fecha de relevamiento: 2026-06-12

## Alcance

Inventario estatico basado en el cliente Android, sin ejecutar requests live contra el servidor.

Fuentes revisadas:

- `.codex/skills/multipartes-external-api/references/endpoint-catalog.md`
- `.codex/skills/multipartes-external-api/scripts/collect_api_context.py .`
- `app/src/main/java/py/multipartesapp/comm/CommReq.java`
- Call sites con `Comm.requestGet`, `Comm.requestPost`, `HttpPost`, `HttpGet`, `DownloaderPdf` y descargas directas con `Comm.URL`
- `docs/codex/consultas-remitos.md` para el endpoint vigente de remitos

## Resumen ejecutivo

Conteo principal: rutas unicas usadas por el cliente Android. Las rutas con parametros dinamicos se cuentan una vez como template. Se conserva el namespace real visto en codigo, sin normalizar `multip/api`, `erp/api` ni `api`.

| Namespace | Rutas unicas usadas | Usos/callsites encontrados | Nota |
| --- | ---: | ---: | --- |
| `multip/api` | 27 | 34 | Namespace dominante de sync, guardado y reportes |
| `erp/api` | 4 | 4 | Login JWT, cuenta, usuarios y stock |
| `api` | 4 | 4 | Reportes PDF y reintento de cobro |
| Otro | 1 | 1 | `j_spring_security_check`, login legacy |
| **Total** | **36** | **43** | Incluye duplicados de uso, pero no duplica rutas iguales |

Referencias externas que no se contaron como endpoints REST reales:

- `Comm.ControlURL`: socket de control remoto (`IO.socket(...)`), no REST.
- `https://mi-pagina.com/api/`: base URL placeholder en `ApiAdapter`, sin endpoint concreto observado.
- `api/report/extracto`: aparece comentado en `ConsultaClienteWebViewActivity`; el flujo activo usa `multip/api/report/extracto`.

## Rutas `erp/api`

| Metodo | Ruta | Uso principal |
| --- | --- | --- |
| `POST` | `erp/api/authenticate` | Login JWT activo |
| `GET` | `erp/api/account` | Datos de cuenta luego del login |
| `GET` | `erp/api/users` | Sincronizacion de usuarios |
| `GET` | `erp/api/multipartes/stock/sincronizar/{ultimaActualizacion}` | Sincronizacion de stock, respuesta `Base64 + GZIP` |

Total `erp/api`: 4 rutas unicas.

## Rutas `api`

| Metodo | Ruta | Uso principal |
| --- | --- | --- |
| `POST` | `api/cobro/registrar-cobro` | Reintento offline de cobro; namespace inconsistente con el envio principal |
| `GET` | `api/report/cobros` | PDF de cobros |
| `GET` | `api/report/entregas` | PDF de entregas |
| `GET` | `api/report/app/visitas` | PDF de visitas |

Total `api`: 4 rutas unicas.

## Rutas `multip/api`

| Metodo | Ruta | Uso principal |
| --- | --- | --- |
| `POST` | `multip/api/cobro/registrar-cobro` | Envio principal de cobros |
| `GET` | `multip/api/report/extracto` | PDF de estado de cuenta |
| `GET` | `multip/api/report/pedidos/detalle` | PDF de pedidos |
| `GET` | `multip/api/report/remitos` | PDF de remitos |
| `GET` | `multip/api/product/stock-producto/` | Consulta puntual de stock por producto |
| `GET` | `multip/api/delivery/save` | Guardado/envio de entregas |
| `GET` | `multip/api/delivery/summary` | Sincronizacion de entregas |
| `POST` | `multip/api/routes/update` | Actualizacion de rutas |
| `POST` | `multip/api/routes/save` | Guardado de rutas/visitas de ruta |
| `GET` | `multip/api/routes/sincronizar/hoja-ruta/TODOS` | Sincronizacion de hoja de ruta por usuario |
| `GET` | `multip/api/user/current` | Login legacy / usuario actual |
| `GET` | `multip/api/location/save` | Envio de ubicacion |
| `POST` | `multip/api//order/save` | Envio/reenvio de pedidos; tiene doble slash por `CommReqEnviarPedido` |
| `POST` | `multip/api/visit/visit/save` | Guardado principal de visitas |
| `POST` | `/multip/api/visit/save` | Reintento de visitas; tiene slash inicial y difiere del endpoint principal |
| `GET` | `multip/api/product/img/file/` | Descarga directa de imagen de producto |
| `GET` | `multip/api/client/sincronizar/{ultimaActualizacion}` | Sincronizacion de clientes, respuesta `Base64 + GZIP` |
| `GET` | `multip/api/order/summary/{ultimaActualizacion}` | Sincronizacion de pedidos por usuario |
| `GET` | `multip/api/product/sincronizar/{ultimaActualizacion}` | Sincronizacion de productos, respuesta `Base64 + GZIP` |
| `GET` | `multip/api/product/price/version/list` | Lista de categorias/versiones de precio |
| `GET` | `multip/api/product/price/summary/new/{ultimaActualizacion}` | Sincronizacion de precios, respuesta `Base64 + GZIP` |
| `GET` | `multip/api/charge/summary/{ultimaActualizacion}` | Sincronizacion de cobros |
| `GET` | `multip/api/invoice/sincronizar/{ultimaActualizacion}` | Sincronizacion de facturas, respuesta `Base64 + GZIP` |
| `GET` | `multip/api/product/family/summary` | Sincronizacion de familias de producto |
| `GET` | `multip/api/product/subfamily/summary` | Sincronizacion de subfamilias de producto |
| `GET` | `multip/api/product/img/summary/{ultimaActualizacion}` | Sincronizacion de metadata de imagenes |
| `GET` | `multip/api/visit/user/` | Sincronizacion/consulta de visitas por usuario y fechas |

Total `multip/api`: 27 rutas unicas.

## Otros

| Metodo | Ruta/referencia | Uso principal |
| --- | --- | --- |
| `POST` | `j_spring_security_check` | Login legacy todavia referenciado en `LoginActivity` |

Total otros: 1 ruta unica REST legacy.

## Constantes `CommReq` declaradas pero no vistas como llamadas activas

Estas constantes existen en `CommReq.java`, pero en el barrido de callsites no aparecieron como rutas activas directas en el flujo actual:

| Namespace | Ruta |
| --- | --- |
| `multip/api` | `multip/api/client/summary` |
| `multip/api` | `multip/api/visit/save/params` |
| `multip/api` | `multip/api/user/admin/users` |
| `multip/api` | `multip/api/product/summary` |
| `multip/api` | `multip/api/product/price/summary` |
| `multip/api` | `multip/api/charge/save` |
| `multip/api` | `multip/api/invoice/summary` |
| `multip/api` | `multip/api/report/pedidos` |

Si se cuentan tambien estas constantes no usadas, el total potencial por namespace queda:

| Namespace | Rutas usadas | Constantes no vistas activas | Total potencial |
| --- | ---: | ---: | ---: |
| `multip/api` | 27 | 8 | 35 |
| `erp/api` | 4 | 0 | 4 |
| `api` | 4 | 0 | 4 |
| Otro | 1 | 0 | 1 |
| **Total** | **36** | **8** | **44** |

## Inconsistencias relevantes

- Cobros usa dos namespaces para la misma intencion: `multip/api/cobro/registrar-cobro` en el envio principal y `api/cobro/registrar-cobro` en el reintento.
- Visitas usa dos rutas distintas: `multip/api/visit/visit/save` y `/multip/api/visit/save`.
- Pedidos usa `multip/api//order/save`, con doble slash.
- Reportes PDF estan mezclados: pedidos, extracto y remitos usan `multip/api`, pero cobros, entregas y visitas usan `api`.
- `erp/api` queda concentrado en autenticacion, cuenta, usuarios y stock sincronizado.
