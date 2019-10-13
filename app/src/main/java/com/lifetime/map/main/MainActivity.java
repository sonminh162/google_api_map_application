package com.lifetime.map.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.lifetime.map.R;
import com.lifetime.map.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST = 500;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation;

    private GoogleMap mMap;

    private EditText mSearchText;

    List<LatLng> locations = new ArrayList<>();

    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSearchText = findViewById(R.id.input_search);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        geocoder = new Geocoder(MainActivity.this);

        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setMyLocationEnabled(false);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},LOCATION_REQUEST);
            return;
        }

        getDeviceLocation();

        findViewById(R.id.border).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDeviceLocation();
            }
        });

        init();

        findViewById(R.id.border_clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                locations.clear();
                mMap.clear();
            }
        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();

                getCurrentLocationNoMove();

                List<Address> resultAddresses = null;

                for (LatLng latLngLeuLeu : locations) {
                    try {
                        resultAddresses = geocoder.getFromLocation(latLngLeuLeu.latitude, latLngLeuLeu.longitude, 1);
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    mMap.addMarker(new MarkerOptions()
                            .position(latLngLeuLeu)
                            .title(resultAddresses.get(0).getAddressLine(0))
                    );
                }

                showCurrentPlaceInformation(latLng);
            }
        });
    }

    private void showCurrentPlaceInformation(LatLng latLng){
        List<Address> resultAddresses = null;
        try {
            resultAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e){
            e.printStackTrace();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        markerOptions.title(resultAddresses.get(0).getAddressLine(0));
        mMap.addMarker(markerOptions);

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng,13);
        mMap.animateCamera(cameraUpdate);
    }

    private void getDeviceLocation(){
        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    mLastKnownLocation = task.getResult();
                    LatLng target = new LatLng(mLastKnownLocation.getLatitude()
                            ,mLastKnownLocation.getLongitude());
                    MarkerOptions mark = new MarkerOptions()
                            .title("Current Position.")
                            .position(target)
                            .icon(BitmapDescriptorFactory.fromBitmap(Utils.createMarker(MainActivity.this,R.drawable.watashi,"Watashi")));
                    mMap.addMarker(mark);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(target,13);
                    mMap.animateCamera(cameraUpdate);
                } else {
                    Toast.makeText(MainActivity.this, "problem here", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void getCurrentLocationNoMove(){
        Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if(task.isSuccessful()){
                    mLastKnownLocation = task.getResult();
                    LatLng target = new LatLng(mLastKnownLocation.getLatitude()
                            ,mLastKnownLocation.getLongitude());
                    MarkerOptions mark = new MarkerOptions()
                            .title("Current Position.")
                            .position(target)
                            .icon(BitmapDescriptorFactory.fromBitmap(Utils.createMarker(MainActivity.this,R.drawable.watashi,"Watashi")));
                    mMap.addMarker(mark);
                } else {
                    Toast.makeText(MainActivity.this, "problem here", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void init(){
        Log.d("TAG","init: initializing");

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){

                    showResult();
                }
                return false;
            }
        });
    }

    private void showResult(){
        final CameraUpdate zoom = CameraUpdateFactory.zoomTo(12);

        //declare a new marker
        final MarkerOptions mp = new MarkerOptions();
        //declare a EditText to get user informed address
        EditText etEndereco = findViewById(R.id.input_search);


        List<Address> addresses = null;
        try{
            addresses = geocoder.getFromLocationName(etEndereco.getText().toString(),1);
        } catch (IOException e){
            e.printStackTrace();
        }

        if(addresses.size() > 0) {
            double latitude = addresses.get(0).getLatitude();
            double longitude = addresses.get(0).getLongitude();
            locations.add(new LatLng(latitude, longitude));
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (LatLng latLng : locations) {
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(addresses.get(0).getAddressLine(0)));
                builder.include(latLng);
            }

            LatLngBounds bounds = builder.build();
            int padding = 200;
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }
}
