package com.lifetime.map.demo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lifetime.map.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MarkerDemoActivity extends AppCompatActivity implements
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMarkerDragListener,
        SeekBar.OnSeekBarChangeListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        OnMapAndViewReadyListener.OnGlobalLayoutAndMapReadyListener {
    private static final LatLng BRISBANE = new LatLng(-27.47093, 153.0235);

    private static final LatLng MELBOURNE = new LatLng(-37.81319, 144.96298);

    private static final LatLng DARWIN = new LatLng(-12.4634, 130.8456);

    private static final LatLng SYDNEY = new LatLng(-33.87365, 151.20689);

    private static final LatLng ADELAIDE = new LatLng(-34.92873, 138.59995);

    private static final LatLng PERTH = new LatLng(-31.952854, 115.857342);

    private static final LatLng ALICE_SPRINGS = new LatLng(-24.6980, 133.8807);

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter{

        private final View mWindow;

        private final View mContents;

        CustomInfoWindowAdapter(){
            mWindow = getLayoutInflater().inflate(R.layout.custom_info_window, null);
            mContents = getLayoutInflater().inflate(R.layout.custom_info_contents,null);
        }
        @Override
        public View getInfoWindow(Marker marker) {
            if(mOptions.getCheckedRadioButtonId() != R.id.custom_info_window){
                return null;
            }
            render(marker, mWindow);
            return mWindow;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    }

    private void render(Marker marker, View view) {
        int badge;

        if (marker.equals(mBrisbane)) {
            badge = R.drawable.badge_qld;
        } else if (marker.equals(mAdelaide)) {
            badge = R.drawable.badge_sa;
        } else if (marker.equals(mSydney)) {
            badge = R.drawable.badge_nsw;
        } else if (marker.equals(mMelbourne)){
            badge = R.drawable.badge_victoria;
        } else if (marker.equals(mPerth)) {
            badge = R.drawable.badge_wa;
        } else if (marker.equals(mDarwin1)) {
            badge = R.drawable.badge_nt;
        } else if (marker.equals(mDarwin2)) {
            badge = R.drawable.badge_nt;
        } else if (marker.equals(mDarwin3)) {
            badge = R.drawable.badge_nt;
        } else if (marker.equals(mDarwin4)) {
            badge = R.drawable.badge_nt;
        } else {
            badge = 0 ;
        }

        ((ImageView) view.findViewById(R.id.badge)).setImageResource(badge);

        String title = marker.getTitle();
        TextView titleUi = ((TextView) view.findViewById(R.id.title));
        if(title != null) {
            SpannableString titleText = new SpannableString(title);
            titleText.setSpan(new ForegroundColorSpan(Color.RED),0,titleText.length(),0);
            titleUi.setText(titleText);
        }else{
            titleUi.setText("");
        }

        String snippet = marker.getSnippet();
        TextView snippetUi = ((TextView) view.findViewById(R.id.snippet));
        if(snippet != null && snippet.length() > 12){
            SpannableString snippetText = new SpannableString(snippet);
            snippetText.setSpan(new ForegroundColorSpan(Color.MAGENTA),0,10, 0);
            snippetText.setSpan(new ForegroundColorSpan(Color.BLUE),12,snippet.length(),0);
            snippetUi.setText(snippetText);
        }else{
            snippetUi.setText("");
        }
    }

    private GoogleMap mMap;

    private Marker mPerth;

    private Marker mSydney;

    private Marker mBrisbane;

    private Marker mAdelaide;

    private Marker mMelbourne;

    private Marker mDarwin1;
    private Marker mDarwin2;
    private Marker mDarwin3;
    private Marker mDarwin4;

    private Marker mLastSelectMarker;

    private final List<Marker> mMarkerRainbow = new ArrayList<Marker>();

    private TextView mTopText;

    private SeekBar mRotationBar;

    private CheckBox mFlatBox;

    private RadioGroup mOptions;

    private final Random mRandom = new Random();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.marker_demo);

        mTopText = (TextView) findViewById(R.id.top_text);

        mRotationBar = (SeekBar) findViewById(R.id.rotationSeekBar);

        mRotationBar.setMax(360);

        mRotationBar.setOnSeekBarChangeListener(this);

        mFlatBox = (CheckBox) findViewById(R.id.flat);

        mOptions = (RadioGroup) findViewById(R.id.custom_info_window_options);
        mOptions.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                if(mLastSelectMarker != null && mLastSelectMarker.isInfoWindowShown()){
                    mLastSelectMarker.showInfoWindow();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        new OnMapAndViewReadyListener(mapFragment, this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(false);

        addMarkersToMap();
    }

    private void addMarkersToMap(){
        mBrisbane = mMap.addMarker(new MarkerOptions()
                .position(BRISBANE)
                .title("Brisbane")
                .snippet("Population: 2,074,200")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        );

        mSydney = mMap.addMarker(new MarkerOptions()
                .position(SYDNEY)
                .title("Sydney")
                .snippet("Population: 4,627,300")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.arrow))
                .infoWindowAnchor(0.5f,0.5f)
        );

        mMelbourne = mMap.addMarker(new MarkerOptions()
                .position(MELBOURNE)
                .title("Melbourne")
                .snippet("Population: 4,137,400")
                .draggable(true)
        );

        mDarwin1 = mMap.addMarker(new MarkerOptions()
                .position(DARWIN)
                .title("Darwin Marker 1")
                .snippet("z-index 1")
                .zIndex(1)
        );

        mDarwin2 = mMap.addMarker(new MarkerOptions()
                .position(DARWIN)
                .title("Darwin Marker 2")
                .snippet("z-index 2")
                .zIndex(2)
        );

        mDarwin3 = mMap.addMarker(new MarkerOptions()
                .position(DARWIN)
                .title("Darwin Marker 3")
                .snippet("z-index 3")
                .zIndex(3)
        );

        mDarwin4 = mMap.addMarker(new MarkerOptions()
                .position(DARWIN)
                .title("Darwin Marker 4")
                .snippet("z-index 4")
                .zIndex(4)
        );

        mPerth = mMap.addMarker(new MarkerOptions()
        .position(PERTH)
        .title("Perth")
        .snippet("Population: 1,738,800"));

        mAdelaide = mMap.addMarker(new MarkerOptions()
        .position(ADELAIDE)
        .title("Adelaide")
        .snippet("Population: 1,218,000"));

        mMap.addMarker(new MarkerOptions()
                .position(ALICE_SPRINGS)
                .icon(vectorToBitMap(R.drawable.ic_android,Color.parseColor("#A4C639")))
                .title("Alice Springs")
        );

        float rotation = mRotationBar.getProgress();
        boolean flat = mFlatBox.isChecked();

        int numMarkersInRainbow = 12;
        for(int i = 0; i < numMarkersInRainbow;i++){
            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(new LatLng(
                        - 30 + 10 * Math.sin(i*Math.PI/(numMarkersInRainbow -1 )),
                        135 - 10 * Math.cos(i*Math.PI/(numMarkersInRainbow -1))))
                .title("Marker" + i)
                .icon(BitmapDescriptorFactory.defaultMarker(i*360/numMarkersInRainbow))
                .flat(flat)
                .rotation(rotation));
            mMarkerRainbow.add(marker);
        }
    }

    private BitmapDescriptor vectorToBitMap(@DrawableRes int id,@ColorInt int color){
        Drawable vectorDrawable = ResourcesCompat.getDrawable(getResources(),id,null);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0,0,canvas.getWidth(),canvas.getHeight());
        DrawableCompat.setTint(vectorDrawable,color);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private boolean checkReady() {
        if(mMap == null){
            Toast.makeText(this,R.string.map_not_ready, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    public void onClearMap(View view){
        if(!checkReady()){
            return;
        }
        mMap.clear();
    }

    public void onResetMap(View view){
        if(!checkReady()){
            return;
        }
        boolean flat = mFlatBox.isChecked();
        for (Marker marker : mMarkerRainbow){
            marker.setFlat(flat);
        }
    }

    public void onToggleFlat(View view){
        if(!checkReady()){
            return;
        }
        boolean flat = mFlatBox.isChecked();
        for(Marker marker: mMarkerRainbow){
            marker.setFlat(flat);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(!checkReady()){
            return;
        }
        float rotation = seekBar.getProgress();
        for(Marker marker : mMarkerRainbow){
            marker.setRotation(rotation);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(marker.equals(mPerth)){
            final Handler handler = new Handler();
            final long start = SystemClock.uptimeMillis();
            final long duration = 1500;

            final Interpolator interpolator = new BounceInterpolator();

            handler.post(new Runnable(){
                @Override
                public void run() {
                    long elapsed = SystemClock.uptimeMillis() - start;
                    float t = Math.max(
                            1 - interpolator.getInterpolation((float) elapsed/duration),0);
                    marker.setAnchor(0.5f,1.0f + 2*t);

                    if(t>0.0) {
                        handler.postDelayed(this,16);
                    }
                }
            });

        } else if( marker.equals(mAdelaide)) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(mRandom.nextFloat() * 360));
            marker.setAlpha(mRandom.nextFloat());
        }

        float zIndex = marker.getZIndex() + 1.0f;
        marker.setZIndex(zIndex);
        Toast.makeText(this, marker.getTitle()+"z-index set to" + zIndex, Toast.LENGTH_SHORT).show();
        mLastSelectMarker = marker;
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onInfoWindowClose(Marker marker) {

    }

    @Override
    public void onInfoWindowLongClick(Marker marker) {
        Toast.makeText(this, "Info window long click", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        mTopText.setText("onMarkerDragStart");
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        mTopText.setText("onMarkerDragEnd");
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        mTopText.setText("onMarkerDrag. Current Position: "+ marker.getPosition());
    }
}
