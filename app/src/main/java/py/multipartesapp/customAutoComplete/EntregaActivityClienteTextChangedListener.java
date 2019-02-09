package py.multipartes2.customAutoComplete;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;

import py.multipartesapp.activities.EntregaActivity;

/**
 * Created by Adolfo on 07/07/2016.
 */
public class EntregaActivityClienteTextChangedListener implements TextWatcher {

    public static final String TAG = EntregaActivityClienteTextChangedListener.class.getSimpleName();
    Context context;

    public EntregaActivityClienteTextChangedListener(Context context){
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
        Log.d(TAG, "texto a buscar ->"+userInput);
        // buscar solo a partir de dos caracteres y hasta 15
        if (userInput.length() > 1 && userInput.length() < 15) {
            EntregaActivity entregaActivity = ((EntregaActivity) context);

            // query the database based on the user input
            entregaActivity.itemsClientes = entregaActivity.getClientesFiltrados(userInput.toString());

            // update the adapater
            entregaActivity.clienteAdapter.notifyDataSetChanged();
            entregaActivity.clienteAdapter = new ArrayAdapter<String>(entregaActivity, android.R.layout.simple_dropdown_item_1line, entregaActivity.itemsClientes);
            entregaActivity.clienteAutoComplete.setAdapter(entregaActivity.clienteAdapter);
        }

    }
}
