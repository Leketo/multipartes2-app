package py.multipartesapp.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import py.multipartesapp.activities.LoginActivity;
import py.multipartesapp.activities.Main;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

public class LogoutReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Lógica para cerrar sesión
        AppDatabase db = new AppDatabase(context);
        db.deleteLogin();
        db.deleteSession();
        Globals.cookieStore.clear();

        Intent loginIntent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(loginIntent);

        Main.programarCierreDeSesionDiario(context);


    }
}
