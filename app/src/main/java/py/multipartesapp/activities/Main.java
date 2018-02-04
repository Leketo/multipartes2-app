package py.multipartesapp.activities;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

//import org.apache.http.impl.cookie.BasicClientCookie;

import java.io.File;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.LocationTable;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.locationServices.LocationService;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;


public class Main extends ActionBarActivity {

    public static final String TAG = Main.class.getSimpleName();

    private TextView nombreUsuarioTextView;
    private Button registroVisistasBtn;
    private Button sincronizarBtn;
    private Button rutasBtn;
    private Button entregasBtn;
    private Button pedidosBtn;
    private Button cobranzasBtn;
    private Button catalogoBtn;
    private Button crearRutaBtn;
    private Button consultasBtn;

    private AppDatabase db = new AppDatabase(this);
    private static Context context;
    private LocationTable location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Multipartes");

        nombreUsuarioTextView = (TextView) findViewById(R.id.main_nombre_usuario);
        registroVisistasBtn = (Button) findViewById(R.id.main_btn_registroVisita);
        sincronizarBtn = (Button) findViewById(R.id.main_btn_sincronizar_datos);
        rutasBtn = (Button) findViewById(R.id.main_btn_rutas);
        entregasBtn = (Button) findViewById(R.id.main_btn_entrega);
        pedidosBtn = (Button) findViewById(R.id.main_btn_pedido);
        cobranzasBtn = (Button) findViewById(R.id.main_btn_cobranza);
        catalogoBtn = (Button) findViewById(R.id.main_btn_catalogo);
        crearRutaBtn = (Button) findViewById(R.id.main_btn_crear_ruta);
        consultasBtn = (Button) findViewById(R.id.main_btn_consultas);

        registroVisistasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListRegistroVisitasActivity.class);
                startActivity(intent);
            }
        });

        sincronizarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, SincronizarActivity.class);
                startActivity(intent);
            }
        });

        rutasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListRutasActivity.class);
                startActivity(intent);
            }
        });

        entregasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListEntregaActivity.class);
                startActivity(intent);
            }
        });

        pedidosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListPedidoActivity.class);
                startActivity(intent);
            }
        });

        cobranzasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListCobranzaActivity.class);
                startActivity(intent);
            }
        });

        catalogoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ListProductosImagenesActivity.class);
                startActivity(intent);
            }
        });

        crearRutaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, RutaLocationNewActivity.class);
                startActivity(intent);
            }
        });

        consultasBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, ConsultasActivity.class);
                startActivity(intent);
            }
        });

        Session session = db.selectUsuarioLogeado();
        Usuario usuario = db.selectUsuarioById(session.getUserId());

        if (usuario.getRole() != null && !usuario.getRole().equals("ROOT")){
            crearRutaBtn.setVisibility(View.INVISIBLE);
        }
        if (usuario.getName() != null){
            nombreUsuarioTextView.setText("Bienvenido, "+usuario.getName() + " "+ usuario.getLastname()+"!");
        }

        Main.context = getApplicationContext();
        startCheckLocationService();
    }

    public static Context getAppContext() {
        return Main.context;
    }

    /**
     * Check location service.
     */
    private void startCheckLocationService() {

        Boolean servicioCorriendo = false;
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {

            if (LocationService.class.getName().equals(service.service.getClassName())) {
                servicioCorriendo = true;
            }
        }
        Log.d(TAG, "=====================State checkLocation running: " + servicioCorriendo);
        if (!servicioCorriendo) {
            Intent mServiceIntent = new Intent(this, LocationService.class);
            startService(mServiceIntent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ConfiguracionActivity.class);
            startActivity(intent);
            //finish();
            return true;
        } else if (id == R.id.action_logout) {
            //borramos los cookies
            db.deleteLogin();
            db.deleteSession();
            Globals.cookieStore.clear();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public static void enviarLocationPendientes(Context context) {
        new Main().enviarLocation(context);
    }

    public void enviarLocation(Context context) {
        //temporal
//        if (Globals.cookieStore.getCookies().size() == 0){
//            BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", "un_cookie_test_en_enviar_locations");
//            cookie.setDomain("186.2.196.105");
//            cookie.setPath("/");
//            Globals.cookieStore.addCookie(cookie);
//        }

        /*
        if (Globals.cookieStore == null || Globals.cookieStore.getCookies().isEmpty()) {
            Log.d(TAG, "==============Cookie NULL:  No se envian LOCATIONS Pendientes");
            return;
        }
        */

        db = new AppDatabase(context);
        List<LocationTable> list = db.selectLocationByEstado("PENDIENTE");
        Log.d(TAG, "============== Se encontraron " + list.size() + " Locations PENDIENTES ");

        if (list.size() > 0) {
            for (LocationTable l: list){
                location = l;

                CommDelegateAndroid delegateSendLocation = new CommDelegateAndroid() {
                    @Override
                    public void onError() {
                        Log.e(TAG, this.exception.getMessage());
                        //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
                    }

                    @Override
                    public void onSuccess() {
                       Log.d(TAG, "LOCATION enviada correctamente. OK");
                    }
                };

                new Comm().requestGet(CommReq.CommReqSendLocation, new String[][]{
                        {"latitude", l.getLatitude()},
                        {"longitude", l.getLongitude()},
                        {"user_id", l.getId_user()},
                        {"time", l.getTime()},
                        {"date", l.getDate()},
                }, delegateSendLocation);

                db.deleteLocation(location);
            }
        }



    }
}
