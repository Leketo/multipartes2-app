package py.multipartesapp.activities;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import py.multipartes2.R;
import py.multipartes2.beans.CobranzaDetalleItem;
import py.multipartes2.beans.Factura;
import py.multipartes2.beans.FormaPago;
import py.multipartes2.customAutoComplete.CobranzaActivityBancoTextChangedListener;
import py.multipartes2.customAutoComplete.CustomAutoCompleteView;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;
import py.multipartes2.utils.Globals;

/**
 * Created by Adolfo on 18/10/2015.
 */
public class CobranzaDetalleItemActivity extends ActionBarActivity implements View.OnClickListener {
    public static final String TAG = CobranzaDetalleItemActivity.class.getSimpleName();
    public static final String COD_EFECTIVO = "1";
    public static final String COD_CHEQUE = "2";
    public static final String COD_EFECTIVOCHEQUE= "3";

    private AppDatabase db = new AppDatabase(this);

    private TextView totalDeuda;
    private TextView totalSaldo;
    private TextView totalFp;
    private EditText montoEfectivoEditText;
    private EditText montoChequeEditText;
    private EditText nroChequeEditText;
    private EditText nombreChequeEditText;
    private Spinner formaPagoSpinner;
    private Button cargarPagoBtn;
    private FormaPago formaPagoSeleccionado;
    private SimpleDateFormat dateFormatter;

    private LinearLayout datosChequeLinearLayout;
    private LinearLayout datosEfectivoLinearLayout;

    private EditText fechaVencChequeEditText;
    private DatePickerDialog fechaVencChequeDialog;
    private ImageButton calendarBtn;
    public CustomAutoCompleteView bancoAutoComplete;

    public String[] itemsBancos = new String[] {"Buscar por nombre..."};
    public CustomAutoCompleteView bancoutoComplete;
    // adapter for auto-complete
    public ArrayAdapter<String> bancoAdapter;
    private List<String> bancosFiltrados;
    private String bancoSeleccionado;
    double tDeuda = 0;
    double totAmountFp = 0;
    double saldo = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza_detalle_item);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Cargar cobro");

        montoEfectivoEditText = (EditText) findViewById(R.id.cobranza_detalles_monto_efectivo);
        montoChequeEditText = (EditText) findViewById(R.id.cobranza_detalles_monto_cheque);

        nroChequeEditText = (EditText) findViewById(R.id.cobranza_detalle_nro_cheque);
        bancoAutoComplete = (CustomAutoCompleteView) findViewById(R.id.cobranza_detalle_banco_autocomplete);
        nombreChequeEditText = (EditText) findViewById(R.id.cobranza_detalles_nombre_cheque);
        datosChequeLinearLayout = (LinearLayout) findViewById(R.id.cobranza_detalle_datos_cheque);
        datosEfectivoLinearLayout = (LinearLayout) findViewById(R.id.cobranza_detalle_datos_efectivo);

        formaPagoSpinner = (Spinner) findViewById(R.id.cobranza_forma_pago);
        cargarPagoBtn = (Button) findViewById(R.id.agregar_item_pago);

        totalDeuda = (TextView) findViewById(R.id.deuda_total);
        totalSaldo = (TextView) findViewById(R.id.deuda_saldo);
        totalFp = (TextView) findViewById(R.id.deuda_fp);

        //montoEfectivoEditText.setText(CobranzaActivity.facturaSeleccionada.getGrandtotal().toString());
        //Rellenamos los datos del Combo Forma de Pago
        FormaPago tipo1 = new FormaPago("1", "EFECTIVO");
        FormaPago tipo2 = new FormaPago("2", "CHEQUE");
        FormaPago tipo3 = new FormaPago("3", "EFECTIVO y CHEQUE");

        List<FormaPago> listFormaPago = new ArrayList<FormaPago>();
        listFormaPago.add(tipo1);
        listFormaPago.add(tipo2);
        //listFormaPago.add(tipo3);
        formaPagoSeleccionado = tipo1;

        ArrayAdapter<FormaPago> formaPagoArrayAdapter = new ArrayAdapter<FormaPago>(this, android.R.layout.simple_dropdown_item_1line, listFormaPago);
        //adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formaPagoSpinner.setAdapter(formaPagoArrayAdapter);

        //mostrar el de efectivo,
        datosEfectivoLinearLayout.setVisibility(View.VISIBLE);
        datosChequeLinearLayout.setVisibility(View.GONE);

        formaPagoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                formaPagoSeleccionado = (FormaPago) parent.getAdapter().getItem(position);

                if (formaPagoSeleccionado.getCodigo().equals(COD_EFECTIVO)){
                    datosEfectivoLinearLayout.setVisibility(View.VISIBLE);
                    datosChequeLinearLayout.setVisibility(View.GONE);
                } else if (formaPagoSeleccionado.getCodigo().equals(COD_CHEQUE)){
                    datosEfectivoLinearLayout.setVisibility(View.GONE);
                    datosChequeLinearLayout.setVisibility(View.VISIBLE);
                }  else if (formaPagoSeleccionado.getCodigo().equals(COD_EFECTIVOCHEQUE)){
                    datosEfectivoLinearLayout.setVisibility(View.VISIBLE);
                    datosChequeLinearLayout.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // add the listener so it will tries to suggest while the user types
        bancoAutoComplete.addTextChangedListener(new CobranzaActivityBancoTextChangedListener(this));

        // set our adapter
        bancoAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, itemsBancos);
        bancoAutoComplete.setAdapter(bancoAdapter);

        //al seleccionar un banco de la lista filtrada
        bancoAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //clienteTextView.clearFocus();
                bancoSeleccionado = bancosFiltrados.get(position);

            }
        });
        //boton limpiar texto cliente
        bancoAutoComplete.setOnTouchListener(new View.OnTouchListener() {
            final int DRAWABLE_LEFT = 0;
            final int DRAWABLE_TOP = 1;
            final int DRAWABLE_RIGHT = 2;
            final int DRAWABLE_BOTTOM = 3;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    int leftEdgeOfRightDrawable = bancoAutoComplete.getRight()
                            - bancoAutoComplete.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width();
                    // when EditBox has padding, adjust leftEdge like
                    // leftEdgeOfRightDrawable -= getResources().getDimension(R.dimen.edittext_padding_left_right);
                    if (event.getRawX() >= leftEdgeOfRightDrawable) {
                        // clicked on clear icon
                        bancoAutoComplete.setText("");
                        bancoSeleccionado = null;
                        return true;
                    }
                }
                return false;
            }
        });

        //Configuramos el calendario en Fecha de vencimiento de cheque
        dateFormatter = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        fechaVencChequeEditText = (EditText) findViewById(R.id.cobranza_fecha_vencimiento_cheque);
        fechaVencChequeEditText.setInputType(InputType.TYPE_NULL);
        calendarBtn = (ImageButton) findViewById(R.id.cobranza_calendar_btn);

        calendarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fechaVencChequeDialog.show();
            }
        });
        setDateTimeField();

        cargarPagoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarItem();
            }
        });
        setTotalDeuda();
        actualizarImporteFormaPago();
        actualizarSaldo();
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    private void agregarItem (){

        if (formaPagoSeleccionado.getCodigo().equals(COD_EFECTIVO)){
            String str = montoEfectivoEditText.getText().toString();
            if (str.equals("") || str.equals("0")){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "Ingrese un monto valido.", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }else if(Double.parseDouble(str) > tDeuda){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "El valor ingresado sobrepasa el total.", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }
        } else if (formaPagoSeleccionado.getCodigo().equals(COD_CHEQUE)){
            String str = montoChequeEditText.getText().toString();
            if (str.equals("") || str.equals("0")){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "Ingrese un monto valido.", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }else if(Double.parseDouble(str) > tDeuda){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "El valor ingresado sobrepasa el total.", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }
            if (bancoSeleccionado == null || bancoSeleccionado.trim().equals("")){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "Seleccione un banco.", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }
            if (nombreChequeEditText.getText().toString() == null || nombreChequeEditText.getText().toString().equals("")){
                String[] buttons = {"Ok"};
                AppUtils.show(null, "Ingrese un nombre de cheque", buttons, CobranzaDetalleItemActivity.this, false, dialogOnclicListener);
                return;
            }
        }


        Integer montoIngresado = Integer.valueOf(montoEfectivoEditText.getText().toString());
        //CobranzaActivity.facturaSeleccionada.setMontoCobrado(montoIngresado.toString());
        //CobranzaActivity.facturaSeleccionada.setItems();

//        CobranzaActivity.facturaSeleccionada.setBank(bancoSeleccionado);
//        CobranzaActivity.facturaSeleccionada.setCheck_number(nroChequeEditText.getText().toString());
//        CobranzaActivity.facturaSeleccionada.setPayment_type(formaPagoSeleccionado.getDescripcion());
//        CobranzaActivity.facturaSeleccionada.setExpired_date(fechaVencChequeEditText.getText().toString());
//        CobranzaActivity.facturaSeleccionada.setCheck_name(nombreChequeEditText.getText().toString());


        CobranzaDetalleItem itemCobro = new CobranzaDetalleItem();
        itemCobro.setPayment_type(formaPagoSeleccionado.getDescripcion());


        if (formaPagoSeleccionado.getCodigo().equals(COD_EFECTIVO)){
            Integer montoEfectivo = Integer.valueOf(montoEfectivoEditText.getText().toString());
            itemCobro.setAmount(montoEfectivo);

        }  else if (formaPagoSeleccionado.getCodigo().equals(COD_CHEQUE)){
            Integer montoCheque = Integer.valueOf(montoChequeEditText.getText().toString());
            itemCobro.setAmount(montoCheque);
            itemCobro.setBank(bancoSeleccionado);
            itemCobro.setCheck_number(nroChequeEditText.getText().toString());
            itemCobro.setExpired_date(fechaVencChequeEditText.getText().toString());
            itemCobro.setCheck_name(nombreChequeEditText.getText().toString());
        }
        if(Globals.getItemCobroList() == null){
            Globals.setItemCobroList(new ArrayList<CobranzaDetalleItem>());
        }
        Globals.getItemCobroList().add(itemCobro);
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
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    //On click de la Fecha
    @Override
    public void onClick(View view) {
        if (view == fechaVencChequeEditText){
            fechaVencChequeDialog.show();
        }
    }

    //Setear fecha al editText
    private void setDateTimeField() {
        fechaVencChequeEditText.setOnClickListener(this);
        Calendar newCalendar = Calendar.getInstance();

        fechaVencChequeDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                fechaVencChequeEditText.setText(dateFormatter.format(newDate.getTime()));
            }
        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
    }

    // this function is used in CustomAutoCompleteTextChangedListener.java
    public String[] getBancosFiltrados(String searchTerm){

        // add itemsBancos on the array dynamically
        bancosFiltrados = selectBancoByNombre(searchTerm);
        int rowCount = bancosFiltrados.size();
        String[] item = new String[rowCount];
        int i = 0;
        for (String c : bancosFiltrados) {
            item[i] = c;
            i++;
        }

        return item;
    }


    public List<String> selectBancoByNombre (String nombre){
       String[] bancos = new String[] {
               "BBVA", "Banco GNB", "Banco Itau", "Banco Familiar", "Vision Banco", "Banco Amambay", "Banco Continental",
               "Banco Atlas", "Banco Regional", "Banco do Brasil", "Bancoop", "Sudameris", "Citbank", "Banco Nacional de Fomento",
               "Intefisa Banco", "Otro"
       };
       List<String> respuesta = new ArrayList<String>();
       for (String item : bancos){
           if (item.toUpperCase().contains(nombre.toUpperCase())){
               respuesta.add(item);
           }
       }
        return respuesta;
    }

    private void setTotalDeuda(){
        tDeuda = 0;
        if(CobranzaActivity.facturasFiltrados != null) {
            for (Factura f: CobranzaActivity.facturasFiltrados){
                if(f.getMontoCobrado() != null){
                    if(!f.getMontoCobrado().isEmpty()){
                        tDeuda += Double.parseDouble(f.getMontoCobrado());
                    }
                }
            }
        }
        totalDeuda.setText(String.valueOf(tDeuda));
    }

    private void actualizarImporteFormaPago(){
        totAmountFp = 0;
        if(Globals.getItemCobroList() != null) {
            for (CobranzaDetalleItem c : Globals.getItemCobroList()) {
                totAmountFp += c.getAmount();
            }
        }
        totalFp.setText(String.valueOf(totAmountFp));
    }

    private void actualizarSaldo(){
        saldo = tDeuda - totAmountFp;
        totalFp.setText(String.valueOf(saldo));
    }

}
