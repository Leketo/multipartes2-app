package py.multipartes2.android.activities;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import py.multipartes2.R;
import py.multipartes2.beans.Cliente;
import py.multipartes2.beans.RegistroVisita;
import py.multipartes2.beans.TipoVisita;
import py.multipartes2.beans.Session;
import py.multipartes2.beans.Usuario;
import py.multipartes2.comm.Comm;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;
import py.multipartes2.utils.Globals;
import py.multipartes2.utils.MyLocation;


public class RegistroVisitasActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = RegistroVisitasActivity.class.getSimpleName();

    private SimpleDateFormat dateFormatter;

    private EditText fechaProxVisitaEditText;
    private DatePickerDialog fechaProxVisitaDialog;
    private ImageButton calendarBtn;

    private AutoCompleteTextView clienteTextView;
    private Spinner clienteSpinner;
    private Spinner tipoVisitaSpinner;
    private RadioButton radioButtonSinVenta;
    private RadioButton radioButtonConVenta;
    private Button guardarVisitaBtn;
    private EditText observacionTextView;
    private LinearLayout datosClienteLinearLayout;

    private TextView telefonoTextView;
    private TextView creditoDisponibleTextView;
    private TextView creditoUsadoTextView;
    private TextView chequePendienteTextView;
    private TextView facturaViejaTextView;
    private TextView plazoMaxChequeTextView;


    DecimalFormat formateador = new DecimalFormat("###,###.##");
    private Location myLocation;
    private Cliente clienteSeleccionado;
    private TipoVisita tipoVisitaSeleccionado;


    private AppDatabase db = new AppDatabase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrovisitas);

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Registro de Visita");

        if (db.countCliente() == 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        clienteTextView = (AutoCompleteTextView) findViewById(R.id.registrovisita_cliente2);
        clienteSpinner = (Spinner) findViewById(R.id.registrovisita_cliente);
        tipoVisitaSpinner = (Spinner) findViewById(R.id.registrovisita_tipo_visita);
        radioButtonConVenta = (RadioButton) findViewById(R.id.radio_con_venta);
        radioButtonSinVenta = (RadioButton) findViewById(R.id.radio_sin_venta);
        observacionTextView = (EditText) findViewById(R.id.registrovisita_observacion);
        guardarVisitaBtn = (Button) findViewById(R.id.guardar_visita);
        datosClienteLinearLayout = (LinearLayout) findViewById(R.id.registrovisita_datos_cliente);

        telefonoTextView = (TextView) findViewById(R.id.registrovisita_telefono);
        creditoDisponibleTextView = (TextView) findViewById(R.id.registrovisita_cred_disponible);
        creditoUsadoTextView = (TextView) findViewById(R.id.registrovisita_cred_usado);
        chequePendienteTextView = (TextView) findViewById(R.id.registrovisita_cheques_pendiente);
        facturaViejaTextView = (TextView) findViewById(R.id.registrovisita_factura_vieja);
        plazoMaxChequeTextView = (TextView) findViewById(R.id.registrovisita_plazo_max_cheque);

        fechaProxVisitaEditText = (EditText) findViewById(R.id.registrovisita_fecha_prox_visita);
        fechaProxVisitaEditText.setInputType(InputType.TYPE_NULL);
        String fechaVisita = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        fechaProxVisitaEditText.setText(fechaVisita);

        calendarBtn = (ImageButton) findViewById(R.id.registrovisita_calendar_btn);

        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               fechaProxVisitaDialog.show();
            }
        });

        TipoVisita tipo1 = new TipoVisita("1", "Visita de Cobro");
        TipoVisita tipo2 = new TipoVisita("2", "Visita de Venta");

        List<TipoVisita> listTiposVisita = new ArrayList<TipoVisita>();
        listTiposVisita.add(tipo1);
        listTiposVisita.add(tipo2);


        List<Cliente> lista_clientes = db.selectAllCliente();
        ArrayAdapter<Cliente> adapter = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, lista_clientes);

        //temporal
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clienteTextView.setAdapter(adapter);

        ArrayAdapter<TipoVisita> tipoVisitaArrayAdapter = new ArrayAdapter<TipoVisita>(this, android.R.layout.simple_spinner_item, listTiposVisita);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoVisitaSpinner.setAdapter(tipoVisitaArrayAdapter);

        setupRadioButtonConVenta();
        setupRadioButtonSinVenta();
        handleLocation();
        setDateTimeField();

        tipoVisitaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoVisitaSeleccionado = (TipoVisita) parent.getAdapter().getItem(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Si proviene de Lista de Rutas, cargar el cliente en cuestion
        if (Globals.getClienteSeleccionadoRuta() != null){
            clienteSeleccionado = Globals.getClienteSeleccionadoRuta();
            clienteTextView.setText(clienteSeleccionado.toString());
            datosClienteLinearLayout.setVisibility(View.VISIBLE);

            String credito_usado = "-";
            String credito_disponible = "-";
            String factura_vieja = "-";
            String cheques_pendiente = "-";
            String plazo_max_cheque = "-";
            String telefono = "-";

            if (clienteSeleccionado.getTelefono() != null){
                telefono = clienteSeleccionado.getTelefono();
            }

            if (clienteSeleccionado.getCredito_usado() != null){
                credito_usado = String .valueOf(clienteSeleccionado.getCredito_usado().intValue());
                credito_usado = formatearMoneda(credito_usado);
            }
            if (clienteSeleccionado.getCredito_disponible() != null) {
                credito_disponible = String.valueOf(clienteSeleccionado.getCredito_disponible().intValue());
                credito_disponible = formatearMoneda(credito_disponible);
            }
            if (clienteSeleccionado.getFactura_vieja() != null) {
                factura_vieja = String.valueOf(clienteSeleccionado.getFactura_vieja());

            }
            if (clienteSeleccionado.getCheques_pend() != null ){
                cheques_pendiente = String.valueOf(clienteSeleccionado.getCheques_pend().intValue());
                cheques_pendiente = formatearMoneda(cheques_pendiente);
            }
            if (clienteSeleccionado.getPlazomax() != null){
                plazo_max_cheque = String.valueOf(clienteSeleccionado.getPlazomax());

            }

            creditoUsadoTextView.setText("Crédito Usado: "+credito_usado);
            creditoDisponibleTextView.setText("Cred. Disponible: "+ credito_disponible);
            facturaViejaTextView.setText("Factura vieja: "+factura_vieja);
            chequePendienteTextView.setText("Cheque Pendiente: " + cheques_pendiente);
            plazoMaxChequeTextView.setText("Plazo Max. Cheque: "+ plazo_max_cheque);
            telefonoTextView.setText("Teléfono: "+telefono);
            Globals.setClienteSeleccionadoRuta(null);
        }


        clienteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clienteTextView.clearFocus();
                clienteSeleccionado = (Cliente) parent.getAdapter().getItem(position);
                datosClienteLinearLayout.setVisibility(View.VISIBLE);

                String credito_usado = "-";
                String credito_disponible = "-";
                String factura_vieja = "-";
                String cheques_pendiente = "-";
                String plazo_max_cheque = "-";
                String telefono = "-";

                if (clienteSeleccionado.getTelefono() != null){
                    telefono = clienteSeleccionado.getTelefono();
                }

                if (clienteSeleccionado.getCredito_usado() != null){
                    credito_usado = String .valueOf(clienteSeleccionado.getCredito_usado().intValue());
                    credito_usado = formatearMoneda(credito_usado);
                }
                if (clienteSeleccionado.getCredito_disponible() != null) {
                    credito_disponible = String.valueOf(clienteSeleccionado.getCredito_disponible().intValue());
                    credito_disponible = formatearMoneda(credito_disponible);
                }
                if (clienteSeleccionado.getFactura_vieja() != null) {
                    factura_vieja = String.valueOf(clienteSeleccionado.getFactura_vieja());

                }
                if (clienteSeleccionado.getCheques_pend() != null ){
                    cheques_pendiente = String.valueOf(clienteSeleccionado.getCheques_pend().intValue());
                    cheques_pendiente = formatearMoneda(cheques_pendiente);
                }
                if (clienteSeleccionado.getPlazomax() != null){
                    plazo_max_cheque = String.valueOf(clienteSeleccionado.getPlazomax());

                }

                creditoUsadoTextView.setText("Crédito Usado: "+credito_usado);
                creditoDisponibleTextView.setText("Cred. Disponible: "+ credito_disponible);
                facturaViejaTextView.setText("Factura vieja: "+factura_vieja);
                chequePendienteTextView.setText("Cheque Pendiente: " + cheques_pendiente);
                plazoMaxChequeTextView.setText("Plazo Max. Cheque: "+ plazo_max_cheque);
                telefonoTextView.setText("Teléfono: "+telefono);

                //Toast.makeText(RegistroVisitasActivity.this, "selecciono-"+clienteSeleccionado.getNombre(), Toast.LENGTH_LONG).show();
            }
        });

        guardarVisitaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                guardarVisitaBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);
                enviarVisita();
            }
        });

    }


    //metodo que obtiene la ubicación
    private void handleLocation (){
        MyLocation.LocationResult locationResult = new MyLocation.LocationResult(){
            @Override
            public void gotLocation(Location location){
                //Got the location!
                if (location != null) {
                    Log.d(TAG, "Location ok: latitud: " + location.getLatitude() + " longitud: " + location.getLongitude() + " precisión: " + location.getAccuracy() + " m" + " provider:" + location.getProvider());
                    //Toast.makeText(getApplicationContext(), "x:"+location.getLatitude() + ", y: "+location.getLongitude(), Toast.LENGTH_LONG).show();
                    myLocation = location;
                    //findPlaces(location);
                } else {
                    Log.d(TAG, "Got location is null");
                }
            }
        };
        MyLocation myLocation = new MyLocation();
        Boolean existLocation = myLocation.getLocation(this, locationResult);
        if (!existLocation){
            Toast.makeText(getApplicationContext(), "Favor active GPS o Red para obtener ubicación de visita.", Toast.LENGTH_LONG).show();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    RegistroVisitasActivity.this);
            alertDialogBuilder
                    .setMessage("GPS está desactivado en su dispositivo. Activar?")
                    .setCancelable(false)
                    .setPositiveButton("Activar GPS",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int id) {
                                    Intent callGPSSettingIntent = new Intent(
                                            android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    RegistroVisitasActivity.this.startActivity(callGPSSettingIntent);
                                    finish();
                                }
                            });
            alertDialogBuilder.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            Log.d(TAG, "==== Le dio click en cancelar ");
                            finish();
                        }
                    });
            AlertDialog alert = alertDialogBuilder.create();
            alert.show();
        }
        Log.d(TAG, "Exist location "+ existLocation);
    }

    private void setupRadioButtonConVenta() {

        radioButtonConVenta.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                radioButtonSinVenta.setChecked(false);
            }
        });
    }

    private void setupRadioButtonSinVenta() {

        radioButtonSinVenta.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                radioButtonConVenta.setChecked(false);
            }
        });
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    public void enviarVisita (){

        if (clienteSeleccionado == null || clienteTextView.getText().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, RegistroVisitasActivity.this, false, dialogOnclicListener);
            guardarVisitaBtn.setEnabled(true);
            return;
        }

        boolean enLinea = AppUtils.isOnline(getApplicationContext());
        Log.d(TAG,"Conexión a internet: " + enLinea);

        //Crear Objeto Registro Visita
        String fechaVisita = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // formateo fecha Proxima visita
        String fechaProximaVisitaSpinner = fechaProxVisitaEditText.getText().toString();
        Log.d(TAG, "fecha del Spinner: "+ fechaProximaVisitaSpinner);

        SimpleDateFormat inputFecha = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFecha = new SimpleDateFormat("yyyy-MM-dd");
        String fechaProximaVisita = null;
        try {
            fechaProximaVisita = outputFecha.format(inputFecha.parse(fechaProximaVisitaSpinner));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String horaVisita = new SimpleDateFormat("HH:mm").format(new Date());

        //obtener usuario logueado
        Session sessionLogueado = db.selectUsuarioLogeado();
        Usuario usuario = db.selectUsuarioById(sessionLogueado.getUserId());

        double latitude = 0.0;
        double longitude = 0.0;
        if (myLocation != null){
            latitude = myLocation.getLatitude();
            longitude = myLocation.getLongitude();
        }
        String ent_sal="";
        if(radioButtonSinVenta.isChecked()){
            ent_sal="ENT";
        }else{
            ent_sal="SAL";
        }

        RegistroVisita r = new RegistroVisita();
        r.setTipo_visita(tipoVisitaSeleccionado.getCodigo());
        r.setRuc(clienteSeleccionado.getRuc());
        r.setCliente(clienteSeleccionado.getId());
        r.setLatitude(latitude);
        r.setLongitude(longitude);
        r.setObservation(observacionTextView.getText().toString());
        r.setStatus("A");
        r.setUsuario(sessionLogueado.getUserId().toString());
        r.setFechavisita(fechaVisita);
        r.setHoravisita(horaVisita);
        r.setFecha_prox_visita(fechaProximaVisita);
        r.setEnt_sal(ent_sal);
        r.setNombreUsuario(usuario.getName());

        if (!enLinea){
            Log.d(TAG, "Sin conexion, se guarda la visita");

            //guardar con estado PENDIENTE para su posterior envio
            r.setEstado_envio("PENDIENTE");
            db.insertRegistroVisita(r);

            Context context = getApplicationContext();
            CharSequence text = "No hay conexión. Se guarda y se volverá a intentar mas tarde.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();
            finish();
            return;
        }

        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            String url = Comm.URL + "/api/visit/save";
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject

            JSONObject jsonObject = new JSONObject();
            //jsonObject.accumulate("id", 3);
            jsonObject.accumulate("tipo_visita", r.getTipo_visita());
            jsonObject.accumulate("ruc", r.getRuc());
            jsonObject.accumulate("cliente", r.getCliente());
            jsonObject.accumulate("latitude", String.valueOf(r.getLatitude()));
            jsonObject.accumulate("longitude", String.valueOf(r.getLongitude()));
            jsonObject.accumulate("observation",  r.getObservation());
            jsonObject.accumulate("status", r.getStatus());
            jsonObject.accumulate("usuario", r.getUsuario());
            jsonObject.accumulate("fechavisita", r.getFechavisita());
            jsonObject.accumulate("horavisita", r.getHoravisita());
            jsonObject.accumulate("fecha_prox_visita", r.getFecha_prox_visita());
            jsonObject.accumulate("ent_sal", r.getEnt_sal());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Log.d(TAG, "enviando post: "+ httpPost.toString());
            Log.d(TAG, "mensaje post: "+ json);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();
            //httpclient2.setCookieStore(Globals.cookieStore);

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: "+code);

            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

            if (code == 200){
                if (result.contains("Portal Movil Tigo")){
                    //guardar con estado PENDIENTE para su posterior envio
                    r.setEstado_envio("PENDIENTE");
                    db.insertRegistroVisita(r);

                    Context context = getApplicationContext();
                    CharSequence text = "No hay conexión. Se guarda y se volverá a intentar mas tarde.";
                    Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
                    toast.show();
                    finish();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Visita enviada correctamente.", Toast.LENGTH_LONG).show();
                guardarVisitaBtn.setEnabled(true);
                //guardar con estado ENVIADO
                r.setEstado_envio("ENVIADO");
                db.insertRegistroVisita(r);
                finish();
            }

            // 9. receive response as inputStream

            Log.d(TAG, "resultado  post: "+ result);
        } catch (Exception e) {
            AppUtils.handleError("Error al enviar visita.", RegistroVisitasActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            guardarVisitaBtn.setEnabled(true);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, ConfiguracionActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String formatearMoneda (String valor){
        String formatted = "";
        String valorLimpio = limpiarMoneda(valor);
        if (!valorLimpio.isEmpty()){
            int  i = Integer.valueOf(valorLimpio);
            formatted = formateador.format(i).toString();
        }
        return formatted;
    }

    public String limpiarMoneda (String valor){
        String valorLimpio = valor.replaceAll("[.]","");
        valorLimpio = valorLimpio.replaceAll("[,]","");
        return  valorLimpio;
    }

    //On click de la Fecha
    @Override
    public void onClick(View view) {
        if (view == fechaProxVisitaEditText){
            fechaProxVisitaDialog.show();
        }
    }

    //Setear fecha al editText
    private void setDateTimeField() {
        fechaProxVisitaEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaProxVisitaDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fechaProxVisitaEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public static void enviarVisitasPendientes (Context context){
        new RegistroVisitasActivity().enviarVisitas(context);
    }

    public void enviarVisitas (Context context){
        /*
        if (Globals.cookieStore == null || Globals.cookieStore.getCookies().isEmpty()){
            Log.d(TAG, "==============Cookie NULL:  No se envian Visitas Pendientes");
            return;
        }
        */

        db = new AppDatabase(context);
        List<RegistroVisita> list = db.selectRegistroVisitaByEstado("PENDIENTE");
        Log.d(TAG, "============== Se encontraron " + list.size() +" Registros de Visita PENDIENTES ");

        if (list.size() > 0){
            CharSequence text = "Enviando "+ list.size() + " Visitas ";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        for (RegistroVisita r : list){

            InputStream inputStream = null;
            String result = "";
            try {
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                String url = Comm.URL + "/api/visit/save";
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                String fechaVisita = r.getFechavisita();
                String fechaProximaVisita = r.getFecha_prox_visita();
                String horaVisita = r.getHoravisita();

                JSONObject jsonObject = new JSONObject();
                //jsonObject.accumulate("id", 3);
                jsonObject.accumulate("tipo_visita", r.getTipo_visita());
                jsonObject.accumulate("ruc", r.getRuc());
                jsonObject.accumulate("cliente", r.getCliente());
                jsonObject.accumulate("latitude", String.valueOf(r.getLatitude()));
                jsonObject.accumulate("longitude", String.valueOf(r.getLongitude()));
                jsonObject.accumulate("observation",  r.getObservation());
                jsonObject.accumulate("status", "A");
                jsonObject.accumulate("usuario", r.getUsuario());
                jsonObject.accumulate("fechavisita", fechaVisita);
                jsonObject.accumulate("horavisita", horaVisita);
                jsonObject.accumulate("fecha_prox_visita", fechaProximaVisita);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                // ObjectMapper mapper = new ObjectMapper();
                // json = mapper.writeValueAsString(person);

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                Log.d(TAG, "enviando post: "+ httpPost.toString());
                Log.d(TAG, "mensaje post: "+ json);

                DefaultHttpClient httpclient2 = new DefaultHttpClient();
                //httpclient2.setCookieStore(Globals.cookieStore);

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient2.execute(httpPost);

                int code = httpResponse.getStatusLine().getStatusCode();
                //si llega 401 es error de login
                Log.d(TAG, "responde code envio visita pendiente: "+code);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                if(inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                } else {
                    result = "Did not work!";
                }

                if (code == 200){
                    if (result.contains("Portal Movil Tigo")){
                        Log.e(TAG, "Portal tigo; Sin saldo");
                        return;
                    }

                    r.setEstado_envio("ENVIADO");
                    db.updateRegistroVisita(r);
                }
                Log.d(TAG, "resultado  post: "+ result);
            } catch (Exception e) {
                e.printStackTrace();
                //AppUtils.handleError("Error al enviar visita.", RegistroVisitasActivity.this);
                Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            }
        }
    }

}
