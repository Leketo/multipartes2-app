package py.multipartesapp.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

//import org.apache.http.HttpResponse;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.entity.StringEntity;
//import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import py.multipartes2.R;
import py.multipartes2.beans.Cliente;
import py.multipartes2.comm.Comm;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.DownloaderPdf;


public class ConsultaClienteActivity extends ActionBarActivity  {

    public static final String TAG = ConsultaClienteActivity.class.getSimpleName();

    private AutoCompleteTextView clienteTextView;
    private Cliente clienteSeleccionado;
    private Button consultarBtn;
    ///private TextView categoriaClienteTextView;
    //private TextView creditoClienteTextView;

    DecimalFormat formateador = new DecimalFormat("###,###.##");

    private AppDatabase db = new AppDatabase(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_cliente);


        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consulta Cliente");

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        clienteTextView = (AutoCompleteTextView) findViewById(R.id.consulta_cliente_nom_cliente);
        consultarBtn = (Button) findViewById(R.id.consulta_cliente_btnm);
        //categoriaClienteTextView = (TextView) findViewById(R.id.consulta_cliente_cat_cliente);
        //creditoClienteTextView = (TextView) findViewById(R.id.consulta_cliente_cred_disponible);


        if (db.countCliente() == 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        List<Cliente> lista_clientes = db.selectAllCliente();
        //ArrayAdapter<Cliente> adapterClientes = new ArrayAdapter<Cliente>(this, android.R.layout.simple_spinner_item, lista_clientes);
        ArrayAdapter<Cliente> adapterClientes = new ArrayAdapter<Cliente>(this, android.R.layout.simple_dropdown_item_1line, lista_clientes);
        //temporal
        adapterClientes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        //simple_spinner_dropdown_item
        clienteTextView.setAdapter(adapterClientes);

        clienteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clienteTextView.clearFocus();
                clienteSeleccionado = (Cliente) parent.getAdapter().getItem(position);



//                //mostrar categoria cliente
//                PrecioCategoria precioCategoria = db.selectPrecioCategoriaById(clienteSeleccionado.getCategoria_precio());
//                categoriaClienteTextView.setText(precioCategoria.getName());
//
//                //mostrar credito de cliente
//                Integer creditoDisponible = clienteSeleccionado.getCredito_disponible().intValue();
//                creditoClienteTextView.setText(formatearMoneda(MyFormatter.formatearMoneda(creditoDisponible.toString())));

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
//                        categoriaClienteTextView.setText("");
//                        creditoClienteTextView.setText("");
//
                      clienteSeleccionado = null;

                        return true;
                    }
                }
                return false;
            }
        });

        consultarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (clienteSeleccionado == null){
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(ConsultaClienteActivity.this, "Seleccione un cliente para ver datos", duration);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
                    toast.show();
                    return;
                }

                String pathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +  "";
                Log.d("---------",pathDownload);
                File folder = new File(pathDownload);

                File file = new File(folder, "cliente_multipartes.pdf");
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

                //DESCOMENTAR ESTA LINEA
                DownloaderPdf.DownloadFile(Comm.URL + "api/report/extracto?clientId="+clienteSeleccionado.getId()+"&userId=0&format=pdf", file);
                //DownloaderPdf.DownloadFile("http://www.pdf995.com/samples/pdf.pdf", file);


                Intent intent = new Intent(ConsultaClienteActivity.this, ConsultaClienteWebViewActivity.class);
                intent.putExtra("ID_CLIENTE", clienteSeleccionado.getId());
                startActivity(intent);
            }
        });

    }

    private class CallBack extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading (WebView view, String url){
            return false;
        }
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }


    @Override
    protected void onResume() {
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





}
