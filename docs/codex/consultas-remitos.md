# Consultas y Mis Remitos

## Resumen

Esta memoria documenta la investigacion e implementacion de la opcion `Mis Remitos` en la pantalla `Consultas` del app Android legacy.

El trabajo se hizo sin modernizar arquitectura, Gradle, dependencias ni flujo general de reportes. Se mantuvo el patron legacy de Activities Java, layouts XML, descarga directa de PDF con `DownloaderPdf` y visualizacion local con `PDFView`.

Estado final de UI:

- `Estado de Cuenta Cliente`: visible.
- `Stock Producto`: visible.
- `Mis Visitas`: visible.
- `Mis Pedidos`: visible.
- `Mis Cobros`: oculto visualmente, sin borrar codigo.
- `Mis Remitos`: visible.

Nota historica: inicialmente se ocultaron `Mis Pedidos` y `Mis Cobros`, pero luego el requisito cambio a dejar `Mis Pedidos` visible y ocultar solo `Mis Cobros`.

## Estado del repo al documentar

El repo tenia cambios locales previos y archivos untracked antes y durante esta tarea. No se aplico stash ni se revirtieron cambios existentes.

Archivos funcionales relacionados con esta feature:

- `app/src/main/java/py/multipartesapp/activities/ConsultasActivity.java`
- `app/src/main/res/layout/activity_consultas.xml`
- `app/src/main/java/py/multipartesapp/activities/ConsultaRemitosActivity.java`
- `app/src/main/java/py/multipartesapp/activities/ConsultaRemitosWebViewActivity.java`
- `app/src/main/res/layout/activity_consulta_remitos.xml`
- `app/src/main/res/layout/activity_consulta_remitos_webview.xml`
- `app/src/main/AndroidManifest.xml`

Archivos modificados por otras tareas previas o estado local preexistente incluyen Gradle, `LoginActivity`, `Main`, utilidades y cache/directorios locales. No asumir que todo el diff actual corresponde a Remitos.

## Pantalla Consultas

La pantalla principal de consultas esta implementada en:

- Activity: `app/src/main/java/py/multipartesapp/activities/ConsultasActivity.java`
- Layout: `app/src/main/res/layout/activity_consultas.xml`
- Registro manifest: `app/src/main/AndroidManifest.xml`, activity `py.multipartesapp.activities.ConsultasActivity`

Botones en `activity_consultas.xml`:

- `consultas_estado_cuenta`: texto `Estado de Cuenta Cliente`.
- `consultas_stock_producto`: texto `Stock Producto`.
- `consultas_visitas`: texto `Mis Visitas`.
- `consultas_entregas`: texto `Mis Entregas`, ya estaba oculto por codigo con `View.INVISIBLE`.
- `consultas_pedidos`: texto `Mis Pedidos`.
- `consultas_cobranzas`: texto `Mis Cobros`.
- `consultas_remitos`: texto `Mis Remitos`, agregado para esta feature.

Wiring en `ConsultasActivity.java`:

- `consultas_estado_cuenta` abre `ConsultaClienteActivity`.
- `consultas_stock_producto` abre `ConsultaStockActivity`.
- `consultas_visitas` abre `ConsultaVisitasActivity`.
- `consultas_pedidos` abre `ConsultaPedidosActivity`.
- `consultas_cobranzas` sigue apuntando a `ConsultaCobrosActivity`, pero el boton se oculta con `View.GONE`.
- `consultas_remitos` abre `ConsultaRemitosActivity`.

## Cambios visuales

No se borro codigo legacy de pedidos ni cobros.

Estado final:

- `Mis Pedidos` queda visible en el layout y conserva su listener a `ConsultaPedidosActivity`.
- `Mis Cobros` se oculta visualmente:
  - En `activity_consultas.xml`, `consultas_cobranzas` tiene `android:visibility="gone"`.
  - En `ConsultasActivity.java`, `misCobrosBtn.setVisibility(View.GONE)` refuerza la ocultacion.
- `Mis Remitos` se agrego como nuevo boton visible en `activity_consultas.xml` y se enlazo a `ConsultaRemitosActivity`.

## Flujo de Estado de Cuenta Cliente usado como referencia

El flujo de `Estado de Cuenta Cliente` fue la referencia principal para:

- Carga de clientes desde SQLite con `AppDatabase.selectAllCliente()`.
- Uso de `AutoCompleteTextView` para buscar/seleccionar cliente.
- Validacion visual y comportamiento legacy.
- Descarga de PDF a `Environment.DIRECTORY_DOWNLOADS`.
- Apertura de una segunda Activity con `PDFView`.

Archivos de referencia:

- `ConsultaClienteActivity.java`
- `activity_consulta_cliente.xml`
- `ConsultaClienteWebViewActivity.java`
- `activity_consulta_cliente_webview.xml`

Endpoint de Estado de Cuenta:

```text
{Comm.URL}multip/api/report/extracto?clientId={clienteId}&userId=0&format=pdf
```

La descarga usa `DownloaderPdf.DownloadFile(...)`, que hace un `GET` con `HttpURLConnection` y header `api_version: BuildConfig.VERSION_NAME`.

## Mis Remitos

Se crearon dos pantallas nuevas:

- `ConsultaRemitosActivity`: formulario de filtros y descarga del PDF.
- `ConsultaRemitosWebViewActivity`: visor local del PDF descargado.

Layouts nuevos:

- `activity_consulta_remitos.xml`
- `activity_consulta_remitos_webview.xml`

Se registraron las Activities nuevas en `AndroidManifest.xml`, porque este proyecto navega con `Intent` explicito entre Activities.

## Filtros de Mis Remitos

`ConsultaRemitosActivity` tiene:

- `Fecha desde`
- `Fecha hasta`
- `Cliente`
- `Delivery`
- Boton `Consultar`

Reglas finales de fechas:

- `Desde` y `Hasta` son opcionales para el usuario porque ya vienen precargados.
- Ambos campos se inicializan con la fecha de hoy.
- `Desde` solo permite elegir hasta 90 dias hacia atras desde hoy.
- `Hasta` solo permite elegir una fecha igual o mayor que `Desde`.
- Si se cambia `Desde` y `Hasta` queda menor, `Hasta` se ajusta automaticamente a la nueva fecha `Desde`.
- Los campos de fecha no quedan editables manualmente; se eligen desde calendario.

Regla final de cliente:

- `Cliente` es opcional.
- Si se selecciona cliente, se envia su id.
- Si no se selecciona cliente, se envia `clientId=null` al endpoint.

Regla final de delivery:

- `Delivery` es opcional y se muestra como checkbox.
- Por defecto queda marcado.
- Si se marca, se envia `isDelivery=Y`.
- Si queda desmarcado, se envia `isDelivery=N`.

Formato de fechas enviado al backend:

- La UI muestra `dd-MM-yyyy`.
- El backend recibe `yyyy-MM-dd`.

## Backend PDF

El endpoint correcto descubierto y validado es:

```text
{Comm.URL}multip/api/report/remitos?from={yyyy-MM-dd}&to={yyyy-MM-dd}&clientId={id|null}&userId={userId}&isDelivery={Y|N}&format=pdf
```

Ejemplo validado por curl:

```text
http://app.multipartes.com.py/multip/api/report/remitos?from=2026-05-21&to=2026-05-21&clientId=180012791&userId=88&format=pdf
```

Hallazgo importante:

- La app inicialmente llamaba `api/report/remitos`.
- Esa ruta devolvia `502 Bad Gateway` en HTML.
- El visor PDF fallaba porque se guardaba ese HTML como `remitos_multipartes.pdf`.
- La ruta correcta es `multip/api/report/remitos`, que devuelve `200 OK`, `Content-Type: application/pdf` y contenido que empieza con `%PDF`.

Sobre `api_version`:

- La app no lo manda como query param.
- `DownloaderPdf` lo manda como header:

```text
api_version: BuildConfig.VERSION_NAME
```

## Descarga y visualizacion de PDF

Descarga:

- `ConsultaRemitosActivity` crea el archivo `remitos_multipartes.pdf`.
- El archivo se guarda en `Environment.DIRECTORY_DOWNLOADS`.
- La descarga se hace con `DownloaderPdf.DownloadFile(url, file)`.

Visualizacion:

- Luego de descargar, `ConsultaRemitosActivity` abre `ConsultaRemitosWebViewActivity`.
- `ConsultaRemitosWebViewActivity` lee:

```text
Downloads/remitos_multipartes.pdf
```

- El PDF se muestra con:

```java
webViewPdf.fromUri(url).load();
```

igual que los reportes legacy.

## Compilacion

Comando minimo:

```powershell
.\gradlew.bat :app:assembleDebug
```

En este entorno, el `JAVA_HOME` normal apuntaba a JDK 17 y/o el `java` visible podia ser JDK 21, lo cual rompe Gradle 5.4.1/Groovy antes de compilar.

Validacion que funciono sin cambiar archivos:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:assembleDebug --no-daemon
```

Resultado observado: `BUILD SUCCESSFUL`.

## Prueba manual

1. Instalar APK debug en un dispositivo/emulador.
2. Iniciar sesion.
3. Entrar al menu principal.
4. Abrir `Consultas`.
5. Verificar:
   - `Mis Pedidos` visible.
   - `Mis Cobros` no visible.
   - `Mis Remitos` visible.
6. Abrir `Mis Remitos`.
7. Verificar que `Desde` y `Hasta` cargan con fecha de hoy.
8. Verificar que `Desde` no permite ir mas de 90 dias atras.
9. Verificar que `Hasta` no permite ser menor que `Desde`.
10. Probar sin cliente seleccionado; debe enviar `clientId=null`.
11. Probar con cliente seleccionado; debe enviar el id del cliente.
12. Presionar `Consultar`.
13. Verificar que se abre el PDF en el visor interno.

## Riesgos y pendientes

- `DownloaderPdf` hace descarga sincronica en UI thread, igual que los reportes legacy. Puede congelar la pantalla si la red es lenta.
- No hay validacion robusta de que el archivo descargado sea PDF antes de abrirlo. Si el backend devuelve HTML/error, el visor falla.
- `WRITE_EXTERNAL_STORAGE` y `Downloads` pueden tener problemas en Android moderno.
- El endpoint depende de que `Comm.URL` este configurado con base correcta, normalmente `http://app.multipartes.com.py/`.
- El path correcto de remitos usa `multip/api/...`; no cambiarlo a `api/...`.
- `clientId=null` se envia como string literal cuando no hay cliente. El backend debe tolerarlo.
- La lista de clientes depende de sincronizacion previa local.
- Hay cambios locales preexistentes en el repo; antes de nuevas tareas revisar `git status` y separar cambios no relacionados.
