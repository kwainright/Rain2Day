package com.example.rainnotifications;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity implements LocationListener, HomeFragment.MyListener {
    private LocationManager locationManager;
    private String lat, lon;
    private final OkHttpClient client = new OkHttpClient();
    private HomeFragment homeFrag;

    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher, as an instance variable.




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        getSupportFragmentManager().beginTransaction()
                .add(R.id.rootView, new HomeFragment(), "homeFrag")
                .commit();

        homeFrag = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFrag");
    }

    @Override
    public void onLocationTap() {
        getLocation();
    }

    private void getLocation() {
        Log.d("demo", "getLocation: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            String[] PERMISSIONS = {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION,
            };
            ActivityCompat.requestPermissions(this, PERMISSIONS, 1);


        } else {
            // top line is getting locaiton updates continuously while the app is running, bottom line makes one call
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) this);
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, (LocationListener) this, null);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.lat = String.valueOf(location.getLatitude());
        this.lon = String.valueOf(location.getLongitude());

        Log.d("demo", "onLocationChanged: " + this.lat + " , " + this.lon );
        requestWeather( this.lat, this.lon );
    }

    private void requestWeather( String lat, String lon ){
        String url = "https://api.openweathermap.org/data/2.5/onecall?lat=" + lat + "&lon=" + lon + "&appid=c4b382413b42b17bb2c07d34f3c48dd8&units=imperial&exclude=hourly,current,minutely";

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

                    JSONObject json = new JSONObject(responseBody.string());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // do in UI thread
                            try {
                                String pop = json.getJSONArray("daily").getJSONObject(0).getString("pop");
                                boolean incoming = Double.parseDouble( pop ) > 0.2;
                                HomeFragment test = (HomeFragment) getSupportFragmentManager().findFragmentByTag("homeFrag");
                                test.setWeather( incoming, pop);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

}