package py.multipartesapp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
//import com.joanzapata.pdfview.PDFView;

import java.io.File;
import java.io.IOException;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.PdfFileUtils;


public class ConsultaClienteWebViewActivity extends ActionBarActivity  {

    public static final String TAG = ConsultaClienteWebViewActivity.class.getSimpleName();
    private static final int REQUEST_CODE_WRITE_STORAGE = 202;

    private Cliente clienteSeleccionado;
    private WebView webView;
    private PDFView webViewPdf;
    private Button descargarPdfBtn;
    private File pdfFile;

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
        descargarPdfBtn = (Button) findViewById(R.id.consulta_cliente_descargar_pdf_btn);

        pdfFile = new File(downloadPath+"/cliente_multipartes.pdf");
        Uri url = Uri.fromFile(pdfFile);
        webViewPdf.fromUri(url).load();

        descargarPdfBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                descargarPdf();
            }
        });

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

    private void descargarPdf() {
        if (!hasStoragePermission()) {
            requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_WRITE_STORAGE);
            return;
        }

        try {
            File savedFile = PdfFileUtils.copyToDownloads(pdfFile, "estado_cuenta");
            Toast.makeText(this, "PDF guardado en Descargas: " + savedFile.getName(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "No se pudo guardar el PDF.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean hasStoragePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        return checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                descargarPdf();
            } else {
                Toast.makeText(this, "Permiso de almacenamiento requerido para descargar.", Toast.LENGTH_LONG).show();
            }
        }
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
