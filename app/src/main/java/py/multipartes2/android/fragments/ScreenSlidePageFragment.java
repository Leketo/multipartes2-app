package py.multipartes2.fragments;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;

import py.multipartes2.R;
import py.multipartes2.android.activities.Main;
import py.multipartes2.beans.Producto;
import py.multipartes2.beans.ProductoImagen;
import py.multipartes2.db.AppDatabase;
import py.multipartes2.utils.Globals;

/**
 * Created by Adolfo on 23/05/2016.
 */
public class ScreenSlidePageFragment extends Fragment {
    public static final String TAG = ScreenSlidePageFragment.class.getSimpleName();

    public static final String ARG_PAGE = "page";
    private int mPageNumber;
    public static Producto producto = new Producto();
    public static Bitmap bitmap;
    TouchImageView imgDisplay;
    public static ScreenSlidePageFragment create(int pageNumber) {

        ScreenSlidePageFragment fragment = new ScreenSlidePageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, pageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public ScreenSlidePageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPageNumber = getArguments().getInt(ARG_PAGE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout containing a image.
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_screen_slide_page, container, false);

        //Bitmap bitmap = null;
        ProductoImagen p = new ProductoImagen();

        TextView pagina = (TextView) rootView.findViewById(R.id.catalogo_fragment_producto_pagina);
        TextView nombreProducto = (TextView)rootView.findViewById(R.id.catalogo_fragment_producto_nombre);
        TextView codProducto = (TextView)rootView.findViewById(R.id.catalogo_fragment_producto_cod);
        //TextView stockProducto = (TextView)rootView.findViewById(R.id.catalogo_fragment_producto_stock);
        //ImageView fotoProducto = (ImageView) rootView.findViewById(R.id.catalogo_fragment_image);
        TouchImageView fotoProducto = (TouchImageView) rootView.findViewById(R.id.catalogo_fragment_image);
        Button buttonAgregarAPedido = (Button) rootView.findViewById(R.id.catalogo_fragment_btn_agregar);
        String nombreArchivo = "";

        //mostrar boton Agregar si proviene desde Pedidos
        if (Globals.isCatalogoDesdePedido()){
            buttonAgregarAPedido.setVisibility(View.VISIBLE);
        }

        Log.i("screen width",""+getScreenWidth());
        try {

            AppDatabase db = new AppDatabase(getActivity().getApplicationContext());
            p = Globals.getNombresImagenes().get(getPageNumber());
            producto = db.selectProductById(p.getM_product_id());

            nombreArchivo = p.getImg();
            db.closeDatabase();

            FileInputStream fis = Main.getAppContext().openFileInput(nombreArchivo);

            final BitmapFactory.Options options = new BitmapFactory.Options();
            //  1/6 del tamanho original
            //options.inSampleSize = 2;
            options.inSampleSize=1;
            bitmap = BitmapFactory.decodeStream(fis, null, options);

            fis.close();
            fotoProducto.setImageBitmap(bitmap);

            bitmap = null;

            buttonAgregarAPedido.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Globals.setProductoSeleccionadoCatalogo(producto);
                    Globals.setImagenSeleccionadaCatalogo(bitmap);


                    CharSequence text = "Producto Agregado";
                    int duration = Toast.LENGTH_LONG;
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), text, duration);
                    toast.setGravity(Gravity.CENTER|Gravity.CENTER,0,0);
                    toast.show();
                    getActivity().finish();
                }
            });

        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        pagina.setText((getPageNumber()+1)+"/"+Globals.getNombresImagenes().size());
        nombreProducto.setText(producto.getName());
        codProducto.setText("Código: "+producto.getCodinterno());
        //stockProducto.setText("Stock: "+p.getStock());

        return rootView;
    }

    /*
 * getting screen width
 */
    public int getScreenWidth() {
        int columnWidth;
        WindowManager wm = (WindowManager) getActivity().getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        final Point point = new Point();
        try {
            display.getSize(point);
        } catch (java.lang.NoSuchMethodError ignore) { // Older device
            point.x = display.getWidth();
            point.y = display.getHeight();
        }
        columnWidth = point.x;
        return columnWidth;
    }
    public int getPageNumber() {
        return mPageNumber;
    }


}
