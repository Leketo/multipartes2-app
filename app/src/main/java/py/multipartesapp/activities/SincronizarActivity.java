package py.multipartesapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import py.multipartes2.R;
import py.multipartes2.beans.ClienteList;
import py.multipartes2.beans.CobranzaList;
import py.multipartes2.beans.Configuracion;
import py.multipartes2.beans.EntregaList;
import py.multipartes2.beans.FacturaList;
import py.multipartes2.beans.Pedido;
import py.multipartes2.beans.PedidoDetalle;
import py.multipartes2.beans.PedidoList;
import py.multipartes2.beans.PrecioCategoriaList;
import py.multipartes2.beans.PrecioVersionList;
import py.multipartes2.beans.ProductoFamiliaList;
import py.multipartes2.beans.ProductoImagen;
import py.multipartes2.beans.ProductoImagenList;
import py.multipartes2.beans.ProductoList;
import py.multipartes2.beans.ProductoSubFamiliaList;
import py.multipartes2.beans.RegistroVisitaList;
import py.multipartes2.beans.RutaLocationList;
import py.multipartes2.beans.UsuarioList;
import py.multipartes2.comm.Comm;
import py.multipartes2.comm.CommDelegateAndroid;
import py.multipartes2.comm.CommReq;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;

/*
* @author adolfo
*/

public class SincronizarActivity extends ActionBarActivity {
    public static final String TAG = SincronizarActivity.class.getSimpleName();

    private Button sincronizarBtn;
    private ProgressBar progressBar;
    private TextView mensajeTextView;
    private AppDatabase db = new AppDatabase(this);

    private ClienteList clienteList;
    private UsuarioList usuarioList;
    private PedidoList pedidoList;
    private ProductoList productoList;
    private PrecioCategoriaList precioCategoriaList;
    private PrecioVersionList precioVersionList;
    private RutaLocationList rutaLocationList;
    private CobranzaList cobranzaList;
    private FacturaList facturaList;
    private ProductoFamiliaList productoFamiliaList;
    private ProductoSubFamiliaList productoSubFamiliaList;
    private ProductoImagenList productoImagenList;
    private RegistroVisitaList registroVisitaList;
    private EntregaList entregaList;

    private CheckBox catalogoCheckbox;
    private int count=0;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sincronizar);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Sincronizar");

        sincronizarBtn = (Button) findViewById(R.id.btn_sincronizar);
        progressBar = (ProgressBar) findViewById(R.id.sincronizar_progress_bar);
        mensajeTextView = (TextView) findViewById(R.id.sincronizar_mensaje);
        catalogoCheckbox = (CheckBox) findViewById(R.id.sincronizar_checkbox_catalog);

        sincronizarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sincronizarBtn.setEnabled(false);
                progressBar.setVisibility(View.VISIBLE);
                mensajeTextView.setText("Sincronizando datos...");
                sincronizarDatos();
            }
        });


    }


    public void obtenerImagenesProducto(){
        /*
        int id = R.drawable.me_dos;
        Resources res = getResources();
        Bitmap b = BitmapFactory.decodeResource(res, id);
        //nombre unico de la imagen
        String nombre = "001_" + sdf.format(new Date()) ;

        Boolean result = saveImageToInternalStorage(b, nombre);
        Log.d(TAG, "resultado guardar imagen ----->"+result);
        */
        /*
        File dir = getFilesDir();
        File file = new File(dir, "my_filename");
        boolean deleted = file.delete();
         */
        for (String nombre : getFilesDir().list()){
            File file = new File(getFilesDir(), nombre);
            boolean deleted = file.delete();
            Log.d(TAG, "Eliminando foto:"+nombre+" -> "+deleted);
        }

        List<ProductoImagen> listNombresImagenes = db.selectAllProductoImagen();
        String url = Comm.URL+ CommReq.CommReqGetProductImageFile;


        for (ProductoImagen p : listNombresImagenes){
            new DownloadImage().execute(url + p.getM_product_id(), p.getImg());
        }

        /*
        for (int i=0; i<5; i++){
            ProductoImagen p = listNombresImagenes.get(i);
            new DownloadImage().execute(url + p.getM_product_id(), p.getImg());
        }
        */

        //progressBar.setVisibility(View.INVISIBLE);
        //sincronizarBtn.setEnabled(true);
        //mensajeTextView.setText("Imagen agregada.");
        return;
    }

    public void sincronizarDatos (){


        String userId = db.selectUsuarioLogeado().getUserId().toString();

        /* ============================== || 1 ||========================================= */

        CommDelegateAndroid delegateClientes = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Clientes. Datos recibidos");
                Comm.CommResponse r = response;
                clienteList =  (ClienteList) r.getBean();
                if(clienteList!=null) {
                    Log.d(TAG, "Clientes. Tamaño lista " + clienteList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Clientes. Insertando registros");

                            //Si ya existe actualizar
                            if (db.countCliente() > 0) {
                                db.insertOrUpdateClienteList(clienteList.getList());
                            } else {
                                Log.d(TAG, "Se eliminará la tabla clientes...");
                                db.deleteCliente();
                                Log.d(TAG, "Eliminacion exitosa");
                                db.insertClienteLista(clienteList.getList(), getApplicationContext());
                            }
                            clienteList.getList().clear();

                            //insertar fecha ultima actualizacion cliente
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("CLIENT_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("CLIENT_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);

                            Log.d(TAG, "Clientes. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    //progressBar.setVisibility(View.INVISIBLE);

                                    //sincronizarBtn.setEnabled(true);
                                    //mensajeTextView.setText("Sincronización finalizada.");

                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }

                                }
                            });
                        }
                    }).start();
                }
            }
        };

        String ultmaActualizacionCliente="TODOS";
        Configuracion c = db.selectConfiguracionByClave("CLIENT_LAST_UPDATED");
        if (c.getValor() != null){
            ultmaActualizacionCliente = c.getValor();
        }

        HashMap mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllClients+"/"+ultmaActualizacionCliente, ClienteList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllClients+"/"+ultmaActualizacionCliente, new String[][]{
        }, delegateClientes);

        /* ============================== || 2 ||========================================= */

        CommDelegateAndroid delegateUsuarios = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess() {

                Log.d(TAG, "Usuarios. Datos recibidos");
                Comm.CommResponse r = response;
                usuarioList = (UsuarioList) r.getBean();
                if (usuarioList!=null) {
                    Log.d(TAG, "Usuarios. Tamaño lista " + usuarioList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Usuario. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Usuarios...");
                            db.deleteUsuario();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertUsuarioLista(usuarioList.getList(), getApplicationContext());

                            Log.d(TAG, "Usuarios. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();

                }
            }
        };
        new Comm().requestGet(CommReq.CommReqGetAllUsers, new String[][]{
        }, delegateUsuarios);

        /* ============================== || 3 ||========================================= */

        CommDelegateAndroid delegatePedidos = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Pedidos. Datos recibidos");
                Comm.CommResponse r = response;
                pedidoList =  (PedidoList) r.getBean();
                if(pedidoList !=null) {
                    Log.d(TAG, "Pedidos. Tamaño lista " + pedidoList.getList().size());

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "Pedidos. Insertando registros");

                        Log.d(TAG, "Se eliminará la tabla Pedidos y Detalles...");
                        //db.deletePedidoSinEstado();
                        //db.deletePedidoDetalle();
                        Log.d(TAG, "Eliminacion exitosa");

                        db.insertPedidoLista(pedidoList.getList(), getApplicationContext());
                        for (Pedido pedido : pedidoList.getList()){
                            //Log.d(TAG, "Insertando Pedido cabecera");
                            for (PedidoDetalle detalle : pedido.getDetalles()){
                                db.insertPedidoDetalle(detalle);
                            }
                        }
                        Log.d(TAG, "Pedidos. Registros insertados exitosamente");

                        //insertar fecha ultima actualizacion pedidos
                        Configuracion fechaActualizacion = new Configuracion();
                        fechaActualizacion.setClave("ORDER_LAST_UPDATED");
                        String now =  new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                        fechaActualizacion.setValor(now);

                        db.deleteConfiguracionByClave("ORDER_LAST_UPDATED");
                        db.insertConfiguracion(fechaActualizacion);

                        runOnUiThread(new Runnable() {
                            public void run() {
                                count++;
                                mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                if (count == getTotal()) {
                                    mensajeTextView.setText("Sincronización finalizada.");
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
                    }
                }).start();
                }
            }
        };



        String ultmaActualizacionPedidos="TODOS";
        Configuracion pedido_last_updated = db.selectConfiguracionByClave("ORDER_LAST_UPDATED");
        if (pedido_last_updated.getValor() != null){
            ultmaActualizacionPedidos = pedido_last_updated.getValor();
        }


        mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllOrders+"/"+ultmaActualizacionPedidos, PedidoList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllOrders+"/"+ultmaActualizacionPedidos, new String[][]{
                {"userId", userId
        }

        }, delegatePedidos);

        /* ============================== || 4 ||========================================= */

        CommDelegateAndroid delegateProductos = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Productos. Datos recibidos");
                Comm.CommResponse r = response;
                productoList =  (ProductoList) r.getBean();

                if(productoList!=null) {
                    Log.d(TAG, "Productos. Tamaño lista " + productoList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Productos. Insertando registros");

                            //Si ya existe actualizar
                            if (db.countProduct() > 0){
                                db.insertOrUpdateProductoList(productoList.getList());
                            }else{
                                Log.d(TAG, "Se eliminará la tabla Productos...");
                                db.deleteProducto();
                                Log.d(TAG, "Eliminacion exitosa");
                                db.insertProductoLista(productoList.getList(), getApplicationContext());
                                productoList.getList().clear();
                            }

                            Log.d(TAG, "Productos. Registros insertados exitosamente");

                            //insertar fecha ultima actualizacion precio_version
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("PRODUCTO_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("PRODUCTO_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        String ultmaActualizacionProductos="TODOS";
        Configuracion producto_last_updated = db.selectConfiguracionByClave("PRODUCTO_LAST_UPDATED");
        if (producto_last_updated.getValor() != null){
            ultmaActualizacionProductos = producto_last_updated.getValor();
        }

        mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllProduct+"/"+ultmaActualizacionProductos, ProductoList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllProduct+"/"+ultmaActualizacionProductos, new String[][]{
        }, delegateProductos);

        /* ============================== || 5 ||========================================= */

        CommDelegateAndroid delegatePrecioCategoria = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess() {

                Log.d(TAG, "PrecioCategoria. Datos recibidos");
                Comm.CommResponse r = response;
                precioCategoriaList = (PrecioCategoriaList) r.getBean();
                if (precioCategoriaList != null) {
                    Log.d(TAG, "PrecioCategoria. Tamaño lista " + precioCategoriaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "PrecioCategoria. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Usuarios...");
                            db.deletePrecioCategoria();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertPrecioCategoriaList(precioCategoriaList.getList());
                            Log.d(TAG, "PrecioCategoria. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();

                }
            }
        };
        new Comm().requestGet(CommReq.CommReqGetAllPrecioCategoria, new String[][]{
        }, delegatePrecioCategoria);

        /* ============================== || 6 ||========================================= */
        CommDelegateAndroid delegatePrecioVersion = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "PrecioVersion. Datos recibidos");
                Comm.CommResponse r = response;
                precioVersionList =  (PrecioVersionList) r.getBean();
                if(precioVersionList!=null) {
                    Log.d(TAG, "PrecioVersion. Tamaño lista " + precioVersionList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "PrecioVersion. Insertando registros");

                            //Si ya existe actualizar
                            if (db.countPrecioVersion() > 0) {
                                db.insertOrUpdatePrecioVersionList(precioVersionList.getList());
                            } else {
                                Log.d(TAG, "Se eliminará la tabla PrecioVersion...");
                                db.deletePrecioVersion();
                                Log.d(TAG, "Eliminacion exitosa");
                                db.insertPrecioVersionLista(precioVersionList.getList(), getApplicationContext());
                            }

                            //insertar fecha ultima actualizacion precio_version
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("PRECIO_VERSION_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("PRECIO_VERSION_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);

                            Log.d(TAG, "PrecioVersion. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        String ultmaActualizacionPrecioVersion="TODOS";

        Configuracion conf = db.selectConfiguracionByClave("PRECIO_VERSION_LAST_UPDATED");
        if (conf.getValor() != null){
            ultmaActualizacionPrecioVersion = conf.getValor();
        }

        mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllPrecioVersion+"/"+ultmaActualizacionPrecioVersion, PrecioVersionList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllPrecioVersion+"/"+ultmaActualizacionPrecioVersion, new String[][]{
        }, delegatePrecioVersion);

        /* ============================== || 7 ||========================================= */
        CommDelegateAndroid delegateRutas = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "RutasLocation. Datos recibidos");
                Comm.CommResponse r = response;
                rutaLocationList =  (RutaLocationList) r.getBean();
                if(rutaLocationList!=null) {
                    Log.d(TAG, "RutasLocation. Tamaño lista " + rutaLocationList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "RutasLocation. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla RutasLocation...");
                            db.deleteRutaLocation();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertRutaLocationLista(rutaLocationList.getList(), getApplicationContext());
                            Log.d(TAG, "RutasLocation. Registros insertados exitosamente");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };
        String fechahoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        //String fechahoy = "2016-03-01";

        userId = db.selectUsuarioLogeado().getUserId().toString();
        HashMap mapClaseResponseRoutes = new HashMap();
        mapClaseResponseRoutes.put(CommReq.CommReqGetAllRoutes+"/"+userId, RutaLocationList.class.getName());
        new Comm(mapClaseResponseRoutes).requestGet(CommReq.CommReqGetAllRoutes+"/"+userId, new String[][]{
        }, delegateRutas);

        /* ============================== || 8 ||========================================= */
        CommDelegateAndroid delegateCobranzas = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Cobranza. Datos recibidos");
                Comm.CommResponse r = response;
                cobranzaList =  (CobranzaList) r.getBean();
                if(cobranzaList!=null) {
                    Log.d(TAG, "Cobranza. Tamaño lista " + cobranzaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Cobranza. Insertando registros");

                            //Si ya existe actualizar
                            if (db.countCobranza() > 0) {
                                db.insertOrUpdateCobranzaList(cobranzaList.getList());
                            } else {
                                Log.d(TAG, "Se eliminará la tabla Cobranza...");
                                db.deleteCobranza();
                                Log.d(TAG, "Eliminacion exitosa");
                                db.insertCobranzaLista(cobranzaList.getList(), getApplicationContext());
                                Log.d(TAG, "Cobranza. Registros insertados exitosamente");
                            }

                            //insertar fecha ultima actualizacion Cobranza
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("COBRANZA_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("COBRANZA_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        String ultmaActualizacionCobros="TODOS";

        Configuracion confCobranza = db.selectConfiguracionByClave("COBRANZA_LAST_UPDATED");
        if (confCobranza.getValor() != null){
            ultmaActualizacionCobros = confCobranza.getValor();
        }

        mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllCobros+"/"+ultmaActualizacionCobros, CobranzaList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllCobros +"/"+ultmaActualizacionCobros, new String[][]{
        }, delegateCobranzas);


        /* ============================== || 9 ||========================================= */
        CommDelegateAndroid delegateFacturas = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Factura. Datos recibidos");
                Comm.CommResponse r = response;
                facturaList =  (FacturaList) r.getBean();
                if(facturaList!=null) {
                    Log.d(TAG, "Factura. Tamaño lista " + facturaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Factura. Insertando registros");

                            //Si ya existe actualizar
                            if (db.countFactura() > 0) {
                                db.insertOrUpdateFacturaList(facturaList.getList());
                            } else {
                                Log.d(TAG, "Se eliminará la tabla Factura...");
                                db.deleteFactura();
                                Log.d(TAG, "Eliminacion exitosa");
                                db.insertFacturaLista(facturaList.getList(), getApplicationContext());
                            }

                            Log.d(TAG, "Factura. Registros insertados exitosamente");

                            //insertar fecha ultima actualizacion factura
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("FACTURA_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("FACTURA_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);

                            runOnUiThread(new Runnable() {
                                public void run() {

                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        String ultmaActualizacionFactura="TODOS";

        Configuracion confFactura = db.selectConfiguracionByClave("FACTURA_LAST_UPDATED");
        if (confFactura.getValor() != null){
            ultmaActualizacionFactura = confFactura.getValor();
        }

        mapClaseResponse = new HashMap();
        mapClaseResponse.put(CommReq.CommReqGetAllFacturas+"/"+ultmaActualizacionFactura, FacturaList.class.getName());

        new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllFacturas+"/"+ultmaActualizacionFactura, new String[][]{
        }, delegateFacturas);

        /* ============================== || 10 ||========================================= */
        CommDelegateAndroid delegateProductoFamilia = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Producto Familia. Datos recibidos");
                Comm.CommResponse r = response;
                productoFamiliaList =  (ProductoFamiliaList) r.getBean();
                if(productoFamiliaList!=null) {
                    Log.d(TAG, "Producto Familia. Tamaño lista " + productoFamiliaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Producto Familia. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Producto Familia...");
                            db.deleteProductoFamilia();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertProductoFamiliaLista(productoFamiliaList.getList(), getApplicationContext());
                            Log.d(TAG, "Producto Familia. Registros insertados exitosamente");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };
        if (catalogoCheckbox.isChecked() ) {
            new Comm().requestGet(CommReq.CommReqGetAllProductFamily, new String[][]{
            }, delegateProductoFamilia);
        }

        /* ============================== || 11 ||========================================= */
        CommDelegateAndroid delegateProductoSubFamilia = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Producto Sub Familia. Datos recibidos");
                Comm.CommResponse r = response;
                productoSubFamiliaList =  (ProductoSubFamiliaList) r.getBean();
                if(productoSubFamiliaList!=null) {
                    Log.d(TAG, "Producto Sub Familia. Tamaño lista " + productoSubFamiliaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Producto Sub Familia. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Producto Sub Familia...");
                            db.deleteProductoSubFamilia();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertProductoSubFamiliaLista(productoSubFamiliaList.getList(), getApplicationContext());
                            Log.d(TAG, "Producto Sub Familia. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };
        if (catalogoCheckbox.isChecked() ) {
            new Comm().requestGet(CommReq.CommReqGetAllProductSubFamily, new String[][]{
            }, delegateProductoSubFamilia);
        }

        /* ============================== || 12 ||========================================= */
        CommDelegateAndroid delegateProductoImagen = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Producto Imagenes. Datos recibidos");
                Comm.CommResponse r = response;
                productoImagenList =  (ProductoImagenList) r.getBean();
                if(productoImagenList!=null) {
                    Log.d(TAG, "Producto Imagenes. Tamaño lista " + productoImagenList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Producto Imagenes. Insertando registros");
                            Log.d(TAG, "Se eliminará la tabla Producto Imagenes...");
                            //db.deleteProductoImagen();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertProductoImagenLista(productoImagenList.getList(), getApplicationContext());
                            Log.d(TAG, "Producto Imagenenes. Registros insertados exitosamente");

                            //insertar fecha ultima actualizacion producto imagen
                            Configuracion fechaActualizacion = new Configuracion();
                            fechaActualizacion.setClave("PRODUCTO_IMG_LAST_UPDATED");
                            String now = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date());
                            fechaActualizacion.setValor(now);

                            db.deleteConfiguracionByClave("PRODUCTO_IMG_LAST_UPDATED");
                            db.insertConfiguracion(fechaActualizacion);


                            runOnUiThread(new Runnable() {
                                public void run() {

                                    obtenerImagenesProducto();

                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        if (catalogoCheckbox.isChecked() ){

            String ultmaActualizacionProductImg="TODOS";
            Configuracion confProductoImagen = db.selectConfiguracionByClave("PRODUCTO_IMG_LAST_UPDATED");
            if (confProductoImagen.getValor() != null){
                ultmaActualizacionProductImg = confProductoImagen.getValor();
            }
            mapClaseResponse = new HashMap();
            mapClaseResponse.put(CommReq.CommReqGetAllProductImages+"/"+ultmaActualizacionProductImg, ProductoImagenList.class.getName());

            new Comm(mapClaseResponse).requestGet(CommReq.CommReqGetAllProductImages+"/"+ultmaActualizacionProductImg, new String[][]{
            }, delegateProductoImagen);
        }

        /* ============================== || 13 ||========================================= */
        CommDelegateAndroid delegateRegistroVisita = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Registro Visita Datos recibidos");
                Comm.CommResponse r = response;
                registroVisitaList =  (RegistroVisitaList) r.getBean();
                if(registroVisitaList!=null) {
                    Log.d(TAG, "Registro Visita. Tamaño lista " + registroVisitaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Registro Visita. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Registro Visita...");
                            db.deleteRegistroVisita();
                            Log.d(TAG, "Eliminacion exitosa");

                            db.insertRegistroVisitaLista(registroVisitaList.getList(), getApplicationContext());

                            Log.d(TAG, "Registro Visita. Registros insertados exitosamente");

                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }

            }
        };
        userId = db.selectUsuarioLogeado().getUserId().toString();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, -31);
        String ayer = new SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());
        String hoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());


        new Comm().requestGet(CommReq.CommReqGetRegistroVisita, new String[][]{
                {"userId", userId},
                {"fechaDesde", ayer},
                {"fechaHasta", hoy},

        }, delegateRegistroVisita);

        /* ============================== || 14 ||========================================= */
        CommDelegateAndroid delegateEntrega = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.INVISIBLE);
                AppUtils.handleError(this.exception.getMessage(), SincronizarActivity.this);
                sincronizarBtn.setEnabled(true);
            }

            @Override
            public void onSuccess(){

                Log.d(TAG, "Entrega. Datos recibidos");
                Comm.CommResponse r = response;
                entregaList =  (EntregaList) r.getBean();
                if(entregaList!=null) {
                    Log.d(TAG, "Entrega. Tamaño lista " + entregaList.getList().size());

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Entrega. Insertando registros");

                            Log.d(TAG, "Se eliminará la tabla Entrega...");
                            db.deleteEntrega();
                            Log.d(TAG, "Eliminacion exitosa");
                            db.insertEntregaLista(entregaList.getList(), getApplicationContext());
                            Log.d(TAG, "Entrega. Registros insertados exitosamente");
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    count++;
                                    mensajeTextView.setText("Sincronizando " + count+"/"+getTotal()+"");
                                    if (count == getTotal()) {
                                        mensajeTextView.setText("Sincronización finalizada.");
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                }
                            });
                        }
                    }).start();
                }
            }
        };

        new Comm().requestGet(CommReq.CommReqGetAllEntrega, new String[][]{{"userId", userId}
        }, delegateEntrega);


    }

    public int getTotal () {
        int TOTAL = 10; //10 servicios

        //si esta marcado catalogo son 4 servicios extras, lista imagenes, familia, subfamilia, los archivos
        if (catalogoCheckbox.isChecked())
            TOTAL = TOTAL + 4;

        return TOTAL;
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
                //finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public boolean saveImageToInternalStorage(Bitmap image, String name) {
        try {
            // Use the compress method on the Bitmap object to write image to
            // the OutputStream
            FileOutputStream fos = getApplicationContext().openFileOutput(name, Context.MODE_PRIVATE);

            // Writing the bitmap to the output stream
            image.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            //image.com
            fos.flush();
            fos.close();
            return true;
        } catch (Exception e) {
            Log.e(TAG, "ERROR al insertar imagen");
            e.printStackTrace();
            return false;
        }
    }


    public class DownloadImage extends AsyncTask<String, Integer, HashMap> {
        String url;

        @Override
        protected HashMap doInBackground(String... arg0) {
            // This is done in a background thread
            return downloadImage(arg0[0], arg0[1]);
        }

        /**
         * Called after the image has been downloaded
         * -> this calls a function on the main thread again
         */
        protected void onPostExecute(HashMap result){
            if (result != null){
                Bitmap image = (Bitmap) result.get("bitmap");
                String nombreArchivo = (String) result.get("nombreArchivo");
                Log.d(TAG, "guardada imagen-> "+nombreArchivo );
                saveImageToInternalStorage(image, nombreArchivo);
            }

        }

        /**
         * Actually download the Image from the _url
         * @param _url
         * @return
         */
        private HashMap downloadImage(String _url, String nombreArchivo)
        {
            this.url = _url.toString();
            //Prepare to download image
            URL url;
            InputStream in;
            BufferedInputStream buf;

            //BufferedInputStream buf;
            try {
                Log.d(TAG, "invocar URL->"+_url);

                url = new URL(_url);
                in = url.openStream();

                // Read the inputstream
                buf = new BufferedInputStream(in);

                // Convert the BufferedInputStream to a Bitmap
                Bitmap bMap = BitmapFactory.decodeStream(buf);
                //bMap.compress(Bitmap.CompressFormat.JPEG, )
                if (in != null) {
                    in.close();
                }
                if (buf != null) {
                    buf.close();
                }
                Log.d(TAG, "archivo obtenido correctamente, width->"+bMap.getWidth()+"x"+bMap.getHeight());

                HashMap result = new HashMap();
                result.put("bitmap", bMap);
                result.put("nombreArchivo", nombreArchivo);
                return result;
            } catch (Exception e) {
                Log.e("Error reading file", e.toString());
            }
            return null;
        }

    }


}
