package py.multipartesapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.LocatorDTO;
import py.multipartesapp.beans.PrecioCategoria;
import py.multipartesapp.beans.PrecioVersion;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.StockDTO;
import py.multipartesapp.beans.StockList;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.customAutoComplete.ConsultaStockActivityProductoTextChangedListener;
import py.multipartesapp.customAutoComplete.CustomAutoCompleteView;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 08/11/2016.
 */
public class ConsultaStockActivity extends ActionBarActivity {
    public static final String TAG = ConsultaStockActivity.class.getSimpleName();

    private AppDatabase db = new AppDatabase(this);

    private Producto productoSeleccionado;

    private TextView codigoProductoTxtView;
    private TextView precioProductoTxtView;
    private TextView stockProductoTxtView;

    public String[] itemsProductos = new String[] {"Buscar por nombre..."};
    public CustomAutoCompleteView productoAutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> productoAdapter;
    private List<Producto> productosFiltrados;

    private ImageView vistaPreviaImgView;

    public ImageAdapter stockListAdapter;

    private StockList stockList = new StockList();

    private ListView stockListView;

    private TextView idProductoTextView;

    private TextView precioPublicoTextView;
    private TextView precioMayoristaTextView;




    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_stock);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consulta Producto");

        idProductoTextView=(TextView) findViewById(R.id.id_producto);

        productoAutoComplete = (CustomAutoCompleteView) findViewById(R.id.consulta_stock_producto_autocomplete);

        //verCatalogoBtn = (Button) findViewById(R.id.pedido_detalle_nuevo_catalogo_btn);
        vistaPreviaImgView = (ImageView) findViewById(R.id.agregar_detalle_pedido_vista_previa);

        precioPublicoTextView=(TextView) findViewById(R.id.precio_publico);
        precioMayoristaTextView=(TextView) findViewById(R.id.precio_mayorista);

        /*
        verCatalogoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick (View v){

                Globals.setCatalogoDesdePedido(true);
                Intent intent = new Intent(ConsultaStockActivity.this, ListProductosImagenesActivity.class);
                //intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);
            }
        }); */

        stockListView=(ListView) findViewById(R.id.stock_list);

        StockDTO stockDTO= new StockDTO();
        final Producto producto = new Producto();
        producto.setCodinterno("");
        producto.setM_product_id(0);
        producto.setName("");
        stockDTO.setProducto(producto);
        LocatorDTO locatorDTO = new LocatorDTO();
        locatorDTO.setM_locator_id("");

        locatorDTO.setM_locator_value("");
        stockDTO.setLocator(locatorDTO);
        stockDTO.setStock_disponible(0);

        List<StockDTO> listStock= new ArrayList<>();
        listStock.add(stockDTO);
        stockList.setList(listStock);

        stockListAdapter=new ImageAdapter(this);
        stockListView.setAdapter(stockListAdapter);


        if (db.countProduct()== 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }


        // add the listener so it will tries to suggest while the user types
        productoAutoComplete.addTextChangedListener(new ConsultaStockActivityProductoTextChangedListener(this));

        // set our adapter
        productoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsProductos);
        productoAutoComplete.setAdapter(productoAdapter);

        //al seleccionar un producto de la lista filtrada
        productoAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                //productoSeleccionado = productosFiltrados.get(position);

                productoSeleccionado=productosFiltrados.get(0);

                idProductoTextView.setText(""+productoSeleccionado.getM_product_id());
                calcularPrecioProducto();

                //obtener el stock del producto haciendo la llamada al servicio stock-productos
                CommDelegateAndroid delegate = new CommDelegateAndroid(){
                    @Override
                    public void onError(){
                        Log.e(TAG, this.exception.getMessage());
                    }
                    @Override
                    public void onSuccess(){
                        Log.d(TAG, "Datos de producto");
                        Comm.CommResponse r = response;

                        stockList.getList().clear();
                        stockList=(StockList) r.getBean();

                        //Actualizamos el adapter de la lista de Stock
                        stockListAdapter.notifyDataSetChanged();
                    }
                };

                new Comm().requestGet(Comm.URL, CommReq.CommReqGetStockProducto, new String[][]{
                        {"codigo_producto",productoSeleccionado.getM_product_id().toString()}
                }, delegate);

                
//                //pedidoLinearLayout.setVisibility(View.VISIBLE);
//                codigoProductoTxtView.setText(productoSeleccionado.getCodinterno());
//                if (productoSeleccionado.getPrice()!= null)
//                    precioProductoTxtView.setText(productoSeleccionado.getPrice().toString());
//                if (productoSeleccionado.getStock() != null)
//                    stockProductoTxtView.setText(productoSeleccionado.getStock().toString());
            }
        });



        //boton limpiar texto producto
        productoAutoComplete.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = productoAutoComplete.getRight()
                            - productoAutoComplete.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        productoAutoComplete.setText("");
                        limpiarListViewStock();
//                        codigoProductoTxtView.setText("");
//                        precioProductoTxtView.setText("");
//                        stockProductoTxtView.setText("");
                        return true;
                    }
                }
                return false;
            }
        });
    }

    public void limpiarListViewStock(){
        stockList.getList().clear();

        stockListAdapter.notifyDataSetChanged();
    }


    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };



    public Integer calcularPrecioProducto (){


        //1. Obtener precio categoria del cliente
//        PrecioCategoria precioCategoria = db.selectPrecioCategoriaById(Globals.clienteSeleccionadoPedido.getCategoria_precio());

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
        DecimalFormat dfFormat = new DecimalFormat("###,###");

        precioPublicoTextView.setText(""+dfFormat.format(precioVersion.getPrecio_publico().intValue()));
        precioMayoristaTextView.setText(""+dfFormat.format(precioVersion.getPrecio_mayorista_a()));


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

    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getProductosFiltrados(String searchTerm){
        // add itemsClientes on the array dynamically
        productosFiltrados = db.selectProductByNombreOrCodigo(searchTerm);
        int rowCount = productosFiltrados.size();
        String[] item = new String[rowCount];
        int i = 0;
        for (Producto c : productosFiltrados) {
            item[i] = c.getCodinterno() + "-" + c.getName();
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
            return stockList.getList().size();
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

            StockDTO item = stockList.getList().get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_registro_generico, viewGroup, false);
                //v.setTag(R.id.img_places, v.findViewById(R.id.img_places));
                v.setTag(R.id.txt1, v.findViewById(R.id.txt1));
                v.setTag(R.id.txt2, v.findViewById(R.id.txt2));
//                v.setTag(R.id.icon, v.findViewById(R.id.icon));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1);
            if(item.getLocator()!=null){
                titleTextView.setText("Depósito: "+item.getLocator().getM_locator_value());
            }

            if(item.getProducto()!=null){

            }


            TextView subTitleTextView = (TextView) v.findViewById(R.id.txt2);
            subTitleTextView.setText("Cant. Disponible: "+item.getStock_disponible());


            return v;
        }
    }


}
