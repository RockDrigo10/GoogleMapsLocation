package com.example.admin.googlemapslocation;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.admin.googlemapslocation.Model.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 10;
    private static final String TAG = "MainActivity";
    public static final String GEO_KEY = "AIzaSyDpduZ0nsXKl4eSfyCeOmIKgooJZwOydy4";
    //public static final String GEO_KEY = "AIzaSyAea75UAZHdYIZ-w1dsQ5dIbh6qiaWRmL8";
    FusedLocationProviderClient fusedLocationProviderClient;

    TextView tvText1, tvText2;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Here, thisActivity is the current activity
        tvText1 = (TextView) findViewById(R.id.tvText1);
        tvText2 = (TextView) findViewById(R.id.tvText2);
        checkPermissions();

    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            getLocation();
        }
    }

    public void getLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        currentLocation = location;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: " + e.toString());
                    }
                });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                } else {
                }
                return;
            }


        }
    }

    public void getLocationNow(View view) {
        switch (view.getId()) {
            case R.id.btnMaps:
                if (currentLocation != null) {
                    tvText1.setText(String.valueOf(currentLocation.getLatitude()));
                    tvText2.setText(String.valueOf(currentLocation.getLongitude()));
                    Intent intent = new Intent(this, MapsActivity.class);
                    intent.putExtra("location", currentLocation);
                    startActivity(intent);
                }
                break;
            case R.id.btnGeo:
                if (currentLocation != null) {
                    getGeocodeAddress();
                }
                break;
        }

    }

    public void getGeocodeAddress() {
        String currentLatLong = currentLocation.getLatitude() + "," + currentLocation.getLongitude();
        OkHttpClient client = new OkHttpClient();
        HttpUrl url = new HttpUrl.Builder()
                .scheme("https")
                .host("maps.googleapis.com")
                .addPathSegment("maps")
                .addPathSegment("api")
                .addPathSegment("geocode")
                .addPathSegment("json")
                .addQueryParameter("latlng", currentLatLong)
                .addQueryParameter("key", GEO_KEY)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: " + e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Gson gson = new Gson();
                GeoLocation addressResponse = gson.fromJson(response.body().string(), GeoLocation.class);
                Log.d(TAG, "onResponse: " + addressResponse.getResults().get(0).getFormattedAddress());
            }
        });
    }
}
