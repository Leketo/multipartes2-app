package py.multipartes2.android.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import py.multipartes2.R;
import py.multipartes2.beans.Producto;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;

/**
 * Created by Adolfo on 22/06/2016.
 */
public class ProductoImagenActivity extends ActionBarActivity {
    public static final String TAG = ProductoImagenActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE_CAPTURE = 1;

    private AppDatabase db = new AppDatabase(this);
    private Producto productoSeleccionado;
    private AutoCompleteTextView productoTextView;
    private TextView codigoProductoTxtView;
    private TextView precioProductoTxtView;
    private ImageView fotoImageView;
    private Button tomarFotoBtn;
    private Button guardarFotoBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_producto_imagen);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Tomar Foto");

        productoTextView = (AutoCompleteTextView) findViewById(R.id.producto_img_producto);
        codigoProductoTxtView = (TextView) findViewById(R.id.producto_img_codigo);
        precioProductoTxtView = (TextView) findViewById(R.id.producto_img_precio);
        fotoImageView = (ImageView) findViewById(R.id.producto_img_photo);
        tomarFotoBtn = (Button) findViewById(R.id.producto_img_tomar_foto);
        guardarFotoBtn = (Button) findViewById(R.id.producto_img_guardar);


        List<Producto> lista_productos = db.selectAllProducto();
        Log.d(TAG, "Cantidad de productos: " +lista_productos.size());

        ArrayAdapter<Producto> adapterProductos = new ArrayAdapter<Producto>(this, android.R.layout.simple_spinner_item, lista_productos);
        adapterProductos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        productoTextView.setAdapter(adapterProductos);

        guardarFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarFoto();
            }
        });

        tomarFotoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tomarFotoIntent();
            }
        });

        productoTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //productoTextView.clearFocus();
                productoSeleccionado = (Producto) parent.getAdapter().getItem(position);
                codigoProductoTxtView.setText(productoSeleccionado.getCodinterno());
                precioProductoTxtView.setText(productoSeleccionado.getPrice().toString());
            }
        });
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    public void tomarFotoIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void guardarFoto(){
        //validar producto
        if (productoSeleccionado == null || productoTextView.getText().equals("")){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Seleccione un producto", buttons, ProductoImagenActivity.this, false, dialogOnclicListener);
            return;
        }
        //validar foto
        if (fotoImageView.getDrawable() == null){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Debe tomar una fotografía.", buttons, ProductoImagenActivity.this, false, dialogOnclicListener);
            return;
        }

        finish();
        return;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            fotoImageView.setImageBitmap(imageBitmap);
        }
    }

}
