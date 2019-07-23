package py.multipartesapp.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;

import org.apache.mina.util.byteaccess.ByteArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.RegistroVisita;
import py.multipartesapp.beans.Session;
import py.multipartesapp.beans.Usuario;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 18/10/2015.
 */
public class ListRegistroVisitasActivity  extends ActionBarActivity {
    public static final String TAG = ListRegistroVisitasActivity.class.getSimpleName();

    private List<RegistroVisita> listRegistroVisita;
    private AppDatabase db = new AppDatabase(this);
    private ImageAdapter adapter;
    private Button nuevaVisitaBtn;
    private ListView listRegistroVisitaListView;
    private Button ordenarBtn;

    Usuario usuario;
    ArrayList<RegistroVisita> ejemplo;
    ArrayList<String>ListaInformacion;
    ArrayList<Usuario>ListaUsuarios;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_registrovisistas);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Registros de Visita");

        nuevaVisitaBtn = (Button) findViewById(R.id.reg_visita_btn_new);

        listRegistroVisitaListView = (ListView) findViewById(R.id.reg_visita_list);

        ordenarBtn = (Button) findViewById(R.id.reg_visita_ordenar_btn);

        Session session = db.selectUsuarioLogeado();
        usuario = db.selectUsuarioById(session.getUserId());
        listRegistroVisita = db.selectRegistroVisitaByNomUser(String.valueOf(session.getUserId()), Globals.ordenRegVisitas);

        adapter = new ImageAdapter (this);
        listRegistroVisitaListView.setAdapter(adapter);

        Log.d(TAG,"cantidad de visitas encontradas:"+ listRegistroVisita.size());
        //Toast.makeText(getApplicationContext(), "hola"+listRegistroVisita.size(), Toast.LENGTH_LONG).show();

        nuevaVisitaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRegistroVisitasActivity.this, RegistroVisitasActivity.class);
                startActivity(intent);
            }
        });

        ordenarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Globals.ordenRegVisitas.equals("ASC")){
                    ordenarBtn.setText("Más nuevo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_collapse, 0, 0, 0);
                    Globals.setOrdenRegVisitas("DESC");

                    listRegistroVisita = db.selectRegistroVisitaByNomUser(String.valueOf(session.getUserId()), Globals.ordenRegVisitas);
                    ((BaseAdapter) listRegistroVisitaListView.getAdapter()).notifyDataSetChanged();

                }else {
                    ordenarBtn.setText("Más viejo");
                    ordenarBtn.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_expand, 0, 0, 0);
                    Globals.setOrdenRegVisitas("ASC");

                    listRegistroVisita = db.selectRegistroVisitaByNomUser(String.valueOf(session.getUserId()), Globals.ordenRegVisitas);
                    ((BaseAdapter) listRegistroVisitaListView.getAdapter()).notifyDataSetChanged();

                }
            }
        });
///////////////////////////////////////////////////////////////////////

        listRegistroVisitaListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RegistroVisita pedido = (RegistroVisita) listRegistroVisita.get(position);
                Globals.setVisitaSeleccionado(pedido);
                if (pedido.getEstado_envio().equalsIgnoreCase("ENVIADO")){
                    Globals.setAccion_RV("VER");
                }else{
                    Globals.setAccion_RV("EDITAR");
                }


                Intent intent = new Intent(ListRegistroVisitasActivity.this, RegistroVisitasActivity.class);
                startActivity(intent);
            }
        });

//////////////////////////////////////////////////////////////////////
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        Session sessionLogueado = db.selectUsuarioLogeado();
        listRegistroVisita = db.selectRegistroVisitaByNomUser(String.valueOf(sessionLogueado.getUserId()), Globals.ordenRegVisitas);
        ((BaseAdapter) listRegistroVisitaListView.getAdapter()).notifyDataSetChanged();
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
            return listRegistroVisita.size();
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


            RegistroVisita item = listRegistroVisita.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_registro_visita, viewGroup, false);
                //v.setTag(R.id.img_places, v.findViewById(R.id.img_places));
                v.setTag(R.id.txt1_item_registro_visita, v.findViewById(R.id.txt1_item_registro_visita));
                v.setTag(R.id.txt2_item_registro_visita, v.findViewById(R.id.txt2_item_registro_visita));
                v.setTag(R.id.icon_item_registro_visita, v.findViewById(R.id.icon_item_registro_visita));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_registro_visita);
            Cliente c = db.selectClienteById(item.getCliente());
            titleTextView.setText(c.getNombre());

            TextView subTitleTextView = (TextView) v.findViewById(R.id.txt2_item_registro_visita);
            //formateo de fecha
            SimpleDateFormat inputFecha = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputFecha = new SimpleDateFormat("yyyy-MM-dd");
            String fechaVisita = null;
            try {
                fechaVisita = inputFecha.format(outputFecha.parse(item.getFechavisita()));
            } catch (Exception e){
                e.printStackTrace();
            }
            subTitleTextView.setText(fechaVisita + " - "+item.getHoravisita() + "hs");

            ImageView iconItem = (ImageView) v.getTag(R.id.icon_item_registro_visita);
            if (item.getEstado_envio().equals("ENVIADO")){
                iconItem.setImageResource(R.drawable.ic_check);
            }else if (item.getEstado_envio().equals("PENDIENTE")){
                iconItem.setImageResource(R.drawable.ic_sync);
            }

            return v;
        }
    }
}
