package py.multipartesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import py.multipartesapp.R;

public class SeleccioneSucursalActivity extends ActionBarActivity {

    private Spinner spinnerSucursales;
    private Button botonCargarPedido;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seleccione_sucursal);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Seleccione Sucursal");

        spinnerSucursales = (Spinner) findViewById(R.id.spinnerSucursales);
        botonCargarPedido = (Button) findViewById(R.id.botonCargarPedido);

        // Supongamos que tienes una función que te devuelve las sucursales
        List<String> sucursales = obtenerSucursales();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, sucursales);
        spinnerSucursales.setAdapter(adapter);

        botonCargarPedido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sucursalSeleccionada = spinnerSucursales.getSelectedItem().toString();
                String[] partes = sucursalSeleccionada.split(" - ");
                String idSucursal = partes[0].trim(); // ID de la sucursal
                String descripcionSucursal = partes[1].trim(); // Descripción completa

                Intent intent = new Intent(SeleccioneSucursalActivity.this, PedidoActivity.class);
                intent.putExtra("idSucursal", idSucursal);
                intent.putExtra("descripcionSucursal", descripcionSucursal);
                startActivity(intent);
            }
        });

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

    private List<String> obtenerSucursales() {
        List<String> sucursales = new ArrayList<>();
        sucursales.add("1000047 - Casa Central");
        sucursales.add("1010047 - Agencia Ciudad del Este");
        sucursales.add("1200000 - Vidrios");
        return sucursales;
    }
}
