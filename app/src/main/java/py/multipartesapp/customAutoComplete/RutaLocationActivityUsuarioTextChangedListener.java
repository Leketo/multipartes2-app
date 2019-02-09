package py.multipartes2.customAutoComplete;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import py.multipartesapp.activities.RutaLocationNewActivity;

/**
 * Created by Adolfo on 07/07/2016.
 */
public class RutaLocationActivityUsuarioTextChangedListener implements TextWatcher {

    public static final String TAG = RutaLocationActivityUsuarioTextChangedListener.class.getSimpleName();
    Context context;

    public RutaLocationActivityUsuarioTextChangedListener(Context context){
        this.context = context;
    }

    @Override
    public void afterTextChanged(Editable s) {
        // TODO Auto-generated method stub

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count,
                                  int after) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTextChanged(CharSequence userInput, int start, int before, int count) {

        // buscar solo a partir de dos caracteres y hasta 15
        if (userInput.length() > 1 && userInput.length() < 10) {
            RutaLocationNewActivity rutaLocationNewActivity = ((RutaLocationNewActivity) context);

            // query the database based on the user input
            rutaLocationNewActivity.itemsVendedores = rutaLocationNewActivity.getVendedoresFiltrados(userInput.toString());

            // update the adapater
            rutaLocationNewActivity.vendedoresAdapter.notifyDataSetChanged();
            rutaLocationNewActivity.vendedoresAdapter = new ArrayAdapter<String>(rutaLocationNewActivity, android.R.layout.simple_dropdown_item_1line, rutaLocationNewActivity.itemsVendedores);
            rutaLocationNewActivity.vendedoresAutoComplete.setAdapter(rutaLocationNewActivity.vendedoresAdapter);
        }

    }
}
