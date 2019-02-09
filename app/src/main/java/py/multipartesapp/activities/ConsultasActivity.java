package py.multipartesapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import py.multipartes2.R;

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

        misVisitasBtn = (Button) findViewById(R.id.consultas_visitas);
        misPedidosBtn = (Button) findViewById(R.id.consultas_pedidos);
        misEntregasBtn = (Button) findViewById(R.id.consultas_entregas);
        misCobrosBtn = (Button) findViewById(R.id.consultas_cobranzas);


        estadoCuentaBtn = (Button) findViewById(R.id.consultas_estado_cuenta);
        stockProductoBtn = (Button) findViewById(R.id.consultas_stock_producto);

        misVisitasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaVisitasActivity.class);
                startActivity(intent);
            }
        });

        misPedidosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaPedidosActivity.class);
                startActivity(intent);
            }
        });

        misEntregasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaEntregasActivity.class);
                startActivity(intent);
            }
        });

        misCobrosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ConsultasActivity.this, ConsultaCobrosActivity.class);
                startActivity(intent);
            }
        });


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


}
