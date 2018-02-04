package py.multipartesapp.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
//import com.joanzapata.pdfview.PDFView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.db.AppDatabase;


public class ConsultaClienteWebViewActivity extends ActionBarActivity  {

    public static final String TAG = ConsultaClienteWebViewActivity.class.getSimpleName();

    private Cliente clienteSeleccionado;
    private WebView webView;
    private PDFView webViewPdf;

    private AppDatabase db = new AppDatabase(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_cliente_webview);


        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Datos Cliente");


        String downloadPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +  "";


        Integer idCliente = getIntent().getIntExtra("ID_CLIENTE", 0);
        webViewPdf = (PDFView) findViewById(R.id.webView1);

        Uri url = Uri.fromFile(new File(downloadPath+"/cliente_multipartes.pdf"));
        webViewPdf.fromUri(url).load();

        /*
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
        webView.setWebViewClient(new CallBack());

        webView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //------------- AQUI CARGAR URL DEL CLIENTE ----------------------------------

                //String urlPdf = Comm.URL + "api/report/extracto?clientId="+idCliente+"&userId=0&format=pdf";
                String urlPdf = "www.pdf995.com/samples/pdf.pdf";

                webView.loadUrl("http://docs.google.com/gview?embedded=true&url="+urlPdf);
            }
        }, 500); */


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
