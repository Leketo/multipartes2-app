package py.multipartesapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.LocatorDTO;
import py.multipartesapp.beans.PedidoDetalle;
import py.multipartesapp.beans.PrecioCategoria;
import py.multipartesapp.beans.PrecioVersion;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.StockDTO;
import py.multipartesapp.beans.StockList;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 18/10/2015.
 */
public class PedidoDetalleNuevoActivity extends ActionBarActivity {
    public static final String TAG = PedidoDetalleNuevoActivity.class.getSimpleName();

    private AppDatabase db = new AppDatabase(this);
    private Button agregar_detalle_pedido;
    private Producto productoSeleccionado;
    private AutoCompleteTextView productoTextView;
    private TextView codigoProductoTxtView;
    private TextView codigoSistemaTxtView;
    private TextView precioProductoTxtView;
    private TextView stockProductoTxtView;
    private EditText cantidadTxtView;
    private Button verCatalogoBtn;

    private ImageView vistaPreviaImgView;


    private List<StockDTO> listStock= new ArrayList<>();


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedido_detalle_nuevo);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Agregar detalle");

        agregar_detalle_pedido = (Button) findViewById(R.id.agregar_detalle_pedido);
        productoTextView = (AutoCompleteTextView) findViewById(R.id.pedido_detalle_nuevo_producto);
        codigoProductoTxtView = (TextView) findViewById(R.id.pedido_nuevo_detalle_codigo);
        precioProductoTxtView = (TextView) findViewById(R.id.pedido_nuevo_detalle_precio);
        stockProductoTxtView = (TextView) findViewById(R.id.pedido_nuevo_detalle_stock);
        cantidadTxtView = (EditText) findViewById(R.id.pedido_nuevo_detalle_cantidad);
        verCatalogoBtn = (Button) findViewById(R.id.pedido_detalle_nuevo_catalogo_btn);
        vistaPreviaImgView = (ImageView) findViewById(R.id.agregar_detalle_pedido_vista_previa);

        verCatalogoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

                Globals.setCatalogoDesdePedido(true);
                Intent intent = new Intent(PedidoDetalleNuevoActivity.this, ListProductosImagenesActivity.class);
                //intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        });


        List<Producto> lista_productos = db.selectAllProducto();
        Log.d(TAG, "Cantidad de productos: " +lista_productos.size());

        ArrayAdapter<Producto> adapterProductos = new ArrayAdapter<Producto>(this, android.R.layout.simple_spinner_item, lista_productos);

        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productoTextView.setAdapter(adapterProductos);

        agregar_detalle_pedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarDetalle();
            }
        });

        productoTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //productoTextView.clearFocus();
                productoSeleccionado = (Producto) parent.getAdapter().getItem(position);

                codigoProductoTxtView.setText(productoSeleccionado.getCodinterno());
                Integer precio = calcularPrecioProducto();
                precioProductoTxtView.setText(precio.toString());

                int sumaAsu=0;
                int sumaSucursales=0;

                if(productoSeleccionado.getM_product_id()!=null){
                    obtenerStockArticulo();

                    /*
                    for(StockDTO stockDTO: listStock){
                        if(stockDTO.getLocator().getM_locator_id().equalsIgnoreCase("ASU")){
                            sumaAsu=stockDTO.getStock_disponible();
                            Log.d(TAG, "llego stock en ASU: " +sumaAsu);
                        }else{
                            sumaSucursales=stockDTO.getStock_disponible();
                        }
                    }

                    // Setear el stock, solo el de ASU para validar al guardar. adolfo 24-02-2019
                    Log.d(TAG, "setear stock producto seleccionado: " +sumaAsu);
                    productoSeleccionado.setStock(sumaAsu);
                    */
                }

            }
        });



        //boton limpiar texto
        productoTextView.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = productoTextView.getRight()
                            - productoTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        productoTextView.setText("");
                        productoSeleccionado = null;
                        codigoProductoTxtView.setText("");
                        precioProductoTxtView.setText("");
                        stockProductoTxtView.setText("");
                        Globals.setProductoSeleccionadoCatalogo(null);
                        return true;
                    }
                }
                return false;
            }
        });
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };


    public void obtenerStockArticulo(){
        StockDTO stockDTO= new StockDTO();

        listStock= new ArrayList<>();

        //obtener el stock del producto haciendo la llamada al servicio stock-productos
//        CommDelegateAndroid delegate = new CommDelegateAndroid(){
//            @Override
//            public void onError(){
//                Log.e(TAG, this.exception.getMessage());
//            }
//            @Override
//            public void onSuccess(){
//                Log.d(TAG, "Datos de producto");
//                Comm.CommResponse r = response;
//
//                StockList stockList=(StockList) r.getBean();
//
//                listStock=stockList.getList();
//
//                procesarStock(listStock);
//
//            }
//        };
//
//        new Comm().requestGet(Comm.URL, CommReq.CommReqGetStockProducto, new String[][]{
//                {"codigo_producto",productoSeleccionado.getM_product_id().toString()}
//        }, delegate);

        //obtenemos el stock consultando a la tabla de Stock

        listStock=db.selectStockPorProducto(productoSeleccionado.getM_product_id().toString());
        procesarStock(listStock);

    }

    private void procesarStock(List<StockDTO> listStock){
        int sumaAsu=0;
        int sumaSucursales=0;
        for(StockDTO stockDTO: listStock){
            //TODO verificar por sucursal
            if(stockDTO.getLocator().getM_locator_value().contains("ASU")){
                sumaAsu=sumaAsu+stockDTO.getStock_disponible();
            }else{
                sumaSucursales=sumaSucursales+stockDTO.getStock_disponible();
            }
        }
        Log.d(TAG, "setear stock producto seleccionado ASU: " +sumaAsu);
        productoSeleccionado.setStock(sumaAsu);

        stockProductoTxtView.setText("ASU:"+sumaAsu+" / SUC.:"+sumaSucursales);
    }

    private void agregarDetalle (){
        //validar producto
        if (productoSeleccionado == null || productoTextView.getText().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un producto", buttons, PedidoDetalleNuevoActivity.this, false, dialogOnclicListener);
            //guardarVisitaBtn.setEnabled(true);
            return;
        }

        //verificar que producto no exista ya en el pedido
        for (PedidoDetalle detalle : PedidoActivity.detallesList){
            if (detalle.getProduct_id().equals(productoSeleccionado.getM_product_id())){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "El producto ya existe en el Pedido.", buttons, PedidoDetalleNuevoActivity.this, false, dialogOnclicListener);
                //guardarVisitaBtn.setEnabled(true);
                return;
            }
        }

        //validar cantidad
        if (cantidadTxtView == null || cantidadTxtView.getText().toString().equals("") || cantidadTxtView.getText().toString().equals("0")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Ingrese una cantidad válida", buttons, PedidoDetalleNuevoActivity.this, false, dialogOnclicListener);
            //guardarVisitaBtn.setEnabled(true);
            return;
        } else if (Integer.valueOf(cantidadTxtView.getText().toString()) > Integer.valueOf(productoSeleccionado.getStock())){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "No hay stock suficiente para la cantidad ingresada", buttons, PedidoDetalleNuevoActivity.this, false, dialogOnclicListener);
            //guardarVisitaBtn.setEnabled(true);
            return;
        }

        Integer precio = calcularPrecioProducto();

        PedidoDetalle nuevoDetalle = new PedidoDetalle ();
        nuevoDetalle.setPrice(precio);

        //nuevoDetalle.setPrice(productoSeleccionado.getPrice());
        nuevoDetalle.setIsactive("Y");
        nuevoDetalle.setProduct_id(productoSeleccionado.getM_product_id());
        nuevoDetalle.setQuantity(Integer.valueOf(cantidadTxtView.getText().toString()));

        Integer total = Integer.valueOf(cantidadTxtView.getText().toString()) * nuevoDetalle.getPrice();
        nuevoDetalle.setTotal(total);

        Globals.setNuevoPedidoDetalle(nuevoDetalle);

        Globals.setProductoSeleccionadoCatalogo(null);
        finish();
        return;
    }

    public Integer calcularPrecioProducto (){


        //1. Obtener precio categoria del cliente
        PrecioCategoria precioCategoria = db.selectPrecioCategoriaById(Globals.clienteSeleccionadoPedido.getCategoria_precio());

        //2. obtener precio version
        PrecioVersion precioVersion = db.selectPrecioVersionByProducto( productoSeleccionado.getM_product_id());

        Integer precio = 0;
        /*
        --1000581 inicial octubre
        --1000582 abit noviembre
        --1000587 ventas inicial
        --1010590 costo inicial
        --1010591 precio publico
        --1010596 mayorista a
        --1010597 mayorista b
        --1010598 precio lista
        --1010599 vidrieros
        --130320000 radiadoristas
        */
        //3. Sacar el precio a usar segun categoria
        switch (precioCategoria.getM_pricelist_id()){
            case 1000587:
                precio = precioVersion.getPrecio_ventas_inicial();
                break;
            case 1010590:
                precio = precioVersion.getPrecio_costo_inicial();
                break;
            case 1010591:
                precio = precioVersion.getPrecio_publico();
                break;
            case 1010596:
                precio = precioVersion.getPrecio_mayorista_a();
                break;
            case 1010597:
                precio = precioVersion.getPrecio_mayorista_b();
                break;
            case 1010598:
                precio = precioVersion.getPrecio_lista();
                break;
            case 1010599:
                precio = precioVersion.getPrecio_vidrieros();
                break;
            case 130320000:
                precio = precioVersion.getPrecio_radiadoritas();
                break;
        }

        if (precio == null){
            precio = 0;
        }
        return precio;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        //si selecciono un pedido de catalogo
        if (Globals.getProductoSeleccionadoCatalogo() != null && Globals.getProductoSeleccionadoCatalogo().getM_product_id() != null){
            productoSeleccionado = Globals.getProductoSeleccionadoCatalogo();

            codigoProductoTxtView.setText(productoSeleccionado.getCodinterno());
            Integer precio = calcularPrecioProducto();
            precioProductoTxtView.setText(precio.toString());

            //obtenemos el stock de los articulos que no sean de la sucursal ASU




            stockProductoTxtView.setText(productoSeleccionado.getStock().toString()+"");
            productoTextView.setText(productoSeleccionado.toString());

            if (Globals.imagenSeleccionadaCatalogo != null){
                vistaPreviaImgView.setImageBitmap(Globals.imagenSeleccionadaCatalogo);
            }
        }
        Globals.setCatalogoDesdePedido(false);
        Globals.setProductoSeleccionadoCatalogo(null);
        super.onResume();
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

}
