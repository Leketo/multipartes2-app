package py.multipartesapp.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Session;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.DownloaderPdf;

public class ConsultaRemitosActivity extends ActionBarActivity implements View.OnClickListener {

    public static final String TAG = ConsultaRemitosActivity.class.getSimpleName();

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 201;
    private static final int DIAS_ATRAS_PERMITIDOS = 90;

    private DatePickerDialog fechaDialogDesde;
    private DatePickerDialog fechaDialogHasta;
    private SimpleDateFormat dateFormatter;

    private EditText desdeEditText;
    private EditText hastaEditText;
    private AutoCompleteTextView clienteTextView;
    private CheckBox deliveryCheckBox;
    private Cliente clienteSeleccionado;

    private ImageButton desdeBtn;
    private ImageButton hastaBtn;
    private Button consultarBtn;

    private AppDatabase db = new AppDatabase(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consulta_remitos);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consulta Remitos");

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        consultarBtn = (Button) findViewById(R.id.consultas_remitos_btn);
        desdeEditText = (EditText) findViewById(R.id.consultas_remitos_desde);
        hastaEditText = (EditText) findViewById(R.id.consultas_remitos_hasta);
        clienteTextView = (AutoCompleteTextView) findViewById(R.id.consultas_remitos_cliente);
        deliveryCheckBox = (CheckBox) findViewById(R.id.consultas_remitos_delivery);

        desdeBtn = (ImageButton) findViewById(R.id.consultas_remitos_desde_calendar_btn);
        hastaBtn = (ImageButton) findViewById(R.id.consultas_remitos_hasta_calendar_btn);
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        Calendar hoy = Calendar.getInstance();
        desdeEditText.setText(dateFormatter.format(hoy.getTime()));
        hastaEditText.setText(dateFormatter.format(hoy.getTime()));
        desdeEditText.setKeyListener(null);
        hastaEditText.setKeyListener(null);

        if (db.countCliente() == 0){
            Toast.makeText(getApplicationContext(), "Favor sincronizar datos primero.", Toast.LENGTH_LONG).show();
        }

        List<Cliente> lista_clientes = db.selectAllCliente();
        ArrayAdapter<Cliente> adapterClientes = new ArrayAdapter<Cliente>(this, android.R.layout.simple_dropdown_item_1line, lista_clientes);
        adapterClientes.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        clienteTextView.setAdapter(adapterClientes);

        clienteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clienteTextView.clearFocus();
                clienteSeleccionado = (Cliente) parent.getAdapter().getItem(position);
            }
        });

        clienteTextView.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_RIGHT = 2;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = clienteTextView.getRight()
                            - clienteTextView.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        clienteTextView.setText("");
                        clienteSeleccionado = null;
                        return true;
                    }
                }
                return false;
            }
        });

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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int hasWritePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                    if (hasWritePermission != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                }

                if (desdeEditText.getText().toString().equals("") || hastaEditText.getText().toString().equals("") ){
                    String[] buttons = {"Ok"};
                    AppUtils.show(null, "Seleccione el rango de fechas Desde/Hasta", buttons, ConsultaRemitosActivity.this, false, dialogOnclicListener);
                    return;
                }

                String pathDownload = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +  "";
                File folder = new File(pathDownload);

                File file = new File(folder, "remitos_multipartes.pdf");
                try {
                    file.createNewFile();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

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

                if (!rangoFechasValido()){
                    String[] buttons = {"Ok"};
                    AppUtils.show(null, "La fecha Hasta debe ser igual o mayor a Desde", buttons, ConsultaRemitosActivity.this, false, dialogOnclicListener);
                    return;
                }

                String clientId = "null";
                if (clienteSeleccionado != null){
                    clientId = String.valueOf(clienteSeleccionado.getId());
                }

                String isDelivery = deliveryCheckBox.isChecked() ? "Y" : "N";

                String url = Comm.URL + "multip/api/report/remitos?from="+desde+"&to="+hasta+"&clientId="+clientId+"&userId="+session.getUserId()+"&isDelivery="+isDelivery+"&format=pdf";
                Log.d(TAG, url);

                DownloaderPdf.DownloadFile(url, file);

                Intent intent = new Intent(ConsultaRemitosActivity.this, ConsultaRemitosWebViewActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view == desdeEditText ){
            fechaDialogDesde.show();
        } else if (view == hastaEditText){
            fechaDialogHasta.show();
        }
    }

    private void setDateTimeFieldDesde() {
        desdeEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaDialogDesde = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                desdeEditText.setText(dateFormatter.format(newDate.getTime()));
                ajustarFechaHastaMinima(newDate);
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        Calendar fechaMinima = Calendar.getInstance();
        fechaMinima.add(Calendar.DAY_OF_MONTH, -DIAS_ATRAS_PERMITIDOS);
        fechaDialogDesde.getDatePicker().setMinDate(fechaMinima.getTimeInMillis());
        fechaDialogDesde.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
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
        fechaDialogHasta.getDatePicker().setMinDate(getFechaDesde().getTimeInMillis());
        fechaDialogHasta.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
    }

    private void ajustarFechaHastaMinima(Calendar fechaDesde) {
        fechaDialogHasta.getDatePicker().setMinDate(fechaDesde.getTimeInMillis());
        Calendar fechaHasta = getFechaHasta();
        if (fechaHasta.before(fechaDesde)) {
            hastaEditText.setText(dateFormatter.format(fechaDesde.getTime()));
        }
    }

    private boolean rangoFechasValido() {
        Calendar fechaDesde = getFechaDesde();
        Calendar fechaHasta = getFechaHasta();
        return fechaDesde != null && fechaHasta != null && !fechaHasta.before(fechaDesde);
    }

    private Calendar getFechaDesde() {
        return getFechaDesdeTexto(desdeEditText.getText().toString());
    }

    private Calendar getFechaHasta() {
        return getFechaDesdeTexto(hastaEditText.getText().toString());
    }

    private Calendar getFechaDesdeTexto(String fecha) {
        Calendar calendar = Calendar.getInstance();
        try {
            calendar.setTime(dateFormatter.parse(fecha));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return calendar;
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
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
