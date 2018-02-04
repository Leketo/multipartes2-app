package py.multipartesapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cobranza;
import py.multipartesapp.beans.Entrega;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 09/02/2016.
 */
public class ListEntregaActivity extends ActionBarActivity {
    public static final String TAG = ListEntregaActivity.class.getSimpleName();

    private List<Entrega> listEntrega;
    private AppDatabase db = new AppDatabase(this);
    private ImageAdapter adapter;
    private Button nuevoEntregaBtn;
    private ListView listEntregaListView;
    DecimalFormat formateador = new DecimalFormat("###,###.##");
    private Button ordenarBtn;
    private Integer userId;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_entregas);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Lista de Entregas");

        nuevoEntregaBtn = (Button) findViewById(R.id.entregas_btn_new);
        listEntregaListView = (ListView) findViewById(R.id.entrega_list);
        ordenarBtn = (Button) findViewById(R.id.entregas_ordenar_btn);

        userId = db.selectUsuarioLogeado().getUserId();
        listEntrega = db.selectEntregaByIdUser(userId, Globals.ordenEntregas);
        adapter = new ImageAdapter (this);
        listEntregaListView.setAdapter(adapter);

        Log.d(TAG, "cantidad de entregas encontrados:" + listEntrega.size());

        nuevoEntregaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListEntregaActivity.this, EntregaActivity.class);
                startActivity(intent);
            }
        });

        ordenarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                userId = db.selectUsuarioLogeado().getUserId();
                if (Globals.ordenEntregas.equals("ASC")){
                    ordenarBtn.setText("Más nuevo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_collapse, 0, 0, 0);
                    Globals.setOrdenEntregas("DESC");

                    listEntrega = db.selectEntregaByIdUser(userId, Globals.ordenEntregas);
                    ((BaseAdapter) listEntregaListView.getAdapter()).notifyDataSetChanged();

                }else {
                    ordenarBtn.setText("Más viejo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_expand, 0, 0, 0);
                    Globals.setOrdenEntregas("ASC");

                    listEntrega = db.selectEntregaByIdUser(userId, Globals.ordenEntregas);
                    ((BaseAdapter) listEntregaListView.getAdapter()).notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        userId = db.selectUsuarioLogeado().getUserId();
        listEntrega = db.selectEntregaByIdUser(userId, Globals.ordenCobros);
        ((BaseAdapter) listEntregaListView.getAdapter()).notifyDataSetChanged();
        super.onResume();
    }

    public String formatearMoneda (String valor){
        String formatted = "";
        String valorLimpio = limpiarMoneda(valor);
        if (!valorLimpio.isEmpty()){
            int  i = Integer.valueOf(valorLimpio);
            //formatted = formateador.format(i).toString();

            NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.GERMANY);
            DecimalFormat decimalFormat = (DecimalFormat)numberFormat;
            decimalFormat.applyPattern("###,###.###");
            formatted = decimalFormat.format(i);
        }
        return formatted;
    }

    public String limpiarMoneda (String valor){
        String valorLimpio = valor.replaceAll("[.]","");
        valorLimpio = valorLimpio.replaceAll("[,]","");
        return  valorLimpio;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, ConfiguracionActivity.class);
                startActivity(intent);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private LayoutInflater mInflater;

        public ImageAdapter(Context c) {
            mContext = c;
            mInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return listEntrega.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {

            Entrega item = listEntrega.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_registro_visita, viewGroup, false);
                //v.setTag(R.id.img_places, v.findViewById(R.id.img_places));
                v.setTag(R.id.txt1_item_registro_visita, v.findViewById(R.id.txt1_item_registro_visita));
                v.setTag(R.id.txt2_item_registro_visita, v.findViewById(R.id.txt2_item_registro_visita));
                v.setTag(R.id.icon_item_registro_visita, v.findViewById(R.id.icon_item_registro_visita));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_registro_visita);
            titleTextView.setText(item.getNombre_cliente());

            TextView subTitleTextView = (TextView) v.findViewById(R.id.txt2_item_registro_visita);
            subTitleTextView.setText("Fecha: "+item.getDate_delivered());

            ImageView iconItem = (ImageView) v.getTag(R.id.icon_item_registro_visita);
            if (item.getEstado_envio() == null) {
                iconItem.setImageResource(R.drawable.ic_check);
            } else if (item.getEstado_envio().equals("ENVIADO")){
                iconItem.setImageResource(R.drawable.ic_check);
            }else if (item.getEstado_envio().equals("PENDIENTE")){
                iconItem.setImageResource(R.drawable.ic_sync);
            }

            return v;
        }
    }
}
