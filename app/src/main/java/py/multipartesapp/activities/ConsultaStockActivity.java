package py.multipartesapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import py.multipartes2.R;
import py.multipartes2.beans.PrecioCategoria;
import py.multipartes2.beans.PrecioVersion;
import py.multipartes2.beans.Producto;
import py.multipartes2.customAutoComplete.ConsultaStockActivityProductoTextChangedListener;
import py.multipartes2.customAutoComplete.CustomAutoCompleteView;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.Globals;

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


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_stock);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consulta Producto");


        productoAutoComplete = (CustomAutoCompleteView) findViewById(R.id.consulta_stock_producto_autocomplete);
        codigoProductoTxtView = (TextView) findViewById(R.id.consuta_stock_codigo);
        precioProductoTxtView = (TextView) findViewById(R.id.consuta_stock_precio);
        stockProductoTxtView = (TextView) findViewById(R.id.consuta_stock_detalle_stock);

        //verCatalogoBtn = (Button) findViewById(R.id.pedido_detalle_nuevo_catalogo_btn);
        vistaPreviaImgView = (ImageView) findViewById(R.id.agregar_detalle_pedido_vista_previa);

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

        if (db.countProduct()== 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }


        // add the listener so it will tries to suggest while the user types
        productoAutoComplete.addTextChangedListener(new ConsultaStockActivityProductoTextChangedListener(this));

        // set our adapter
        productoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsProductos);
        productoAutoComplete.setAdapter(productoAdapter);

        //al seleccionar un cliente de la lista filtrada
        productoAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                productoSeleccionado = productosFiltrados.get(position);
                //pedidoLinearLayout.setVisibility(View.VISIBLE);
                codigoProductoTxtView.setText(productoSeleccionado.getCodinterno());
                if (productoSeleccionado.getPrice()!= null)
                    precioProductoTxtView.setText(productoSeleccionado.getPrice().toString());
                if (productoSeleccionado.getStock() != null)
                    stockProductoTxtView.setText(productoSeleccionado.getStock().toString());
            }
        });


        //boton limpiar texto
        //boton limpiar texto cliente
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
                        codigoProductoTxtView.setText("");
                        precioProductoTxtView.setText("");
                        stockProductoTxtView.setText("");
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
        productosFiltrados = db.selectProductByNombre(searchTerm);
        int rowCount = productosFiltrados.size();
        String[] item = new String[rowCount];
        int i = 0;
        for (Producto c : productosFiltrados) {
            item[i] = c.getName();
            i++;
        }
        return item;
    }


}
