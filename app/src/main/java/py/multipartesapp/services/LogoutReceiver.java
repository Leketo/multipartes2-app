package py.multipartesapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import py.multipartesapp.activities.LoginActivity;
import py.multipartesapp.activities.Main;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

public class LogoutReceiver extends BroadcastReceiver {
    private static final String TAG = LogoutReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "Ejecutando cierre de sesion automatico");

        // Lógica para cerrar sesión
        AppDatabase db = new AppDatabase(context);
        db.deleteLogin();
        db.deleteSession();
        Globals.cookieStore.clear();

        Intent loginIntent = new Intent(context, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(loginIntent);

        Main.programarCierreDeSesionDiario(context);


    }
}
