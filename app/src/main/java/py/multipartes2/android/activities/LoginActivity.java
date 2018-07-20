package py.multipartes2.android.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

//import org.apache.http.cookie.Cookie;
//import org.apache.http.impl.cookie.BasicClientCookie;

import py.multipartes2.beans.Login;
import py.multipartes2.beans.Session;
import py.multipartes2.beans.Usuario;
import py.multipartes2.comm.Comm;
import py.multipartes2.comm.CommDelegateAndroid;
import py.multipartes2.comm.CommReq;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.R;
import py.multipartes2.utils.AppUtils;
import py.multipartes2.utils.Globals;

/**
 * Created by Adolfo on 10/06/2015.
 */
public class LoginActivity extends Activity {
    public static final String TAG = LoginActivity.class.getSimpleName();


    private Button loginBtn;
    private EditText usernameEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;

    private AppDatabase db = new AppDatabase(this);
    private String versionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activiy_login);

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d(TAG, "version name:" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //Si aun no tiene Configuracion, saltar a la pagina de Configuracion
        /*
        Configuracion url = db.selectConfiguracionByClave("URL");
        if (url.getValor() == null){
            Intent intent = new Intent(LoginActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
            finish();
        }
        Globals.setUrl(url);
        Configuracion puerto = db.selectConfiguracionByClave("PUERTO");
        Globals.setPuerto(puerto);
        */

        //Si ya esta logueado saltar al Menu principal y guardar url
        /*
        if (Globals.cookieStore != null && Globals.cookieStore.getCookies().size() > 0){
            Intent intent = new Intent(LoginActivity.this, Main.class);
            startActivity(intent);
            finish();
        } else {
        */
            //buscamos un login activo
            Login login = db.selectLoginActive();
            if (login.getSessionID() != null){
                Log.d(TAG, "Ya existe un Login Activo en la BD: "+login.getSessionID());
//                BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", login.getSessionID());
//                cookie.setDomain("186.2.196.105");
//                cookie.setPath("/");
//                Globals.cookieStore.addCookie(cookie);
                Intent intent = new Intent(LoginActivity.this, Main.class);
                startActivity(intent);
                finish();
            }
        //}


        usernameEditText = (EditText) findViewById(R.id.login_username);
        passwordEditText = (EditText) findViewById(R.id.login_password);
        loginBtn = (Button) findViewById(R.id.login_btn_entrar);
        progressBar = (ProgressBar) findViewById(R.id.login_progress_bar);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (usernameEditText.getText().toString().trim().isEmpty()){
                    AppUtils.handleError("Ingrese nombre de usuario.", LoginActivity.this);
                    return;
                }
                if (passwordEditText.getText().toString().trim().isEmpty()){
                    AppUtils.handleError("Ingrese contraseña.", LoginActivity.this);
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                login();


            }
        });
    }

    private void login (){
        Boolean enLinea = AppUtils.isOnline(getApplicationContext());
        //login sin conexion
        if (!enLinea){
            //buscar usuario
            Usuario usuario = db.buscarUsuario(usernameEditText.getText().toString().trim(), passwordEditText.getText().toString());
            if (usuario.getName() == null){
                progressBar.setVisibility(View.GONE);
                String[] buttons = {"Ok"};
                AppUtils.show(null, "Acceso denegado.", buttons, LoginActivity.this, false, null);
                return;
            }else {
                // borrar login y session
                db.deleteLogin();
                db.deleteSession();

                //setear login y cookie
                Login login = new Login();
                login.setUserName(usernameEditText.getText().toString());
                login.setStatus("ACTIVE");
                login.setSessionID("un_cookie_creado_en_login_sin_conexion");
                db.insertLogin(login);

                //setear session
                Session session = new Session();
                session.setUserId(usuario.getId());
                db.insertSession(session);

                Intent intent = new Intent(LoginActivity.this, Main.class);
                startActivity(intent);
                finish();
                return;
            }
        }

        CommDelegateAndroid delegateLogin = new CommDelegateAndroid(){
            @Override
            public void onError(){
                progressBar.setVisibility(View.GONE);
                AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
            }
            @Override
            public void onSuccess(){
                progressBar.setVisibility(View.GONE);
                Log.d(TAG, "======Login code_request: "+Globals.getLast_request_code());
                Globals.setIsLogged(true);
                Log.d(TAG, "=============== Valor de is Logged en login: "+Globals.isLogged);

                if (Globals.getLast_request_code() == 200){
                    Login login = new Login();
                    login.setUserName(usernameEditText.getText().toString());
                    login.setStatus("ACTIVE");
                    login.setSessionID("logeado online");

                    //Se guarda cookie en la BD
                    //for (Cookie c : Globals.cookieStore.getCookies()){
                    //    login.setSessionID(c.getValue());
                    //}
                    //al llegar un login correcto almacenar en la bd el user
                    db.insertLogin(login);
                    getUsuarioLogeado();
                }else{
                    AppUtils.handleError("Acceso denegado.", LoginActivity.this);
                }
            }
        };

        new Comm().requestPost(CommReq.CommReqLogin, new String[][]{
                {"j_username", usernameEditText.getText().toString().trim()},
                {"j_password", passwordEditText.getText().toString().trim()},
        }, delegateLogin);


    }

    public  void getUsuarioLogeado(){

        Log.d(TAG, "=============== Se logueo bien se llama a currentUser");
        CommDelegateAndroid delegateGetUsuario = new CommDelegateAndroid(){
            @Override
            public void onError(){
                Log.e(TAG, this.exception.getMessage());
                //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
            }
            @Override
            public void onSuccess(){
                Log.d(TAG, "Usuario. Datos recibidos");
                Comm.CommResponse r = response;
                Session session = null;

                try {
                    session = (Session) r.getBean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "Usuario Logueado. Datos recibidos: "+ session.toString());
                db.insertSession(session);

                Intent intent = new Intent(LoginActivity.this, Main.class);
                startActivity(intent);
                finish();
            }
        };

        new Comm().requestGet(CommReq.CommReqGetUserLoged, new String[][]{
                {"username",usernameEditText.getText().toString()}
        }, delegateGetUsuario);

    }
}
