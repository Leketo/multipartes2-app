package py.multipartes2.locationServices;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import py.multipartes2.android.activities.Main;
import py.multipartes2.beans.Session;
import py.multipartes2.comm.Comm;
import py.multipartes2.comm.CommDelegateAndroid;
import py.multipartes2.comm.CommReq;
import py.multipartes2.db.AppDatabase;

/**
 * Created by Adolfo on 08/12/2015.
 */
public class LocationService extends Service {
    LocationManager lm;
    public static final String TAG = LocationService.class.getSimpleName();

    AppDatabase db;
    @Override
    public int onStartCommand(Intent intent,int flags,int startId){

        Log.i(TAG,"Service ontstartcommand");
        addLocationListener();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        //Log.d("Location Service", "==============se llama a onDestroy del servicio");
        super.onDestroy();
    }

    private void addLocationListener(){

        Thread triggerService;
        triggerService = new Thread(new Runnable(){
            public void run(){
                try{
                    Looper.prepare();//Initialise the current thread as a looper.
                    lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

                    Criteria c = new Criteria();
                    c.setAccuracy(Criteria.ACCURACY_COARSE);
                    //final String PROVIDER = lm.getBestProvider(c, true);

                    boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                    boolean network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

                    String provider=null;
                    if (gps_enabled){
                        provider = LocationManager.GPS_PROVIDER;
                    }else {
                        provider = LocationManager.NETWORK_PROVIDER;
                    }

                    MyLocationListener myLocationListener = new MyLocationListener();
                    // 600000=10 minutos, 30000=30 segundos
                    lm.requestLocationUpdates(provider, 1000, 1, myLocationListener);
                    Log.d("Location Service", "==============Service RUNNING! cada 5 minutos");
                    Looper.loop();
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }, "LocationThread");
        triggerService.start();
    }

    public  void updateLocation(Location location)
    {
        Log.d("LocationService", "=========== updateLocation");

        Context appCtx = Main.getAppContext();
        double latitude, longitude;

        latitude = location.getLatitude();
        longitude = location.getLongitude();

        Intent filterRes = new Intent();
        filterRes.setAction("xxx.yyy.intent.action.LOCATION");
        filterRes.putExtra("latitude", latitude);
        filterRes.putExtra("longitude", longitude);

        String hora = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String fecha = new SimpleDateFormat("dd-MM-yyyy").format(new Date());

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
        final TelephonyManager tm = (TelephonyManager) getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

        final String tmDevice, tmSerial, androidId;
        tmDevice = "" + tm.getDeviceId();
        tmSerial = "" + tm.getSimSerialNumber();
        androidId = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        UUID deviceUuid = new UUID(androidId.hashCode(), ((long)tmDevice.hashCode() << 32) | tmSerial.hashCode());
        String deviceId = deviceUuid.toString();


        Log.d(TAG,"androidId: "+androidId);
        Log.d(TAG,"tmDevice: "+tmDevice);
        Log.d(TAG,"tmSerial: "+tmSerial);
        Log.d(TAG,"deviceUuid: "+deviceId);


        //lanzar solo si applicationContext != null
        if (appCtx != null){
            appCtx.sendBroadcast(filterRes);
        }else{

            db = new AppDatabase(this.getApplicationContext());
            Session session = db.selectUsuarioLogeado();

            new Comm().requestGet(CommReq.CommReqSendLocation, new String[][]{
                    {"latitude", String.valueOf(latitude)},
                    {"longitude", String.valueOf(longitude)},
                    {"user_id", ""+session.getUserId().toString()},
                    {"time", hora},
                    {"date", fecha},
            }, delegateSendLocation);
        }


    }

    class MyLocationListener implements LocationListener
    {
        @Override
        public void onLocationChanged(Location location)
        {
            updateLocation(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}
        @Override
        public void onProviderEnabled(String provider) {}
        @Override
        public void onProviderDisabled(String provider) {}
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
