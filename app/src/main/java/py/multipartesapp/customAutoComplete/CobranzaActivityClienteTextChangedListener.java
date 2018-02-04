package py.multipartesapp.customAutoComplete;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;

import py.multipartesapp.activities.CobranzaActivity;
import py.multipartesapp.activities.EntregaActivity;

/**
 * Created by Adolfo on 07/07/2016.
 */
public class CobranzaActivityClienteTextChangedListener implements TextWatcher {

    public static final String TAG = CobranzaActivityClienteTextChangedListener.class.getSimpleName();
    Context context;

    public CobranzaActivityClienteTextChangedListener(Context context){
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
        if (userInput.length() > 1 && userInput.length() < 15) {
            CobranzaActivity cobranzaActivity = ((CobranzaActivity) context);

            // query the database based on the user input
            cobranzaActivity.itemsClientes = cobranzaActivity.getClientesFiltrados(userInput.toString());

            // update the adapater
            cobranzaActivity.clienteAdapter.notifyDataSetChanged();
            cobranzaActivity.clienteAdapter = new ArrayAdapter<String>(cobranzaActivity, android.R.layout.simple_dropdown_item_1line, cobranzaActivity.itemsClientes);
            cobranzaActivity.clienteAutoComplete.setAdapter(cobranzaActivity.clienteAdapter);
        }

    }
}
