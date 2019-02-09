package py.multipartesapp.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Cobranza;
import py.multipartesapp.beans.CobranzaDetalle;
import py.multipartesapp.beans.CobranzaDetalleItem;
import py.multipartesapp.beans.Factura;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.customAutoComplete.CobranzaActivityClienteTextChangedListener;
import py.multipartesapp.customAutoComplete.CustomAutoCompleteView;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;
import py.multipartesapp.utils.MyFormatter;

/**
 * Created by Adolfo on 05/04/2016.
 */

public class CobranzaActivity extends ActionBarActivity {

    public static final String TAG = CobranzaActivity.class.getSimpleName();

    private SimpleDateFormat dateFormatter;

    private Button guardarCobranzaBtn;
    private EditText observacionEditText;
    private EditText nroFacturaEditText;

    private Cobranza c;
    public static Factura facturaSeleccionada;
    private Cliente clienteSeleccionado;
    private ListView pedidosListView;
    private ImageAdapter adapterDetalles;
    private TextView totalCobrado;
    private TextView totalFormaPago;
    private TextView totalDeuda;
    private Button agregarItemBtn;


    public String[] itemsClientes = new String[] {"Buscar por nombre o ruc..."};
    public CustomAutoCompleteView clienteAutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> clienteAdapter;
    private List<Cliente> clientesFiltrados;

    public static List<Factura> facturasFiltrados = new ArrayList<>();

    private AppDatabase db = new AppDatabase(this);
    /*Impresion*/
    Boolean use_printer = false;
    private static SharedPreferences sharedPreferences;

    BluetoothService mService = null;
    private String address;
    BluetoothDevice con_dev = null;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    volatile boolean stopWorker;
    int readBufferPosition;
    Thread workerThread;
    byte[] readBuffer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza);

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Cobros");

        if (db.countPedido() == 0){
            Toast.makeText(getApplicationContext(), "No existen Pedidos a cobrar. Pruebe sincronizar", Toast.LENGTH_LONG).show();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        observacionEditText = (EditText) findViewById(R.id.cobranza_observacion);
        nroFacturaEditText = (EditText) findViewById(R.id.cobranza_nro_factura);
        totalCobrado = (TextView) findViewById(R.id.cobranza_total_cobrado);
        totalDeuda= (TextView) findViewById(R.id.cobranza_total_deuda);
        totalFormaPago = (TextView) findViewById(R.id.total_forma_pago);
        agregarItemBtn = (Button) findViewById(R.id.cobranza_detalle_add_cobro);

        guardarCobranzaBtn = (Button) findViewById(R.id.guardar_cobranza);

        pedidosListView = (ListView) findViewById(R.id.cobranza_detalle_list);

        clienteAutoComplete = (CustomAutoCompleteView) findViewById(R.id.cobranza_cliente_autocomplete);

        adapterDetalles = new ImageAdapter (this);
        pedidosListView.setAdapter(adapterDetalles);

        // add the listener so it will tries to suggest while the user types
        clienteAutoComplete.addTextChangedListener(new CobranzaActivityClienteTextChangedListener(this));

        // set our adapter
        clienteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsClientes);
        clienteAutoComplete.setAdapter(clienteAdapter);

        //si proviene de Lista de Rutas, cargar el cliente en cuestion
        if (Globals.getClienteSeleccionadoRuta() != null){
            clienteSeleccionado = Globals.getClienteSeleccionadoRuta();
            clienteAutoComplete.setText(clienteSeleccionado.toString());
            facturasFiltrados = db.selectFacturaByClientId(clienteSeleccionado.getId());
            adapterDetalles.notifyDataSetChanged();
            Globals.setClienteSeleccionadoRuta(null);
        }

        //al seleccionar un cliente de la lista filtrada
        clienteAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                clienteSeleccionado = clientesFiltrados.get(position);
                facturasFiltrados = db.selectFacturaByClientId(clienteSeleccionado.getId());
                adapterDetalles.notifyDataSetChanged();
                actualizarTotalDeuda();
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
                        facturaSeleccionada = null;
                        facturasFiltrados = new ArrayList<Factura>();
                        adapterDetalles.notifyDataSetChanged();
                        actualizarTotal();
                        actualizarTotalDeuda();

                        return true;
                    }
                }
                return false;
            }
        });
        guardarCobranzaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarCobranzaBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);
                enviarCobranza();
            }
        });

        //para la impresion de recibos
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        mService = new BluetoothService(this, mHandler);
        address = sharedPreferences.getString("mac_address_bt", null);
        use_printer = sharedPreferences.getBoolean("configImpresora", false);
        if(use_printer){
            if(address!=null) {
                con_dev = mService.getDevByMac(address);
            }else {
                configurarImpresora(CobranzaActivity.this);
            }
        }

        agregarItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CobranzaActivity.this, CobranzaDetalleItemActivity.class);
                startActivity(intent);
            }
        });
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
        }
    };

    public void enviarCobranza (){

        if (clienteSeleccionado == null || clienteAutoComplete.getText().toString().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, CobranzaActivity.this, false, dialogOnclicListener);
            guardarCobranzaBtn.setEnabled(true);
            return;
        }

        if (nroFacturaEditText.getText().toString().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Ingrese número de recibo", buttons, CobranzaActivity.this, false, dialogOnclicListener);
            guardarCobranzaBtn.setEnabled(true);
            return;
        }
        if (facturasFiltrados.size() == 0){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "El cliente no tiene Facturas a cobrar.", buttons, CobranzaActivity.this, false, dialogOnclicListener);
            guardarCobranzaBtn.setEnabled(true);
            return;
        }

        boolean cargoCobro = false;
        for (Factura p : facturasFiltrados){
            if (p.getMontoCobrado()!= null && !p.getMontoCobrado().equals("0")){
                cargoCobro = true;
                break;
            }
        }
        if (cargoCobro == false){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Debe cargar un monto cobrado de alguna factura", buttons, CobranzaActivity.this, false, dialogOnclicListener);
            guardarCobranzaBtn.setEnabled(true);
            return;
        }

        //obtener usuario logueado
        Session sessionLogueado = db.selectUsuarioLogeado();

        //Crear Objeto Cobranza
        c = new Cobranza();
        Integer id =  (int) (new Date().getTime()/1000);
        c.setId(id);

        // cargar los detalles del Cobro
        List<CobranzaDetalle> detalles = new ArrayList<CobranzaDetalle>();
        for (Factura p: facturasFiltrados){
            //usar solo las facturas donde cargo dinero
            if (p.getMontoCobrado() != null && !p.getMontoCobrado().equals("0") ){
                CobranzaDetalle cd = new CobranzaDetalle();
                cd.setAmount(p.getGrandtotal());
                cd.setCashed(Integer.valueOf(p.getMontoCobrado()));
                //cd.setCharge_id(c.getId());
                cd.setInvoice(p.getId().toString());

                cd.setItems(p.getItems());
                cd.setNroFactura(p.getNroFacturaImprimir());
                //hasta aca--------------
//                cd.setExpired_date(p.getExpired_date());
//                cd.setBank(p.getBank());
//                cd.setPayment_type(p.getPayment_type());
//                cd.setCheck_number(p.getCheck_number());
//                cd.setCheck_name(p.getCheck_name());

                detalles.add(cd);
            }
        }
        c.setDetalles(detalles);


        c.setUser_id(sessionLogueado.getUserId());
        c.setClient_id(clienteSeleccionado.getId());
        c.setAmount(getTotalCobrado());
        c.setInvoice_number(nroFacturaEditText.getText().toString());

        String observacion = observacionEditText.getText().toString();
        observacion = observacion.replace(" ","%20");
        c.setObservation(observacion);

        Usuario vendedor = db.selectUsuarioById(sessionLogueado.getUserId());
        c.setNombre_vendedor(vendedor.getName());
        c.setNombre_cliente(clienteSeleccionado.getNombre());
        String print = crearTicket(c);
        boolean enLinea = AppUtils.isOnline(getApplicationContext());
        Log.d(TAG,"Conexión a internet: " + enLinea);
        imprimir(print);
        if (!enLinea){
            Log.d(TAG, "Sin conexion, se guarda el cobro");
            /*
            //guardar con estado PENDIENTE para su posterior envio
            c.setEstado_envio("PENDIENTE");
            db.insertCobranza(c);
            for (CobranzaDetalle cd : c.getDetalles()){
                db.insertCobranzaDetalle(cd);
            }*/

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
            String url = Comm.URL + "/api/charge/save";
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();

            //jsonObject.accumulate("order_id", "1000010");
            SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");


            jsonObject.accumulate("date_received", dateFormatter.format(new Date()));
            jsonObject.accumulate("time_received", timeFormatter.format(new Date()));
            jsonObject.accumulate("client_id", c.getClient_id());
            jsonObject.accumulate("user_id", c.getUser_id());
            jsonObject.accumulate("amount", c.getAmount());
            jsonObject.accumulate("receipt_number", c.getInvoice_number());
            jsonObject.accumulate("observation", c.getObservation());
            jsonObject.accumulate("status", "A");


            //Agregamos los detalles
            JSONArray detallesJsonArray = new JSONArray();
            for (CobranzaDetalle detalle: c.getDetalles()){
                JSONObject detalleJson = new JSONObject();
                detalleJson.put("invoice_id", detalle.getInvoice());
                detalleJson.put("amount", detalle.getAmount());
                detalleJson.put("cashed", detalle.getCashed());


                JSONObject formaPagoJson = new JSONObject();
                JSONArray cobrosJsonArray = new JSONArray();
                for (CobranzaDetalleItem cobro : detalle.getItems()){
                    JSONObject cobroJson = new JSONObject();
                    cobroJson.put("payment_type", cobro.getPayment_type());
                    cobroJson.put("amount", cobro.getAmount());
                    cobroJson.put("bank", cobro.getBank());
                    cobroJson.put("check_number", cobro.getCheck_number());
                    cobroJson.put("expired_date", cobro.getExpired_date());
                    cobroJson.put("check_name", cobro.getCheck_name());
                    cobrosJsonArray.put(cobroJson);

                }


                formaPagoJson.accumulate("paymentline", cobrosJsonArray);


                //detalleJson.put("charge_id", detalle.getCharge_id());

//                detalleJson.put("expired_date", detalle.getExpired_date());
//                detalleJson.put("bank", detalle.getBank());
//                detalleJson.put("payment_type", detalle.getPayment_type());
//                detalleJson.put("check_number", detalle.getCheck_number());
//                detalleJson.put("check_name", detalle.getCheck_name());

                detallesJsonArray.put(detalleJson);
            }


            jsonObject.accumulate("chargesline", detallesJsonArray);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json, HTTP.ASCII);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Log.d(TAG, "enviando post: "+ httpPost.toString());
            Log.d(TAG, "mensaje post: "+ json);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: "+code);

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
                    Log.d(TAG, "Sin conexion, se guarda la entrega");

                    //guardar con estado PENDIENTE para su posterior envio
                    //c.setEstado_envio("PENDIENTE");
//                    db.insertCobranza(c);
//                    for (CobranzaDetalle cd : c.getDetalles()){
//                        db.insertCobranzaDetalle(cd);
//                    }

                    Context context = getApplicationContext();
                    CharSequence text = "No hay conexión. Se guarda y se volverá a intentar mas tarde.";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
                    toast.show();
                    finish();
                    return;

                }
                Toast.makeText(getApplicationContext(), "Cobro enviado correctamente.", Toast.LENGTH_LONG).show();
                guardarCobranzaBtn.setEnabled(true);
                //guardar con estado ENVIADO
                c.setEstado_envio("ENVIADO");
                db.insertCobranza(c);
                for (CobranzaDetalle cd : c.getDetalles()){
                    db.insertCobranzaDetalle(cd);
                }
                finish();
            } else {
                Toast.makeText(getApplicationContext(), "Error al enviar cobro .", Toast.LENGTH_LONG).show();
                guardarCobranzaBtn.setEnabled(true);
            }


            Log.d(TAG, "resultado  post: "+ result);
        } catch (Exception e) {
            AppUtils.handleError("Error al enviar pedido.", CobranzaActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            guardarCobranzaBtn.setEnabled(true);
        }
    }

    private void imprimir(String datos) {
        if (use_printer) {
            try {
                findBT();
                openBT(datos);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void actualizarTotal (){
        Integer totalCobradoInteger = getTotalCobrado();
        totalCobrado.setText("Gs. "+MyFormatter.formatMoney(totalCobradoInteger.toString()));
    }

    private Integer getTotalCobrado(){
        Integer total = 0;
        for (Factura p: facturasFiltrados){
            if (p.getMontoCobrado() != null && !p.getMontoCobrado().equals("0")){
                total = total + Integer.valueOf(p.getMontoCobrado());
            }
        }
        return total;
    }

    private void actualizarTotalDeuda() {
        Integer totalDeudaInteger = getTotalDeuda();
        totalDeuda.setText("Gs. "+MyFormatter.formatMoney(totalDeudaInteger.toString()));
    }

    private void actualizarImporteFormaPago(){
        double totAmountFp = 0;
        if(Globals.getItemCobroList() != null) {
            for (CobranzaDetalleItem c : Globals.getItemCobroList()) {
                totAmountFp += c.getAmount();
            }
        }
        totalFormaPago.setText(String.valueOf(totAmountFp));

    }
    private Integer getTotalDeuda(){
        Integer total = 0;
        for (Factura p: facturasFiltrados){
            if (p.getGrandtotal() != null && !p.getGrandtotal().equals("0")){
                total = total + Integer.valueOf(p.getGrandtotal());
            }
        }
        return total;
    }

    @Override
    protected void onResume() {
        adapterDetalles.notifyDataSetChanged();
        actualizarTotal();
        actualizarTotalDeuda();
        actualizarImporteFormaPago();
        super.onResume();
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


    public static void enviarCobrosPendientes (Context context){
        new CobranzaActivity().enviarCobranzas(context);
    }

    public void enviarCobranzas (Context context){

        db = new AppDatabase(context);
        List<Cobranza> list = db.selectCobranzaByEstado("PENDIENTE");
        Log.d(TAG, "============== Se encontraron " + list.size() +" Cobros PENDIENTES ");

        if (list.size() > 0){
            CharSequence text = "Enviando "+ list.size() + " Cobros ";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        for (Cobranza cobranza : list){

            InputStream inputStream = null;
            String result = "";
            try {
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                String url = Comm.URL + "/api/charge/save";
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();

                //jsonObject.accumulate("order_id", "1000010");
                SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
                SimpleDateFormat timeFormatter = new SimpleDateFormat("HH:mm");

                jsonObject.accumulate("date_received", dateFormatter.format(new Date()));
                jsonObject.accumulate("time_received", timeFormatter.format(new Date()));
                jsonObject.accumulate("client_id", c.getClient_id());
                jsonObject.accumulate("user_id", c.getUser_id());
                jsonObject.accumulate("amount", c.getAmount());
                jsonObject.accumulate("receipt_number", c.getInvoice_number());
                jsonObject.accumulate("observation", c.getObservation());
                jsonObject.accumulate("status", "A");


                //Agregamos los detalles
                JSONArray detallesJsonArray = new JSONArray();
                List<CobranzaDetalle> detallesCobranza = db.selectCobranzaDetalleByCobro(cobranza.getId());

                for (CobranzaDetalle detalle: detallesCobranza){
                    JSONObject detalleJson = new JSONObject();
                    detalleJson.put("invoice_id", detalle.getInvoice());
                    detalleJson.put("amount", detalle.getAmount());
                    detalleJson.put("cashed", detalle.getCashed());
                    //detalleJson.put("charge_id", detalle.getCharge_id());
                    detallesJsonArray.put(detalleJson);
                }
                jsonObject.accumulate("chargesline", detallesJsonArray);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                //Log.d(TAG, "enviando post: "+ httpPost.toString());
                //Log.d(TAG, "mensaje post: "+ json);

                DefaultHttpClient httpclient2 = new DefaultHttpClient();

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient2.execute(httpPost);

                int code = httpResponse.getStatusLine().getStatusCode();
                //si llega 401 es error de login
                Log.d(TAG, "responde code: "+code);
                if (code == 200){
                    //guardar con estado ENVIADO
                    cobranza.setEstado_envio("ENVIADO");
                    db.updateCobranza(cobranza);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error al enviar cobro .", Toast.LENGTH_LONG).show();
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
                AppUtils.handleError("Error al enviar cobro.", CobranzaActivity.this);
                Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            }

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

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return facturasFiltrados.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            Factura item = facturasFiltrados.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_cobranza_pedido, viewGroup, false);

                v.setTag(R.id.txt1_item_cobranza_nro_factura, v.findViewById(R.id.txt1_item_cobranza_nro_factura));
                v.setTag(R.id.txt2_item_cobranza_fecha, v.findViewById(R.id.txt2_item_cobranza_fecha));
                v.setTag(R.id.total_item_cobranza, v.findViewById(R.id.total_item_cobranza));
                v.setTag(R.id.monto_cobrado_item_cobranza, v.findViewById(R.id.monto_cobrado_item_cobranza));
                //v.setTag(R.id.editar_item_cobranza_btn, v.findViewById(R.id.editar_item_cobranza_btn));
                v.setTag(R.id.check_item_cobranza_btn, v.findViewById(R.id.check_item_cobranza_btn));

            }
            final CheckBox cbItem = (CheckBox) v.getTag(R.id.check_item_cobranza_btn);
            TextView nroFactura = (TextView) v.findViewById(R.id.txt1_item_cobranza_nro_factura);
            nroFactura.setText(item.getNroFacturaImprimir());

            TextView fechaPedido = (TextView) v.findViewById(R.id.txt2_item_cobranza_fecha);
            String fecha = item.getDateinvoiced().substring(0, 10);
            fechaPedido.setText(fecha);

            TextView totalPedido = (TextView) v.findViewById(R.id.total_item_cobranza);
            totalPedido.setText(item.getGrandtotal().toString());

            final EditText montoCobrado = (EditText) v.findViewById(R.id.monto_cobrado_item_cobranza);
            /*if (item.getMontoCobrado() != null ){
                montoCobrado.setText(item.getMontoCobrado().toString());
            }else {
                montoCobrado.setText("0");
            }*/
            montoCobrado.setText(item.getGrandtotal().toString());

            montoCobrado.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v1, boolean hasFocus) {
                    if(!hasFocus){
                        /*Actualizar monto en la lista*/
                        if(cbItem.isChecked()) {
                            View parentRow = (View) v1.getParent();
                            ViewParent viewParent = parentRow.getParent();
                            ListView listView = (ListView) viewParent.getParent();
                            int position = listView.getPositionForView((View) viewParent);
                            String montoCob = montoCobrado.getText().toString();
                            if(!montoCob.isEmpty()) {
                                facturasFiltrados.get(position).setMontoCobrado(montoCob);
                                //todo: calcular total
                                actualizarTotal();
                            }
                        }
                    }
                }
            });

            cbItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    View parentRow = (View) v1.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    int position = listView.getPositionForView((View) viewParent);
                    String montoCob = montoCobrado.getText().toString();
                    if(cbItem.isChecked()){
                        if(!montoCob.isEmpty()) {
                            facturasFiltrados.get(position).setMontoCobrado(montoCob);
                            //todo: calcular total
                            actualizarTotal();
                            facturaSeleccionada = facturasFiltrados.get(position);
                        }
                    }else {
                        facturasFiltrados.get(position).setMontoCobrado("0");
                        //todo: calcular total
                        actualizarTotal();
                    }





                }
            });
            //Configuramos el boton para editar la Cobranza
            /*ImageButton iconItem = (ImageButton) v.getTag(R.id.editar_item_cobranza_btn);
            iconItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {

                    View parentRow = (View) v1.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    int position = listView.getPositionForView((View) viewParent);
                    facturaSeleccionada = facturasFiltrados.get(position);
                    Intent intent = new Intent(CobranzaActivity.this, CobranzaDetalleActivity.class);
                    startActivity(intent);
                }
            });*/

            return v;
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

    private String crearTicket(Cobranza cobranza){
        SimpleDateFormat sdfdd_MM_yyyyHhMm = new SimpleDateFormat("dd/MM/yyyy");
        String hoy = sdfdd_MM_yyyyHhMm.format(new Date());
        int printWidth = 48;
        String print = centerText("Multipartes S.R.L", printWidth);
        print += centerText("Recibo de cobro", printWidth);
        print += fillAfterColumn("Fecha: " + hoy, 20) + fillAfterColumn("Nro. Recibo: " + cobranza.getInvoice_number(),28);
        print += "Cliente: " + cobranza.getNombre_cliente() + "\n";
        print += "Cobrador: " + cobranza.getNombre_vendedor() + "\n";
        print += fillAfterColumn("Nro Factura", 15) + fillColumn("Monto", 33);
        print += makeLine(printWidth);
        for(CobranzaDetalle cd: cobranza.getDetalles()) {
            print += fillAfterColumn(cd.getNroFactura(), 15) +
                    fillColumn(MyFormatter.formatearMoneda(String.valueOf(cd.getCashed())), 33) + "\n";
        }
        print += makeLine(printWidth);
        print += fillAfterColumn("Total:",15) + fillColumn(MyFormatter.formatearMoneda(String.valueOf(cobranza.getAmount())),33);
        print += centerText("*** Gracias por su pago ***", printWidth);
        print += "\n\n\n";
        Log.d(TAG, "\n" + print);
        return print;
    }

    public static String centerText(String text, int printWidth){
        if(text == null){
            text = "";
        }
        int textWidth = text.length();
        String ret = "";
        if(textWidth < printWidth){
            int width = (printWidth - textWidth)/2;
            ret = fillColumn(text, width + textWidth) + "\n";
        }else{
            ret = text + "\n";
        }
        return ret;
    }

    public static String fillColumn(String col, int n) {
        while (col.length() < n) {
            col = " " + col;
        }
        return col;
    }
    public static String fillAfterColumn(String col, int n) {
        while (col.length() < n) {
            col += " ";
        }
        return col;
    }
    /*Metodo para hacer una linea*/
    public static String makeLine(int printWidth){
        String line = "";
        for(int i = 0; i < printWidth; i++){
            line += "-";
        }
        line += "\n";
        return line;
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "Conexion exitosa",
                                    Toast.LENGTH_SHORT).show();
                            break;
                        case BluetoothService.STATE_CONNECTING:
                            Log.d(TAG, "Conectando...");
                            break;
                        case BluetoothService.STATE_LISTEN:
                        case BluetoothService.STATE_NONE:
                            Log.d(TAG, "Buscando...");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:
                    Toast.makeText(getApplicationContext(), "Conexion con el dispositivo perdida",
                            Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:
                    Toast.makeText(getApplicationContext(), "Conexion con el dispositivo no disponible",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void configurarImpresora(Context context) {
        DialogInterface.OnClickListener dialogOkClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        intent = new Intent(CobranzaActivity.this, DOPrintMainActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        Toast.makeText(getApplicationContext(),"No se ha configurado la impresora.\n"+
                                        "Si desea utilizarla, vaya al apartado de configuraciones y agregue una.",
                                Toast.LENGTH_LONG).show();
                        sharedPreferences.edit().putBoolean("impresora", false).apply();
                        sharedPreferences.edit().putBoolean("configImpresora", false).apply();
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Debe configurar una impresora antes de seguir")
                .setPositiveButton("Ok", dialogOkClickListener)
                .show();
    }

    void findBT() {

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
//            WalrusUtils.showToast("No bluetooth adapter available", getApplicationContext());
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 0);
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
                .getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (device.getAddress().equals(address)) {
                    mmDevice = device;
                    break;
                }
            }
        }
        Log.d(TAG, "1. Bluetooth Device Found");
    }

    void openBT(String dataPrint) throws IOException {
        try {
            // Standard SerialPortService ID
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            Log.d(TAG, "2. Let's open a Bluetooth conection");
            mmSocket.connect();
            Log.d(TAG, "3. Bluetooth conection established");
            mmOutputStream = mmSocket.getOutputStream();
            mmInputStream = mmSocket.getInputStream();
            beginListenForData();
            sendData(dataPrint);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
//            WalrusUtils.showToast("No se pudo conectar a la impresora.", getApplicationContext());
            Log.d(TAG, "3. Error when Bluetooth conection establishing");
            e.printStackTrace();
        }
    }

    // After opening a connection to bluetooth printer device,
// we have to listen and check if a data were sent to be printed.
    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // This is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {
                    while (!Thread.currentThread().isInterrupted()
                            && !stopWorker) {

                        try {

                            int bytesAvailable = mmInputStream.available();
                            if (bytesAvailable > 0) {
                                byte[] packetBytes = new byte[bytesAvailable];
                                mmInputStream.read(packetBytes);
                                for (int i = 0; i < bytesAvailable; i++) {
                                    byte b = packetBytes[i];
                                    if (b == delimiter) {
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length);
                                        final String data = new String(
                                                encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        handler.post(new Runnable() {
                                            public void run() {
//												myLabel.setText(data);
//                                                WalrusUtils.showToast(data, getApplicationContext());
                                            }
                                        });
                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (IOException ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * This will send data to be printed by the bluetooth printer
     */
    void sendData(String data) throws IOException {
        try {
            /*Calculamos el tiempo que va a esperar para cerrar la conexion bluetooth
            * Por cada 1000 caracteres le damos 2 segundos.
            * Redondeamos para arriba si la longitud de la cadena pasa el multiplo de 1000
            * Ej: 1010 caracteres se redondea a 2000
            *     5001 caracteres se redondea a 6000*/
            double a = data.length();
            double b = 1000;
            double div = a/b;
            double d = roundUp(div, 0);

            long sleepTime = 2000 * new Double(d).longValue();
            Log.d(TAG, "4. Sending data");
            mmOutputStream.write(data.getBytes());
            Log.d(TAG, "5. Data sent");
            Log.d(TAG, "Sleep for " + sleepTime + " ms.");
            Thread.sleep(sleepTime);
            closeBT();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Close the connection to bluetooth printer.
    void closeBT() throws IOException {
        try {
            Log.d(TAG, "6. Closing bt.. ");
            stopWorker = true;
            mmOutputStream.close();
            mmInputStream.close();
            mmSocket.close();
            Log.d(TAG, "6. Bluetooth Closed");
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static double roundUp(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.UP);
        return bd.doubleValue();
    }
}
