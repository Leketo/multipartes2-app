package py.multipartesapp.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import py.multipartesapp.R;
import py.multipartesapp.beans.Configuracion;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;


public class ConfiguracionActivity extends ActionBarActivity {

    private Button guardarConfiguracionBtn;
    private EditText urlEditText;
    private EditText puertoEditText;
    private Switch configImpresora;
    private Button btnConfigPrinter2;
    boolean hasConfigImpresora;
    private AppDatabase db = new AppDatabase(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuracion);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Configuración");

        guardarConfiguracionBtn = (Button) findViewById(R.id.guardar_configuracion);
        urlEditText = (EditText) findViewById(R.id.configuracion_url);
        puertoEditText = (EditText) findViewById(R.id.configuracion_puerto);
        configImpresora = (Switch) findViewById(R.id.config_impresora);
        btnConfigPrinter2 = (Button) findViewById(R.id.config_printer2);
        Configuracion url = db.selectConfiguracionByClave("URL");

        //si ya existe una url completar campos
        if (url.getValor() != null){
            urlEditText.setText(url.getValor().replace("http://",""));
            Comm.URL=url.getValor();

            Configuracion puerto = db.selectConfiguracionByClave("PUERTO");
            puertoEditText.setText(puerto.getValor());
        }

        guardarConfiguracionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarConfiguracion();
            }
        });

        if(configImpresora.isChecked()){
            btnConfigPrinter2.setEnabled(true);
        }else{
            btnConfigPrinter2.setEnabled(false);
        }
        listenerSwitchButtonImpresora();
        setupConfigPrinterBtn();

    }

    private void listenerSwitchButtonImpresora() {
        final boolean configOn = true;
        final boolean configOff = false;
        final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        hasConfigImpresora = sharedPreferences.getBoolean("configImpresora", false);
        if(!hasConfigImpresora){
            sharedPreferences.edit().putBoolean("impresora", configOff).apply();
            configImpresora.setChecked(false);
            sharedPreferences.edit().putBoolean("configImpresora", false).apply();
        }else{
            boolean configActual = sharedPreferences.getBoolean("impresora", true);
            if(configActual== configOn){
                configImpresora.setChecked(true);
                btnConfigPrinter2.setEnabled(true);
            }else{
                configImpresora.setChecked(false);
            }
        }
        configImpresora.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (configImpresora.isChecked()) {
                    Log.d("ConfigActivity", "Botón Toggle: ON");
                    sharedPreferences.edit().putBoolean("impresora", configOn).apply();
                    sharedPreferences.edit().putBoolean("configImpresora", configOn).apply();
                    btnConfigPrinter2.setEnabled(true);
                } else {
                    Log.d("ConfigActivity", "Botón Toggle: OFF");
                    sharedPreferences.edit().putBoolean("impresora", configOff).apply();
                    sharedPreferences.edit().putBoolean("configImpresora", configOff).apply();
                    btnConfigPrinter2.setEnabled(false);
                }
                //sharedPreferences.edit().putBoolean("configImpresora", false).apply();
            }
        });
    }

    private void guardarConfiguracion(){

        if (urlEditText.getText().toString().isEmpty() ){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Complete todos los campos", buttons, ConfiguracionActivity.this, false, dialogOnclicListener);
            return;
        }
        //borramos toda la configuracion
        db.deleteConfiguracion();

        String url = "http://"+urlEditText.getText().toString().trim();
        String puerto = puertoEditText.getText().toString().trim();


        Configuracion urlConfiguracion = new Configuracion();
        urlConfiguracion.setClave("URL");
        urlConfiguracion.setValor(url);

        Configuracion puertoConfiguracion = new Configuracion();
        puertoConfiguracion.setClave("PUERTO");
        puertoConfiguracion.setValor(puerto);

        db.insertConfiguracion(urlConfiguracion);
        //db.insertConfiguracion(puertoConfiguracion);

        Comm.URL=url;

        //Globals.setUrl(urlConfiguracion);
        //Globals.setPuerto(puertoConfiguracion);

        Toast.makeText(getApplicationContext(), "Configuracion guardada." + urlConfiguracion.getValor(), Toast.LENGTH_LONG).show();

        Intent intent = new Intent(ConfiguracionActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();

    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_catalogo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    private void setupConfigPrinterBtn() {
        btnConfigPrinter2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConfiguracionActivity.this, DOPrintMainActivity.class);
                ConfiguracionActivity.this.startActivity(intent);
            }
        });
    }
}
