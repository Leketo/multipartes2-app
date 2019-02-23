package py.multipartesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import py.multipartesapp.R;

//import org.apache.http.cookie.Cookie;
//import org.apache.http.impl.cookie.BasicClientCookie;

/**
 * Created by Adolfo on 10/06/2015.
 */
public class ConsultasActivity extends ActionBarActivity {
    public static final String TAG = ConsultasActivity.class.getSimpleName();


    private Button misVisitasBtn;
    private Button misPedidosBtn;
    private Button misEntregasBtn;
    private Button misCobrosBtn;

    private Button estadoCuentaBtn;
    private Button stockProductoBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultas);

                /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Consultas");

//        misVisitasBtn = (Button) findViewById(R.id.consultas_visitas);
        misPedidosBtn = (Button) findViewById(R.id.consultas_pedidos);
//        misEntregasBtn = (Button) findViewById(R.id.consultas_entregas);
//        misCobrosBtn = (Button) findViewById(R.id.consultas_cobranzas);


        estadoCuentaBtn = (Button) findViewById(R.id.consultas_estado_cuenta);
        stockProductoBtn = (Button) findViewById(R.id.consultas_stock_producto);

//        misVisitasBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ConsultasActivity.this, ConsultaVisitasActivity.class);
//                startActivity(intent);
//            }
//        });
//
        misPedidosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaPedidosActivity.class);
                startActivity(intent);
            }
        });
//
//        misEntregasBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ConsultasActivity.this, ConsultaEntregasActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        misCobrosBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(ConsultasActivity.this, ConsultaCobrosActivity.class);
//                startActivity(intent);
//            }
//        });


        estadoCuentaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaClienteActivity.class);
                startActivity(intent);
            }
        });

        stockProductoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaStockActivity.class);
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

}
