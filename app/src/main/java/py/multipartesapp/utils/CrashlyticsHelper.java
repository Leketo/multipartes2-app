package py.multipartesapp.utils;

import android.content.Context;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.fabric.sdk.android.services.concurrency.UnmetDependencyException;
import py.multipartesapp.BuildConfig;
import py.multipartesapp.beans.Usuario;

public final class CrashlyticsHelper {

    private static final String TAG = CrashlyticsHelper.class.getSimpleName();
    private static boolean initialized;

    private CrashlyticsHelper() {
    }

    public static synchronized void initialize(Context context) {
        if (initialized || BuildConfig.DEBUG) {
            return;
        }

        try {
            Fabric.with(context, new Crashlytics());
            initialized = true;
        } catch (UnmetDependencyException e) {
            Log.w(TAG, "Crashlytics disabled: missing build ID.", e);
        } catch (RuntimeException e) {
            Log.w(TAG, "Crashlytics disabled: runtime initialization failed.", e);
        }
    }

    public static void logUser(Usuario usuario) {
        if (!initialized || usuario == null) {
            return;
        }

        Crashlytics.setUserIdentifier(String.valueOf(usuario.getId()));
        Crashlytics.setUserEmail(usuario.getMail());
        Crashlytics.setUserName(usuario.getName());
    }
}
