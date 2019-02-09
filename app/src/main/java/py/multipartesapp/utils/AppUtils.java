package py.multipartes2.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import py.multipartes2.beans.Login;

/**
 * Created by Adolfo on 11/06/2015.
 */
public class AppUtils {
    private static String TAG = AppUtils.class.getSimpleName();
    private static Boolean servidorEnLinea = false;


    public static void handleError(String msg, Context context) {
        AppUtils.showError(msg, context);
    }

    public static AlertDialog showError(String msg, Context ctx) {
        return show("Error", msg, null, ctx, true, null);
    }

    public static AlertDialog show(String title, String msg, String[] buttons,
                                   Context ctx, boolean cancelable, DialogInterface.OnClickListener onClickListener) {
        try {
            AlertDialog.Builder b = new AlertDialog.Builder(ctx);
            AlertDialog a = b.create();
            a.setTitle(title);
            a.setMessage(msg);
            a.setCancelable(cancelable);
            if (buttons != null) {
                if (buttons.length > 3) {
                    throw new RuntimeException("buttons.length>3, len = "
                            + buttons.length);
                }
                if (buttons.length >= 1)
                    a.setButton(buttons[0], onClickListener);
                if (buttons.length >= 2)
                    a.setButton2(buttons[1], onClickListener);
                if (buttons.length >= 3)
                    a.setButton3(buttons[2], onClickListener);

            }
            a.show();
            return a;
        }catch (Exception e){
            Log.e(TAG,"Tu código apunta a la nada :'(");
        }
        return null;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        Boolean conectadoRed = netInfo != null && netInfo.isConnectedOrConnecting();
        servidorEnLinea = false;

        return conectadoRed;
        /*
        if (conectadoRed){
            CommDelegateAndroid delegate = new CommDelegateAndroid() {
                @Override
                public void onError() {
                    //handleError(this.exception);
                    Log.d(TAG, "Sin conexión al servidor");
                }
                @Override
                public void onSuccess() {

                    servidorEnLinea = true;
                }
            };
            new Comm().requestGet(CommReq.CommReqStatusTestValidConn, new String[][]{
            }, delegate);

            Log.d(TAG,"Servidor en linea -> "+ servidorEnLinea);
        }
        return  false ; */
    }

    public static Login openLoginFile(Context ctx) {
        FileInputStream fi = null;

        try {
            fi = ctx.openFileInput("login.json");
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int r;
            while ((r = fi.read(buffer)) != -1) {
                bo.write(buffer, 0, r);
            }
            JSONObject json = new JSONObject(new String(bo.toByteArray(), "UTF-8"));
            json.put("success", true);
            return (Login) new Login(json);

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            Log.e("error", "IOException al intentar abrir archivo de configuración", e);
            // if file doens't exist, it's ok, user not logged in
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } finally {
            if (fi != null) {
                try {
                    fi.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                };
            }
        }
        return null;
    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    public static List<String> getListaNombresByIdProducto (String[] files, String idProducto){
        List<String> result = new ArrayList<String>();
        for (String name : files){
            if (name.contains(idProducto)){
                result.add(name);
            }
        }
        return result;
    }

}
