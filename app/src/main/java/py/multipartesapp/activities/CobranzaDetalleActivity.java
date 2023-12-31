package py.multipartesapp.activities;

import android.content.Context;
import android.content.DialogInterface;
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
import android.view.ViewParent;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import py.multipartesapp.R;
import py.multipartesapp.beans.CobranzaFormaPago;
import py.multipartesapp.db.AppDatabase;
import py.multipartesapp.utils.AppUtils;
import py.multipartesapp.utils.Globals;
import py.multipartesapp.utils.MyFormatter;

/**
 * Created by Adolfo on 18/10/2015.
 */
public class CobranzaDetalleActivity extends ActionBarActivity  {
    public static final String TAG = CobranzaDetalleActivity.class.getSimpleName();

    private AppDatabase db = new AppDatabase(this);


    private TextView fechaPedidoTextView;
    private TextView totalPedidoTextView;
    private Button cargarCobroBtn;
    private Button agregarItemBtn;
    private ListView detallesListView;
    private ImageAdapter adapterDetalles;

    public static List<CobranzaFormaPago> detallesList;
    //private ImageAdapter adapterDetalles;
    private TextView totalACobrar;

    private SimpleDateFormat dateFormatter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cobranza_detalle);

        /* Configuracion ActionBar*/
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //muestra el boton atras
        getSupportActionBar().setTitle("Detalle cobro");

        fechaPedidoTextView = (TextView) findViewById(R.id.cobranza_detalle_fecha);
        totalPedidoTextView = (TextView) findViewById(R.id.cobranza_detalle_total_pedido);
        cargarCobroBtn = (Button) findViewById(R.id.agregar_detalle_cobranza);
        totalACobrar = (TextView) findViewById(R.id.cobranza_detalle_total);
        detallesListView = (ListView) findViewById(R.id.cobranza_detalle_pagos_list);
        agregarItemBtn = (Button) findViewById(R.id.cobranza_detalle_add_cobro);

        String fechaFactura = CobranzaActivity.facturaSeleccionada.getDateinvoiced();
        fechaFactura = fechaFactura.substring(0,10);
        fechaPedidoTextView.setText(fechaFactura);

        String totalFactura = CobranzaActivity.facturaSeleccionada.getGrandtotal().toString();
        totalPedidoTextView.setText(MyFormatter.formatMoney(totalFactura));

        detallesList = new ArrayList<CobranzaFormaPago>();

        adapterDetalles = new ImageAdapter (this);
        detallesListView.setAdapter(adapterDetalles);

        cargarCobroBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agregarDetalle();
            }
        });

        agregarItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CobranzaDetalleActivity.this, CobranzaDetalleItemActivity.class);
                startActivity(intent);
            }
        });
    }

    DialogInterface.OnClickListener dialogOnclicListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            //clic en boton Ok
        }
    };

    private void agregarDetalle (){
        //verificar para que no supere el total de la factura
        Integer totalFactura = CobranzaActivity.
                facturaSeleccionada.getGrandtotal();
        Integer totalAcobrar = calcularTotal();

        Log.i("total_factura",""+totalFactura);
        Log.i("total_cobrar",""+totalAcobrar);

        if (totalAcobrar > totalFactura){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "El monto a cobrar no puede superar el monto total de la Factura.", buttons, CobranzaDetalleActivity.this, false, dialogOnclicListener);
            return;
        }

        //validar cantidad
        if (calcularTotal() == 0 ){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "El monto a cobrar no es valido.", buttons, CobranzaDetalleActivity.this, false, dialogOnclicListener);
            return;

        }
        if (detallesList.isEmpty() ){
            String[] buttons = {"Ok"};
            AppUtils.show(null, "Agregue algun cobro presionando el boton '+'.", buttons, CobranzaDetalleActivity.this, false, dialogOnclicListener);
            return;

        }

        CobranzaActivity.facturaSeleccionada.setMontoCobrado(calcularTotal().toString());
        CobranzaActivity.facturaSeleccionada.setItems(detallesList);

//        CobranzaActivity.facturaSeleccionada.setBank(bancoSeleccionado);
//        CobranzaActivity.facturaSeleccionada.setCheck_number(nroChequeEditText.getText().toString());
//        CobranzaActivity.facturaSeleccionada.setPayment_type(formaPagoSeleccionado.getDescripcion());
//        CobranzaActivity.facturaSeleccionada.setExpired_date(fechaVencChequeEditText.getText().toString());
//        CobranzaActivity.facturaSeleccionada.setCheck_name(nombreChequeEditText.getText().toString());

        finish();
        return;
    }


    public Integer calcularTotal (){
        Integer total = 0;
        for (CobranzaFormaPago item : detallesList){
            total = total + item.getAmount();
        }
        return total;
    }

    public void actualizarTotal (){
        Integer total = calcularTotal();
        totalACobrar.setText("Gs. "+MyFormatter.formatMoney(total.toString()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds itemsClientes to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        //actualizar lista de detalle
        if (Globals.getItemCobroList() != null) {
            //detallesList.add(Globals.getNuevoItemCobro());
            adapterDetalles.notifyDataSetChanged();

            //calcular total
            actualizarTotal();
            //Globals.setNuevoItemCobro(null);
        }
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
            return detallesList.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            CobranzaFormaPago item = detallesList.get(i);
            View v = view;
            if (v == null) {
                v = mInflater.inflate(R.layout.list_item_cobranza_itemcobro, viewGroup, false);

                v.setTag(R.id.txt1_item_forma_pago, v.findViewById(R.id.txt1_item_forma_pago));
                v.setTag(R.id.txt2_item_banco, v.findViewById(R.id.txt2_item_banco));
                v.setTag(R.id.monto_cobrado_item_cobro, v.findViewById(R.id.monto_cobrado_item_cobro));
                v.setTag(R.id.borrar_item_cobro_btn, v.findViewById(R.id.borrar_item_cobro_btn));
            }

            TextView titleTextView = (TextView) v.findViewById(R.id.txt1_item_forma_pago);
            titleTextView.setText(item.getPayment_type());

            TextView banco = (TextView) v.findViewById(R.id.txt2_item_banco);
            if (item.getBank() != null){
                banco.setText(item.getBank());
            }
            TextView monto = (TextView) v.findViewById(R.id.monto_cobrado_item_cobro);
            monto.setText(MyFormatter.formatMoney(item.getAmount().toString()));


            //Configuramos el boton para editar la Cobranza
            ImageButton borrarBtn = (ImageButton) v.getTag(R.id.borrar_item_cobro_btn);
            borrarBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v1) {

                    View parentRow = (View) v1.getParent();
                    ViewParent viewParent = parentRow.getParent();
                    ListView listView = (ListView) viewParent.getParent();
                    int position = listView.getPositionForView((View) viewParent);
                    CobranzaFormaPago detalleSelected = detallesList.get(position);

                    Iterator<CobranzaFormaPago> i = detallesList.iterator();
                    while (i.hasNext()) {
                        CobranzaFormaPago o = i.next();
                        if (o.getAmount().equals(detalleSelected.getAmount()) && o.getPayment_type().equals(detalleSelected.getPayment_type()) ){
                            i.remove();
                            break;
                        }
                    }
                    adapterDetalles.notifyDataSetChanged();
                    actualizarTotal();
                }
            });

            return v;
        }
    }




}
