package py.multipartesapp.activities;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Entrega;
import py.multipartesapp.beans.RutaLocation;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.customAutoComplete.CustomAutoCompleteView;
import py.multipartesapp.customAutoComplete.RutaLocationActivityClienteTextChangedListener;
import py.multipartesapp.customAutoComplete.RutaLocationActivityUsuarioTextChangedListener;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;


public class RutaLocationNewActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = RutaLocationNewActivity.class.getSimpleName();

    private SimpleDateFormat dateFormatter;
    private EditText fechaVisitaEditText;
    private DatePickerDialog fechaVisitaDialog;
    private ImageButton calendarBtn;
    private Spinner tipoRutaSpinner;

    private Button guardarRutaBtn;
    private EditText observacionEditText;
    private EditText prioridadEditText;


    private Cliente clienteSeleccionado;
    private Usuario vendedorSeleccionado;
    private String tipoRutaSeleccionado;


    public String[] itemsClientes = new String[] {"Buscar por nombre o ruc..."};
    public String[] itemsVendedores = new String[] {"Buscar por nombre o ci..."};
    public CustomAutoCompleteView clienteAutoComplete;
    public CustomAutoCompleteView vendedoresAutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> clienteAdapter;
    private List<Cliente> clientesFiltrados;

    public ArrayAdapter<String> vendedoresAdapter;
    private List<Usuario> vendedoresFiltrados;

    private AppDatabase db = new AppDatabase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ruta_location_new);

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Crear Rutas");

        if (db.countCliente() == 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        observacionEditText = (EditText) findViewById(R.id.rutalocation_observacion);
        prioridadEditText = (EditText) findViewById(R.id.rutalocation_prioridad);
        tipoRutaSpinner = (Spinner) findViewById(R.id.rutalocation_tipo_spinner);
        guardarRutaBtn = (Button) findViewById(R.id.guardar_ruta);


        fechaVisitaEditText = (EditText) findViewById(R.id.rutalocation_fecha_visita);
        fechaVisitaEditText.setInputType(InputType.TYPE_NULL);
        calendarBtn = (ImageButton) findViewById(R.id.rutalocation_calendar_btn);

        clienteAutoComplete = (CustomAutoCompleteView) findViewById(R.id.rutalocation_cliente_autocomplete);
        vendedoresAutoComplete = (CustomAutoCompleteView) findViewById(R.id.rutalocation_vendedor_autocomplete);

        // add the listener so it will tries to suggest while the user types
        clienteAutoComplete.addTextChangedListener(new RutaLocationActivityClienteTextChangedListener(this));
        vendedoresAutoComplete.addTextChangedListener(new RutaLocationActivityUsuarioTextChangedListener(this));

        // set our adapter
        clienteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsClientes);
        vendedoresAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsVendedores);

        clienteAutoComplete.setAdapter(clienteAdapter);
        vendedoresAutoComplete.setAdapter(vendedoresAdapter);

        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               fechaVisitaDialog.show();
            }
        });

        setDateTimeField();

        //al seleccionar un cliente de la lista filtrada
        clienteAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                clienteSeleccionado = clientesFiltrados.get(position);
            }
        });

        //al seleccionar un cliente de la lista filtrada
        vendedoresAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                vendedorSeleccionado = vendedoresFiltrados.get(position);
            }
        });

        //boton limpiar texto cliente
        clienteAutoComplete.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = clienteAutoComplete.getRight()
                            - clienteAutoComplete.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        clienteAutoComplete.setText("");
                        clienteSeleccionado = null;
                        return true;
                    }
                }
                return false;
            }
        });

        //boton limpiar texto cliente
        vendedoresAutoComplete.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = vendedoresAutoComplete.getRight()
                            - vendedoresAutoComplete.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        vendedoresAutoComplete.setText("");
                        vendedorSeleccionado= null;
                        return true;
                    }
                }
                return false;
            }
        });

        String[] listTipoRuta = {"ENTREGA","PEDIDO","COBRANZA","VISITA"};

        ArrayAdapter<String> tipoRutaArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, listTipoRuta);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        tipoRutaSpinner.setAdapter(tipoRutaArrayAdapter);


        tipoRutaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                tipoRutaSeleccionado = (String) parent.getAdapter().getItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        guardarRutaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarRutaBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);
                enviarRuta();
            }
        });

    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
        }
    };

    public void enviarRuta (){

        if (clienteSeleccionado == null || clienteAutoComplete.getText().toString().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, RutaLocationNewActivity.this, false, dialogOnclicListener);
            guardarRutaBtn.setEnabled(true);
            return;
        }

        if (vendedorSeleccionado == null || vendedoresAutoComplete.getText().toString().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un vendedor", buttons, RutaLocationNewActivity.this, false, dialogOnclicListener);
            guardarRutaBtn.setEnabled(true);
            return;
        }

        if ( fechaVisitaEditText.getText().toString().isEmpty()){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Ingrese una fecha", buttons, RutaLocationNewActivity.this, false, dialogOnclicListener);
            guardarRutaBtn.setEnabled(true);
            return;
        }

        if ( prioridadEditText.getText().toString().isEmpty()){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Ingrese un número de prioridad", buttons, RutaLocationNewActivity.this, false, dialogOnclicListener);
            guardarRutaBtn.setEnabled(true);
            return;
        }

        if ( tipoRutaSeleccionado == null || tipoRutaSeleccionado.isEmpty()){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un tipo de Ruta.", buttons, RutaLocationNewActivity.this, false, dialogOnclicListener);
            guardarRutaBtn.setEnabled(true);
            return;
        }


        boolean enLinea = AppUtils.isOnline(getApplicationContext());
        Log.d(TAG,"Conexión a internet: " + enLinea);

        //Crear Objeto Entrega
        String fechaRealEntrega = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // formateo fecha Entrega
        String fechaEntregaSpinner = fechaVisitaEditText.getText().toString();
        Log.d(TAG, "fecha del Spinner: "+ fechaEntregaSpinner);

        SimpleDateFormat inputFecha = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        String fechaRutaVisita = null;
        try {
            fechaRutaVisita = outputFecha.format(inputFecha.parse(fechaEntregaSpinner));
        } catch (ParseException e) {
            e.printStackTrace();
        }


        RutaLocation ruta = new RutaLocation();

        SimpleDateFormat fecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        //ruta.setDate(fecha.format(new Date()));
        ruta.setDate(fechaRutaVisita);
        ruta.setClient_id(clienteSeleccionado.getId());
        ruta.setUser_id(vendedorSeleccionado.getId());
        ruta.setPriority(Integer.valueOf(prioridadEditText.getText().toString()));
        ruta.setStatus("V");
        ruta.setType(tipoRutaSeleccionado);
        ruta.setObservation(observacionEditText.getText().toString());

        if (!enLinea){
            Log.d(TAG, "Sin conexion, se guarda la entrega");

            //guardar con estado PENDIENTE para su posterior envio
            //e.setEstado_envio("PENDIENTE");
            //db.insertEntrega(e);

            /*
            List<Entrega> entregasInsertadas = db.selectEntregaByEstado("PENDIENTE");
            for (Entrega e: entregasInsertadas){
                Log.d(TAG, e.toString());
            } */

            Context context = getApplicationContext();
            CharSequence text = "No hay conexión.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();
            //finish();
            return;
        }

        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            String url = Comm.URL + CommReq.CommReqPostSaveRoute;
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();

            jsonObject.accumulate("date", ruta.getDate());
            jsonObject.accumulate("user_id", ruta.getUser_id());
            jsonObject.accumulate("client_id", ruta.getClient_id());
            jsonObject.accumulate("zone", 1);
            jsonObject.accumulate("priority", ruta.getPriority());
            jsonObject.accumulate("status", ruta.getStatus());
            jsonObject.accumulate("observation", ruta.getObservation());
            jsonObject.accumulate("type", ruta.getType());

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json, "UTF-8");

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            //httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Log.d(TAG, "enviando post: "+ httpPost.toString());
            Log.d(TAG, "mensaje post: "+ json);

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: "+code);
            if (code == 200){
                Toast.makeText(getApplicationContext(), "Ruta enviada correctamente.", Toast.LENGTH_LONG).show();
                guardarRutaBtn.setEnabled(true);
                //guardar con estado ENVIADO
                /*
                c.setEstado_envio("ENVIADO");
                db.insertCobranza(c);
                for (CobranzaDetalle cd : c.getDetalles()){
                    db.insertCobranzaDetalle(cd);
                } */
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Error al enviar ruta .", Toast.LENGTH_LONG).show();
                guardarRutaBtn.setEnabled(true);
            }

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }
            Log.d(TAG, "resultado  post: "+ result);
        } catch (Exception e) {
            AppUtils.handleError("Error al enviar ruta.", RutaLocationNewActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            guardarRutaBtn.setEnabled(true);
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

    //On click de la Fecha
    @Override
    public void onClick(View view) {
        if (view == fechaVisitaEditText){
            fechaVisitaDialog.show();
        }
    }

    //Setear fecha al editText
    private void setDateTimeField() {
        fechaVisitaEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaVisitaDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fechaVisitaEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public static void enviarEntregasPendientes (Context context){
        new RutaLocationNewActivity().enviarEntregas(context);
    }

    public void enviarEntregas (Context context){

        db = new AppDatabase(context);
        List<Entrega> list = db.selectEntregaByEstado("PENDIENTE");
        Log.d(TAG, "============== Se encontraron " + list.size() +" Entregas PENDIENTES ");

        if (list.size() > 0){
            CharSequence text = "Enviando "+ list.size() + " Entregas ";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();
        }
        for (Entrega entrega : list){
            //e = entrega;
            CommDelegateAndroid delegateSendDelivery = new CommDelegateAndroid(){
                @Override
                public void onError(){
                    Log.e(TAG, this.exception.getMessage());
                    //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
                }
                @Override
                public void onSuccess(){
                    //guardar con estado ENVIADO
                    //e.setEstado_envio("ENVIADO");
                    //db.updateEntrega(e);
                    //finish();
                }
            };
            new Comm().requestGet(CommReq.CommReqSendDelivery, new String[][]{
                    {"user_id", entrega.getUser_id()},
                    {"client_id", entrega.getClient_id()},
                    {"order_id",entrega.getOrder_id()},
                    {"date_delivered", entrega.getDate_delivered()},
                    {"time_delivered", entrega.getTime_delivered()},
                    {"observation", entrega.getObservation()}
            }, delegateSendDelivery);
        }
    }

    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getClientesFiltrados(String searchTerm){
        // add itemsClientes on the array dynamically
        clientesFiltrados = db.selectClienteByNombreCedula(searchTerm);
        int rowCount = clientesFiltrados.size();
        String[] item = new String[rowCount];
        int i = 0;
        for (Cliente c : clientesFiltrados) {
            item[i] = c.getNombre() + " - " +c.getRuc();
            i++;
        }
        return item;
    }

    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getVendedoresFiltrados(String searchTerm){
        // add itemsClientes on the array dynamically
        vendedoresFiltrados = db.selectUsuarioByNombre(searchTerm);
        int rowCount = vendedoresFiltrados.size();
        String[] item = new String[rowCount];
        int i = 0;
        for (Usuario c : vendedoresFiltrados) {
            item[i] = c.getName() + " " + c.getLastname();
            i++;
        }
        return item;
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

}
