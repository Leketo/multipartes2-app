package py.multipartesapp.activities;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.zj.btsdk.BluetoothService;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import py.multipartes2.R;
import py.multipartesapp.activities.adapters.ExpandableDeviceListAdapter;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.DOPrintSettings;
import py.multipartes2.utils.ListaEntrada;
import py.multipartes2.utils.QuickstartPreferences;

public class DOPrintMainActivity extends ActionBarActivity {

    private static final String TAG = DOPrintMainActivity.class.getSimpleName();
    //Expandable list view
    ExpandableDeviceListAdapter listAdapterDevice;
    private ExpandableListView expListViewDevice;
    private List<String> listDataHeader;
    private HashMap<String, List<ListaEntrada>> listDataChildDevice;
    private List<PairedBluetoothDevices> pairedDeviceList;

    ArrayAdapter<String> printTypeadapter;
    List<String> listPrintType = new ArrayList<>();

    private EditText macAdressTxt;

    //Keys to pass data to/from FileBrowseActivity
    static final String FOLDER_NAME_KEY = "com.datamaxoneil.doprint.Folder_Name_Key";
    static final String FOLDER_PATH_KEY = "com.datamaxoneil.doprint.Folder_Path_Key";

    //Keys to pass data to Connection Activity
    static final String CONNECTION_MODE_KEY = "com.datamaxoneil.doprint.Connection_Mode_Key";
    static final String PRINTER_IPADDRESS_KEY = "com.datamaxoneil.doprint.PRINTER_IPAddress_Key";
    static final String PRINTER_TCPIPPORT_KEY = "com.datamaxoneil.doprint.PRINTER_TCPIPPort_Key";
    static final String BLUETOOTH_DEVICE_ADDR_KEY = "com.datamaxoneil.doprint.PRINTER_Bluetooth_Device_Addr_Key";

    //Variable for folder content
    private String m_selectedPath;

    //Variable for Connection information
    private String m_printerAddress = "Unknown";
    private int m_printerPort = 515;
    private String connectionType = "Bluetooth";

    ArrayAdapter<CharSequence> adapter = null;

    //array to contain the filenames inside a directory
    List<String> filesList = new ArrayList<String>();
    static final int CONFIG_CONNECTION_REQUEST = 0; // for Connection Settings
    private static final int REQUEST_PICK_FILE = 1; //for File browsing

    // use to update the UI information.
    private Handler m_handler = new Handler(); // Main thread

    Button m_printButton;
    Button m_configConnectionButton;

    DOPrintSettings g_appSettings = new DOPrintSettings("", 0, "/", "", 0,0,0,0);
    private static String typeSelected = "";
    private static SharedPreferences sharedPreferences;
    private AppDatabase db = new AppDatabase();

    //Viejo
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice mmDevice;
    BluetoothSocket mmSocket;
    OutputStream mmOutputStream;
    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;
    String helloWorld = "Prueba de impresion.\n";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_doprint_main);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        //Lista de dispositivos vinculados
        expListViewDevice = (ExpandableListView) findViewById(R.id.connDevices);
        //txt mac address
        macAdressTxt = (EditText) findViewById(R.id.mac_adress);
        if (sharedPreferences.getBoolean(QuickstartPreferences.BLUETOOTH_MAC_ADDRESS, false)) {
            macAdressTxt.setText(sharedPreferences.getString("mac_address_bt", null));
        }
        //======Mapping UI controls from our activity xml===========//
        m_configConnectionButton = (Button)findViewById(R.id.configConn_button);
        m_printButton = (Button)findViewById(R.id.print_button);

        setupConfigBtn();

        bluetoothadd();
        macAdressTxtListener();

        setupPrintBtn();
        cargarExpListView();
        listAdapterDevice= new ExpandableDeviceListAdapter(this, listDataHeader, listDataChildDevice);
        expListViewDevice.setAdapter(listAdapterDevice);
        /* Evento clic */
        expListViewDevice.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // ListView Clicked item value
                Log.d("", "Group position: " + groupPosition + " - Child Position: " + childPosition);
                listDataHeader.get(groupPosition);
                ListaEntrada itemValue = listDataChildDevice.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);
                String macAddress = itemValue.getDeviceMacAddress();
                String device_name = itemValue.getDeviceName();
                Log.d("device_name:", device_name);
                macAdressTxt.setText(macAddress);
                return false;
            }
        });
        if(macAdressTxt.getText().toString().isEmpty()){
            m_configConnectionButton.setEnabled(false);
        }else {
            m_configConnectionButton.setEnabled(true);
        }
        setFieldsAsDefault();
    }

    private void setFieldsAsDefault(){
        String mac = sharedPreferences.getString("mac_address_bt", null);
        if(mac != null)
            macAdressTxt.setText(mac);
    }
    /**
     * Handle Print/Send Function
     */
    private void setupPrintBtn(){
        m_printButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //imprime la prueba
                m_printButton.setEnabled(false);
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
                    // Do something for lollipop and above versions
                    try {
                        findBT();
                        openBT();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else{
                    if(macAdressTxt.getText().toString() == null){
//                        WalrusUtils.showToast("La direccion MAC no esta configurada.", getApplicationContext());
                    }else{
                        print(helloWorld);
                        //imprimirFactura();
                    }
                }
            }
        });
    }
    /**
     * Handles when user presses connection config button
     */
    private void setupConfigBtn(){

        m_configConnectionButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                boolean enviar = true;
                if (enviar) {
                    m_configConnectionButton.setEnabled(false);
                    try {
                        String m_printerBluetoothAddr = macAdressTxt.getText().toString().toUpperCase(Locale.US);
                        //validate if its a MAC address
                        Pattern pattern = Pattern.compile("[0-9A-Fa-f]{12}");
                        Matcher matcher = pattern.matcher(m_printerBluetoothAddr);
                        if (matcher.matches())
                            m_printerBluetoothAddr = formatBluetoothAddress(m_printerBluetoothAddr);

                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter == null) {
                            // Device does not support Bluetooth
                        }else if (!mBluetoothAdapter.isEnabled()) {
                            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivity(enableBtIntent);
                            //pairedDevices = mBluetoothAdapter.getBondedDevices();
                            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                            startActivity(discoverableIntent);

                            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            //startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                        }
                        if (!BluetoothAdapter.checkBluetoothAddress(m_printerBluetoothAddr)) {
                            throw new Exception("El formato de dirección de Bluetooth no es válido.");
                        } else {
                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                            sharedPreferences.edit().putBoolean(QuickstartPreferences.BLUETOOTH_MAC_ADDRESS, true).apply();
                            sharedPreferences.edit().putString("mac_address_bt", macAdressTxt.getText().toString()).apply();
//                            WalrusUtils.showToast("Configuracion exitosa", getApplicationContext());
                        }
                        //=====Error handling============//
                    } catch (NumberFormatException nfe) {
                        nfe.printStackTrace();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DOPrintMainActivity.this);
                        builder.setTitle("Error")
                                .setMessage("Formato de puerto inválido. Por favor vuelva a ingresar el puerto.")
                                .setCancelable(false)
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        AlertDialog.Builder builder = new AlertDialog.Builder(DOPrintMainActivity.this);
                        builder.setTitle("Error")
                                .setMessage(e.getMessage())
                                .setCancelable(false)
                                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                } else {
//                    WalrusUtils.showToast("Debe establecer una cantidad de impresiones", getApplicationContext());
                }
            }
        });
    }
    /**
     * Listener para activar/desactivar boton de configuracion
     */
    private void macAdressTxtListener(){
        macAdressTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.toString().isEmpty()){
                    m_configConnectionButton.setEnabled(false);
                }else{
                    m_configConnectionButton.setEnabled(true);
                }
            }
        });
    }
    /**
     * Metodo que carga la lista de dispositivos bluetooth asociados
     */
    private void cargarExpListView() {

        listDataHeader = new ArrayList<String>();
        listDataHeader.add("Dispositivos vinculados");
        /* Pendientes */
        listDataChildDevice = new HashMap<String, List<ListaEntrada>>();
        for (int i = 0; i < listDataHeader.size(); i++) {
            List<ListaEntrada> bluetoothDevice = new ArrayList<ListaEntrada>();

            for (PairedBluetoothDevices p : pairedDeviceList) {
                bluetoothDevice.add(new ListaEntrada(
                        p.getDeviceName(),
                        p.getMACAddress()));
            }
            listDataChildDevice.put(listDataHeader.get(i), bluetoothDevice);
        }
    }

//	public void DisplayPrintingStatusMessage(String MsgStr) {
//		g_PrintStatusStr = MsgStr;
//
//		m_handler.post(new Runnable() {
//			public void run() {
//			}// run()
//		});
//	}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode)
        {
            //get results from connection settings activity
            case CONFIG_CONNECTION_REQUEST:
            {
                if (resultCode == RESULT_OK)
                {
                    // get the bundle data from the TCP/IP Config Intent.
                    Bundle extras = data.getExtras();
                    if (extras != null)
                    {
                        //===============Get data from Bluetooth configuration=================//
                        if(connectionType.equals("Bluetooth"))
                        {
                            m_printerAddress = extras.getString(BLUETOOTH_DEVICE_ADDR_KEY);
                            m_printerAddress = m_printerAddress.toUpperCase(Locale.US);
                            if(!m_printerAddress.matches("[0-9A-fa-f:]{17}"))
                            {
                                m_printerAddress = formatBluetoothAddress(m_printerAddress);
                            }
                        }
                        g_appSettings.setPrinterAddress(m_printerAddress);
                        g_appSettings.setPrinterPort(m_printerPort);

                    }
                }
                break;
            }


	/*  results from file browsing activity*/
            case REQUEST_PICK_FILE:
            {
                if (resultCode == RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if(extras != null) {
                        //========Get the file path===============//
                        m_selectedPath = extras.getString(FOLDER_PATH_KEY);
                        if(!filesList.contains(m_selectedPath))
                            filesList.add(m_selectedPath);
                        if(adapter != null)
                        {
                            //if item is not on the list, then add
                            if(adapter.getPosition(m_selectedPath) < 0)
                            {
                                adapter.add((CharSequence)m_selectedPath);
                                adapter.notifyDataSetChanged();
                            }
                        }
                    }
                }
                break;
            }
        }
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //reload your ScrollBars by checking the newConfig

    }

    public void showToast(final String toast) {
        Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_LONG).show();
    }
    /** Converts Bluetooth Address string from 00ABCDEF0102 format => 00:AB:CD:EF:01:02 format
     * @param bluetoothAddr - Bluetooth Address string to convert
     */
    public String formatBluetoothAddress(String bluetoothAddr)
    {
        //Format MAC address string
        StringBuilder formattedBTAddress = new StringBuilder(bluetoothAddr);
        for (int bluetoothAddrPosition = 2; bluetoothAddrPosition <= formattedBTAddress.length() - 2; bluetoothAddrPosition += 3)
            formattedBTAddress.insert(bluetoothAddrPosition, ":");
        return formattedBTAddress.toString();
    }

    /**
     * Método que activa el bluetooth si no lo esta y busca los dispositivos bluetooth vinculados
     * al dispositivo del usuario.
     *
     */
    private void bluetoothadd(){
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices = null;
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
            Log.e("Bluetooth ","not found");
        }
        pairedDeviceList = new ArrayList<PairedBluetoothDevices>();
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBtIntent);
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
            startActivity(discoverableIntent);

            // If there are paired devices
            if (pairedDevices.size() > 0) {
                // Loop through paired devices
                for (BluetoothDevice device : pairedDevices) {
                    PairedBluetoothDevices pbd = new PairedBluetoothDevices();
                    pbd.setMACAddress(mBluetoothAdapter.getRemoteDevice(device.getAddress())+"");
                    pbd.setDeviceName(device.getName());
                    Log.d("Mac Addressess","are:  "+pbd.getMACAddress());
                    Log.d("Device Name",""+pbd.getDeviceName());
                    pairedDeviceList.add(pbd);
                }
            }
        }else{
            pairedDevices = mBluetoothAdapter.getBondedDevices();
            // If there are paired devices

        }
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                PairedBluetoothDevices pbd = new PairedBluetoothDevices();
                pbd.setMACAddress(mBluetoothAdapter.getRemoteDevice(device.getAddress())+"");
                pbd.setDeviceName(device.getName());

                Log.d("Mac Addressess","are:  "+pbd.getMACAddress());
                Log.d("Device Name",""+pbd.getDeviceName());
                pairedDeviceList.add(pbd);
            }
        }
    }

    /*bluetooth*/
    BluetoothService mService = null;
    BluetoothDevice con_dev = null;
    String mesageToPrint;
    private void print(String data){
        mesageToPrint = data;
        mService = new BluetoothService(this, mHandler);
        if( mService.isAvailable() == false ){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
//        WalrusUtils.showToast("Enviando datos a la impresora...", Ctx.walrusContext);
        con_dev = mService.getDevByMac(macAdressTxt.getText().toString());
        if(mService.getState() != BluetoothService.STATE_CONNECTED) {
            mService.connect(con_dev);
        }
    }

    private void imprimirFactura(){
        Log.d(TAG, "imprimirFactura");
        mService.sendMessage(mesageToPrint + "\n", "GBK");
        try {
            Thread.sleep(2000);
            mService.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:   //ÒÑÁ¬½Ó
                            Toast.makeText(getApplicationContext(), "Conexion exitosa",
                                    Toast.LENGTH_SHORT).show();
                            imprimirFactura();
                            break;
                        case BluetoothService.STATE_CONNECTING:  //ÕýÔÚÁ¬½Ó
                            Log.d("mHandler","STATE_CONNECTING");
                            break;
                        case BluetoothService.STATE_LISTEN:     //¼àÌýÁ¬½ÓµÄµ½À´
                        case BluetoothService.STATE_NONE:
                            Log.d("mHandler","STATE_NONE");
                            break;
                    }
                    break;
                case BluetoothService.MESSAGE_CONNECTION_LOST:    //À¶ÑÀÒÑ¶Ï¿ªÁ¬½Ó
					/*Toast.makeText(getApplicationContext(), "Se perdio la conexion con el dispositivo",
							Toast.LENGTH_SHORT).show();*/
                    break;
                case BluetoothService.MESSAGE_UNABLE_CONNECT:     //ÎÞ·¨Á¬½ÓÉè±¸
                    Toast.makeText(getApplicationContext(), "No se puede conectar al dispositivo",
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }

    };
    void findBT() {

//        try {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
//            WalrusUtils.showToast("No bluetooth adapter available", getApplicationContext());
            //myLabel.setText("No bluetooth adapter available");
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

                // MP300 is the name of the bluetooth printer device
                if (device.getAddress().equals(macAdressTxt.getText().toString())) {
                    mmDevice = device;
                    //WalrusUtils.showToast("Bluetooth Device Found", getApplicationContext());
                    break;
                }
            }
        }
        Log.d(TAG, "1. Bluetooth Device Found");

    }

    void openBT() throws IOException {
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

//			myLabel.setText("Bluetooth Opened");
            //WalrusUtils.showToast("Bluetooth Opened", getApplicationContext());
            sendData(helloWorld);
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
//            WalrusUtils.showToast("Error al conectar con el Bluetooth", getApplicationContext());
            Log.d(TAG, "3. Error when Bluetooth conection establishing");
            e.printStackTrace();
            m_printButton.setEnabled(true);
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

//			// the text typed by the user
//			String msg = myTextbox.getText().toString();
//			msg += "\n";
            Log.d(TAG, "4. Sending data");
            mmOutputStream.write(data.getBytes());
            Log.d(TAG, "5. Data sent");
            // tell the user data were sent
//			myLabel.setText("Data Sent");
            //WalrusUtils.showToast("Data Sent", getApplicationContext());
            Thread.sleep(2000);
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
//			myLabel.setText("Bluetooth Closed");
            Log.d(TAG, "6. Bluetooth Closed");
            //WalrusUtils.showToast("Bluetooth Closed", getApplicationContext());
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            m_printButton.setEnabled(true);
        }
    }
}
