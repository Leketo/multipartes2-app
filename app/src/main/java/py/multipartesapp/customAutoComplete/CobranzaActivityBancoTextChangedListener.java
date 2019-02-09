package py.multipartesapp.customAutoComplete;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import py.multipartesapp.activities.CobranzaDetalleItemActivity;

/**
 * Created by Adolfo on 07/07/2016.
 */
public class CobranzaActivityBancoTextChangedListener implements TextWatcher {

    public static final String TAG = CobranzaActivityBancoTextChangedListener.class.getSimpleName();
    Context context;

    public CobranzaActivityBancoTextChangedListener(Context context){
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
        if (userInput.length() > 1 && userInput.length() < 5) {
            CobranzaDetalleItemActivity cobranzaDetalleItemActivity = ((CobranzaDetalleItemActivity) context);

            // query the database based on the user input
            cobranzaDetalleItemActivity.itemsBancos = cobranzaDetalleItemActivity.getBancosFiltrados(userInput.toString());

            // update the adapater
            cobranzaDetalleItemActivity.bancoAdapter.notifyDataSetChanged();
            cobranzaDetalleItemActivity.bancoAdapter = new ArrayAdapter<String>(cobranzaDetalleItemActivity, android.R.layout.simple_dropdown_item_1line, cobranzaDetalleItemActivity.itemsBancos);
            cobranzaDetalleItemActivity.bancoAutoComplete.setAdapter(cobranzaDetalleItemActivity.bancoAdapter);
        }

    }
}
