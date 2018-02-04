package py.multipartesapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import py.multipartesapp.R;
import py.multipartesapp.beans.Producto;
import py.multipartesapp.beans.ProductoFamilia;
import py.multipartesapp.beans.ProductoImagen;
import py.multipartesapp.beans.ProductoSubFamilia;
import py.multipartesapp.beans.RutaLocation;
import py.multipartesapp.beans.TipoVisita;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.db.AppContract;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 11/03/2016.
 */
public class ListProductosImagenesActivity extends ActionBarActivity {
    public static final String TAG = ListProductosImagenesActivity.class.getSimpleName();

    private List<ProductoImagen> listIdsProductos = new ArrayList<ProductoImagen>();
    private ImageAdapter adapter;
    private Button verCatalogoCompletoBtn;
    private ListView listProductListView;
    private EditText myFilterEditText;

    private Spinner familiaSpinner;
    private Spinner subFamiliaSpinner;
    private ProductoFamilia familiaSeleccionada;
    private ProductoSubFamilia subFamiliaSeleccionada;

    private AppDatabase db = new AppDatabase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_productos_img);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Catalogo");

        listProductListView = (ListView) findViewById(R.id.productos_imagenes_list);
        myFilterEditText = (EditText) findViewById(R.id.productos_imagenes_myFilter);
        verCatalogoCompletoBtn = (Button) findViewById(R.id.productos_imagenes_btn);
        familiaSpinner = (Spinner) findViewById(R.id.productos_imagenes_spinner_familia);
        subFamiliaSpinner = (Spinner) findViewById(R.id.productos_imagenes_spinner_sub_familia);


        List<ProductoFamilia> listProductoFamilia = new ArrayList<ProductoFamilia>();
        ProductoFamilia todos = new ProductoFamilia();
        todos.setDescription("TODOS");
        listProductoFamilia.add(todos);

        listProductoFamilia.addAll(db.selectAllProductoFamilia());

        ArrayAdapter<ProductoFamilia> familiaAdapter = new ArrayAdapter<ProductoFamilia>
                (this, android.R.layout.simple_dropdown_item_1line, listProductoFamilia);
        familiaSpinner.setAdapter(familiaAdapter);


        familiaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                familiaSeleccionada = (ProductoFamilia) parent.getAdapter().getItem(position);
                //si selecciono TODOS
                if (familiaSeleccionada.getM_product_family_id() == null){
                    Log.d(TAG, "obtenes TODOS productos imagenes");
                    filtrar(null, null, null);
                    List<ProductoSubFamilia> listProductoSubFamilia = new ArrayList<ProductoSubFamilia>();
                    ArrayAdapter<ProductoSubFamilia> subFamiliaAdapter = new ArrayAdapter<ProductoSubFamilia>
                            (ListProductosImagenesActivity.this, android.R.layout.simple_dropdown_item_1line, listProductoSubFamilia);
                    subFamiliaSpinner.setAdapter(subFamiliaAdapter);
                } else {
                    List<ProductoSubFamilia> listProductoSubFamilia = db.selectProductoSubFamiliaByIdFamilia(familiaSeleccionada.getM_product_family_id());
                    ArrayAdapter<ProductoSubFamilia> subFamiliaAdapter = new ArrayAdapter<ProductoSubFamilia>
                            (ListProductosImagenesActivity.this, android.R.layout.simple_dropdown_item_1line, listProductoSubFamilia);
                    subFamiliaSpinner.setAdapter(subFamiliaAdapter);
                }

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No selecciono ninguna familia.");
            }
        });

        subFamiliaSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Log.d(TAG, "On change subfamilia -> position" + position);
                subFamiliaSeleccionada = (ProductoSubFamilia) parent.getAdapter().getItem(position);

                filtrar(familiaSeleccionada.getM_product_family_id(), subFamiliaSeleccionada.getId(), null);
                //listIdsProductos = db.selectProductoImagenByNombre(familiaSeleccionada.getM_product_family_id(),
                //        subFamiliaSeleccionada.getId(), null);
                //adapter.notifyDataSetChanged();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No selecciono ninguna familia.");
            }
        });

        /*
        File files = getFilesDir();
        Set<String> listIdsProductosHash = new HashSet<String>();
        for (String name : fileList()){
            if (name.contains("_")) {
                String[] temp = name.split("_");
                String idProducto = temp[0];
                listIdsProductosHash.add(idProducto);
            }
        }
        */
        listIdsProductos = new ArrayList<ProductoImagen>();
        adapter = new ImageAdapter (this);
        listProductListView.setAdapter(adapter);

        listProductListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "On Item Long click en list");
                return false;
            }
        });

        listProductListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ProductoImagen productoImagen = listIdsProductos.get(position);

                List<ProductoImagen> listImagenes = new ArrayList<ProductoImagen>();
                listImagenes.add(productoImagen);

                //Globals.setNombresImagenes(listImagenes);
                Globals.setNombresImagenes(listIdsProductos);

                Intent intent = new Intent(ListProductosImagenesActivity.this, ScreenSlidePagerActivity.class);
                startActivity(intent);
            }
        });

        verCatalogoCompletoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listIdsProductos = db.selectAllProductoImagen();
                List<String> nombresArchivo = new ArrayList<String>();
                for (ProductoImagen p : listIdsProductos){
                    //nombresArchivo.add(p.getNombreArchivo());
                }
                //Globals.setNombresImagenes(nombresArchivo);
                if (listIdsProductos.size() > 0){
                    Intent intent = new Intent(ListProductosImagenesActivity.this, ScreenSlidePagerActivity.class);
                    startActivity(intent);
                }
            }
        });

        //ocultar teclado
        hideSoftKeyboard();

        myFilterEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if (s.length() > 1 ){
                    Integer idFamilia = null;
                    Integer idSubfamilia = null;
                    if  (familiaSeleccionada != null && familiaSeleccionada.getM_product_family_id() != null)
                        idFamilia = familiaSeleccionada.getM_product_family_id();

                    if  (subFamiliaSeleccionada != null && subFamiliaSeleccionada.getId() != null)
                        idSubfamilia = subFamiliaSeleccionada.getId();
                    listIdsProductos = db.selectProductoImagenByNombre(idFamilia,
                            idSubfamilia, s);
                } else {
                    listIdsProductos = new ArrayList<ProductoImagen>();
                }
                adapter.notifyDataSetChanged();
            }
        });

        //boton limpiar texto
        myFilterEditText.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = myFilterEditText.getRight()
                            - myFilterEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        myFilterEditText.setText("");
                        if (familiaSeleccionada != null && subFamiliaSeleccionada != null){
                            filtrar(familiaSeleccionada.getM_product_family_id(), subFamiliaSeleccionada.getId(), null);
                        }
                        return true;
                    }
                }
                return false;
            }
        });

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalogo, menu);
        return true;
    }

    @Override
    protected void onResume() {

        if (Globals.getProductoSeleccionadoCatalogo() != null){
            finish();
        }

        if (familiaSeleccionada != null && subFamiliaSeleccionada != null){
            String nombreFiltar = myFilterEditText.getText().toString();
            filtrar(familiaSeleccionada.getM_product_family_id(), subFamiliaSeleccionada.getId(), nombreFiltar);
        }
        //((BaseAdapter) listProductListView.getAdapter()).notifyDataSetChanged();
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
            case R.id.action_tomar_foto:
                Intent intent1 = new Intent(this, ProductoImagenActivity.class);
                startActivity(intent1);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public List<ProductoImagen> filtrar (Integer idFamilia, Integer idSubFamilia, String nombre){
        List<ProductoImagen> list = new ArrayList<ProductoImagen>();

        nombre = myFilterEditText.getText().toString();
        if (nombre == null || nombre.equals(""))
            nombre = null;

        listIdsProductos = db.selectProductoImagenByNombre(idFamilia, idSubFamilia, nombre);
        adapter.notifyDataSetChanged();
        return list;
    }


    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return listIdsProductos.size();
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


            ProductoImagen item = listIdsProductos.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_pedido_detalle, viewGroup, false);
                v.setTag(R.id.txt1_item_pedido_detalle, v.findViewById(R.id.txt1_item_pedido_detalle));
                v.setTag(R.id.precio_item_pedido_detalle, v.findViewById(R.id.precio_item_pedido_detalle));
                v.setTag(R.id.cantidad_item_pedido_detalle, v.findViewById(R.id.cantidad_item_pedido_detalle));
                v.setTag(R.id.total_item_pedido_detalle, v.findViewById(R.id.total_item_pedido_detalle));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_pedido_detalle);
            Producto p = db.selectProductById(item.getM_product_id());
            //Cliente c = db.selectClienteById(item.getClient_id());
            titleTextView.setText(p.getName() + " - "+p.getCodinterno());

            TextView precioTextView = (TextView) v.findViewById(R.id.precio_item_pedido_detalle);
            precioTextView.setText("Nº. "+(i+1));

            TextView subTitleTextView = (TextView) v.findViewById(R.id.cantidad_item_pedido_detalle);
            subTitleTextView.setText(" ");

            TextView nextTitle = (TextView) v.findViewById(R.id.total_item_pedido_detalle);
            nextTitle.setText(">");

            //ImageButton iconItem = (ImageButton) v.findViewById(R.id.icon_item_registro_visita);
            //iconItem.setImageResource(R.drawable.ic_no_visitado);
            return v;
        }
    }


}
