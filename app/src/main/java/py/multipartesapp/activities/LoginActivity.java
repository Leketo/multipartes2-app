package py.multipartesapp.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

//import org.apache.http.cookie.Cookie;
//import org.apache.http.impl.cookie.BasicClientCookie;

import android.widget.Toast;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import py.multipartesapp.beans.Configuracion;
import py.multipartesapp.beans.Login;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.comm.CommDelegateAndroid;
import py.multipartesapp.comm.CommReq;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.R;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activiy_login);

        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            Log.d(TAG, "version name:" + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT > 9){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //Si aun no tiene Configuracion, saltar a la pagina de Configuracion
        Configuracion url = db.selectConfiguracionByClave("URL");
        if (url.getValor() == null){
            Intent intent = new Intent(LoginActivity.this, ConfiguracionActivity.class);
            startActivity(intent);
            finish();
        } else {
            Comm.URL = url.getValor();
        }
        //Globals.setUrl(url);
        //Configuracion puerto = db.selectConfiguracionByClave("PUERTO");
        //Globals.setPuerto(puerto);


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

    private void login () {
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
        /*
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
        */

        JSONObject json = null;
        try {
            json = prepararDatosEnvio(usernameEditText.getText().toString().trim(),
                    passwordEditText.getText().toString().trim());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        loginServer(json.toString());

    }


    public  void getUsuarioLogeadoNuevo(String token){

        Log.d(TAG, "getUsuarioLogeadoNuevo. Datos recibidos");
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
        }, delegateGetUsuario,false);
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
        }, delegateGetUsuario,false);

    }

    private String loginServer(String json){
        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            String url = Comm.URL + "erp/api/authenticate";
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);


            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json, HTTP.ASCII);

            // 6. set httpPost Entity
            httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            Log.d(TAG, "enviando post: "+ url);
            Log.d(TAG, "mensaje post: "+ json);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: "+code);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

            if (code == 200){
                if (result.contains("Portal Movil Tigo")){
                    Log.d(TAG, "Sin conexion para login");


                    Context context = getApplicationContext();
                    CharSequence text = "No hay conexión.";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
                    toast.show();
                    finish();

                    return "NO_ENVIADO_SIN_CONEXION_A_INTERNET";

                }

                /* si llego hasta aca, login correcto */
                JSONObject resultJson = new JSONObject(result);
                Log.d(TAG, resultJson.get("id_token").toString());
                String token=resultJson.get("id_token").toString();

                Login login = new Login();
                login.setUserName(usernameEditText.getText().toString());
                login.setStatus("ACTIVE");
                login.setSessionID(resultJson.get("id_token").toString());


                Globals.setIsLogged(true);
                db.insertLogin(login);
//                getUsuarioLogeado();
                getDatosUsuario(token);



                return "ENVIADO_CORRECTAMENTE";
            } else {
                AppUtils.handleError("Acceso denegado.", LoginActivity.this);
                progressBar.setVisibility(View.GONE);
            }

            Log.d(TAG, "resultado  post: "+ result);
        } catch (Exception e) {
            AppUtils.handleError("Error al enviar login.", LoginActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
        }
        return "ENVIADO_CORRECTAMENTE";

    }

    private String getDatosUsuario(String token){
        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            String url = Comm.URL + "erp/api/account";
            // 2. make POST request to the given URL
            HttpGet httpPost = new HttpGet(url);


            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("Authorization", "Bearer "+token);

            Log.d(TAG, "obtener datos usuario: "+ url);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: "+code);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }

            if (code == 200){


                /* si llego hasta aca, login correcto */
                JSONObject resultJson = new JSONObject(result);
                Log.d(TAG, resultJson.get("id").toString());
                int userId=Integer.parseInt(resultJson.get("id").toString());

                Session session = new Session();
                session.setUserId(userId);
                
                session.setSession(token);
                db.insertSession(session);

                Intent intent = new Intent(LoginActivity.this, Main.class);
                startActivity(intent);
                finish();


                return "ENVIADO_CORRECTAMENTE";
            } else {
                AppUtils.handleError("Acceso denegado.", LoginActivity.this);
                progressBar.setVisibility(View.GONE);
            }

            Log.d(TAG, "resultado  post: "+ result);
        } catch (Exception e) {
            AppUtils.handleError("Error al obtener datos de usuario.", LoginActivity.this);
            Log.e(TAG, e.getStackTrace().toString() + e.getMessage());
        }
        return "ENVIADO_CORRECTAMENTE";

    }


    private JSONObject prepararDatosEnvio(String user, String pass) throws JSONException {

        //  build jsonObject
        JSONObject jsonObject = new JSONObject();

        jsonObject.accumulate("username", user);
        jsonObject.accumulate("password", pass);
        jsonObject.accumulate("remember", true);

        return jsonObject;

    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

}
