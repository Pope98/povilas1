package com.example.povilas.povilas;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

public class SecondScreen extends AppCompatActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener{
    private static final String TAG = "SecondScreen";
    private JSONObject jsonQuery; // WordsApi results
    private GoogleMap mMap;
    private String markerType;

    private ListView listView;

    private boolean mLocationPermissionsGranted = false;
    private static final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second_screen);
        ScrollView scrollView = (ScrollView) findViewById(R.id.scrollView);
        listView = (ListView)findViewById(R.id.listView);
        // gets results from screen1
        try {
            jsonQuery = new JSONObject(getIntent().getStringExtra("queryResults"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // gets Word, Definition, PartOfSpeech & Examples
        try {
            getQueryResults();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        getLocationPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.second_options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handles item selection to determine what type of icon to pick for google maps onLongClick
        switch (item.getItemId()) {
            case R.id.gas:
                markerType = "gas";
                break;
            case R.id.flight:
                markerType = "flight";
                break;
            case R.id.groceries:
                markerType = "groceries";
                break;
            case R.id.hotel:
                markerType = "hotel";
                break;
            case R.id.restaurant:
                markerType = "restaurant";
                break;
            case R.id.defaultMarker:
                markerType = "default";
                break;
            case R.id.random:
                getRandomMaterialIcon();
                break;
            case R.id.normal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.satellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.terrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.cleanMap:
                mMap.clear();
                break;
            default:
                markerType = "default";
        }
        return true;
    }

    public void getRandomMaterialIcon(){
        Random rand = new Random();
        int  n = rand.nextInt(5) + 1; // chooses from five icons
        switch (n){
            case 1:
                markerType = "gas";
                break;
            case 2:
                markerType = "flight";
                break;
            case 3:
                markerType = "groceries";
                break;
            case 4:
                markerType = "hotel";
                break;
            case 5:
                markerType = "restaurant";
                break;
            default:
                break;
        }
    }

    // (Word, PartOfSpeech, Definition & Examples) are bound to listView inside ScrollView
    public void getQueryResults() throws JSONException {
        QueryParser parser = new QueryParser(jsonQuery);
        String[] results = new String[4];
        results[0] = parser.getQueryWord();
        results[1] = parser.getPartOfSpeech();
        results[2] = parser.getDefinition();
        results[3] = parser.getExamples();
        ArrayAdapter<String>adapter = new ArrayAdapter<String>(SecondScreen.this,
                android.R.layout.simple_list_item_1, results);
        listView.setSelector(android.R.color.transparent);
        listView.setAdapter(adapter);
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionsGranted = true;
                    initMap();
                }
            }
        }
    }

    // gets user approval for device location sharing for Google Maps
    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() {
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices
                .getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionsGranted) {
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    // gets user's location and zooms in
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(),
                                            currentLocation.getLongitude()),
                                    DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "onComplete: Current location is null");
                            Toast.makeText(SecondScreen.this,
                                    "Location not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation : securityException -> " + e.getMessage());
        }

    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    public void setUpMap() {

        // checks permissions before marking location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // marks the location of user
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
        if(mLocationPermissionsGranted){
            getDeviceLocation();
        }
        mMap.setOnMapLongClickListener(SecondScreen.this);

    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        MarkerOptions marker = new MarkerOptions().position(latLng);
        // gets marker icon based on options item selected
        if(markerType != null){
            switch (markerType){
                case "gas":
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.gas));
                    break;
                case "flight":
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.flight));
                    break;
                case "groceries":
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.groceries));
                    break;
                case "hotel":
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.hotel));
                    break;
                case "restaurant":
                    marker.icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant));
                    break;
                case "default":
                    marker.icon(BitmapDescriptorFactory.defaultMarker());
                default:
                    break;
            }
        }
        mMap.addMarker(marker);
        /*
        Creates custom info window with information from screen1
         */
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(SecondScreen.this, jsonQuery));
    }
}