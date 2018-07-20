package py.multipartes2.android.activities;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.ui.IconGenerator;

import java.util.List;

import py.multipartes2.R;
import py.multipartes2.beans.Cliente;
import py.multipartes2.beans.RutaLocation;
import py.multipartes2.db.AppDatabase;

public class RutasActivity extends FragmentActivity {
    public static final String TAG = RutasActivity.class.getSimpleName();

    private AppDatabase db = new AppDatabase(this);
    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rutas);
        setUpMapIfNeeded();
    }


    public void onMapReady(GoogleMap map) {
        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        map.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {

        List<RutaLocation> rutaLocationList = db.selectAllRutaLocation();

        RutaLocation r1 = new RutaLocation();
        r1.setPriority(1);
        r1.setLatitude("-25.322417");
        r1.setLongitude("-57.499759");

        RutaLocation r2 = new RutaLocation();
        r2.setPriority(2);
        r2.setLatitude("-25.289301");
        r2.setLongitude("-57.485796");

        RutaLocation r3 = new RutaLocation();
        r3.setPriority(3);
        r3.setLatitude("-25.280456");
        r3.setLongitude("-57.4854751");

        //rutaLocationList.add(r1);
        //rutaLocationList.add(r2);
        //rutaLocationList.add(r3);

        LatLng prev = null;
        LatLng current = null;


        IconGenerator mIconGenerator = new IconGenerator(getApplicationContext());

        for (RutaLocation r : rutaLocationList){

            if (r.getLatitude() != null && r.getLongitude() != null){
                double latitude = Double.valueOf(r.getLatitude());
                double longitude = Double.valueOf(r.getLongitude());
                Log.d(TAG, "una ruta: "+latitude);

                Cliente cliente = db.selectClienteById(r.getClient_id());

                Log.d(TAG, "status: " + r.getStatus());

                if(r.getStatus().equalsIgnoreCase("A")){
                    mIconGenerator.setStyle(IconGenerator.STYLE_RED);
                }else{
                    mIconGenerator.setStyle(IconGenerator.STYLE_GREEN);
                }

                Bitmap iconBitmap = mIconGenerator.makeIcon("" + r.getPriority());
                mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(iconBitmap)).position(new LatLng(latitude, longitude)).title(r.getPriority().toString() +"-"+cliente.getNombre()).snippet(""+r.getObservation()));

                current = new LatLng(latitude, longitude);
                if (prev != null){
                    mMap.addPolyline((new PolylineOptions())
                            .add(prev, current).width(6).color(Color.BLACK)
                            .visible(true));
                }
                prev = current;
                current = null;
            }
        }

        //mMap.addMarker(new MarkerOptions().position(new LatLng(-25.322417, -57.499759)).title("Casa de Willy"));
        //mMap.addMarker(new MarkerOptions().position(new LatLng(-25.289301, -57.485796)).title("Casa de Adolfo"));

        LatLng callei = new LatLng(-25.322417, -57.499759);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(callei));
        mMap.animateCamera( CameraUpdateFactory.zoomTo( 12.0f ) );

        mMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            private float currentZoom = -1;

            @Override
            public void onCameraChange(CameraPosition position) {
                if (position.zoom != currentZoom) {
                    currentZoom = position.zoom;  // here you get zoom level
                    Log.d(TAG, "zoom: " + currentZoom);
                }
            }
        });

    }
}
