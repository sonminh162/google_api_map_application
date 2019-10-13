package com.lifetime.map.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
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
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.lifetime.map.R;
import com.lifetime.map.utils.DirectionsParser;
import com.lifetime.map.utils.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST = 500;

    private FusedLocationProviderClient mFusedLocationProviderClient;

    private Location mLastKnownLocation;

    private GoogleMap mMap;

    private EditText mSearchText;

    List<LatLng> locations = new ArrayList<>();

    Geocoder geocoder;

    //test
    List<LatLng> listPoints;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSearchText = findViewById(R.id.input_search);

        listPoints = new ArrayList<>();

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

//        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
//            @Override
//            public void onMapLongClick(LatLng latLng) {
//                mMap.clear();
//
//                getCurrentLocationNoMove();
//
//                List<Address> resultAddresses = null;
//
//                for (LatLng latLngLeuLeu : locations) {
//                    try {
//                        resultAddresses = geocoder.getFromLocation(latLngLeuLeu.latitude, latLngLeuLeu.longitude, 1);
//                    } catch (IOException e){
//                        e.printStackTrace();
//                    }
//                    mMap.addMarker(new MarkerOptions()
//                            .position(latLngLeuLeu)
//                            .title(resultAddresses.get(0).getAddressLine(0))
//                    );
//                }
//
//                showCurrentPlaceInformation(latLng);
//            }
//        });

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if(listPoints.size() == 2) {
                    listPoints.clear();
                    mMap.clear();
                }

                listPoints.add(latLng);

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                if(listPoints.size()==1){
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                    markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                }
                mMap.addMarker(markerOptions);

                if(listPoints.size()==2){
                    String url = getRequestUrl(listPoints.get(0),listPoints.get(1));
                    new TaskRequestDirections().execute(url);
                }
            }

        });
    }

    public class TaskRequestDirections extends AsyncTask<String,Void,String>{

        @Override
        protected String doInBackground(String... strings) {
            String responseString = "";
            try{
                responseString = requestDirection(strings[0]);
            } catch (IOException e){
                e.printStackTrace();
            }
            return responseString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            new TaskParser().execute(s);
        }
    }

    public class TaskParser extends AsyncTask<String,Void,List<List<HashMap<String,String>>>>{

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... strings) {
            JSONObject jsonObject = null;
            List<List<HashMap<String,String>>> routes = null;
            try{
                jsonObject = new JSONObject(strings[0]);
                DirectionsParser directionsParser = new DirectionsParser();
                routes = directionsParser.parse(jsonObject);
            }catch(JSONException e){
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> lists) {
            ArrayList points = null;

            PolylineOptions polylineOptions = null;

            for(List<HashMap<String,String>> path : lists) {
                points = new ArrayList();
                polylineOptions = new PolylineOptions();

                for(HashMap<String,String> point : path) {
                    double lat = Double.parseDouble(point.get("lat"));
                    double lon = Double.parseDouble(point.get("long"));

                    points.add(new LatLng(lat,lon));
                }

                polylineOptions.addAll(points);
                polylineOptions.width(15);
                polylineOptions.color(Color.BLUE);
                polylineOptions.geodesic(true);
            }
            if(polylineOptions != null){
                mMap.addPolyline(polylineOptions);
            }else{
                Toast.makeText(getApplicationContext(), "Direction not found", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String requestDirection(String reqUrl) throws IOException{
        String responseString = "";
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try{
            URL url = new URL(reqUrl);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.connect();

            //Get the response result
            inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuffer stringBuffer = new StringBuffer();
            String line = "";
            while((line = bufferedReader.readLine())!=null){
                stringBuffer.append(line);
            }

            responseString = stringBuffer.toString();
            bufferedReader.close();
            inputStreamReader.close();
        }catch(Exception e){
            e.printStackTrace();
        }finally {
            if(inputStream != null){
                inputStream.close();
            }
            httpURLConnection.disconnect();
        }
        return responseString;
    }

    private String getRequestUrl(LatLng origin, LatLng dest){
        //https://route.api.here.com/routing/7.2/calculateroute.
        //app_id=IHvLTkkCTu4oUixgJ4gR
        //&
        // app_code=E_CvlpVwmJtn3uVlJvqPlg
        // &
        // waypoint0=21.006323%2C105.843127
        // &
        // waypoint1=21.009153%2C105.828569
        // &
        // mode=fastest%3Bcar%3Btraffic%3Aenabled
        // &
        // departure=now

        String str_org = "waypoint0="+origin.latitude+"%2C"+origin.longitude;
        String str_dest = "waypoint1="+dest.latitude+"%2C"+dest.longitude;
        String app_id = "app_id="+getResources().getString(R.string.app_id);
        String app_code = "app_code="+getResources().getString(R.string.app_code);
        String mode = "mode=fastest%3Bcar%3Btraffic%3Aenabled";
        String departure = "departure=now";

        String output = "json";
        String param = app_id +"&" + app_code + "&" + str_org+"&"+str_dest+"&"+mode+"&"+ departure;

        String url = "https://route.api.here.com/routing/7.2/calculateroute."+output+"?"+param;
        return url;
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
