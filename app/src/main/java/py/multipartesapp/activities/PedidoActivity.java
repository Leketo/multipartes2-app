package py.multipartesapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.sprite.Sprite;
import com.github.ybq.android.spinkit.style.Circle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Pedido;
import py.multipartesapp.beans.PedidoDetalle;
import py.multipartesapp.beans.PrecioCategoria;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.Session;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;
import py.multipartesapp.utils.MyFormatter;


public class PedidoActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = PedidoActivity.class.getSimpleName();

    private AutoCompleteTextView clienteTextView;
    private Cliente clienteSeleccionado;
    private ListView detallesListView;
    public static List<PedidoDetalle> detallesList;
    private ImageAdapter adapterDetalles;
    private Button agregarDetalleBtn;
    private TextView totalGeneral;
    private TextView nroPedidoTextView;
    private Button guardarPedidoBtn;
    private Button enviarPedidoBtn;
    private PedidoDetalle detalleSeleccionado;
    private TextView categoriaClienteTextView;
    private TextView creditoClienteTextView;

    private ProgressBar progressBar;
    private Sprite progressStyle;

    private SimpleDateFormat dateFormatter;
    DecimalFormat formateador = new DecimalFormat("###,###.##");

    private AppDatabase db = new AppDatabase(this);

    private TextView textViewDescripcionSucursal;

    private String idSucursal;
    private String descripcionSucursal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido);

        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Pedido");


        //progressBar = (ProgressBar)findViewById(R.id.indeterminateBar);
        progressStyle = new Circle();

        progressStyle.setVisible(false,true);
        progressStyle.setBounds(0,0,100,100);

        Intent intent = getIntent();
        if (intent != null) {
            idSucursal = intent.getStringExtra("idSucursal");
            descripcionSucursal = intent.getStringExtra("descripcionSucursal");

            // Mostrar la descripción en un campo de texto no editable
            textViewDescripcionSucursal = findViewById(R.id.textViewDescripcionSucursal);
            if (textViewDescripcionSucursal != null) {
                textViewDescripcionSucursal.setText(descripcionSucursal);
            } else {
                Log.e("PedidoActivity", "textViewDescripcionSucursal es null.");
            }
        } else {
            Log.e("PedidoActivity", "Intent es null.");
        }

        clienteTextView = (AutoCompleteTextView) findViewById(R.id.pedido_cliente);
        detallesListView = (ListView) findViewById(R.id.pedido_detalle_list);
        agregarDetalleBtn = (Button) findViewById(R.id.pedido_agregar_detalle);
        totalGeneral = (TextView) findViewById(R.id.pedido_total);
        guardarPedidoBtn = (Button) findViewById(R.id.guardar_pedido);
        enviarPedidoBtn = (Button) findViewById(R.id.enviar_pedido);
        categoriaClienteTextView = (TextView) findViewById(R.id.pedido_categoria_cliente);
        creditoClienteTextView = (TextView) findViewById(R.id.pedido_cred_disponible);
        nroPedidoTextView = (TextView) findViewById(R.id.pedido_nropedido_interno);

        enviarPedidoBtn.setEnabled(false);


        detallesList = new ArrayList<PedidoDetalle>();

        if (db.countCliente() == 0) {
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        List<Cliente> lista_clientes = db.selectAllCliente();
        ArrayAdapter<Cliente> adapterClientes = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, lista_clientes);

        //temporal
        adapterClientes.setDropDownViewResource(android.R.layout.simple_spinner_item);
        //simple_spinner_dropdown_item
        clienteTextView.setAdapter(adapterClientes);

        adapterDetalles = new ImageAdapter(this);
        detallesListView.setAdapter(adapterDetalles);

        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);

        //Si proviene desde Activity Ruta, cargar el cliente en cuestion
        if (Globals.getClienteSeleccionadoRuta() != null) {
            clienteSeleccionado = Globals.getClienteSeleccionadoRuta();
            Globals.setClienteSeleccionadoPedido(clienteSeleccionado);
            mostrarDatosCliente(clienteSeleccionado);

            Globals.setClienteSeleccionadoRuta(null);
        }


        clienteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clienteTextView.clearFocus();
                clienteSeleccionado = (Cliente) parent.getAdapter().getItem(position);
                Globals.setClienteSeleccionadoPedido(clienteSeleccionado);

                //mostrar categoria cliente
                PrecioCategoria precioCategoria = db.selectPrecioCategoriaById(clienteSeleccionado.getCategoria_precio());
                categoriaClienteTextView.setText(precioCategoria.getName());

                //mostrar credito de cliente
                Integer creditoDisponible = clienteSeleccionado.getCredito_disponible().intValue();
                creditoClienteTextView.setText(formatearMoneda(MyFormatter.formatearMoneda(creditoDisponible.toString())));

            }
        });

        agregarDetalleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (clienteSeleccionado == null) {
                    String[] buttons = {"Ok"};
                    AppUtils.show(null, "Seleccione un cliente", buttons, PedidoActivity.this, false, dialogOnclicListener);
                    return;
                }
                if (detallesList.size() == 15) {
                    String[] buttons = {"Ok"};
                    AppUtils.show(null, "Solo se pueden agregar hasta 15 productos en el Pedido.", buttons, PedidoActivity.this, false, dialogOnclicListener);
                    return;
                }
                Intent intent = new Intent(PedidoActivity.this, PedidoDetalleNuevoActivity.class);
                intent.putExtra("idSucursal", idSucursal);
                startActivity(intent);


            }
        });

        guardarPedidoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //guardarPedidoBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);

                if (nroPedidoTextView.getText().length() > 0) {
                    actualizarPedido();
                } else {
                    guardarPedido();
                }


            }
        });


        enviarPedidoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //guardarPedidoBtn.setEnabled(false);
                //progressBar.setVisibility(View.VISIBLE);
                enviarPedido();
            }
        });
        detallesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                detalleSeleccionado = (PedidoDetalle) detallesList.get(position);

                String[] buttons = {"Si", "No"};
                if(Globals.getPedidoSeleccionado() ==null || !Globals.getPedidoSeleccionado().getEstado_envio().equalsIgnoreCase("ENVIADO")) {
                    AppUtils.show(null, "Eliminar detalle? ", buttons, PedidoActivity.this, false, dialogOnclicListenerEliminarDetalle);
                }
                //Intent intent = new Intent(OrderActivity.this, OrderProductActivity.class);
                //startActivity(intent);
                return false;
            }
        });

        //boton limpiar texto
        clienteTextView.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = clienteTextView.getRight()
                            - clienteTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        clienteTextView.setText("");
                        categoriaClienteTextView.setText("");
                        creditoClienteTextView.setText("");
                        Globals.setClienteSeleccionadoPedido(null);
                        clienteSeleccionado = null;

                        detallesList = new ArrayList<PedidoDetalle>();

                        adapterDetalles.notifyDataSetChanged();
                        totalGeneral.setText("Gs. 0");
                        return true;
                    }
                }
                return false;
            }
        });


        //si solo se va mostrar o editar los datos
        if (Globals.accion_pedido != null) {

            nroPedidoTextView.setText("" + Globals.getPedidoSeleccionado().getId());

            Cliente c = db.selectClienteById(Globals.getPedidoSeleccionado().getClient_id());
            clienteSeleccionado = c;
            Globals.setClienteSeleccionadoPedido(clienteSeleccionado);
            mostrarDatosCliente(clienteSeleccionado);

            List<PedidoDetalle> detalles = db.selectPedidoDetalleByPedido(Globals.getPedidoSeleccionado().getId());
            detallesList = detalles;
            adapterDetalles.notifyDataSetChanged();


            actualizarSumaTotalDetalles();

            //ocultar teclado
            clienteTextView.clearFocus();
            hideSoftKeyboard();


            if (Globals.accion_pedido.equals("VER")) {
                guardarPedidoBtn.setVisibility(View.GONE);
                agregarDetalleBtn.setVisibility(View.GONE);
                enviarPedidoBtn.setEnabled(false);
                clienteTextView.setEnabled(false);
            } else {
                guardarPedidoBtn.setText("Actualizar");
                enviarPedidoBtn.setEnabled(true);
            }
            Globals.setAccion_pedido(null);
            //Globals.setPedidoSeleccionado(null);
        }
    }

    public void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void actualizarPedido() {
        if (clienteSeleccionado == null || clienteTextView.getText().equals("")) {
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, PedidoActivity.this, false, dialogOnclicListener);
            guardarPedidoBtn.setEnabled(true);
            return;
        }

        if (detallesList.isEmpty()) {
            String[] buttons = {"Ok"};
            AppUtils.show(null, "El pedido no tiene productos.", buttons, PedidoActivity.this, false, dialogOnclicListener);
            guardarPedidoBtn.setEnabled(true);
            return;
        }

        Pedido pedido = Globals.getPedidoSeleccionado();
        pedido.setTotal(getSumaTotalDetalles());
        pedido.setClient_id(clienteSeleccionado.getId());
        pedido.setEstado_envio("PENDIENTE");
        db.updatePedido(pedido);

        //eliminar detalles
        db.deletePedidoDetalleByIdPedido(pedido.getId());

        //insertar nuevos detalles
        pedido.setDetalles(detallesList);
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            detalle.setOrder_id(pedido.getId());
            db.insertPedidoDetalle(detalle);
        }

        Globals.setPedidoSeleccionado(pedido);
        Context context = getApplicationContext();
        CharSequence text = "Pedido actualizado.";
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();

//        finish();
        //enviarPedidos(context);
        return;
    }

    private void enviarPedido() {


        if (clienteSeleccionado == null || clienteTextView.getText().equals("")) {
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un cliente", buttons, PedidoActivity.this, false, dialogOnclicListener);
            guardarPedidoBtn.setEnabled(true);
            return;
        }

        if (detallesList.isEmpty()) {
            String[] buttons = {"Ok"};
            AppUtils.show(null, "El pedido no tiene productos.", buttons, PedidoActivity.this, false, dialogOnclicListener);
            guardarPedidoBtn.setEnabled(true);
            return;
        }

        boolean enLinea = AppUtils.isOnline(getApplicationContext());
        Log.d(TAG, "Conexión a internet: " + enLinea);


//        if (nroPedidoTextView.getText().length() > 0) {
//            actualizarPedido();
//        } else {
//            guardarPedido();
//        }

        if(!enLinea){
            AppUtils.showError("No posee conexión a internet, no podrá realizar el envio del pedido",PedidoActivity.this);
            return;
        }


        Pedido pedido = Globals.getPedidoSeleccionado();

        pedido.setDetalles(detallesList);

        progressStyle.start();
        progressStyle.setVisible(true,true);

        InputStream inputStream = null;
        String result = "";
        try {
            HttpParams httpParameters = new BasicHttpParams();
// Set the timeout in milliseconds until a connection is established.
// The default value is zero, that means the timeout is not used.
            int timeoutConnection = 5000;
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
// Set the default socket timeout (SO_TIMEOUT)
// in milliseconds which is the timeout for waiting for data.
            int timeoutSocket = 10000;
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient(httpParameters);

            String url = Comm.URL + CommReq.CommReqEnviarPedido;
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);

            String json = "";

            // 3. build jsonObject

            JSONObject jsonObject = new JSONObject();

            jsonObject.accumulate("ad_client_id", "1000010");
            jsonObject.accumulate("ad_org_id", idSucursal);
            jsonObject.accumulate("date_order", pedido.getDate_order());
            jsonObject.accumulate("isactive", pedido.getIsactive());
            jsonObject.accumulate("client_id", pedido.getClient_id());
            jsonObject.accumulate("user_id", pedido.getUser_id());
            jsonObject.accumulate("total", pedido.getTotal());
            jsonObject.accumulate("observation", "Ninguna");
            jsonObject.accumulate("createdby", pedido.getUser_id());
            jsonObject.accumulate("updatedby", pedido.getUser_id());

            //Agregamos los detalles
            JSONArray detallesJsonArray = new JSONArray();
            for (PedidoDetalle detalle : pedido.getDetalles()) {
                JSONObject detalleJson = new JSONObject();
                detalleJson.put("isactive", detalle.getIsactive());
                detalleJson.put("product_id", detalle.getProduct_id());
                detalleJson.put("quantity", detalle.getQuantity());
                detalleJson.put("price", detalle.getPrice());
                detalleJson.put("total", detalle.getTotal());
                detalleJson.put("observation", detalle.getObservation());

                detallesJsonArray.put(detalleJson);
            }
            jsonObject.accumulate("orderline", detallesJsonArray);

            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Log.d(TAG, "enviando post: " + httpPost.toString());
            Log.d(TAG, "mensaje post: " + json);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: " + code);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

            if (code == 200) {

                if (result.contains("Portal Movil Tigo")) {
                    Log.d(TAG, "Sin conexion, se guarda el pedido");

                    Context context = getApplicationContext();
                    CharSequence text = "No hay conexión. Se guarda y se volverá a intentar mas tarde.";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
                    toast.show();
                    finish();
                    return;
                }

                Toast.makeText(getApplicationContext(), "Pedido enviado correctamente.", Toast.LENGTH_LONG).show();
                //Marcamos como enviado
                pedido.setEstado_envio("ENVIADO");
                db.updatePedido(pedido);

                enviarPedidoBtn.setEnabled(false);
                progressStyle.stop();
                progressStyle.setVisible(false,true);

                finish();


            } else {
                Toast.makeText(getApplicationContext(), "Error al enviar el pedido, reintente más tarde .", Toast.LENGTH_LONG).show();
                guardarPedidoBtn.setEnabled(true);
            }


            Log.d(TAG, "resultado  post: " + result);
        } catch (Exception e) {
            AppUtils.handleError("Error al enviar pedido.", PedidoActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            guardarPedidoBtn.setEnabled(true);
        }


        progressStyle.stop();
        progressStyle.setVisible(false,true);

    }

    public void guardarPedido() {


        // 1. Creamos la cabecera
        Pedido pedido = new Pedido();
        pedido.setTotal(getSumaTotalDetalles());
        pedido.setIsactive("Y");
        pedido.setIsinvoiced("N");
        pedido.setClient_id(clienteSeleccionado.getId());
        pedido.setAd_org_id(Integer.valueOf(idSucursal));
        pedido.setObservation(descripcionSucursal);

        String fechaPedido = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        pedido.setDate_order(fechaPedido);
        //usuario logueado
        Session sessionLogueado = db.selectUsuarioLogeado();
        pedido.setUser_id(sessionLogueado.getUserId());


        //2. Agregamos los detalles
        pedido.setDetalles(detallesList);


        Log.d(TAG, "Sin conexion, se guarda el pedido");
        //guardar con estado PENDIENTE para su posterior envio
        pedido.setEstado_envio("PENDIENTE");
        //Obtener ultimo id usado
        Pedido ultimoPedido = db.selectLastPedido();
        int nro_pedido = 1;
        if (ultimoPedido.getId() != null) {
            nro_pedido = ultimoPedido.getId() + 1;
            pedido.setId(nro_pedido);
        } else {
            pedido.setId(nro_pedido);
        }


        //restar debito del cliente

        Integer total = pedido.getTotal();
        Integer creditoDisponible = clienteSeleccionado.getCredito_disponible().intValue();
        Double saldo = Double.valueOf(creditoDisponible - total);
        clienteSeleccionado.setCredito_disponible(saldo);
        db.updateCliente(clienteSeleccionado);

        db.insertPedido(pedido);

        nroPedidoTextView.setText("" + nro_pedido);
        //insertamos los detalles de pedido
        for (PedidoDetalle detalle : pedido.getDetalles()) {
            detalle.setOrder_id(pedido.getId());
            db.insertPedidoDetalle(detalle);
        }

        //habilito el boton de enviar pedido
        enviarPedidoBtn.setEnabled(true);

        Context context = getApplicationContext();
        CharSequence text = "Su pedido se ha guardado correctamente.";

        Globals.setPedidoSeleccionado(pedido);


        int duration = Toast.LENGTH_LONG;
        Toast toast = Toast.makeText(context, text, duration);
        toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
        toast.show();


    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    DialogInterface.OnClickListener dialogOnclicListenerEliminarDetalle = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            Log.d(TAG, "Clic en : " + which);
            //Si=-1, No = -2
            if (which == -1) {
                detallesList.remove(detalleSeleccionado);
                adapterDetalles.notifyDataSetChanged();
                actualizarSumaTotalDetalles();

                Integer creditoDisponible = clienteSeleccionado.getCredito_disponible().intValue();
                Integer total = getSumaTotalDetalles();
                Double saldo = Double.valueOf(creditoDisponible - total);
                String saldoFormateado = MyFormatter.formatMoney(String.valueOf(saldo.intValue()));
                creditoClienteTextView.setText(saldoFormateado);
            }
        }
    };

    @Override
    protected void onResume() {
        //actualizar lista de detalle
        if (Globals.getNuevoPedidoDetalle() != null) {
            detallesList.add(Globals.getNuevoPedidoDetalle());
            adapterDetalles.notifyDataSetChanged();

            //actualizar saldo del cliente
            actualizarSumaTotalDetalles();
            Globals.setNuevoPedidoDetalle(null);

            Integer creditoDisponible = clienteSeleccionado.getCredito_disponible().intValue();
            Integer total = getSumaTotalDetalles();
            Double saldo = Double.valueOf(creditoDisponible - total);
            String saldoFormateado = MyFormatter.formatMoney(String.valueOf(saldo.intValue()));
            creditoClienteTextView.setText(saldoFormateado);

        }
        Globals.setProductoSeleccionadoCatalogo(null);
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

    private void mostrarDatosCliente(Cliente cliente) {
        clienteTextView.setText(cliente.toString());
        //mostrar categoria cliente
        PrecioCategoria precioCategoria = db.selectPrecioCategoriaById(cliente.getCategoria_precio());
        categoriaClienteTextView.setText(precioCategoria.getName());

        //mostrar credito de cliente
        Integer creditoDisponible = cliente.getCredito_disponible().intValue();
        creditoClienteTextView.setText(formatearMoneda(MyFormatter.formatearMoneda(creditoDisponible.toString())));
    }


    private Integer getSumaTotalDetalles() {
        Integer total = 0;
        for (PedidoDetalle detalle : detallesList) {
            if (detalle.getTotal() != null) {
                total = total + detalle.getTotal();
            }
        }
        return total;
    }

    private void actualizarSumaTotalDetalles() {
        String sumaTotal = getSumaTotalDetalles().toString();
        totalGeneral.setText("Gs. " + formatearMoneda(sumaTotal));
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public String formatearMoneda(String valor) {
        String formatted = "";
        String valorLimpio = limpiarMoneda(valor);
        if (!valorLimpio.isEmpty()) {
            int i = Integer.valueOf(valorLimpio);
            formatted = formateador.format(i).toString();
        }
        return formatted;
    }

    public String limpiarMoneda(String valor) {
        String valorLimpio = valor.replaceAll("[.]", "");
        valorLimpio = valorLimpio.replaceAll("[,]", "");
        return valorLimpio;
    }

    //On click de la Fecha
    @Override
    public void onClick(View view) {
        /*
        if (view == fechaProxVisitaEditText){
            fechaProxVisitaDialog.show();
        }
        */
    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return detallesList.size();
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

            PedidoDetalle item = detallesList.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_pedido_detalle, viewGroup, false);
                //v.setTag(R.id.img_places, v.findViewById(R.id.img_places));
                v.setTag(R.id.txt1_item_pedido_detalle, v.findViewById(R.id.txt1_item_pedido_detalle));
                v.setTag(R.id.cantidad_item_pedido_detalle, v.findViewById(R.id.cantidad_item_pedido_detalle));

                v.setTag(R.id.precio_item_pedido_detalle, v.findViewById(R.id.precio_item_pedido_detalle));
                v.setTag(R.id.total_item_pedido_detalle, v.findViewById(R.id.total_item_pedido_detalle));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_pedido_detalle);

            Producto p = db.selectProductById(item.getProduct_id());
            titleTextView.setText(p.getName() + " - " + p.getCodinterno());

            TextView precioUnitarioTextView = (TextView) v.findViewById(R.id.precio_item_pedido_detalle);
            precioUnitarioTextView.setText("Precio: " + item.getPrice());

            TextView subTitleTextView = (TextView) v.findViewById(R.id.cantidad_item_pedido_detalle);
            subTitleTextView.setText("Cantidad: " + item.getQuantity());

            TextView totalTextView = (TextView) v.findViewById(R.id.total_item_pedido_detalle);
            totalTextView.setText(formatearMoneda(item.getTotal().toString()));

            return v;
        }
    }

    public static void enviarPedidosPendientes(Context context) {
        new PedidoActivity().enviarPedidos(context);
    }

    public void enviarPedidos(Context context) {

        db = new AppDatabase(context);
        List<Pedido> list = db.selectPedidoByEstado("PENDIENTE");
        Log.d(TAG, "============== Se encontraron " + list.size() + " Pedidos PENDIENTES ");

        if (list.size() > 0) {
            CharSequence text = "Enviando " + list.size() + " Pedidos ";
            int duration = Toast.LENGTH_LONG;
            Toast toast = Toast.makeText(context, text, duration);
            toast.setGravity(Gravity.CENTER | Gravity.CENTER, 0, 0);
            toast.show();

            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        for (Pedido pedido : list) {

            InputStream inputStream = null;
            String result = "";
            try {
                // 1. create HttpClient
                HttpClient httpclient = new DefaultHttpClient();
                String url = Comm.URL + CommReq.CommReqEnviarPedido;
                // 2. make POST request to the given URL
                HttpPost httpPost = new HttpPost(url);
                String json = "";

                // 3. build jsonObject
                JSONObject jsonObject = new JSONObject();

                jsonObject.accumulate("ad_client_id", "1000010");
                jsonObject.accumulate("ad_org_id", idSucursal);
                jsonObject.accumulate("date_order", pedido.getDate_order());
                jsonObject.accumulate("isactive", pedido.getIsactive());
                jsonObject.accumulate("client_id", pedido.getClient_id());
                jsonObject.accumulate("user_id", pedido.getUser_id());
                jsonObject.accumulate("total", pedido.getTotal());
                jsonObject.accumulate("observation", "Ninguna");
                jsonObject.accumulate("createdby", pedido.getUser_id());
                jsonObject.accumulate("updatedby", pedido.getUser_id());

                //Agregamos los detalles
                JSONArray detallesJsonArray = new JSONArray();
                List<PedidoDetalle> detalles = db.selectPedidoDetalleByPedido(pedido.getId());

                for (PedidoDetalle detalle : detalles) {
                    JSONObject detalleJson = new JSONObject();
                    detalleJson.put("isactive", detalle.getIsactive());
                    detalleJson.put("product_id", detalle.getProduct_id());
                    detalleJson.put("quantity", detalle.getQuantity());
                    detalleJson.put("price", detalle.getPrice());
                    detalleJson.put("total", detalle.getTotal());
                    detalleJson.put("observation", detalle.getObservation());

                    detallesJsonArray.put(detalleJson);
                }
                jsonObject.accumulate("orderline", detallesJsonArray);

                // 4. convert JSONObject to JSON to String
                json = jsonObject.toString();

                // 5. set json to StringEntity
                StringEntity se = new StringEntity(json);

                // 6. set httpPost Entity
                httpPost.setEntity(se);

                // 7. Set some headers to inform server about the type of the content
                httpPost.setHeader("Accept", "application/json");
                httpPost.setHeader("Content-type", "application/json");

                DefaultHttpClient httpclient2 = new DefaultHttpClient();

                // 8. Execute POST request to the given URL
                HttpResponse httpResponse = httpclient2.execute(httpPost);

                int code = httpResponse.getStatusLine().getStatusCode();
                //si llega 401 es error de login
                Log.d(TAG, "responde code envio pedido pendiente: " + code);

                // 9. receive response as inputStream
                inputStream = httpResponse.getEntity().getContent();

                // 10. convert inputstream to string
                if (inputStream != null) {
                    result = convertInputStreamToString(inputStream);
                } else {
                    result = "Did not work!";
                }
                if (code == 200) {
                    if (result.contains("Portal Movil Tigo")) {
                        Log.e(TAG, "Error al enviar pedido. Sin saldo tigo");
                        return;
                    }
                    pedido.setEstado_envio("ENVIADO");
                    db.updatePedido(pedido);

                } else {
                    Toast.makeText(getApplicationContext(), "Error al enviar pedido.", Toast.LENGTH_LONG).show();
                }


                Log.d(TAG, "resultado  post: " + result);
            } catch (Exception e) {
                AppUtils.handleError("Error al enviar pedido.", PedidoActivity.this);
                Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
            }

        }
    }
}
