package com.saber.lacarreteraroutes;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private Toolbar toolbar;
    private Button startButton;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener locationListener;

    private int currentNum = 0;
    private Location lastLocation;

    private boolean collectingData = false;
    private boolean editing = false;
    private List<MarkerOptions> markersList = new ArrayList<>();

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private String start,end,cost;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        toolbar = findViewById(R.id.toolbar);
        setActionBar(toolbar);

        startButton = findViewById(R.id.startButton);

        FirebaseApp.initializeApp(this);
        database = FirebaseDatabase.getInstance();

        start = getIntent().getExtras().getString("start");
        end = getIntent().getExtras().getString("end");
        cost = getIntent().getExtras().getString("cost");

        setTitle(start + " to " + end);

        Toast.makeText(this, start + " to " + end + " " + cost, Toast.LENGTH_SHORT).show();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!collectingData){
                    collectingData = true;
                    startButton.setText(getResources().getString(R.string.maps_activity_button_end));
                }else {
                    if (!editing) {
                        collectingData = false;
                        editing = true;
                        startButton.setText(getResources().getString(R.string.maps_activity_button_save));

                    }else {
                        addPointsToDataBase(markersList);
                        onBackPressed();
                    }
                }
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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.zoomBy(3));

        LatLngBounds egyptBounds = new LatLngBounds(
                new LatLng(22.251109, 25.334812), new LatLng(31.238865, 34.193550)
        );

        mMap.setLatLngBoundsForCameraTarget(egyptBounds);

        locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (!editing) {
                    addLocation(location);
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
//            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            addLocation(lastKnownLocation);
        } else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                for (int i=0;i<markersList.size();i++){
                    if (markersList.get(i).getTitle().equals(marker.getTitle())){
                        markersList.set(i,new MarkerOptions().position(marker.getPosition()).title(marker.getTitle()));
                    }
                }
            }
        });

    }

    public void addLocation(Location newLocation){

        LatLng userLocation = new LatLng(newLocation.getLatitude(),newLocation.getLongitude());

        if (currentNum == 0){
            if (collectingData){
                currentNum = 2;
            }else {
                mMap.clear();
            }
            lastLocation = newLocation;
            MarkerOptions marker = new MarkerOptions().position(userLocation).title("1").draggable(true);
            mMap.addMarker(marker);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
            if (markersList.size() == 0){
                markersList.add(marker);
            }else {
                markersList.set(0,marker);
            }
        }else {
            if (collectingData) {
                float[] results = new float[1];
                Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(), newLocation.getLatitude(), newLocation.getLongitude(), results);

                if (results[0] >= 10) {
                    MarkerOptions marker = new MarkerOptions().position(userLocation).title(String.valueOf(currentNum)).draggable(true);
                    mMap.addMarker(marker);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));
                    markersList.add(marker);
                    currentNum++;
                }
            }
        }
    }

    private void addPointsToDataBase(List<MarkerOptions> markersList){
        myRef = database.getReference().child("Transportation").child(start + " to " + end);
        myRef.child("start").setValue(markersList.get(0).getPosition().latitude+","+markersList.get(0).getPosition().longitude);
        myRef.child("end").setValue(markersList.get(markersList.size()-1).getPosition().latitude+","+markersList.get(markersList.size()-1).getPosition().longitude);
        myRef.child("cost").setValue(cost);

        myRef = database.getReference().child("Transportation").child(start + " to " + end).child("points");
        for (int i=0;i<markersList.size();i++){
            myRef.child(markersList.get(i).getTitle()).setValue(markersList.get(i).getPosition().latitude+","+markersList.get(i).getPosition().longitude);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

//                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                addLocation(lastKnownLocation);
            }
        }
    }
}
