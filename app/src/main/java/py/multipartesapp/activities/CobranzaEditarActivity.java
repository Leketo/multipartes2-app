package py.multipartesapp.activities;

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
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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

import com.zj.btsdk.BluetoothService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
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
import py.multipartesapp.beans.CobranzaFormaPago;
import py.multipartesapp.beans.Factura;
import py.multipartesapp.customAutoComplete.CobranzaActivityClienteTextChangedListener;
import py.multipartesapp.customAutoComplete.CustomAutoCompleteView;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;
import py.multipartesapp.utils.MyFormatter;

public class CobranzaEditarActivity extends ActionBarActivity {



    public static final String TAG = CobranzaEditarActivity.class.getSimpleName();

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
    private TextView totalEfectivo;
    private TextView totalCheque;

    private TextView totalDeuda;
    private Button agregarItemBtn;
    private Button eliminarEfectivoBtn;
    private Button eliminarChequeBtn;

    private TextView montoPagado;


    public String[] itemsClientes = new String[] {"Buscar por nombre o ruc..."};
    public CustomAutoCompleteView clienteAutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> clienteAdapter;
    private List<Cliente> clientesFiltrados;

    public static List<Factura> invoicesListFiltered = new ArrayList<>();

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

    SimpleDateFormat sdf =new SimpleDateFormat("dd/MM/yyyy");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Cobros");


        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        observacionEditText = (EditText) findViewById(R.id.cobranza_observacion);
        nroFacturaEditText = (EditText) findViewById(R.id.cobranza_nro_factura);
        totalCobrado = (TextView) findViewById(R.id.cobranza_total_cobrado);
        totalDeuda= (TextView) findViewById(R.id.cobranza_total_deuda);
        totalFormaPago = (TextView) findViewById(R.id.total_forma_pago);
        agregarItemBtn = (Button) findViewById(R.id.cobranza_detalle_add_cobro);
        eliminarEfectivoBtn=(Button) findViewById(R.id.eliminar_efectivo);
        eliminarChequeBtn=(Button) findViewById(R.id.eliminar_cheque);
        totalEfectivo=(TextView) findViewById(R.id.total_efectivo);
        totalCheque=(TextView) findViewById(R.id.total_cheque);

        montoPagado=(TextView) findViewById(R.id.monto_pagado_item_cobranza);

        guardarCobranzaBtn = (Button) findViewById(R.id.guardar_cobranza);



        pedidosListView = (ListView) findViewById(R.id.cobranza_detalle_list);

        clienteAutoComplete = (CustomAutoCompleteView) findViewById(R.id.cobranza_cliente_autocomplete);

        adapterDetalles = new ImageAdapter(this);

        pedidosListView.setAdapter(adapterDetalles);

        // add the listener so it will tries to suggest while the user types
        clienteAutoComplete.addTextChangedListener(new CobranzaActivityClienteTextChangedListener(this));

        // set our adapter
        clienteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsClientes);
        clienteAutoComplete.setAdapter(clienteAdapter);


        if(Globals.getAccion_cobranza().equalsIgnoreCase("VER")){


        }else{


        }




        Cobranza cobranza=Globals.getCobranzaSeleccionada();

        //set the cliente
        Cliente cliente =db.selectClienteById(cobranza.getClient_id());
        clienteSeleccionado=cliente;
        clienteAutoComplete.setText(""+clienteSeleccionado.toString());

        nroFacturaEditText.setText(""+cobranza.getInvoice_number());

        List<CobranzaDetalle> cdDetalle=db.selectCobranzaDetalleByCobro(cobranza.getId());

        Log.d("cobranza",""+cobranza.toString());

        for (CobranzaDetalle cd : cdDetalle) {
            Factura p = db.selectFacturaById(Integer.parseInt(cd.getInvoice().toString()));
            invoicesListFiltered.add(p);
        }

        adapterDetalles.notifyDataSetChanged();

        List<CobranzaFormaPago> listCobrosFormaPago=db.selectCobranzaFormaPagoByIdCobro(cobranza.getId());
        Globals.setItemCobroList(listCobrosFormaPago);

        actualizarImporteFormaPago();
        actualizarTotal();
        actualizarTotalDeuda();

        //al seleccionar un cliente de la lista filtrada
        //when we edit we must not change the client because
//        clienteAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                //clienteTextView.clearFocus();
//                clienteSeleccionado = clientesFiltrados.get(position);
//                invoicesListFiltered = db.selectFacturaByClientId(clienteSeleccionado.getId());
//
//                Log.d("facturas ","facturas "+invoicesListFiltered);
//
//                adapterDetalles.notifyDataSetChanged();
//                actualizarTotalDeuda();
//            }
//        });

    }



    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
        }
    };

    //despues de guardar correctamente
    DialogInterface.OnClickListener dialogOnclicListenerAfterSave = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
            finish();
        }
    };



    private void mostrarMensajeEnviado (String msj){

        String[] buttons = {"Ok"};
        int id_icon = R.drawable.ic_check;
        AppUtils.showWithIcon("Enviado", id_icon,msj, buttons, CobranzaEditarActivity.this, true, dialogOnclicListenerAfterSave);

    }

    private void mostrarMensajeNoEnviado (String msj){

        String[] buttons = {"Ok"};
        int id_icon = R.drawable.ic_close;
        AppUtils.showWithIcon("NO Enviado", id_icon,msj, buttons, CobranzaEditarActivity.this, true, dialogOnclicListenerAfterSave);

    }

    private void actualizarTotalDeuda() {
        Integer totalDeudaInteger = getTotalDeuda();
        totalDeuda.setText("Gs. "+ MyFormatter.formatMoney(totalDeudaInteger.toString()));

    }

    private void actualizarImporteFormaPago(){
        Integer totAmountFp = 0;
        Integer totEfectivo=0;
        Integer totCheque=0;
        if(Globals.getItemCobroList() != null) {
            for (CobranzaFormaPago c : Globals.getItemCobroList()) {
                totAmountFp += c.getAmount();
                if(c.getPayment_type().equalsIgnoreCase("EFECTIVO")){
                    totEfectivo+=c.getAmount();
                }else if(c.getPayment_type().equalsIgnoreCase("CHEQUE")){
                    totCheque+=c.getAmount();
                }

            }
        }
        totalEfectivo.setText(""+totEfectivo);
        totalCheque.setText(""+totCheque);

        totalFormaPago.setText(MyFormatter.formatMoney(String.valueOf(totAmountFp)));

    }
    private Integer getTotalDeuda(){
        Integer total = 0;
        for (Factura p: invoicesListFiltered){
            if (p.getGrandtotal() != null && !p.getGrandtotal().equals("0")){
                total = total + Integer.valueOf(p.getGrandtotal());
            }
        }
        return total;
    }

    @Override
    protected void onResume() {
        Log.d("cobranza","onresume");
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


    private void updateInvoices(){

        for (Factura p: invoicesListFiltered){
            if (p.getMontoCobrado() != null && !p.getMontoCobrado().equals("0") ){
                Log.d("actualizar factura ",""+p);
                //we must substract the payed from the total
                Integer pend=p.getPend()-Integer.parseInt(p.getMontoCobrado());
                Log.d("pendiente", "el nuevo pendiente es"+pend);
                if(pend<=0){
                    p.setIspaid("Y");
                }
                p.setPend(pend);
                db.updateFactura(p);
            }
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
        for (Factura p: invoicesListFiltered){
            if (p.getMontoCobrado() != null && !p.getMontoCobrado().equals("0")){
                total = total + Integer.valueOf(p.getMontoCobrado());
            }
        }
        return total;
    }



    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return invoicesListFiltered.size();
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
            //final ViewHolder holder;


            Factura item = invoicesListFiltered.get(i);

            Log.d("factura: ",""+item);

            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_cobranza_pedido, viewGroup, false);

                v.setTag(R.id.txt1_item_cobranza_nro_factura, v.findViewById(R.id.txt1_item_cobranza_nro_factura));
                v.setTag(R.id.txt2_item_cobranza_fecha, v.findViewById(R.id.txt2_item_cobranza_fecha));
                v.setTag(R.id.total_item_cobranza, v.findViewById(R.id.total_item_cobranza));
                v.setTag(R.id.monto_a_cobrar_item_cobranza, v.findViewById(R.id.monto_a_cobrar_item_cobranza));
                //v.setTag(R.id.editar_item_cobranza_btn, v.findViewById(R.id.editar_item_cobranza_btn));
                v.setTag(R.id.check_item_cobranza_btn, v.findViewById(R.id.check_item_cobranza_btn));

            }

            CheckBox cbItem = (CheckBox) v.getTag(R.id.check_item_cobranza_btn);
            cbItem.setChecked(invoicesListFiltered.get(i).isSelected());

            TextView nroFactura = (TextView) v.findViewById(R.id.txt1_item_cobranza_nro_factura);
            nroFactura.setText(item.getNroFacturaImprimir());

            TextView fechaPedido = (TextView) v.findViewById(R.id.txt2_item_cobranza_fecha);

            Date fechaParseada=null;
            String fecha="";

            Log.d("fecha factura",""+item.getDateinvoiced());
            SimpleDateFormat sdfParse=new SimpleDateFormat("yyyy-MM-dd");

            if(item.getDateinvoiced()!=null){

                try {
                    fechaParseada=sdfParse.parse(item.getDateinvoiced().substring(0,10));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(fechaParseada!=null){
                    fecha = sdf.format(fechaParseada);
                }
            }



            fechaPedido.setText(fecha);

            TextView totalPedido = (TextView) v.findViewById(R.id.total_item_cobranza);
            totalPedido.setText(item.getGrandtotal().toString());

            final TextView montoPagado = (TextView) v.findViewById(R.id.monto_pagado_item_cobranza);
            int prepaid=0;
            if(item.getGrandtotal()!=null){
                prepaid=item.getGrandtotal()-item.getPend();
            }
            montoPagado.setText(""+prepaid);


            final EditText montoAcobrar = (EditText) v.findViewById(R.id.monto_a_cobrar_item_cobranza);
            Log.i("invoice "," grandtotal: "+item.getGrandtotal()+" montocobrado:   "+item.getMontoCobrado()+" montoPendiente "+item.getPend());

            if (item.getMontoCobrado() != null ){
                montoAcobrar.setText(""+item.getMontoCobrado().toString());
            }else {
                if(item.getPend()!=null){
                    montoAcobrar.setText(""+item.getPend());
                }
            }

            montoAcobrar.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v1, boolean hasFocus) {
                    if(!hasFocus){
                        View parentRow = (View) v1.getParent();
                        ViewParent viewParent = parentRow.getParent();
                        ListView listView = (ListView) viewParent.getParent();
                        int position = listView.getPositionForView((View) viewParent);
                        /*Actualizar monto en la lista*/
                        if (invoicesListFiltered.get(position).isSelected()){
                            //if(cbItem.isChecked()) {
                            String montoCob = montoAcobrar.getText().toString();

                            if(!montoCob.isEmpty()) {
                                invoicesListFiltered.get(position).setMontoCobrado(montoCob);
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
                    String montoCob = montoAcobrar.getText().toString();


                    if (invoicesListFiltered.get(position).isSelected()){
                        invoicesListFiltered.get(position).setSelected(false);
                        invoicesListFiltered.get(position).setMontoCobrado("0");
                        //todo: calcular total
                        actualizarTotal();

                    }
                    else {
                        invoicesListFiltered.get(position).setSelected(true);
                        if(!montoCob.isEmpty()) {
                            invoicesListFiltered.get(position).setMontoCobrado(montoCob);
                            //todo: calcular total
                            actualizarTotal();
                            facturaSeleccionada = invoicesListFiltered.get(position);
                        }
                    }
                    /*
                    if(cbItem.isChecked()){
                        if(!montoCob.isEmpty()) {
                            invoicesListFiltered.get(position).setMontoCobrado(montoCob);
                            //todo: calcular total
                            actualizarTotal();
                            facturaSeleccionada = invoicesListFiltered.get(position);
                        }
                    }else {
                        invoicesListFiltered.get(position).setMontoCobrado("0");
                        //todo: calcular total
                        actualizarTotal();
                    }
                    */

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
                    facturaSeleccionada = invoicesListFiltered.get(position);
                    Intent intent = new Intent(CobranzaActivity.this, CobranzaDetalleActivity.class);
                    startActivity(intent);
                }
            });*/

            return v;
        }
    }






}
