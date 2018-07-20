package py.multipartes2.customAutoComplete;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;

import py.multipartes2.android.activities.ConsultaStockActivity;

/**
 * Created by Adolfo on 08/11/2016.
 */
public class ConsultaStockActivityProductoTextChangedListener implements TextWatcher {

    public static final String TAG = ConsultaStockActivityProductoTextChangedListener.class.getSimpleName();
    Context context;

    public ConsultaStockActivityProductoTextChangedListener(Context context){
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
            ConsultaStockActivity consultaStockActivity = ((ConsultaStockActivity) context);

            // query the database based on the user input
            consultaStockActivity.itemsProductos = consultaStockActivity.getProductosFiltrados(userInput.toString());

            // update the adapater
            consultaStockActivity.productoAdapter.notifyDataSetChanged();
            consultaStockActivity.productoAdapter = new ArrayAdapter<String>(consultaStockActivity, android.R.layout.simple_dropdown_item_1line, consultaStockActivity.itemsProductos);
            consultaStockActivity.productoAutoComplete.setAdapter(consultaStockActivity.productoAdapter);
        }

    }
}
