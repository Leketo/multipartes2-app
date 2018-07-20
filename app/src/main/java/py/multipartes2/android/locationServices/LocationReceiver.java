package py.multipartes2.locationServices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import py.multipartes2.android.activities.Main;
import py.multipartes2.beans.LocationTable;
import py.multipartes2.beans.Session;
import py.multipartes2.comm.Comm;
import py.multipartes2.comm.CommDelegateAndroid;
import py.multipartes2.comm.CommReq;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.AppUtils;

/**
 * Created by Adolfo on 08/12/2015.
 */
public class LocationReceiver extends BroadcastReceiver {

    public static final String TAG = LocationReceiver.class.getSimpleName();
    double latitude, longitude;
    AppDatabase db;

    @Override
    public void onReceive(final Context context, final Intent calledIntent)
    {
        latitude = calledIntent.getDoubleExtra("latitude", -1);
        longitude = calledIntent.getDoubleExtra("longitude", -1);
        updateRemote(latitude, longitude);
    }

    private void updateRemote(final double latitude, final double longitude )
    {
        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
        db = new AppDatabase(Main.getAppContext());
        Session session = db.selectUsuarioLogeado();

        Log.d("LOC_RECEIVER", "hora: "+hora +"========= latitude:"+latitude + " ===== longitude"+longitude );
        Log.d("LOC_RECEIVER", "================= Enviar ubicaciones a servidor");

        //si no hay usuario no hacer nada con el Location y salir
        if (session ==null || session.getUserId() == null ){
            Log.d("LOC_RECEIVER", "================= Session = null, no se envia LOCATION");
            return;
        }

        LocationTable location = new LocationTable();
        location.setLatitude(String.valueOf(latitude));
        location.setLongitude(String.valueOf(longitude));
        location.setDate(fecha);
        location.setTime(hora);
        location.setId_user(session.getUserId().toString());
        location.setEstado_envio("PENDIENTE");

        CommDelegateAndroid delegateSendLocation = new CommDelegateAndroid(){
            @Override
            public void onError(){
                Log.e(TAG, this.exception.getMessage());
                //AppUtils.handleError(this.exception.getMessage(), LoginActivity.this);
            }
            @Override
            public void onSuccess(){
                Log.d(TAG, "LOCATION enviada correctamente. OK");
            }
        };

        boolean enLinea = AppUtils.isOnline(Main.getAppContext());
        if (enLinea){
            new Comm().requestGet(CommReq.CommReqSendLocation, new String[][]{
                    {"latitude", location.getLatitude()},
                    {"longitude", location.getLongitude()},
                    {"user_id", location.getId_user()},
                    {"time", location.getTime()},
                    {"date", location.getDate()},
            }, delegateSendLocation);
        } else {
            db.insertLocation(location);
        }
    }
}
