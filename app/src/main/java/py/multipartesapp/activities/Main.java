package py.multipartesapp.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

//import org.apache.http.impl.cookie.BasicClientCookie;

import com.crashlytics.android.Crashlytics;

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

    private Button testBtn;

    private AppDatabase db = new AppDatabase(this);
    private static Context context;
    private LocationTable location;

    private TextView versionName;

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 123;



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
//        rutasBtn = (Button) findViewById(R.id.main_btn_rutas);
//        entregasBtn = (Button) findViewById(R.id.main_btn_entrega);
        pedidosBtn = (Button) findViewById(R.id.main_btn_pedido);
        cobranzasBtn = (Button) findViewById(R.id.main_btn_cobranza);
        catalogoBtn = (Button) findViewById(R.id.main_btn_catalogo);
//        crearRutaBtn = (Button) findViewById(R.id.main_btn_crear_ruta);
        consultasBtn = (Button) findViewById(R.id.main_btn_consultas);

        testBtn =(Button) findViewById(R.id.main_btn_test);

        versionName=(TextView) findViewById(R.id.versionName);

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

//        rutasBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Main.this, ListRutasActivity.class);
//                startActivity(intent);
//            }
//        });

//        entregasBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(Main.this, ListEntregaActivity.class);
//                startActivity(intent);
//            }
//        });

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

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent intent = new Intent(Main.this, RutaLocationNewActivity.class);
//                startActivity(intent);
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

        //logUser(usuario);

        if (usuario.getRole() != null && !usuario.getRole().equals("ROOT")){
            //crearRutaBtn.setVisibility(View.INVISIBLE);
            testBtn.setVisibility(View.INVISIBLE);
        }
        if (usuario.getName() != null){
            nombreUsuarioTextView.setText("Bienvenido, "+usuario.getName() + " "+ usuario.getLastname()+"!");
        }

        Main.context = getApplicationContext();

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;

            versionName.setText("v"+version);

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }


        String [] permisos= {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        solicitarPermisos(permisos);


       startCheckLocationService();
    }

    private void logUser(Usuario usuario) {

        Crashlytics.setUserIdentifier(""+usuario.getId());
        Crashlytics.setUserEmail(usuario.getMail());
        Crashlytics.setUserName(usuario.getName());
    }

    public void forceCrash(View view) {
        throw new RuntimeException("Crash desde la app");
    }

    public void solicitarPermisos(String[] permisos){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            Toast.makeText(this, "This version is not Android 6 or later " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();

        } else {

            for(int i=0;i<permisos.length;i++){
                int permisoConcedido = checkSelfPermission(permisos[i]);

                if (permisoConcedido != PackageManager.PERMISSION_GRANTED) {

                    requestPermissions(new String[] {permisos[i]},
                            REQUEST_CODE_ASK_PERMISSIONS);

                    //Toast.makeText(this, "Solicitando permiso", Toast.LENGTH_LONG).show();

                }else if (permisoConcedido == PackageManager.PERMISSION_GRANTED){

                    //Toast.makeText(this, "El permiso ya ha sido concedido ", Toast.LENGTH_LONG).show();

                }
            }


        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        if(REQUEST_CODE_ASK_PERMISSIONS == requestCode) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(this, "Permiso concedido ! " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();
//            } else {
//                Toast.makeText(this, "Permiso no concedido ! " + Build.VERSION.SDK_INT, Toast.LENGTH_LONG).show();
//            }
//        }else{
//            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        }
//    }


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
//        if (!servicioCorriendo) {
//            Intent mServiceIntent = new Intent(this, LocationService.class);
//            startService(mServiceIntent);
//        }
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
