package com.vj.spartan.saferide;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    EditText sourceAddressET;
    EditText desinationAddressET;
    RequestQueue queue;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Location mLastLocation;
    private Marker mCurrLocationMarker;
    final static private String GeocodeApiKey = "AIzaSyAUPm1bMoT0AOXxkpz2XwcNECIaljZ5Ozs";
    Double sourceLatitude;
    Double sourceLongititude;
    Double destinationLatitude;
    Double destinationLongitude;
    String sourceUrl;
    String destinationUrl;
    SharedPreferences sharedpreferences;
    int eta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        queue = Volley.newRequestQueue(this);
        Intent intent = getIntent();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        final Button rideRequest = (Button)findViewById(R.id.btn_ride_request);
        rideRequest.setTag(1);
        rideRequest.setText("Request ride");
        rideRequest.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final int status = (Integer) v.getTag();
                sourceAddressET = (EditText) findViewById(R.id.input_source_address);
                desinationAddressET = (EditText)findViewById(R.id.input_destination_address);
                if(status == 1) {
                    String sourceAddress = (sourceAddressET.getText().toString()).replaceAll(" ", "+");
                    String destinationAddress = (desinationAddressET.getText().toString()).replaceAll(" ", "+");
                    Log.d("MAP", "replacedString" + sourceAddress);
                    Log.d("MAP", "replacedDest" + destinationAddress);
                    sourceUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + sourceAddress + "&key=" + GeocodeApiKey;
                    Log.d("MAP", "sourceurl" + sourceUrl);
                    destinationUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + destinationAddress + "&key=" + GeocodeApiKey;
                    try {
                        getLocationDetails(sourceUrl, "source");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    v.setTag(2);
                    rideRequest.setText("Cancel ride");
                }else if (status == 2){
                    try {
                        cancelRide();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    rideRequest.setText("Request ride");
                    v.setTag(1);

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("MAP", "mapready");
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        /*LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MAP", "onconnected");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
    @Override
    public void onLocationChanged(Location location) {
        Log.d("MAP", "LOCATIONCHANGED");
        mLastLocation = location;
        if(mCurrLocationMarker != null){
            mCurrLocationMarker.remove();
        }

        //setMarker(location.getLatitude(),location.getLongitude(), "currentLocation");
        //LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        /*MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        mCurrLocationMarker = mMap.addMarker(markerOptions);
        Log.d("MAP","location"+latLng.toString());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }*/
    }

    public void setMarker(Double lat, Double longi, String locationType){
        LatLng latLng = new LatLng(lat, longi);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(locationType);

        if(locationType == "source"){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }else if(locationType == "destination"){
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        mCurrLocationMarker = mMap.addMarker(markerOptions);
        Log.d("MAP","location"+latLng.toString());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        if(mGoogleApiClient!=null){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }



    protected synchronized void buildGoogleApiClient() {
        Log.d("MAP", "buildGoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    public static final int MY_PERMISSION_REQUEST_LOCATION = 99;

    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSION_REQUEST_LOCATION);
            }
        return false;

        }else {
        return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){

        switch (requestCode){
            case MY_PERMISSION_REQUEST_LOCATION:{
                if ((grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {

                    if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){
                        if(mGoogleApiClient==null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                }else {
                    Toast.makeText(this,"permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }

        }

    }

    public void getLocationDetails(String url, final String locationType) throws JSONException {


        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.GET, url, new JSONObject(), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("MAP", "Response received: " + response);
                try {

                   JSONArray res0 = response.getJSONArray("results");
                    JSONObject res1 = (JSONObject) res0.get(0);
                    JSONObject geo = res1.getJSONObject("geometry");
                    JSONObject loc = geo.getJSONObject("location");
                     Double lat = loc.getDouble("lat");
                    Double longi = loc.getDouble("lng");
                    Log.d("MAP", "res Response received: "+ res0);
                    Log.d("MAP", "res1 Response received: "+ res1);
                    Log.d("MAP", "Geo Response received: "+ geo);
                    Log.d("MAP", "loc Response received: "+ loc);
                    Log.d("MAP", "lat Response received: "+ lat.toString());
                    Log.d("MAP", "longi Response received: "+ longi.toString());
                    if(locationType == "source"){
                        setMarker(lat, longi, locationType);
                        sourceLatitude = lat;
                        sourceLongititude = longi;
                        getLocationDetails(destinationUrl, "destination");
                    }else if(locationType == "destination"){
                        setMarker(lat, longi, locationType);
                        destinationLatitude = lat;
                        destinationLongitude = longi;
                        postRideDetails();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "error: " + error);
            }
        });
        queue.add(postRequest);
    }

    public void postRideDetails() throws JSONException {
        sharedpreferences = getSharedPreferences("authToken", Context.MODE_PRIVATE);
        Map<String, Double> params = new HashMap<String, Double>();

        params.put("pickup_latitude", sourceLatitude );
        params.put("pickup_longitude", sourceLongititude);
        params.put("drop_latitude", destinationLatitude);
        params.put("drop_longitude", destinationLongitude);

        String url = " http://saferide.nagkumar.com/ride/request/";
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", "Response received: " + response);
                Toast.makeText(getApplicationContext(), "Ride requested" , Toast.LENGTH_SHORT).show();
                try {
                    JSONObject ride = response.getJSONObject("ride");
                    eta = ride.getInt("initial_eta");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "ETA: "+eta, Toast.LENGTH_LONG ).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "error: " + error.toString());
                VolleyError verror = new VolleyError(new String(error.networkResponse.data));
                Log.d("Error", verror.toString());

            }
        }
        ){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + sharedpreferences.getString("token", "No token stored") );
                return headers;
            }
        };
        queue.add(postRequest);
        Log.i("Request", postRequest.getBody().toString());
    }

    public void cancelRide() throws JSONException {
        sharedpreferences = getSharedPreferences("authToken", Context.MODE_PRIVATE);
        Map<String, Double> params = new HashMap<String, Double>();

        String url = "http://saferide.nagkumar.com/ride/cancel/";
        JsonObjectRequest deleteRequest = new JsonObjectRequest(Request.Method.DELETE, url, new JSONObject(params), new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("Response", "Response received: " + response);
                Toast.makeText(getApplicationContext(), "Ride cancelled" , Toast.LENGTH_SHORT).show();
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("Error", "error: " + error.toString());
                VolleyError verror = new VolleyError(new String(error.networkResponse.data));
                Log.d("Error", verror.toString());

            }
        }
        ){

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Token " + sharedpreferences.getString("token", "No token stored") );
                return headers;
            }
        };
        queue.add(deleteRequest);
    }
}
