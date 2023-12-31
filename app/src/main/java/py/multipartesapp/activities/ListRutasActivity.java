package py.multipartesapp.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import py.multipartesapp.BuildConfig;
import py.multipartesapp.R;
import py.multipartesapp.beans.Cliente;
import py.multipartesapp.beans.RutaLocation;
import py.multipartesapp.comm.Comm;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;

/**
 * Created by Adolfo on 11/03/2016.
 */
public class ListRutasActivity extends ActionBarActivity {
    public static final String TAG = ListRutasActivity.class.getSimpleName();

    private List<RutaLocation> listRutas;
    private AppDatabase db = new AppDatabase(this);
    private ImageAdapter adapterHojaRuta;
    private ListView listRutasListView;
    private Button nuevoListBtn;
    private Button verMapaBtn;
    private Spinner filtroSpinner;
    private ProgressDialog progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_rutas);

        progress = new ProgressDialog(this);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Lista de Rutas");

        ////////////////////////////////////////////////////
        nuevoListBtn = (Button) findViewById(R.id.listanuevo_btn_new);
        ////////////////////////////////////////////////////
        if (Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        verMapaBtn = (Button) findViewById(R.id.list_rutas_btn_ver_mapa);
        listRutasListView = (ListView) findViewById(R.id.rutas_list);
        filtroSpinner = (Spinner) findViewById(R.id.list_rutas_spinner_filtro);

        List<String> listFiltros = new ArrayList<>();
        listFiltros.add("PENDIENTES");
        listFiltros.add("TODOS");
        listFiltros.add("COMPLETADOS");
        listFiltros.add("OMITIDOS");
        //     listFiltros.add("ENTREGAS");
//        listFiltros.add("PEDIDOS");
        // listFiltros.add("COBROS");
        //listFiltros.add("VISITAS");

        ArrayAdapter<String> filtroAdapter = new ArrayAdapter<String>
                (this, android.R.layout.simple_dropdown_item_1line, listFiltros);
        filtroSpinner.setAdapter(filtroAdapter);

        listRutas = db.selectAllRutaLocation();
        RutaLocation r1 = new RutaLocation();
        r1.setId(1);
        r1.setPriority(1);
        r1.setDate("2016-07-19T11:10:50-0400");
        r1.setClient_id(130320270);
        r1.setEntrada("Y");

        RutaLocation r2 = new RutaLocation();
        r2.setPriority(2);
        r2.setDate("2016-07-19T11:10:50-0400");
        r2.setClient_id(130320270);
        r2.setSalida("Y");

        //listRutas.add(r1);
        //listRutas.add(r2);

        adapterHojaRuta = new ImageAdapter(this);
        listRutasListView.setAdapter(adapterHojaRuta);

        Log.d(TAG, "cantidad de rutas encontradas:" + listRutas.size());

        //////////////////////////////////////////////////////////////////////////////////////////
        nuevoListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRutasActivity.this, RutaLocationNewActivity.class);
                startActivity(intent);
            }
        });
        /////////////////////////////////////////////////////////////////////////////////////////
        //if (listRutas.size() == 0)
        //  Toast.makeText(getApplicationContext(), "No hay hoja de ruta asignada para el día de hoy. Pruebe sincronizar para obtener su hoja de ruta.", Toast.LENGTH_LONG).show();


        verMapaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ListRutasActivity.this, RutasActivity.class);
                startActivity(intent);
            }
        });


        filtroSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String filtro = (String) parent.getAdapter().getItem(position);

                if (filtro.equals("TODOS")) {
                    listRutas = db.selectAllRutaLocation();
                    Log.i(TAG, "ListaRutas: " + listRutas.toString());
                } else if (filtro.equals("PENDIENTES")) {
                    listRutas = db.selectRutaLocationByFilter("A", null, "null", null);
                } else if (filtro.equals("OMITIDOS")) {
                    listRutas = db.selectRutaLocationByFilter("O", null, "null", null);
                } else if (filtro.equals("COMPLETADOS")) {
                    listRutas = db.selectRutaLocationByFilter("V", null, null, null);
                } else if (filtro.equals("ENTREGAS")) {
                    listRutas = db.selectRutaLocationByFilter("TODOS", "ENTREGA", null, null);
                } else if (filtro.equals("PEDIDOS")) {
                    listRutas = db.selectRutaLocationByFilter("TODOS", "PEDIDO", null, null);
                } else if (filtro.equals("COBROS")) {
                    listRutas = db.selectRutaLocationByFilter("TODOS", "COBRANZA", null, null);
                } else if (filtro.equals("VISITAS")) {
                    listRutas = db.selectRutaLocationByFilter("TODOS", "VISITA", null, null);
                }

                listRutasListView.setAdapter(null);
                listRutasListView.setAdapter(adapterHojaRuta);
                adapterHojaRuta.notifyDataSetChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No selecciono ningun filtro familia.");
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
        //listRutas = db.selectAllRutaLocation();
        ((BaseAdapter) listRutasListView.getAdapter()).notifyDataSetChanged();
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
            return listRutas.size();
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


            RutaLocation item = listRutas.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_ruta2, viewGroup, false);

                v.setTag(R.id.txt1_item_ruta, v.findViewById(R.id.txt1_item_ruta));
                //v.setTag(R.id.txt2_item_ruta, v.findViewById(R.id.txt2_item_ruta));
                v.setTag(R.id.item_ruta_entrada_btn, v.findViewById(R.id.item_ruta_entrada_btn));
                v.setTag(R.id.item_ruta_salida_btn, v.findViewById(R.id.item_ruta_salida_btn));
                v.setTag(R.id.item_ruta_entrada_btn, v.findViewById(R.id.item_ruta_entrada_btn));
                v.setTag(R.id.item_ruta_salida_btn, v.findViewById(R.id.item_ruta_salida_btn));
                v.setTag(R.id.item_ruta_btn_accion, v.findViewById(R.id.item_ruta_btn_accion));
                v.setTag(R.id.item_ruta_btn_accion, v.findViewById(R.id.item_ruta_btn_accion));
                v.setTag(R.id.item_ruta_edit_observacion, v.findViewById(R.id.item_ruta_edit_observacion));
                v.setTag(R.id.item_ruta_layout_observacion, v.findViewById(R.id.item_ruta_layout_observacion));
                v.setTag(R.id.item_ruta_btn_observacion_btn, v.findViewById(R.id.item_ruta_btn_observacion_btn));
                v.setTag(R.id.item_ruta_observacion_txt, v.findViewById(R.id.item_ruta_observacion_txt));
            }
            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_ruta);
            Cliente c = db.selectClienteById(item.getClient_id());
            titleTextView.setText(item.getPriority() + " - " + c.getNombre());

            SimpleDateFormat inputFecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            SimpleDateFormat outputFecha = new SimpleDateFormat("dd-MM-yyyy");
//                String fecha = null;
//        try {
//                fecha = outputFecha.format(inputFecha.parse(item.getDate()))
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            String dateString = new SimpleDateFormat("dd-MM-yyyy").format(new Date(item.getDate()));

            //Configuramos los botones de entrada y salida
            Button entradaBtn = (Button) v.getTag(R.id.item_ruta_entrada_btn);
            Button salidaBtn = (Button) v.getTag(R.id.item_ruta_salida_btn);
            Button actionBtn = (Button) v.getTag(R.id.item_ruta_btn_accion);
            TextView txtEntradaDate = (TextView) v.getTag(R.id.item_ruta_entrada_btn);
            TextView txtSalidaDate = (TextView) v.getTag(R.id.item_ruta_salida_btn);
            final Button enviarObservacion = (Button) v.getTag(R.id.item_ruta_btn_observacion_btn);
            final EditText observacionEditText = (EditText) v.getTag(R.id.item_ruta_observacion_txt);

            //Editar Observacion
            final ImageButton editObservacionBtn = (ImageButton) v.getTag(R.id.item_ruta_edit_observacion);
            final LinearLayout linearLayoutObservacion = (LinearLayout) v.getTag(R.id.item_ruta_layout_observacion);
            linearLayoutObservacion.setVisibility(View.GONE);
            editObservacionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {
                    if (linearLayoutObservacion.getVisibility() == View.VISIBLE) {
                        linearLayoutObservacion.setVisibility(View.GONE);
                    } else {
                        linearLayoutObservacion.setVisibility(View.VISIBLE);
                    }
                }
            });

            enviarObservacion.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    final int position = listView.getPositionForView((View) viewParent);

                    Log.d(TAG, "texto:" + observacionEditText.getText().toString() + " - pos: " + position);
                    marcarObservacion(position, observacionEditText.getText().toString());
                    linearLayoutObservacion.setVisibility(View.GONE);
                }
            });

            boolean backgroundColor = false;
            //si ya marco entrada, bloquear boton entrada
            if (item.getEntrada() != null && item.getEntrada().equals("Y")) {
                entradaBtn.setEnabled(false);
                backgroundColor = true;
                entradaBtn.setVisibility(View.GONE);
                txtEntradaDate.setVisibility(View.VISIBLE);

                SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    Date dateValue = input.parse(item.getFechaHoraEntrada());
                    SimpleDateFormat output = new SimpleDateFormat("dd/MM/yy HH:mm");
                    String fechaEntrada = output.format(dateValue);
                    txtEntradaDate.setText(fechaEntrada);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            if (item.getSalida() != null && item.getSalida().equals("Y")) {
                ;
                salidaBtn.setEnabled(false);
                if (backgroundColor) {
                    v.setBackgroundColor(Color.parseColor("#8ddebc"));
                    editObservacionBtn.setEnabled(false);
                    salidaBtn.setVisibility(View.GONE);
                    txtSalidaDate.setVisibility(View.VISIBLE);

                    SimpleDateFormat input = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date dateValue = input.parse(item.getFechaHoraSalida());
                        SimpleDateFormat output = new SimpleDateFormat("dd/MM/yy HH:mm");
                        String fechaSalida = output.format(dateValue);
                        txtSalidaDate.setText(fechaSalida);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }

            entradaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    final int position = listView.getPositionForView((View) viewParent);

                    marcarEntrada(position);
                    Log.d(TAG, "clic en boton por el lado de onItemClickListener, pos=" + position);
                }
            });

            salidaBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    View parentRow = (View) v.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    final int position = listView.getPositionForView((View) viewParent);

                    marcarSalida(position);
                    Log.d(TAG, "clic en boton por el lado de onItemClickListener, pos=" + position);
                }
            });

            if (item.getType() != null) {
                if (item.getType().equals("ENTREGA")) {
                    actionBtn.setText("Entregas");
                    actionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View parentRow = (View) v.getParent();
                            ViewParent viewParent = parentRow.getParent();
                            ViewParent viewParent2 = viewParent.getParent();
                            ListView listView = (ListView) viewParent2.getParent();
                            int position = listView.getPositionForView((View) viewParent2);
                            RutaLocation ruta = listRutas.get(position);
                            Cliente c = db.selectClienteById(ruta.getClient_id());
                            Globals.setClienteSeleccionadoRuta(c);

                            Intent intent = new Intent(ListRutasActivity.this, EntregaActivity.class);
                            startActivity(intent);
                        }
                    });
                } else if (item.getType().equals("PEDIDO")) {
                    actionBtn.setText("Pedidos");
                    actionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            View parentRow = (View) v.getParent();
                            ViewParent viewParent = parentRow.getParent();
                            ViewParent viewParent2 = viewParent.getParent();
                            ListView listView = (ListView) viewParent2.getParent();
                            int position = listView.getPositionForView((View) viewParent2);
                            RutaLocation ruta = listRutas.get(position);
                            Cliente c = db.selectClienteById(ruta.getClient_id());
                            Globals.setClienteSeleccionadoRuta(c);

                            Intent intent = new Intent(ListRutasActivity.this, PedidoActivity.class);
                            startActivity(intent);
                        }
                    });
                } else if (item.getType().equals("COBRANZA")) {
                    actionBtn.setText("Cobrar");
                    actionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View parentRow = (View) v.getParent();
                            ViewParent viewParent = parentRow.getParent();
                            ViewParent viewParent2 = viewParent.getParent();
                            ListView listView = (ListView) viewParent2.getParent();
                            int position = listView.getPositionForView((View) viewParent2);
                            RutaLocation ruta = listRutas.get(position);
                            Cliente c = db.selectClienteById(ruta.getClient_id());
                            Globals.setClienteSeleccionadoRuta(c);

                            Intent intent = new Intent(ListRutasActivity.this, CobranzaActivity.class);
                            startActivity(intent);
                        }
                    });
                } else if (item.getType().equals("VISITA")) {
                    actionBtn.setText("OMITIR");
                    //actionBtn.setVisibility(View.GONE);
                    actionBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            View parentRow = (View) v.getParent();
                            ViewParent viewParent = parentRow.getParent();
                            ViewParent viewParent2 = viewParent.getParent();
                            ListView listView = (ListView) viewParent2.getParent();
                            int position = listView.getPositionForView((View) viewParent2);
                            RutaLocation ruta = listRutas.get(position);
                            Cliente c = db.selectClienteById(ruta.getClient_id());
                            Globals.setClienteSeleccionadoRuta(c);

                            ruta.setStatus("O");
                            db.updateRutaLocation(ruta);
                            mostrarProgressBar();
                            enviarRuta(ruta, "ESTADO");
                            cerrarProgressBar();

                            listRutas = db.selectRutaLocationByFilter("A", null, "null", null);
                            adapterHojaRuta.notifyDataSetChanged();
                            String[] buttons = {"Ok"};
                            int id_icon = R.drawable.ic_check;
                            String msj = "El cliente seleccionado ha sido omitido de la lista de hoja de ruta";
                            AppUtils.showWithIcon("Enviado", id_icon, msj, buttons, ListRutasActivity.this, true, dialogOnclicListenerAfterSave);

                            //Toast.makeText(getApplicationContext(), "El cliente seleccionado ha sido omitido con exito", Toast.LENGTH_LONG).show();

//                            Intent intent = new Intent(ListRutasActivity.this, RegistroVisitasActivity.class);
//                            startActivity(intent);
                        }
                    });
                }
            }
            return v;
        }
    }

    public void mostrarProgressBar() {

        progress = AppUtils.mostrarProgressDialog("Procesando...", this);
    }

    public void cerrarProgressBar() {
        progress.dismiss();
    }

    DialogInterface.OnClickListener dialogOnclicListenerAfterSave = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //click en boton Ok
            //finish();
            dialog.dismiss();
        }
    };


    public void marcarObservacion(int position, String texto) {
        RutaLocation rutaLocation = listRutas.get(position);

        rutaLocation.setObservation(texto);
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rutaLocation.setFechaHoraEntrada(dateFormatter.format(new Date()));

        Globals.setRutaLocationSeleccionada(rutaLocation);
        enviarRuta(rutaLocation, "OBSERVACION");
    }

    public void marcarEntrada(int position) {
        RutaLocation rutaLocation = listRutas.get(position);
        rutaLocation.setEntrada("Y");
        //SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        rutaLocation.setFechaHoraEntrada(dateFormatter.format(new Date()));

        Globals.setRutaLocationSeleccionada(rutaLocation);
        String[] buttons = {"Si", "No"};
        AppUtils.show(null, "Marcar entrada?", buttons, ListRutasActivity.this,
                false, dialogOnclicListenerMarcarEntrada);

    }

    public void marcarSalida(int position) {
        RutaLocation rutaLocation = listRutas.get(position);
        rutaLocation.setSalida("Y");
        rutaLocation.setStatus("V");
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        rutaLocation.setFechaHoraSalida(dateFormatter.format(new Date()));

        Globals.setRutaLocationSeleccionada(rutaLocation);
        String[] buttons = {"Si", "No"};
        AppUtils.show(null, "Marcar salida?", buttons, ListRutasActivity.this, false, dialogOnclicListenerMarcarSalida);
    }

    DialogInterface.OnClickListener dialogOnclicListenerMarcarEntrada = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Si=-1, No = -2
            if (which == -1) {
                enviarRuta(Globals.getRutaLocationSeleccionada(), "ENTRADA");
            }
        }
    };

    DialogInterface.OnClickListener dialogOnclicListenerMarcarSalida = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //Si=-1, No = -2
            if (which == -1) {
                enviarRuta(Globals.getRutaLocationSeleccionada(), "SALIDA");
            }
        }
    };

    public void enviarRuta(RutaLocation ruta, String tipo) {

        if (!AppUtils.isOnline(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), "No hay conexión.", Toast.LENGTH_LONG).show();
            return;
        }
        db.updateRutaLocation(ruta);
        InputStream inputStream = null;
        String result = "";
        try {
            // 1. create HttpClient
            HttpClient httpclient = new DefaultHttpClient();
            String url = Comm.URL + "multip/api/routes/update";
            url += "?routeid=" + ruta.getId();
            url += "&tipo=" + tipo;

            url += "&observation=" + ruta.getObservation();

            if (tipo.equals("ENTRADA"))
                url += "&fechahora=" + ruta.getFechaHoraEntrada();
            else if (tipo.equals("SALIDA"))
                url += "&fechahora=" + ruta.getFechaHoraSalida();
            else if (tipo.equals("OBSERVACION")) {
                url += "&fechahora=" + ruta.getFechaHoraSalida();
                url += "&observation=" + ruta.getObservation();
            } else if (tipo.equals("ESTADO")) {
                url += "&fechahora=" + ruta.getFechaHoraEntrada();
                url += "&estado=" + ruta.getStatus() + "";
            }


            url = url.replace(" ", "%20");


            Log.d(TAG, "url: " + url);
            // 2. make POST request to the given URL
            HttpPost httpPost = new HttpPost(url);
            String json = "";

            // 3. build jsonObject
            JSONObject jsonObject = new JSONObject();

            //jsonObject.accumulate("order_id", "1000010");
            // 4. convert JSONObject to JSON to String
            json = jsonObject.toString();

            // 5. set json to StringEntity
            StringEntity se = new StringEntity(json);

            // 6. set httpPost Entity
            //httpPost.setEntity(se);

            // 7. Set some headers to inform server about the type of the content
            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");
            httpPost.setHeader("api_version", BuildConfig.VERSION_NAME);


            Log.d(TAG, "enviando post: " + httpPost.toString());
            Log.d(TAG, "mensaje post: " + json);

            DefaultHttpClient httpclient2 = new DefaultHttpClient();

            // 8. Execute POST request to the given URL
            HttpResponse httpResponse = httpclient2.execute(httpPost);

            int code = httpResponse.getStatusLine().getStatusCode();
            //si llega 401 es error de login
            Log.d(TAG, "responde code: " + code);

            // 9. receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // 10. convert inputstream to string
            if (inputStream != null) {
                result = convertInputStreamToString(inputStream);
            } else {
                result = "Did not work!";
            }
            if (code == 200) {
                if (result.contains("Bienvenidos al Portal Movil Tigo\n")) {
                    Toast.makeText(getApplicationContext(), "No hay conexión.", Toast.LENGTH_LONG).show();
                    return;
                }

                //Toast.makeText(getApplicationContext(), "Ruta actualizada.", Toast.LENGTH_LONG).show();

                //db.updateRutaLocation(ruta);
                ((BaseAdapter) listRutasListView.getAdapter()).notifyDataSetChanged();
                //finish();
            } else {
                Toast.makeText(getApplicationContext(), "Error al enviar la ruta", Toast.LENGTH_LONG).show();
            }

            Log.d(TAG, "resultado  post: " + result);
        } catch (Exception e) {
            e.printStackTrace();
            AppUtils.handleError("Error al enviar ruta.", ListRutasActivity.this);
            Log.e(TAG, e.getMessage());
            //guardarCobranzaBtn.setEnabled(true);
        }
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }


}
