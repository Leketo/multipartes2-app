package py.multipartes2.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import py.multipartesapp.activities.CobranzaActivity;
import py.multipartesapp.activities.EntregaActivity;
import py.multipartesapp.activities.Main;
import py.multipartesapp.activities.PedidoActivity;
import py.multipartesapp.activities.RegistroVisitasActivity;

//import py.com.walrus.mobile.activities.CobranzasCreditoActivity;

/**
 * Created by Emilio on 13/03/2015.
 */
public class NetworkChangesReceiver extends BroadcastReceiver {
    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * {@link android.content.Context# registerReceiver(android.content.BroadcastReceiver,
     * IntentFilter, String, android.os.Handler)}. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p/>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b>  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use
     * {@link android.content.Context#startService(android.content.Intent)} instead of
     * {@link android.content.Context# bindService(android.content.Intent, ServiceConnection, int)}.  If you wish
     * to interact with a service that is already running, you can use
     * {@link #peekService}.
     * <p/>
     * <p>The Intent filters used in {@link android.content.Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(android.content.Context, android.content.Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    String TAG = NetworkChangesReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG,"Se detectaron cambios en la red!");
        ConnectivityManager cm =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        if(isConnected){
            Log.d(TAG,"Conectado a: "+activeNetwork.getTypeName());
            boolean isPing = false;
            isPing = true;
            Log.d(TAG,"Se omite hacer PING.");
            /*
            try {
                isPing  = Ping.pingHost();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            */
            if(isPing){

                Log.d(TAG, "Verificando existencia de Registro de Visitas Pendientes..");
                new RegistroVisitasActivity().enviarVisitasPendientes(context);

                Log.d(TAG, "Verificando existencia de Location Pendientes..");
                new Main().enviarLocation(context);

                Log.d(TAG, "Verificando existencia de Entregas Pendientes..");
                new EntregaActivity().enviarEntregasPendientes(context);

                Log.d(TAG, "Verificando existencia de Pedidos Pendientes..");
                new PedidoActivity().enviarPedidos(context);

                Log.d(TAG, "Verificando existencia de Cobranzas Pendientes..");
                new CobranzaActivity().enviarCobranzas(context);
            }
        }else {
            Log.d(TAG,"No está conectado a ninguna red");
        }
    }
}
