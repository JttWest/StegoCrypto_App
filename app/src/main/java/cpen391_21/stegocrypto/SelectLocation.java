package cpen391_21.stegocrypto;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class SelectLocation extends FragmentActivity implements OnMapReadyCallback {
    Button setGeoKeyBtn;

    private GoogleMap mMap;
    private Marker currMarker;
    private LatLng currCoord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setGeoKeyBtn = (Button) findViewById(R.id.set_geo_key);
        setGeoKeyBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                // Closing select location activity and pass back selected coordinate
                Intent intent = getIntent();
                intent.putExtra("selectedCoord", currCoord);
                setResult(1, intent);
                finish();
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng vancouver = new LatLng(49, -123);
        currMarker = mMap.addMarker(new MarkerOptions().position(vancouver).title("Marker in Vancouver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(vancouver));

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng coord) {

                // 2 decimal points
                Log.v("StegoCrypto", "lat: " + coord.latitude + "   long: " + coord.longitude);
                currMarker.remove();

                currMarker = mMap.addMarker(new MarkerOptions().position(coord).title("New Marker"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(coord));
                currCoord = coord;
            }
        });
    }
}
