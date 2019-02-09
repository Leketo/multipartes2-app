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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.Pedido;
import py.multipartesapp.beans.Session;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 09/02/2016.
 */
public class ListPedidoActivity extends ActionBarActivity {
    public static final String TAG = ListPedidoActivity.class.getSimpleName();

    private List<Pedido> listPedido;
    private AppDatabase db = new AppDatabase(this);
    private ImageAdapter adapter;
    private Button nuevoPedidoBtn;
    private ListView listPedidoListView;
    private Button ordenarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_pedidos);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Lista de Pedidos");

        nuevoPedidoBtn = (Button) findViewById(R.id.pedidos_btn_new);
        listPedidoListView = (ListView) findViewById(R.id.pedidos_list);
        ordenarBtn = (Button) findViewById(R.id.pedidos_ordenar_btn);

        Session session = db.selectUsuarioLogeado();
        listPedido = db.selectPedidoByUser(session.getUserId(), Globals.ordenPedidos);

        adapter = new ImageAdapter (this);
        listPedidoListView.setAdapter(adapter);

        Log.d(TAG, "cantidad de pedidos encontrados:" + listPedido.size());

        nuevoPedidoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListPedidoActivity.this, PedidoActivity.class);
                startActivity(intent);
            }
        });

        ordenarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Session session = db.selectUsuarioLogeado();

                if (Globals.ordenPedidos.equals("ASC")){
                    ordenarBtn.setText("Más nuevo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_collapse, 0, 0, 0);
                    Globals.setOrdenPedidos("DESC");

                    listPedido = db.selectPedidoByUser(session.getUserId(), Globals.ordenPedidos);
                    ((BaseAdapter) listPedidoListView.getAdapter()).notifyDataSetChanged();

                }else {
                    ordenarBtn.setText("Más viejo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_expand, 0, 0, 0);
                    Globals.setOrdenPedidos("ASC");

                    listPedido = db.selectPedidoByUser(session.getUserId(), Globals.ordenPedidos);
                    ((BaseAdapter) listPedidoListView.getAdapter()).notifyDataSetChanged();
                }
            }
        });

        //ver o modificar pedido
        listPedidoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Pedido pedido = (Pedido) listPedido.get(position);
                Globals.setPedidoSeleccionado(pedido);

                if (pedido.getIsactive().equals("Y")){
                    Globals.setAccion_pedido("EDITAR");
                }else{
                    Globals.setAccion_pedido("VER");
                }


                Intent intent = new Intent(ListPedidoActivity.this, PedidoActivity.class);
                startActivity(intent);
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
        Session session = db.selectUsuarioLogeado();
        listPedido = db.selectPedidoByUser(session.getUserId(), Globals.ordenPedidos);

        ((BaseAdapter) listPedidoListView.getAdapter()).notifyDataSetChanged();
        super.onResume();
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
            return listPedido.size();
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

            Pedido item = listPedido.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_registro_visita, viewGroup, false);
                //v.setTag(R.id.img_places, v.findViewById(R.id.img_places));
                v.setTag(R.id.txt1_item_registro_visita, v.findViewById(R.id.txt1_item_registro_visita));
                v.setTag(R.id.txt2_item_registro_visita, v.findViewById(R.id.txt2_item_registro_visita));
                v.setTag(R.id.icon_item_registro_visita, v.findViewById(R.id.icon_item_registro_visita));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_registro_visita);
            Cliente c = db.selectClienteById(item.getClient_id());
            titleTextView.setText(c.getNombre());

            TextView subTitleTextView = (TextView) v.findViewById(R.id.txt2_item_registro_visita);


            //Usuario usuario = db.selectUsuarioById(item.getUser_id());
            //subTitleTextView.setText("Creado por: "+usuario.getName() + "- Fecha: "+ item.getDate_order());
            String cobrado = "";
            if (item.getIsinvoiced()!= null && item.getIsinvoiced().equals("Y"))
                cobrado = "SI";
            else
                cobrado = "NO";
            subTitleTextView.setText("Fecha: "+ item.getDate_order() + " - Cobrado: "+cobrado);

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
