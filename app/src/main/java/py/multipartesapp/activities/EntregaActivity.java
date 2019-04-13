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
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Entrega;
import py.multipartesapp.beans.Pedido;
import py.multipartesapp.beans.Session;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.customAutoComplete.EntregaActivityClienteTextChangedListener;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.customAutoComplete.CustomAutoCompleteView;
import py.multipartesapp.utils.Globals;


public class EntregaActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = EntregaActivity.class.getSimpleName();

    private SimpleDateFormat dateFormatter;
    private EditText fechaEntregaEditText;
    private DatePickerDialog fechaEntregaDialog;
    private ImageButton calendarBtn;

    private LinearLayout pedidoLinearLayout;
    private Spinner pedidoSpinner;
    private Button guardarEntregaBtn;
    private EditText observacionTextView;

    private Entrega e;
    private Cliente clienteSeleccionado;
    private Pedido pedidoSeleccionado;
    ArrayAdapter<Pedido> pedidosArrayAdapter;

    public String[] itemsClientes = new String[] {"Buscar por nombre o ruc..."};
    public CustomAutoCompleteView clienteAutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> clienteAdapter;
    private List<Cliente> clientesFiltrados;

    private List<Pedido> pedidosFiltrados = new ArrayList<Pedido>();

    private AppDatabase db = new AppDatabase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrega);

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Entregas");

        if (db.countCliente() == 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        pedidoSpinner = (Spinner) findViewById(R.id.entrega_pedido);
        observacionTextView = (EditText) findViewById(R.id.entrega_observacion);
        guardarEntregaBtn = (Button) findViewById(R.id.guardar_entrega);
        pedidoLinearLayout = (LinearLayout) findViewById(R.id.entrega_linearlayout_pedido);

        fechaEntregaEditText = (EditText) findViewById(R.id.entrega_fecha_entrega);
        fechaEntregaEditText.setInputType(InputType.TYPE_NULL);
        calendarBtn = (ImageButton) findViewById(R.id.entrega_calendar_btn);

        clienteAutoComplete = (CustomAutoCompleteView) findViewById(R.id.entrega_cliente_autocomplete);

        // add the listener so it will tries to suggest while the user types
        clienteAutoComplete.addTextChangedListener(new EntregaActivityClienteTextChangedListener(this));

        // set our adapter
        clienteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsClientes);
        clienteAutoComplete.setAdapter(clienteAdapter);

        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               fechaEntregaDialog.show();
            }
        });

        setDateTimeField();


        //si proviene de Lista de Rutas, cargar el cliente en cuestion
        if (Globals.getClienteSeleccionadoRuta() != null){
            clienteSeleccionado = Globals.getClienteSeleccionadoRuta();

            clienteAutoComplete.setText(clienteSeleccionado.toString());
            pedidoLinearLayout.setVisibility(View.VISIBLE);
            pedidosFiltrados = db.selectPedidoByClienteId(clienteSeleccionado.getId());
            //cargar el Spinner con los resultados
            pedidosArrayAdapter = new ArrayAdapter<Pedido>(EntregaActivity.this, android.R.layout.simple_spinner_dropdown_item, pedidosFiltrados);
            pedidoSpinner.setAdapter(pedidosArrayAdapter);
            Globals.setClienteSeleccionadoRuta(null);
        }

        //al seleccionar un cliente de la lista filtrada
        clienteAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                clienteSeleccionado = clientesFiltrados.get(position);
                pedidoLinearLayout.setVisibility(View.VISIBLE);
                pedidosFiltrados = db.selectPedidoByClienteId(clienteSeleccionado.getId());
                //cargar el Spinner con los resultados
                pedidosArrayAdapter = new ArrayAdapter<Pedido>(EntregaActivity.this, android.R.layout.simple_spinner_dropdown_item, pedidosFiltrados);
                pedidoSpinner.setAdapter(pedidosArrayAdapter);
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
                        pedidoSeleccionado = null;
                        pedidoLinearLayout.setVisibility(View.GONE);
                        return true;
                    }
                }
                return false;
            }
        });

        pedidoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pedidoSeleccionado = (Pedido) parent.getAdapter().getItem(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (pedidosFiltrados.size() > 0)
                    pedidoSeleccionado = (Pedido) parent.getAdapter().getItem(0);
            }
        });

        guardarEntregaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarEntregaBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);
                enviarEntrega();
            }
        });

    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
        }
    };

    public void enviarEntrega (){

        if (clienteSeleccionado == null || clienteAutoComplete.getText().toString().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, EntregaActivity.this, false, dialogOnclicListener);
            guardarEntregaBtn.setEnabled(true);
            return;
        }

        if ( fechaEntregaEditText.getText().toString().isEmpty()){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Ingrese una fecha", buttons, EntregaActivity.this, false, dialogOnclicListener);
            guardarEntregaBtn.setEnabled(true);
            return;
        }
        if (pedidoSeleccionado == null ){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un pedido del cliente", buttons, EntregaActivity.this, false, dialogOnclicListener);
            guardarEntregaBtn.setEnabled(true);
            return;
        }


        boolean enLinea = AppUtils.isOnline(getApplicationContext());
        Log.d(TAG,"Conexión a internet: " + enLinea);

        //Crear Objeto Entrega
        String fechaRealEntrega = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        // formateo fecha Entrega
        String fechaEntregaSpinner = fechaEntregaEditText.getText().toString();
        Log.d(TAG, "fecha del Spinner: "+ fechaEntregaSpinner);

        SimpleDateFormat inputFecha = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFecha = new SimpleDateFormat("yyyy-MM-dd");
        String fechaEntrega = null;
        try {
            fechaEntrega = outputFecha.format(inputFecha.parse(fechaEntregaSpinner));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String horaEntrega = new SimpleDateFormat("HH:mm").format(new Date());

        //obtener usuario logueado
        Session sessionLogueado = db.selectUsuarioLogeado();

        e = new Entrega();

        e.setUser_id(sessionLogueado.getUserId().toString());
        e.setClient_id(clienteSeleccionado.getId().toString());
        e.setNombre_cliente(clienteSeleccionado.getNombre());
        e.setDate_delivered(fechaEntrega);
        e.setTime_delivered(horaEntrega);
        String observacion = observacionTextView.getText().toString();
        observacion = observacion.replace(" ","%20");
        e.setObservation(observacion);
        e.setOrder_id(pedidoSeleccionado.getId().toString());


        if (!enLinea){
            Log.d(TAG, "Sin conexion, se guarda la entrega");

            //guardar con estado PENDIENTE para su posterior envio
            e.setEstado_envio("PENDIENTE");
            db.insertEntrega(e);

            List<Entrega> entregasInsertadas = db.selectEntregaByEstado("PENDIENTE");
            for (Entrega e: entregasInsertadas){
                Log.d(TAG, e.toString());
            }

            Context context = getApplicationContext();
            CharSequence text = "No hay conexión. Se guarda y se volverá a intentar mas tarde.";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
            toast.show();
            finish();
            return;
        }

        CommDelegateAndroid delegateSendDelivery = new CommDelegateAndroid(){
            @Override
            public void onError(){
                Log.e(TAG, this.exception.getMessage());
                //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
            }
            @Override
            public void onSuccess(){
                Log.d(TAG, "Entrega enviada correctamente. OK");
                Toast.makeText(getApplicationContext(), "Entrega enviada correctamente.", Toast.LENGTH_LONG).show();
                guardarEntregaBtn.setEnabled(true);
                //guardar con estado ENVIADO
                e.setEstado_envio("ENVIADO");
                db.insertEntrega(e);
                finish();
            }
        };

        new Comm().requestGet(CommReq.CommReqSendDelivery, new String[][]{
                {"user_id", e.getUser_id()},
                {"client_id", e.getClient_id()},
                {"order_id",e.getOrder_id()},
                {"date_delivered", e.getDate_delivered()},
                {"time_delivered", e.getTime_delivered()},
                {"observation", e.getObservation()}
        }, delegateSendDelivery);

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
        if (view == fechaEntregaEditText){
            fechaEntregaDialog.show();
        }
    }

    //Setear fecha al editText
    private void setDateTimeField() {
        fechaEntregaEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaEntregaDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fechaEntregaEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    public static void enviarEntregasPendientes (Context context){
        new EntregaActivity().enviarEntregas(context);
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
            e = entrega;
            CommDelegateAndroid delegateSendDelivery = new CommDelegateAndroid(){
                @Override
                public void onError(){
                    Log.e(TAG, this.exception.getMessage());
                    //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
                }
                @Override
                public void onSuccess(){
                    //guardar con estado ENVIADO
                    e.setEstado_envio("ENVIADO");
                    db.updateEntrega(e);
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


}
