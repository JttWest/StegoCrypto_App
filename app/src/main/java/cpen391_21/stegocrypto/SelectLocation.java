package cpen391_21.stegocrypto;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class SelectLocation extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener {
    Button setGeoKeyBtn, findAddrBtn;
    EditText edAddressData;

    private GoogleMap mMap;
    private Marker currMarker;
    private LatLng currCoord;

    private Geocoder geoCoder;

    private LatLng VANCOVUER = new LatLng(49.246292, -123.116226);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        edAddressData = (EditText) findViewById(R.id.edAddressData);

        setGeoKeyBtn = (Button) findViewById(R.id.set_geo_key);
        findAddrBtn = (Button) findViewById(R.id.findAddrBtn);

        setGeoKeyBtn.setOnClickListener(this);
        findAddrBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.set_geo_key:
                Intent intent = getIntent();

                double latitudeString = currCoord.latitude;
                double longitudeString = currCoord.longitude;

                String selectedCoordString = String.format("%.3f,%.3f", latitudeString, longitudeString);;
                intent.putExtra("selectedCoord", selectedCoordString);
                setResult(RESULT_OK, intent);
                finish();
                break;

            case R.id.findAddrBtn:
                String address = edAddressData.getText().toString();
                try {
                    List<Address> addresses = geoCoder.getFromLocationName(address, 1);

                    if (addresses == null || addresses.isEmpty()){
                        Toast.makeText(this, "Location doesn't exist.", Toast.LENGTH_SHORT);
                        break;
                    }

                    LatLng newCoord = new LatLng((double) (addresses.get(0).getLatitude()),
                                                 (double) (addresses.get(0).getLongitude()));

                    currCoord = newCoord;
                    currMarker.remove();
                    currMarker = mMap.addMarker(new MarkerOptions().position(newCoord).title("New Marker"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newCoord, 10));

                    // Zoom in, animating the camera.
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

                } catch (IOException e) { e.printStackTrace(); }
                break;
        }
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
        geoCoder = new Geocoder(this, Locale.getDefault());

        // Add a marker in Sydney and move the camera
        LatLng startingLocation = VANCOVUER;
        currCoord = startingLocation;
        currMarker = mMap.addMarker(new MarkerOptions().position(startingLocation).title("Marker in Vancouver"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingLocation, 20));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng coord) {

                // 2 decimal points
                Log.v("StegoCrypto", "lat: " + coord.latitude + "   long: " + coord.longitude);
                currMarker.remove();

                currMarker = mMap.addMarker(new MarkerOptions().position(coord).title("New Marker"));
                currCoord = coord;
            }
        });
    }
}
