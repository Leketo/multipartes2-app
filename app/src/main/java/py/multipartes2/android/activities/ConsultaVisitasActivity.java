package py.multipartes2.android.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import py.multipartes2.R;
import py.multipartes2.beans.Session;
import py.multipartes2.comm.Comm;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;
import py.multipartes2.utils.DownloaderPdf;


public class ConsultaVisitasActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = ConsultaVisitasActivity.class.getSimpleName();

    private DatePickerDialog fechaDialogDesde;
    private DatePickerDialog fechaDialogHasta;
    private SimpleDateFormat dateFormatter;

    private EditText desdeEditText;
    private EditText hastaEditText;

    private ImageButton desdeBtn;
    private ImageButton hastaBtn;
    private Button consultarBtn;


    DecimalFormat formateador = new DecimalFormat("###,###.##");

    private AppDatabase db = new AppDatabase(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_visitas);


        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consulta Visitas");

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        consultarBtn = (Button) findViewById(R.id.consultas_visitas_btn);
        desdeEditText = (EditText) findViewById(R.id.consultas_visitas_desde);
        hastaEditText = (EditText) findViewById(R.id.consultas_visitas_hasta);

        desdeBtn = (ImageButton) findViewById(R.id.consultas_visitas_desde_calendar_btn);
        hastaBtn = (ImageButton) findViewById(R.id.consultas_visitas_hasta_calendar_btn);
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);


        desdeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechaDialogDesde.show();
            }
        });

        hastaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechaDialogHasta.show();
            }
        });

        setDateTimeFieldDesde();
        setDateTimeFieldHasta();

        consultarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (desdeEditText.getText().toString().equals("") || hastaEditText.getText().toString().equals("") ){
                    String[] buttons = {"Ok"};
                    AppUtils.show(null, "Seleccione el rango de fechas Desde/Hasta", buttons, ConsultaVisitasActivity.this, false, dialogOnclicListener);
                    return;
                }
                //descarga el pdf en la carpeta DESCARGAS
                String pathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +  "";
                File folder = new File(pathDownload);

                File file = new File(folder, "visitas_multipartes.pdf");
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                //obtener usuario logueado
                Session session = db.selectUsuarioLogeado();

                SimpleDateFormat inputFecha = new SimpleDateFormat("dd-MM-yyyy");
                SimpleDateFormat outputFecha = new SimpleDateFormat("yyyy-MM-dd");
                String desde = "";
                String hasta = "";
                try {
                    desde = outputFecha.format(inputFecha.parse(desdeEditText.getText().toString()));
                    hasta = outputFecha.format(inputFecha.parse(hastaEditText.getText().toString()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                String url = Comm.URL + "api/report/app/visitas?from="+desde+"&to="+hasta+"&clientId=0&userId="+session.getUserId()+"&format=pdf";
                Log.d(TAG, url);

                DownloaderPdf.DownloadFile(url, file);
                //DownloaderPdf.DownloadFile("http://www.pdf995.com/samples/pdf.pdf", file);

                Intent intent = new Intent(ConsultaVisitasActivity.this, ConsultaVisitasWebViewActivity.class);
                //intent.putExtra("ID_CLIENTE", clienteSeleccionado.getId());
                startActivity(intent);
            }
        });

    }

    //On click de la Fecha
    @Override
    public void onClick(View view) {
        if (view == desdeEditText ){
            fechaDialogDesde.show();
        } else if (view == hastaEditText){
            fechaDialogHasta.show();
        }
    }

    //Setear fecha al editText
    private void setDateTimeFieldDesde() {
        desdeEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaDialogDesde = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                desdeEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    private void setDateTimeFieldHasta() {
        hastaEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaDialogHasta = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                hastaEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
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

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
        }
    };



}
